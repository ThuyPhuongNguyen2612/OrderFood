package com.finaltest.orderfood.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finaltest.orderfood.Interface.ItemClickListener;
import com.finaltest.orderfood.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView food_name;
    public ImageView food_image;

    private ItemClickListener itemClickListener;

    public void setImItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super(itemView);

        food_name = (TextView) itemView.findViewById(R.id.food_name);
//        food_image = (ImageView) itemView.findViewById(R.id.food_image);

    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
