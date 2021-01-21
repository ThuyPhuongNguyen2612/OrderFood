package com.finaltest.orderfood.Model;

public class Favorites {
    private String foodId, foodName, foodPrice, foodMenuId, foodImage, foodDiscount, foodDescruption, userPhone;

    public Favorites() {
    }

    public Favorites(String foodId, String foodName, String foodPrice, String foodMenuId, String foodImage, String foodDiscount, String foodDescruption, String userPhone) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodMenuId = foodMenuId;
        this.foodImage = foodImage;
        this.foodDiscount = foodDiscount;
        this.foodDescruption = foodDescruption;
        this.userPhone = userPhone;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }

    public String getFoodMenuId() {
        return foodMenuId;
    }

    public void setFoodMenuId(String foodMenuId) {
        this.foodMenuId = foodMenuId;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public String getFoodDiscount() {
        return foodDiscount;
    }

    public void setFoodDiscount(String foodDiscount) {
        this.foodDiscount = foodDiscount;
    }

    public String getFoodDescruption() {
        return foodDescruption;
    }

    public void setFoodDescruption(String foodDescruption) {
        this.foodDescruption = foodDescruption;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}
