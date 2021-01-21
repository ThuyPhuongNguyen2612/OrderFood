package com.finaltest.orderfood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;

import com.finaltest.orderfood.Common.Common;
import com.finaltest.orderfood.Database.Database;
import com.finaltest.orderfood.Helper.RecyclerItemTouchHelper;
import com.finaltest.orderfood.Interface.RecyclerItemTouchHelperListener;
import com.finaltest.orderfood.Model.Favorites;
import com.finaltest.orderfood.Model.Order;
import com.finaltest.orderfood.ViewHolder.FavoritesAdapter;
import com.finaltest.orderfood.ViewHolder.FavoritesViewHolder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FavoritesAdapter adapter;

    RelativeLayout rootLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_fav);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(), R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(controller);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        loadFavorites();
    }

    private void loadFavorites() {
        adapter = new FavoritesAdapter(this,new Database(this).getAllFavorites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoritesViewHolder){
            String name = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(position).getFoodName();

            final int deleteIndex = viewHolder.getAdapterPosition();
            final Favorites deleteItem = ((FavoritesAdapter)recyclerView.getAdapter()).getItem(deleteIndex);

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromFavorites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            //Make Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout,name +" removed from cart!",Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToFavorites(deleteItem);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
    }
}