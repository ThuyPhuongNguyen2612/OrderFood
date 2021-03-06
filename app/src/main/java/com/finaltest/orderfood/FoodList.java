package com.finaltest.orderfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.finaltest.orderfood.Common.Common;
import com.finaltest.orderfood.Database.Database;
import com.finaltest.orderfood.Interface.ItemClickListener;
import com.finaltest.orderfood.Model.Favorites;
import com.finaltest.orderfood.Model.Food;
import com.finaltest.orderfood.Model.Order;
import com.finaltest.orderfood.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.stepstone.apprating.C;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryID = "";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    SwipeRefreshLayout swipeRefreshLayout;

    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //Create photo from Bitmap
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)) {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_food_list);

        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        //Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");

        //Local DB
        localDB = new Database(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Get Intent here
                if (getIntent() != null)
                    categoryID = getIntent().getStringExtra("CategoryID");
                if (!categoryID.isEmpty() && categoryID != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        loadListFood(categoryID);
                    } else {
                        Toast.makeText(FoodList.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //Get Intent here
                if (getIntent() != null)
                    categoryID = getIntent().getStringExtra("CategoryID");
                if (!categoryID.isEmpty() && categoryID != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        loadListFood(categoryID);
                    } else {
                        Toast.makeText(FoodList.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        //Search
        materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        //materialSearchBar.setSpeechMode(false); No need, becuz we have already define it at XML
        loadSuggest();//Write function to load Suggest from Firebase
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //When user tyoe their text, we will change suggest list
                List<String> suggest = new ArrayList<String>();
                for (String search : suggestList) //loop in suggest list
                {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When Search Bar is close
                //Restore original suggest adapter
                if (!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When search finish
                //Show result of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        //Fix click back on FoodDetail and get no itm in FoodList
        if (adapter != null)
            adapter.startListening();
    }

    private void startSearch(CharSequence text) {

        //Create query by name
        Query searchByName = foodList.orderByChild("name").equalTo(text.toString());
        //Create Option with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByName, Food.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i, @NonNull Food food) {
                foodViewHolder.food_name.setText(food.getName());
                Picasso.with(getBaseContext()).load(food.getImage()).into(foodViewHolder.food_image);

                final Food local = food;
                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());//Send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);//Set adapter for Recycler View is Search result
    }

    private void loadSuggest() {
        foodList.orderByChild("menuID").equalTo(categoryID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName());//Add name of food to suggest list
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadListFood(String categoryID) {

        //Create query by category Id
        Query searchByMenuID = foodList.orderByChild("menuID").equalTo(categoryID.toString());
        //Create Option with query
        FirebaseRecyclerOptions<Food> foodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(searchByMenuID, Food.class)
                .build();

        //Like: Select * from Food where MenuID =
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(foodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder foodViewHolder, final int position, @NonNull final Food food) {
                foodViewHolder.food_name.setText(food.getName());
                foodViewHolder.food_price.setText(String.format("$ %s", food.getPrice().toString()));
                Picasso.with(getBaseContext()).load(food.getImage()).into(foodViewHolder.food_image);

                //Quick cart


                foodViewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean isExists = new Database(getBaseContext()).checkFoodExists(adapter.getRef(position).getKey(), Common.currentUser.getPhone());


                        if (!isExists) {
                            new Database(getBaseContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(position).getKey(),
                                    food.getName(),
                                    "1",
                                    food.getPrice(),
                                    food.getDiscount(),
                                    food.getImage()
                            ));


                        } else {
                            new Database(getBaseContext()).increaseCart(Common.currentUser.getPhone(), adapter.getRef(position).getKey());
                        }
                        Toast.makeText(FoodList.this, "Added to Cart", Toast.LENGTH_SHORT).show();

                    }
                });

                //Add favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                    foodViewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);
                }

                //Click to Share
                foodViewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Picasso.with(getApplicationContext())
                                .load(food.getImage())
                                .into(target);
                    }
                });

                //Click to change state of Favorites
                foodViewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(position).getKey());
                        favorites.setFoodName(food.getName());
                        favorites.setFoodDescription(food.getDescription());
                        favorites.setFoodDiscount(food.getDiscount());
                        favorites.setFoodImage(food.getImage());
                        favorites.setFoodMenuId(food.getMenuID());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(food.getPrice());

                        if (!localDB.isFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone())) {
                            localDB.addToFavorites(favorites);
                            foodViewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);
                            Toast.makeText(FoodList.this, "" + food.getName() + " was added to Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey(), Common.currentUser.getPhone());
                            foodViewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                            Toast.makeText(FoodList.this, "" + food.getName() + " was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                final Food local = food;
                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Start new Activity
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());//Send Food Id to new activity
                        startActivity(foodDetail);
                    }
                });
            }


            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        adapter.startListening();
        //Set Adapter
        Log.d("TAG", "" + adapter.getItemCount());
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null)
            adapter.stopListening();
        if (searchAdapter != null)
            searchAdapter.stopListening();

    }
}