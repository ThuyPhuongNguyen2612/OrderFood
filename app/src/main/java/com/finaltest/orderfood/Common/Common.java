package com.finaltest.orderfood.Common;

import com.finaltest.orderfood.Model.User;

public class Common {
    public static User currentUser;
    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals(""))
            return "On my way";
        else
            return "Shipped";
    }
}
