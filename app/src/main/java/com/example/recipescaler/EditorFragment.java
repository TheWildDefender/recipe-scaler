package com.example.recipescaler;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class EditorFragment extends Fragment {

    private ArrayList<ArrayList<EditText>> itemInputs ;
    private ArrayList<ArrayList<TextView>> itemOutputs;
    private ArrayList<CheckBox> checkboxes = new ArrayList<>(0);
    private LinearLayout itemLayout;
    private Menu optionsMenu;
    private EditText recipeNameInput;
    private EditText startSizeInput;
    private EditText endSizeInput;
    private NameInputTextWatcher   nameInputTextWatcher   = new NameInputTextWatcher();
    private AmountInputTextWatcher amountInputTextWatcher = new AmountInputTextWatcher();
    private ItemViewModel itemModel;
    private int ITEM_PX_HEIGHT;
    private int ITEM_TEXT_SIZE;
    private final float QUANTITY_WIDTH_PERCENT = 0.125f;
    private final float UNITS_WIDTH_PERCENT    = 0.175f;
    private final int ITEM_START_INDEX = 3;
    private final int ITEM_LAYOUT_INITIAL_ELEMENTS = 4;
    private final int OPEN_MENU_ITEM_ID         = 0;
    private final int SAVE_MENU_ITEM_ID         = 1;
    private final int SELECT_MENU_ITEM_ID       = 2;
    private final int CANCEL_MENU_ITEM_ID       = 3;
    private final int SELECT_ALL_MENU_ITEM_ID   = 4;
    private final int DELETE_MENU_ITEM_ID       = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        // Initialize constants
        itemModel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
        ITEM_PX_HEIGHT = Math.round(getResources().getDimension(R.dimen.item_px_height));
        ITEM_TEXT_SIZE = 16;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Reset the ArrayLists holding input and output views
        itemInputs  = new ArrayList<>(0);
        itemOutputs = new ArrayList<>(0);

        itemLayout = view.findViewById(R.id.itemLayout);

        // Get inputs already defined in XML
        recipeNameInput = view.findViewById(R.id.recipeName);
        startSizeInput  = view.findViewById(R.id.startSizeInput);
        endSizeInput    = view.findViewById(R.id.endSizeInput);
        // Add appropriate TextWatchers
        startSizeInput  .addTextChangedListener(amountInputTextWatcher);
        endSizeInput    .addTextChangedListener(amountInputTextWatcher);

        // Hide keyboard when parent layouts receive focus
        ConstraintLayout baseConstraintLayout = view.findViewById(R.id.baseConstraintLayout);
        LinearLayout itemLayout = view.findViewById(R.id.itemLayout);
        baseConstraintLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                hideKeyboard(view);
            }
        });
        itemLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                hideKeyboard(view);
            }
        });

        // Fill itemLayout based on itemModel
        ArrayList<Item> items = itemModel.getAll();
        for(int i = 0; i < items.size(); i++) {
            Pair<ArrayList<EditText>, ArrayList<TextView>> itemViews =
                    addEmptyItemToLayout(itemLayout, QUANTITY_WIDTH_PERCENT, UNITS_WIDTH_PERCENT, ITEM_PX_HEIGHT, ITEM_TEXT_SIZE, i, ITEM_START_INDEX);
            itemInputs .add(itemViews.first );
            itemOutputs.add(itemViews.second);
            itemInputs.get(i).get(0).setText(items.get(i).name);
            String quantityString = Float.toString(items.get(i).quantity);
            quantityString = quantityString.equals("0.0") ? "" : quantityString;
            itemInputs.get(i).get(1).setText(quantityString);
            itemInputs.get(i).get(2).setText(items.get(i).units);
        }

        // Set click listener for add button
        ImageButton addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddButtonClick();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set recipe name according to itemModel
        // This needs to be in onStart() so that a new recipe name can overwrite the previous saved name
        String recipeName = itemModel.getRecipeName();
        recipeNameInput.setText(recipeName);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Create menu items
        addMenuItem(menu, R.string.menu_item_open  , R.drawable.open     , OPEN_MENU_ITEM_ID  );
        addMenuItem(menu, R.string.menu_item_save  , R.drawable.save     , SAVE_MENU_ITEM_ID  );
        addMenuItem(menu, R.string.menu_item_select, R.drawable.check_box, SELECT_MENU_ITEM_ID);
        // Capture menu object
        optionsMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        ImageButton addButton = requireView().findViewById(R.id.addButton);
        switch(menuItem.getItemId()) {
            case OPEN_MENU_ITEM_ID: {
                // Save recipe name
                String recipeName = recipeNameInput.getText().toString();
                itemModel.updateRecipeName(recipeName);

                ( (OnSelectOpenListener)requireActivity() ).onSelectOpen();
                break;
            }
            case SAVE_MENU_ITEM_ID: {
                ArrayList<Item> items = itemModel.getAll();
                RecipeFileManager recipeFileManager = ( (MainActivity)requireActivity() ).recipeFileManager;

                EditText recipeNameInput = requireView().findViewById(R.id.recipeName);
                String recipeName = recipeNameInput.getText().toString();
                // Save only if recipe name length is nonzero
                if(recipeName.length() == 0) {
                    Toast toast = Toast.makeText(requireContext(), "Please specify a name for the recipe", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else recipeFileManager.saveRecipe(recipeName, items);
                break;
            }
            case SELECT_MENU_ITEM_ID: {
                optionsMenu.clear();
                addMenuItem(optionsMenu, R.string.menu_item_delete    , R.drawable.delete     , DELETE_MENU_ITEM_ID    );
                addMenuItem(optionsMenu, R.string.menu_item_select_all, R.drawable.checked_box, SELECT_ALL_MENU_ITEM_ID);
                addMenuItem(optionsMenu, R.string.menu_item_cancel    , R.drawable.clear      , CANCEL_MENU_ITEM_ID    );
                int checkboxCount = itemLayout.getChildCount() - ITEM_LAYOUT_INITIAL_ELEMENTS;
                checkboxes = addCheckboxes(itemLayout, ITEM_START_INDEX, checkboxCount);
                addButton.setVisibility(View.GONE);
                break;
            }
            case CANCEL_MENU_ITEM_ID:
                clearCheckboxes(itemLayout, ITEM_START_INDEX, checkboxes);
                optionsMenu.clear();
                addMenuItem(optionsMenu, R.string.menu_item_open  , R.drawable.open     , OPEN_MENU_ITEM_ID  );
                addMenuItem(optionsMenu, R.string.menu_item_save  , R.drawable.save     , SAVE_MENU_ITEM_ID  );
                addMenuItem(optionsMenu, R.string.menu_item_select, R.drawable.check_box, SELECT_MENU_ITEM_ID);
                addButton.setVisibility(View.VISIBLE);
                break;
            case SELECT_ALL_MENU_ITEM_ID:
                for (CheckBox checkbox : checkboxes)
                    checkbox.setChecked(true);
                break;
            case DELETE_MENU_ITEM_ID:
                for (int i = 0; i < checkboxes.size(); i++) {
                    CheckBox checkbox = checkboxes.get(i);
                    if (checkbox.isChecked()) {
                        itemModel.remove(i);
                        removeItemFromLayout(itemLayout, itemInputs, itemOutputs, i, ITEM_START_INDEX);
                        checkboxes.remove(i);
                        i--; // Compensate for deleting an element
                    }
                }
                clearCheckboxes(itemLayout, ITEM_START_INDEX, checkboxes);
                optionsMenu.clear();
                addMenuItem(optionsMenu, R.string.menu_item_open  , R.drawable.open     , OPEN_MENU_ITEM_ID  );
                addMenuItem(optionsMenu, R.string.menu_item_save  , R.drawable.save     , SAVE_MENU_ITEM_ID  );
                addMenuItem(optionsMenu, R.string.menu_item_select, R.drawable.check_box, SELECT_MENU_ITEM_ID);
                addButton.setVisibility(View.VISIBLE);
                break;
            default:
                return false;
        }
        return true;
    }

    private void onAddButtonClick() {
        int positionIndex = itemLayout.getChildCount() - ITEM_LAYOUT_INITIAL_ELEMENTS;
        Pair<ArrayList<EditText>, ArrayList<TextView>> itemViews =
                addEmptyItemToLayout(itemLayout, QUANTITY_WIDTH_PERCENT, UNITS_WIDTH_PERCENT, ITEM_PX_HEIGHT, ITEM_TEXT_SIZE, positionIndex, ITEM_START_INDEX);
        itemInputs .add(itemViews.first );
        itemOutputs.add(itemViews.second);
        itemModel.add("", 0.0f, "");
    }

    private Pair<ArrayList<EditText>, ArrayList<TextView>> addEmptyItemToLayout(LinearLayout layout,
                                                                                float quantityWidthPercent,
                                                                                float unitsWidthPercent,
                                                                                int itemHeight,
                                                                                int textSize,
                                                                                int positionIndex,
                                                                                int startIndex) {

        // Create EditText for item name
        EditText name = new EditText(requireContext());
        name.setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        name.addTextChangedListener(nameInputTextWatcher);
        int nameID = View.generateViewId();
        name.setId(nameID);

        // Create start info EditTexts
        EditText startQuantity  = new EditText(requireContext());
        EditText startUnits     = new EditText(requireContext());
        startQuantity   .setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
        startUnits      .setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
        startQuantity   .setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        startUnits      .setInputType(InputType.TYPE_CLASS_TEXT  );
        startQuantity   .setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        startUnits      .setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        startQuantity   .addTextChangedListener(amountInputTextWatcher);
        startUnits      .addTextChangedListener(amountInputTextWatcher);
        int startQuantityID = View.generateViewId();
        int startUnitsID    = View.generateViewId();
        startQuantity   .setId(startQuantityID);
        startUnits      .setId(startUnitsID   );

        // Create end info TextViews
        TextView endQuantity    = new TextView(requireContext());
        TextView endUnits       = new TextView(requireContext());
        endQuantity .setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
        endUnits    .setLayoutParams(new ConstraintLayout.LayoutParams(0, 0));
        endQuantity .setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        endUnits    .setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        endQuantity .setTextColor(getResources().getColor(R.color.colorText));
        endUnits    .setTextColor(getResources().getColor(R.color.colorText));
        endQuantity .setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        endUnits    .setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        endQuantity .setText("0.0");
        int endQuantityID = View.generateViewId();
        int endUnitsID    = View.generateViewId();
        endQuantity   .setId(endQuantityID);
        endUnits      .setId(endUnitsID   );

        // Create ConstraintLayout for the item row and pack inputs and views into it
        ConstraintLayout row = new ConstraintLayout(requireContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight));
        row.addView(name);
        row.addView(startQuantity); row.addView(startUnits);
        row.addView(endQuantity);   row.addView(endUnits);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(row);
        int[] views = {nameID, startQuantityID, startUnitsID, endQuantityID, endUnitsID};
        constraintSet.createHorizontalChain(ConstraintSet.PARENT_ID, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, views, null, ConstraintSet.CHAIN_SPREAD);
        for(int view : views)
            constraintSet.centerVertically(view, ConstraintSet.PARENT_ID);
        constraintSet.constrainPercentWidth(startQuantityID, quantityWidthPercent); constraintSet.constrainPercentWidth(endQuantityID, quantityWidthPercent);
        constraintSet.constrainPercentWidth(startUnitsID   , unitsWidthPercent   ); constraintSet.constrainPercentWidth(endUnitsID   , unitsWidthPercent   );
        constraintSet.applyTo(row);

        // Add item row to itemLayout
        layout.addView(row, positionIndex + startIndex);

        // Put EditTexts and TextViews into an ArrayList for both types, put the ArrayLists into a Pair, and return the Pair
        ArrayList<EditText> editTexts = new ArrayList<>(0);
        ArrayList<TextView> textViews = new ArrayList<>(0);
        editTexts.add(name);
        editTexts.add(startQuantity); editTexts.add(startUnits);
        textViews.add(endQuantity  ); textViews.add(endUnits  );
        return new Pair<>(editTexts, textViews);
    }

    private void removeItemFromLayout(LinearLayout itemLayout,
                                      ArrayList<ArrayList<EditText>> editTexts,
                                      ArrayList<ArrayList<TextView>> textViews,
                                      int positionIndex,
                                      int startIndex) {

        itemLayout.removeViewAt(positionIndex + startIndex);
        editTexts.remove(positionIndex);
        textViews.remove(positionIndex);
    }

    private void addMenuItem(Menu menu, int titleRes, int iconRes, int id) {
        MenuItem menuItem = menu.add(Menu.NONE, id, Menu.NONE, titleRes);
        menuItem.setIcon(iconRes);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    private ArrayList<CheckBox> addCheckboxes(LinearLayout itemLayout, int startIndex, int count) {
        ArrayList<CheckBox> checkboxes = new ArrayList<>(0);
        for(int i = 0; i < count; i++) {
            CheckBox checkbox = new CheckBox(requireContext());
            checkbox.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
            int checkboxID = View.generateViewId();
            checkbox.setId(checkboxID);

            ConstraintLayout row = (ConstraintLayout)itemLayout.getChildAt(i + startIndex);
            row.addView(checkbox);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(row);
            EditText name = (EditText)row.getChildAt(0);
            constraintSet.addToHorizontalChain(checkboxID, ConstraintSet.PARENT_ID, name.getId());
            constraintSet.centerVertically(checkboxID, ConstraintSet.PARENT_ID);
            constraintSet.applyTo(row);

            checkboxes.add(checkbox);
        }
        return checkboxes;
    }

    private void clearCheckboxes(LinearLayout itemLayout, int startIndex, ArrayList<CheckBox> checkboxes) {
        for(int i = 0; i < checkboxes.size(); i++) {
            ConstraintLayout row = (ConstraintLayout)itemLayout.getChildAt(i + startIndex);
            row.removeView(checkboxes.get(i));
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(row);
            EditText name = (EditText)row.getChildAt(0);
            constraintSet.connect(name.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            constraintSet.applyTo(row);
        }
        checkboxes.clear();
    }

    private ArrayList<Item> calculateEndInfo(float startSize, float endSize, ArrayList<Item> startItems) {
        if(startSize == 0.0f) startSize = 1.0f;
        if(endSize   == 0.0f) endSize   = 1.0f;
        float ratio = endSize / startSize;
        ArrayList<Item> endItems = new ArrayList<>(0);
        for(int i = 0; i < startItems.size(); i++) {
            float startQuantity = startItems.get(i).quantity;
            String startUnits = startItems.get(i).units;

            float endQuantity = roundFloatToHundredths(ratio * startQuantity);
            String endUnits = startUnits;

            Item endItem = new Item(startItems.get(i).name, endQuantity, endUnits);
            endItems.add(endItem);
        }
        return endItems;
    }

    private float readFloatFromString(String string) {
        // Identify cases where parseFloat() would throw a NumberFormatException and handle them
        if(string.length() == 0)
            return 0.0f;
        else if(string.equals("."))
            return 0.0f;
        else
            return Float.parseFloat(string);
    }

    private float roundFloatToHundredths(float input) {
        return Math.round(input * 100) / 100.0f;
    }

    private void hideKeyboard(View view) {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class NameInputTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            ArrayList<Item> items = new ArrayList<>(0);
            for(int i = 0; i < itemInputs.size(); i++) {
                String name = itemInputs.get(i).get(0).getText().toString();
                String quantityString = itemInputs.get(i).get(1).getText().toString();
                float quantity = readFloatFromString(quantityString);
                String units = itemInputs.get(i).get(2).getText().toString();
                Item item = new Item(name, quantity, units);
                items.add(item);
            }
            itemModel.updateAll(items);
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    private class AmountInputTextWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            ArrayList<Item> items = new ArrayList<>(0);
            for(int i = 0; i < itemInputs.size(); i++) {
                String name = itemInputs.get(i).get(0).getText().toString();
                String quantityString = itemInputs.get(i).get(1).getText().toString();
                float quantity = readFloatFromString(quantityString);
                String units = itemInputs.get(i).get(2).getText().toString();
                Item item = new Item(name, quantity, units);
                items.add(item);
            }
            itemModel.updateAll(items);

            float startSize = readFloatFromString(startSizeInput.getText().toString());
            float endSize   = readFloatFromString(endSizeInput  .getText().toString());
            ArrayList<Item> endItems = calculateEndInfo(startSize, endSize, itemModel.getAll());
            for(int i = 0; i < endItems.size(); i++) {
                String quantityText = Float.toString(endItems.get(i).quantity);
                itemOutputs.get(i).get(0).setText(quantityText);
                String unitsText = endItems.get(i).units;
                itemOutputs.get(i).get(1).setText(unitsText);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }

    public interface OnSelectOpenListener {
        void onSelectOpen();
    }

}
