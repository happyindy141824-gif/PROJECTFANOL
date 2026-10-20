package com.psru.ginraidee.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.psru.ginraidee.R;
import com.psru.ginraidee.model.AiSuggestion;
import com.psru.ginraidee.model.Food;
import com.psru.ginraidee.model.FoodManager;
import com.psru.ginraidee.util.BottomNavHelper;

import java.util.ArrayList;
import java.util.List;

public class MenuCatalogActivity extends AppCompatActivity {

    public static final String EXTRA_SELECT_MODE = "extra_select_mode";
    public static final String EXTRA_SELECTED_FOOD = "extra_selected_food";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_catalog);

        boolean isSelectMode = getIntent().getBooleanExtra(EXTRA_SELECT_MODE, false);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView rvMenuCatalog = findViewById(R.id.rvMenuCatalog);
        rvMenuCatalog.setLayoutManager(new LinearLayoutManager(this));
        SuggestionAdapter adapter = new SuggestionAdapter();
        rvMenuCatalog.setAdapter(adapter);

        if (isSelectMode) {
            adapter.setOnItemClickListener(food -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SELECTED_FOOD, food);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }

        List<Food> allFoods = FoodManager.getInstance().getFoodList();
        List<AiSuggestion> list = new ArrayList<>();
        for (Food f : allFoods) {
            list.add(new AiSuggestion(f.getFoodName(), f.getTag() + " • " + f.getCalories() + " แคลอรี", f));
        }
        adapter.submit(list);

        BottomNavHelper.setup(this, R.id.navHome);
    }
}
