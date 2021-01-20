package com.finaltest.orderfood.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.finaltest.orderfood.Model.User;

public class Common {
    public static User currentUser;

    public static final String INTENT_FOOD_ID = "FoodId";
    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals(""))
            return "On my way";
        else
            return "Shipped";
    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";


    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info!=null){
                for (int i=0; i<info.length;i++){
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) return true;
                }
            }
        }
        return false;
    }
}
