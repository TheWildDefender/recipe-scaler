package com.example.recipescaler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        EditorFragment.OnSelectOpenListener,
        FileSelectionFragment.OnFilenameClickListener {

    public RecipeFileManager recipeFileManager;
    private ItemViewModel itemModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recipeFileManager = new RecipeFileManager(getApplicationContext());
        itemModel = new ViewModelProvider(this).get(ItemViewModel.class);
        View navHost = findViewById(R.id.navHostFragment);
        navController = Navigation.findNavController(navHost);
    }

    @Override
    public void onSelectOpen() {
        navController.navigate(R.id.action_editorFragment_to_fileSelectionFragment);
    }

    @Override
    public void onFilenameClick(String filename) {
        ArrayList<Item> items = recipeFileManager.openRecipe(filename);
        itemModel.updateAll(items);
        itemModel.updateRecipeName(filename);

        navController.navigateUp();
    }

}
