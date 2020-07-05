package com.example.recipescaler;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class FileSelectionFragment extends Fragment {

    private ArrayList<CheckBox> checkboxes;
    private ArrayList<String> rawFilenames;
    private LinearLayout filenameLayout;
    private Menu optionsMenu;
    private RecipeFileManager recipeFileManager;
    private int ITEM_PX_HEIGHT;
    private int ITEM_TEXT_SIZE;
    private final int SELECT_MENU_ITEM_ID = 0;
    private final int CANCEL_MENU_ITEM_ID = 1;
    private final int DELETE_MENU_ITEM_ID = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        // Initialize constants
        ITEM_PX_HEIGHT = Math.round(getResources().getDimension(R.dimen.filename_px_height));
        ITEM_TEXT_SIZE = 16;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_selection, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Create the list of recipes
        filenameLayout = view.findViewById(R.id.filenameLayout);
        recipeFileManager = ( (MainActivity)requireActivity() ).recipeFileManager;
        rawFilenames = recipeFileManager.getFileList();
        for(final String rawFilename : rawFilenames) {
            // Create TextView displaying rawFilename
            TextView textView = new TextView(requireContext());
            textView.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
            textView.setText(rawFilename);
            textView.setTextColor(getResources().getColor(R.color.colorText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, ITEM_TEXT_SIZE);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ( (OnFilenameClickListener)requireActivity() ).onFilenameClick( rawFilename.substring(0, rawFilename.length() - 4) );
                }
            });
            int textViewID = View.generateViewId();
            textView.setId(textViewID);

            // Create ConstraintLayout to wrap the TextView so that checkboxes can be added to the left side later
            ConstraintLayout row = new ConstraintLayout(requireContext());
            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ITEM_PX_HEIGHT));
            row.addView(textView);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(row);
            constraintSet.centerHorizontally(textViewID, ConstraintSet.PARENT_ID);
            constraintSet.centerVertically  (textViewID, ConstraintSet.PARENT_ID);
            constraintSet.applyTo(row);

            // Add row to filenameLayout
            filenameLayout.addView(row);

            // Create a divider to make it look good
            View divider = new View(requireContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            float dpRatio = getResources().getDisplayMetrics().density;
            layoutParams.setMargins((int)(10 * dpRatio), (int)(3 * dpRatio), (int)(10 * dpRatio), (int)(3 * dpRatio));
            divider.setLayoutParams(layoutParams);
            divider.setBackgroundColor(0xFFA0A0A0);
            filenameLayout.addView(divider);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Create menu item
        addMenuItem(menu, R.string.menu_item_select, R.drawable.check_box, SELECT_MENU_ITEM_ID);
        // Capture menu object
        optionsMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case SELECT_MENU_ITEM_ID:
                optionsMenu.clear();
                addMenuItem(optionsMenu, R.string.menu_item_delete, R.drawable.delete, DELETE_MENU_ITEM_ID);
                addMenuItem(optionsMenu, R.string.menu_item_cancel, R.drawable.clear , CANCEL_MENU_ITEM_ID);
                checkboxes = addCheckboxes(filenameLayout);
                break;
            case CANCEL_MENU_ITEM_ID:
                clearCheckboxes(filenameLayout, checkboxes);
                optionsMenu.clear();
                addMenuItem(optionsMenu, R.string.menu_item_select, R.drawable.check_box, SELECT_MENU_ITEM_ID);
                break;
            case DELETE_MENU_ITEM_ID:
                for(int i = 0; i < checkboxes.size(); i++) {
                    CheckBox checkbox = checkboxes.get(i);
                    if(checkbox.isChecked()) {
                        String rawFilename = rawFilenames.get(i);
                        String filename = rawFilename.substring(0, rawFilename.length() - 4); // ".txt" is 4 characters long
                        recipeFileManager.deleteRecipe(filename);
                        rawFilenames.remove(i);
                        filenameLayout.removeViewAt(i * 2);
                        filenameLayout.removeViewAt(i * 2); // Don't forget to remove the divider; same index because elements will fall back to fill the gap
                        checkboxes.remove(i);
                        i--; // Compensate for removing an element
                    }
                }
                clearCheckboxes(filenameLayout, checkboxes);
                optionsMenu.clear();
                addMenuItem(optionsMenu, R.string.menu_item_select, R.drawable.check_box, SELECT_MENU_ITEM_ID);
                break;
            default:
                return false;
        }
        return true;
    }

    private void addMenuItem(Menu menu, int titleRes, int iconRes, int id) {
        MenuItem menuItem = menu.add(Menu.NONE, id, Menu.NONE, titleRes);
        menuItem.setIcon(iconRes);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    public ArrayList<CheckBox> addCheckboxes(LinearLayout filenameLayout) {
        ArrayList<CheckBox> checkboxes = new ArrayList<>(0);
        for(int i = 0; i < filenameLayout.getChildCount() / 2; i++) {
            CheckBox checkbox = new CheckBox(requireContext());
            checkbox.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
            int checkboxID = View.generateViewId();
            checkbox.setId(checkboxID);

            ConstraintLayout row = (ConstraintLayout)filenameLayout.getChildAt(i * 2);
            row.addView(checkbox);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(row);
            TextView textView = (TextView)row.getChildAt(0);
            constraintSet.addToHorizontalChain(checkboxID, ConstraintSet.PARENT_ID, textView.getId());
            constraintSet.centerVertically(checkboxID, ConstraintSet.PARENT_ID);
            constraintSet.applyTo(row);

            checkboxes.add(checkbox);
        }
        return checkboxes;
    }

    public void clearCheckboxes(LinearLayout filenameLayout, ArrayList<CheckBox> checkboxes) {
        for(int i = 0; i < checkboxes.size(); i++) {
            ConstraintLayout row = (ConstraintLayout)filenameLayout.getChildAt(i * 2);
            row.removeView(checkboxes.get(i));
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(row);
            TextView textView = (TextView)row.getChildAt(0);
            constraintSet.connect(textView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            constraintSet.applyTo(row);
        }
        checkboxes.clear();
    }

    public interface OnFilenameClickListener {
        void onFilenameClick(String filename);
    }

}
