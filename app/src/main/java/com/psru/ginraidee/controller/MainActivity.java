package com.psru.ginraidee.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.psru.ginraidee.R;
import com.psru.ginraidee.model.FoodManager;
import com.psru.ginraidee.util.BottomNavHelper;

/**
 * หน้าแรกของแอปพลิเคชัน (Story Board หน้าที่ 1)
 *
 * <p>บทบาทตามสถาปัตยกรรม MVC: เป็น <b>Controller</b> ทำหน้าที่รับ Event การกดปุ่มจากผู้ใช้บน View (XML)
 * เพื่อสั่งเปิดไปยังหน้าฟังก์ชันต่างๆ ไม่มีตรรกะประมวลผลซับซ้อนอยู่ในนี้</p>
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. เชื่อมต่อตัวแปร View กับ ID ในไฟล์ XML
        TextView tvMenuCount = findViewById(R.id.tvMenuCount);
        MaterialButton btnStart = findViewById(R.id.btnStart);

        // 2. ดึงจำนวนเมนูอาหารทั้งหมดจาก Model (FoodManager) มาแสดงบนป้ายสถิติ
        int menuCount = FoodManager.getInstance().size();
        tvMenuCount.setText(getString(R.string.main_menu_count, menuCount));

        // 3. ปุ่มกดเริ่มต้นสุ่มอาหาร — เปิดไปยังหน้าเลือกหมวดหมู่อาหาร (ChoiceActivity)
        View.OnClickListener startRandom = v -> startActivity(new Intent(this, ChoiceActivity.class));
        btnStart.setOnClickListener(startRandom);
        findViewById(R.id.bannerRandom).setOnClickListener(startRandom);

        // 4. ปุ่มเปิดคลังเมนูอาหารทั้งหมด (MenuCatalogActivity)
        findViewById(R.id.btnCatalog).setOnClickListener(v ->
                startActivity(new Intent(this, MenuCatalogActivity.class)));

        // 5. การ์ดทางลัด 4 ฟีเจอร์หลัก (Grid Cards)
        // - การ์ดฟีเจอร์ AI แนะนำเมนู (AiActivity)
        findViewById(R.id.cardAi).setOnClickListener(v ->
                startActivity(new Intent(this, AiActivity.class)));

        // - การ์ดฟีเจอร์เมนูโปรด (FavoriteActivity)
        findViewById(R.id.cardFavorite).setOnClickListener(v ->
                startActivity(new Intent(this, FavoriteActivity.class)));

        // - การ์ดฟีเจอร์ค้นหาจากวัตถุดิบ (IngredientSearchActivity)
        findViewById(R.id.cardSearch).setOnClickListener(v ->
                startActivity(new Intent(this, IngredientSearchActivity.class)));

        // - การ์ดฟีเจอร์วางแผนเมนูรายสัปดาห์ (WeeklyPlannerActivity)
        findViewById(R.id.cardWeekly).setOnClickListener(v ->
                startActivity(new Intent(this, WeeklyPlannerActivity.class)));

        // 6. ตั้งค่าการทำงานของแถบเมนูด้านล่าง (Bottom Navigation)
        BottomNavHelper.setup(this, R.id.navHome);
    }
}
