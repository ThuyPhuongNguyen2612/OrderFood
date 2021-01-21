package com.finaltest.orderfood;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;


import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.finaltest.orderfood.Common.Common;
import com.finaltest.orderfood.Database.Database;
import com.finaltest.orderfood.Interface.ItemClickListener;
import com.finaltest.orderfood.Model.Banner;
import com.finaltest.orderfood.Model.Category;
import com.finaltest.orderfood.Service.ListenOrder;
import com.finaltest.orderfood.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    FirebaseDatabase database;
    DatabaseReference category;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    TextView txtFullName;

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    CounterFab fab;

    //Slide
    HashMap<String,String> image_list;
    SliderLayout mSlider;

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

        setContentView(R.layout.activity_home);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);

        //View
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                } else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

        //Default, load for first time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    loadMenu();
                } else {
                    Toast.makeText(getBaseContext(), "Please check your connection !!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");

        Paper.init(this);


        fab = (CounterFab) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent cartIntent = new Intent(Home.this, Cart.class);
                    startActivity(cartIntent);

                }
            });

            fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Name for user
        View headerView = navigationView.getHeaderView(0);
        txtFullName = (TextView) headerView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());

        //Load menu
        recycler_menu = (RecyclerView) findViewById(R.id.recycler_menu);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);




        //Register Service
        Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);

        //Setup Slider
        //Need call this function after you init database firebase
        setupSlider();


    }

    private void setupSlider() {
        mSlider = (SliderLayout) findViewById(R.id.slider);
        image_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Banner");

        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapShot:snapshot.getChildren())
                {
                    Banner banner = postSnapShot.getValue(Banner.class);
                    //We will concat string and id like
                    //PIZZA_01 => And we will use PIZZA for show description, 01 for food id to click
                    image_list.put(banner.getName()+ "@@@" + banner.getId(),banner.getImage());
                }
                for(String key:image_list.keySet())
                {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    //Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView.description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                    Intent intent = new Intent(Home.this,FoodDetail.class);
                                    //We will send food id to FoodDetail
                                    intent.putExtras(textSliderView.getBundle());
                                    startActivity(intent);

                                }
                            });

                    //Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",idOfFood);

                    mSlider.addSlider(textSliderView);

                    //Remove event after finish
                    banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);

    }

    private void loadMenu() {

        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder menuViewHolder, int i, @NonNull Category category) {
                menuViewHolder.txtMenuName.setText(category.getName());
                Picasso.with(getBaseContext()).load(category.getImage()).into(menuViewHolder.imageView);
                final Category clickItem = category;
                menuViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Get CategoryID and send to new Activity
                        Intent foodList = new Intent(Home.this, FoodList.class);
                        //Because CategoryID is key, so we just get key of this item
                        foodList.putExtra("CategoryID", adapter.getRef(position).getKey());
                        startActivity(foodList);
                    }
                });
            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //Animation
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        //Fix click back button form Food and don't see category
        if(adapter!=null)
            adapter.startListening();
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.nav_search)
            startActivity(new Intent(Home.this,SearchActivity.class));

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this,Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_order) {
            Intent orderIntent = new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);

        } else if (id == R.id.nav_exit) {

            //Delete Remember user & password
            Paper.book().destroy();

            //Logout
            Intent signIn = new Intent(Home.this, SignIn.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_change_password){
            showChangePasswordDialog();
        }
        else if (id == R.id.nav_home_address){
            showHpmeAddressDialog();
        }else if(id == R.id.nav_favorites) {
            startActivity(new Intent(Home.this,FavoritesActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHpmeAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("CHANGE HOME ADDRESS");
        builder.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edtHomeAddress = (MaterialEditText) layout_home.findViewById(R.id.edtHomeAddress);

        builder.setView(layout_home);

        builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //Set new Home Address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Update Address Successful", Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
        builder.setTitle("CHANGE PASSWORD");
        builder.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        builder.setView(layout_pwd);

        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //change password here
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                waitingDialog.show();

                //check old password
                if (edtPassword.getText().toString().equals(Common.currentUser.getPassword())) {
                    //check new password and repeat password
                    if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())) {
                        Map<String, Object> passwordUpdate = new HashMap<>();
                        passwordUpdate.put("password", edtNewPassword.getText().toString());

                        //Make update
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone()).updateChildren(passwordUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                Toast.makeText(Home.this, "Password was update", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Home.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Home.this,"New password doesn't match",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    waitingDialog.dismiss();
                    Toast.makeText(Home.this,"Wrong old password",Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        builder.show();


    }
}