package com.psru.ginraidee.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.psru.ginraidee.R;
import com.psru.ginraidee.model.FoodCategory;

/**
 * หน้าเลือกหมวดหมู่อาหารที่อยากรับประทาน (Story Board หน้าที่ 2)
 *
 * <p>บทบาทตามสถาปัตยกรรม MVC: เป็น <b>Controller</b> ที่รับค่าการเลือกหมวดจากผู้ใช้
 * โดยปุ่มแต่ละปุ่มจะผูกกับค่าใน {@link FoodCategory} หมวดละ 1 ค่า แล้วส่งต่อไปยังหน้าสุ่ม {@link RandomActivity}</p>
 */
public class ChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);

        // 1. ผูกปุ่มเลือกหมวดหมู่อาหารแต่ละประเภท
        bindCategory(R.id.btnHealthy, FoodCategory.HEALTHY); // หมวดเพื่อสุขภาพ
        bindCategory(R.id.btnFast, FoodCategory.FAST);       // หมวดจานด่วนเน้นความเร็ว
        bindCategory(R.id.btnSharing, FoodCategory.SHARING); // หมวดสังสรรค์/ครอบครัว
        bindCategory(R.id.btnDessert, FoodCategory.DESSERT); // หมวดของหวาน/มื้อเบา
        bindCategory(R.id.btnAll, FoodCategory.ALL);         // หมวดทั้งหมด (สุ่มทุกประเภท)

        // 2. ปุ่มย้อนกลับไปยังหน้าแรก
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    /**
     * ผูกปุ่ม 1 ปุ่มเข้ากับหมวดหมู่อาหาร 1 หมวด
     * สั่งกำหนดรูปสัญลักษณ์ Emoji และข้อความปุ่ม พร้อมตั้งค่า Intent ส่งชื่อหมวดไปยังหน้าสุ่ม {@link RandomActivity}
     *
     * @param buttonId ID ของปุ่มใน XML Layout
     * @param category วัตถุ Enum ของหมวดหมู่อาหารที่ต้องการผูก
     */
    private void bindCategory(int buttonId, FoodCategory category) {
        Button button = findViewById(buttonId);
        // กำหนดข้อความปุ่มในรูปแบบ: "[Emoji] [ชื่อหมวด]"
        button.setText(getString(R.string.choice_button_format,
                category.getEmoji(), category.getDisplayName()));

        // เมื่อกดปุ่ม ส่งชื่อหมวดไปเปิดหน้า RandomActivity
        button.setOnClickListener((View v) -> {
            Intent intent = new Intent(this, RandomActivity.class);
            intent.putExtra(RandomActivity.EXTRA_CATEGORY, category.name());
            startActivity(intent);
        });
    }
}
