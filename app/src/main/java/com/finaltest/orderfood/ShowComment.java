package com.finaltest.orderfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.finaltest.orderfood.Common.Common;
import com.finaltest.orderfood.Model.Rating;
import com.finaltest.orderfood.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout msSwipeRefreshLayout;

    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;

    String foodId ="";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/restaurant_font.otf")
        .setFontAttrId(R.attr.fontPath)
        .build());
        setContentView(R.layout.activity_show_comment);

        //Firebase
        database =FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerComment);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe Layout
        msSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        msSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(getIntent()!=null)
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if(!foodId.isEmpty() && foodId!= null)
                {
                    //Create request query
                    Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);
                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query,Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull Rating rating) {
                            showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRateValue()));
                            showCommentViewHolder.txtComment.setText(rating.getComment());
                            showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.show_comment_layout,parent,false);
                            return new ShowCommentViewHolder(view);
                        }
                    };
                    loadComment(foodId);
                }
            }
        });

        //Thread to load comment on first launch
        msSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                msSwipeRefreshLayout.setRefreshing(true);

                if(getIntent()!=null)
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if(!foodId.isEmpty() && foodId!= null)
                {
                    //Create request query
                    Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);
                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query,Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull Rating rating) {
                            showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRateValue()));
                            showCommentViewHolder.txtComment.setText(rating.getComment());
                            showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.show_comment_layout,parent,false);
                            return new ShowCommentViewHolder(view);
                        }
                    };
                    loadComment(foodId);
                }
            }
        });
    }

    private void loadComment(String foodId) {
        adapter.startListening();

        recyclerView.setAdapter(adapter);
        msSwipeRefreshLayout.setRefreshing(false);
    }
}