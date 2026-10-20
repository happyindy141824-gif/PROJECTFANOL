package com.psru.ginraidee.controller;

import android.os.Bundle;
import android.widget.TextView;

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
 * หน้าจอแสดงรายการผลลัพธ์การค้นหาเมนูอาหารจากวัตถุดิบ
 *
 * <p>บทบาทตามสถาปัตยกรรม MVC: เป็น <b>Controller</b> ที่รับคีย์เวิร์ดค้นหาวัตถุดิบมาจาก {@link IngredientSearchActivity}
 * แล้วสั่ง Model {@link FoodManager#searchFood(String)} ทำการค้นหา นำผลลัพธ์ที่ได้มาแสดงผลบน RecyclerView</p>
 */
public class SearchResultActivity extends AppCompatActivity {

    /** คีย์สำหรับส่งรับคำค้นหาวัตถุดิบผ่าน Intent */
    public static final String EXTRA_KEYWORD = "extra_keyword";

    /** ตัวจัดรายการแสดงผลการ์ดอาหารบน RecyclerView */
    private SuggestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // 1. อ่านคีย์เวิร์ดคำค้นหาที่ส่งมาจาก Intent
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        if (keyword == null) keyword = "";

        // 2. เชื่อมต่อตัวแปร View และตั้งค่า RecyclerView
        TextView tvResultCount = findViewById(R.id.tvResultCount);
        RecyclerView rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SuggestionAdapter();
        rvResults.setAdapter(adapter);

        // 3. ปุ่มย้อนกลับไปยังหน้าก่อนหน้า
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 4. สั่ง Model (FoodManager) ค้นหาเมนูที่มีวัตถุดิบตรงกับคีย์เวิร์ด
        List<Food> found = FoodManager.getInstance().searchFood(keyword);
        tvResultCount.setText("พบเมนูที่ตรงกับ \"" + keyword + "\" " + found.size() + " รายการ");

        // 5. แปลงรายการ Food เป็น AiSuggestion เพื่อส่งให้ Adapter นำไปวาดรายการการ์ดบน View
        List<AiSuggestion> list = new ArrayList<>();
        for (Food f : found) {
            list.add(new AiSuggestion(f.getFoodName(), f.getTag() + " • " + f.getCalories() + " แคลอรี", f));
        }
        adapter.submit(list);

        // 6. ตั้งค่าการทำงานของแถบเมนูด้านล่าง (Bottom Navigation)
        BottomNavHelper.setup(this, R.id.navSearch);
    }
}
