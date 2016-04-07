package com.example.professorlee.foodnotes;


public class TableItem_search_food {

    private String name;
    private String location; //地址

    private String distance; //距離

    TableItem_search_food(String name, String location, String distance) {
        this.name = name;
        this.location = location;

        this.distance = distance;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    public String getLocation() {
        return location;
    }


}
