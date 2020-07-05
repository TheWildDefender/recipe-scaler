package com.example.recipescaler;

public class Item {
    public String name;
    public float quantity;
    public String units;

    public Item(String name, float quantity, String units) {
        this.name = name;
        this.quantity = quantity;
        this.units = units;
    }

    public Item(Item item) {
        this.name = item.name;
        this.quantity = item.quantity;
        this.units = item.units;
    }
}
