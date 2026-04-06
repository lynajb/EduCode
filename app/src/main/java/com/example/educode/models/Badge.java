package com.example.educode.models;

public class Badge {
    private int id;
    private String name;
    private String description;
    private String iconName;
    private boolean isUnlocked;

    public Badge(int id, String name, String description, String iconName, boolean isUnlocked) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.isUnlocked = isUnlocked;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconName() { return iconName; }
    public boolean isUnlocked() { return isUnlocked; }
    public void setUnlocked(boolean unlocked) { isUnlocked = unlocked; }
}