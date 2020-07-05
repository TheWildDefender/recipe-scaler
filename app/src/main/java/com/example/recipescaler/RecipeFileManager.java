package com.example.recipescaler;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RecipeFileManager {

    private Context context;
    private File appDir;

    public RecipeFileManager(Context context) {
        this.context = context;
        appDir = context.getExternalFilesDir(null);
    }

    public ArrayList<String> getFileList() {
        File[] files = appDir.listFiles();
        assert files != null;
        ArrayList<String> filenames = new ArrayList<>(0);
        for(File file : files) {
            String filename = file.getName();
            filenames.add(filename);
        }
        return filenames;
    }

    public void saveRecipe(String filename, ArrayList<Item> items) {
        File file = new File(appDir, filename + ".txt");
        file.delete();
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            //FileOutputStream fileOutputStream = new FileOutputStream(file);
            for (Item item : items) {
                fileWriter.write(item.name);
                fileWriter.write(',');
                fileWriter.write(Float.toString(item.quantity));
                fileWriter.write(',');
                fileWriter.write(item.units);
                fileWriter.write(',');
                fileWriter.write('\n');
            }

            fileWriter.close();
            Toast toast = Toast.makeText(context, filename + " saved", Toast.LENGTH_SHORT);
            toast.show();
        }
        catch(IOException e) {
            Toast toast = Toast.makeText(context, "IOException occurred in RecipeFileManager.saveRecipe()", Toast.LENGTH_SHORT);
            toast.show();
        }
        catch(SecurityException e) {
            Toast toast = Toast.makeText(context, "SecurityException occurred in RecipeFileManager.saveRecipe()", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public ArrayList<Item> openRecipe(String filename) {
        ArrayList<Item> items = new ArrayList<>(0);
        File file = new File(appDir, filename + ".txt");

        try {
            FileReader fileReader = new FileReader(file);
            //FileInputStream fileInputStream = new FileInputStream(file);
            int raw = 0;
            char c;
            StringBuilder entry = new StringBuilder();
            ArrayList<String> itemLine = new ArrayList<>(0);

            while (raw != -1) {
                raw = fileReader.read();
                c = (char)raw;
                if(c == '\n') {
                    Item item = new Item(
                            itemLine.get(0),
                            Float.parseFloat(itemLine.get(1)),
                            itemLine.get(2) );
                    items.add(item);
                    itemLine = new ArrayList<>(0);
                }
                else if(c == ',') {
                    itemLine.add(entry.toString());
                    entry.delete(0, entry.length());
                }
                else if(raw != -1) entry.append(c);
            }

            fileReader.close();
        }
        catch(IOException e) {
            Toast toast = Toast.makeText(context, "IOException occurred in RecipeFileManager.openRecipe()", Toast.LENGTH_SHORT);
            toast.show();
        }

        return items;
    }

    public void deleteRecipe(String filename) {
        File file = new File(appDir, filename + ".txt");
        file.delete();
    }

}
