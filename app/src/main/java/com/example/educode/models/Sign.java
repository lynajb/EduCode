package com.example.educode.models;

public class Sign {
    private int id;
    private int categoryId;
    private String name;
    private String description;
    private String imageName;

    public Sign(int id, int categoryId, String name, String description, String imageName) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.imageName = imageName;
    }

    public int getId() { return id; }
    public int getCategoryId() { return categoryId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageName() { return imageName; }
}