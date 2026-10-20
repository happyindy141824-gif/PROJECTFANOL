package com.psru.ginraidee.controller;

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

/**
 * หน้าจอแสดงรายการเมนูอาหารโปรดที่ผู้ใช้กดบันทึกไว้ (Story Board หน้าเมนูโปรด)
 *
 * <p>บทบาทตามสถาปัตยกรรม MVC: เป็น <b>Controller</b> ดึงรายการเมนูที่ติดดาวโปรดไว้จาก Model
 * {@link FoodManager#getFavoriteFoods()} นำมาแปลงเป็นรูปแบบ {@link AiSuggestion} เพื่อส่งให้
 * {@link SuggestionAdapter} นำไปแสดงผลบน RecyclerView</p>
 */
public class FavoriteActivity extends AppCompatActivity {

    /** ตัวจัดรายการแสดงผลการ์ดอาหารบน RecyclerView */
    private SuggestionAdapter adapter;

    /** อินสแตนซ์ Singleton ของ Model สำหรับจัดการคลังเมนูอาหาร */
    private FoodManager foodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // 1. ดึงตัวจัดการคลังเมนูอาหาร (Model)
        foodManager = FoodManager.getInstance();

        // 2. ตั้งค่า RecyclerView และ Adapter สำหรับแสดงรายการอาหารโปรด
        RecyclerView rvFavorites = findViewById(R.id.rvFavorites);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SuggestionAdapter();
        rvFavorites.setAdapter(adapter);

        // 3. ปุ่มย้อนกลับไปยังหน้าก่อนหน้า
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 4. โหลดรายการเมนูโปรดมาแสดงผล
        loadFavorites();

        // 5. ตั้งค่าการทำงานของแถบเมนูด้านล่าง (Bottom Navigation)
        BottomNavHelper.setup(this, R.id.navFavorite);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // เมื่อผู้ใช้อยู่หน้านี้แล้วกลับมาจากหน้าอื่น ให้อัปเดตรายการโปรดใหม่เผื่อมีการยกเลิก/กดเพิ่ม
        loadFavorites();
    }

    /**
     * ดึงรายการเมนูโปรดทั้งหมดจาก Model (FoodManager)
     * แล้วแปลงเป็นวัตถุ AiSuggestion เพื่อส่งให้ Adapter นำไปวาดรายการการ์ดบน View
     */
    private void loadFavorites() {
        List<Food> favorites = foodManager.getFavoriteFoods();
        List<AiSuggestion> list = new ArrayList<>();
        for (Food f : favorites) {
            list.add(new AiSuggestion(f.getFoodName(), f.getTag() + " • " + f.getCalories() + " แคลอรี", f));
        }
        adapter.submit(list);
    }
}
