package com.example.recipescaler;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ItemViewModel extends ViewModel {

    private ArrayList<Item> items = new ArrayList<>(0);
    private String recipeName = "";

    public void add(String name, float startQuantity, String startUnits) {
        Item item = new Item(name, startQuantity, startUnits);
        items.add(item);
    }

    public void remove(int index) {
        items.remove(index);
    }

    public ArrayList<Item> getAll() {
        ArrayList<Item> itemsClone = new ArrayList<>(0);
        for(Item item : items) {
            Item itemClone = new Item(item);
            itemsClone.add(itemClone);
        }
        return itemsClone;
    }

    public void updateAll(ArrayList<Item> items) {
        ArrayList<Item> itemsClone = new ArrayList<>(0);
        for(Item item : items) {
            Item itemClone = new Item(item);
            itemsClone.add(itemClone);
        }
        this.items = itemsClone;
    }

    public void updateRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeName() {
        return recipeName;
    }

}
