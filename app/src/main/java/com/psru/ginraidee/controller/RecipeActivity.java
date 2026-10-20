package com.psru.ginraidee.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.psru.ginraidee.R;
import com.psru.ginraidee.model.Food;
import com.psru.ginraidee.model.FoodManager;
import com.psru.ginraidee.util.BottomNavHelper;

public class RecipeActivity extends AppCompatActivity {

    public static final String EXTRA_FOOD = "extra_food";

    private Food food;
    private MaterialButton btnToggleFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        @SuppressWarnings("deprecation")
        Object extra = getIntent().getSerializableExtra(EXTRA_FOOD);
        if (extra instanceof Food) {
            food = (Food) extra;
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView tvEmoji = findViewById(R.id.tvRecipeEmoji);
        TextView tvFoodName = findViewById(R.id.tvRecipeFoodName);
        TextView tvTag = findViewById(R.id.tvRecipeTag);
        TextView tvInfo = findViewById(R.id.tvRecipeInfo);
        TextView tvIngredients = findViewById(R.id.tvRecipeIngredients);
        TextView tvSteps = findViewById(R.id.tvRecipeSteps);
        btnToggleFavorite = findViewById(R.id.btnToggleFavorite);

        if (food != null) {
            tvEmoji.setText(food.getEmoji());
            tvFoodName.setText(food.getFoodName());
            tvTag.setText(food.getTag());
            tvInfo.setText(food.getInfo());
            tvIngredients.setText(food.getIngredients());
            tvSteps.setText(food.getRecipe());
            updateFavoriteButton();
        } else {
            tvEmoji.setText("🍳");
            tvFoodName.setText("ไม่พบข้อมูลเมนู");
            tvTag.setText("-");
            tvInfo.setText("");
            tvIngredients.setText("ไม่มีข้อมูลวัตถุดิบ");
            tvSteps.setText("ไม่มีข้อมูลวิธีทำ");
        }

        btnToggleFavorite.setOnClickListener(v -> toggleFavorite());

        findViewById(R.id.btnViewLocation).setOnClickListener(v -> {
            if (food != null) {
                String query = "ร้าน " + food.getFoodName() + " พิษณุโลก";
                try {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query)));
                        startActivity(webIntent);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "เปิดแผนที่ไม่ได้: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        BottomNavHelper.setup(this, R.id.navHome);
    }

    private void toggleFavorite() {
        if (food == null) return;
        boolean isFav = FoodManager.getInstance().toggleFavorite(food);
        updateFavoriteButton();
        String msg = isFav ? "บันทึกเข้าเมนูโปรดแล้ว ❤️" : "ยกเลิกเมนูโปรดแล้ว 🤍";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteButton() {
        if (food != null && food.isFavorite()) {
            btnToggleFavorite.setText("❤️  อยู่ในรายการโปรดแล้ว");
        } else {
            btnToggleFavorite.setText("🤍  เพิ่มเป็นเมนูโปรด");
        }
    }
}
