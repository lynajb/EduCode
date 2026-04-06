package com.example.educode.models;

public class Category {
    private int id;
    private String name;
    private String iconName;

    public Category(int id, String name, String iconName) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getIconName() { return iconName; }
}