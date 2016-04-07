package com.example.professorlee.foodnotes;


public class TableItem {

    private String location;
    private String shopname;
    private String foodimage;

    TableItem(String shopname, String location, String foodimage) {
        this.location = location;
        this.shopname = shopname;

        this.foodimage = foodimage;

    }

    public String getLocation() {
        return location;
    }

    public String getShopname() {
        return shopname;
    }

    public String getFoodimage() {
        return foodimage;
    }


}
