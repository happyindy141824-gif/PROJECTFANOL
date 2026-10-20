package com.psru.ginraidee.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.psru.ginraidee.R;
import com.psru.ginraidee.util.BottomNavHelper;

/**
 * หน้าจอค้นหาอาหารจากวัตถุดิบที่มีอยู่ในตู้เย็น
 *
 * <p>บทบาทตามสถาปัตยกรรม MVC: เป็น <b>Controller</b> รับข้อความค้นหาจากผู้ใช้
 * (ทั้งจากการพิมพ์ค้นหา หรือเลือกจากปุ่มชิปวัตถุดิบด่วน) แล้วสั่งเปิดหน้า {@link SearchResultActivity}
 * เพื่อแสดงผลการค้นหา</p>
 */
public class IngredientSearchActivity extends AppCompatActivity {

    /** ช่องสำหรับพิมพ์ข้อความค้นหาวัตถุดิบ */
    private EditText etSearchKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredient_search);

        // 1. เชื่อมต่อตัวแปร View กับ ID ใน XML
        etSearchKeyword = findViewById(R.id.etSearchKeyword);
        Button btnSearch = findViewById(R.id.btnSearch);

        // 2. ปุ่มย้อนกลับไปยังหน้าก่อนหน้า
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 3. ตั้งค่าปุ่มชิปทางลัดวัตถุดิบยอดนิยม (กดปุ๊บค้นหาปั๊บ)
        setupChip(R.id.chipEgg, "ไข่");
        setupChip(R.id.chipChicken, "ไก่");
        setupChip(R.id.chipPork, "หมู");
        setupChip(R.id.chipFish, "ปลา");
        setupChip(R.id.chipShrimp, "กุ้ง");
        setupChip(R.id.chipVeg, "ผัก");
        setupChip(R.id.chipMushroom, "เห็ด");
        setupChip(R.id.chipTofu, "เต้าหู้");
        setupChip(R.id.chipRice, "ข้าว");

        // 4. ผูก Event ปุ่มกดค้นหาจากข้อความที่พิมพ์ในช่อง
        btnSearch.setOnClickListener(v -> performSearch(etSearchKeyword.getText().toString()));

        // 5. ตั้งค่าการทำงานของแถบเมนูด้านล่าง (Bottom Navigation)
        BottomNavHelper.setup(this, R.id.navSearch);
    }

    /**
     * ตั้งค่าปุ่มชิปทางลัดวัตถุดิบ
     * เมื่อผู้ใช้กดปุ่ม ระบบจะเอาคำคีย์เวิร์ดไปใส่ในช่องพิมพ์ค้นหา และสั่งค้นหาทันที
     *
     * @param buttonId ID ของปุ่มชิปใน XML Layout
     * @param keyword  คำค้นหาวัตถุดิบ เช่น "ไข่", "ไก่"
     */
    private void setupChip(int buttonId, String keyword) {
        findViewById(buttonId).setOnClickListener(v -> {
            etSearchKeyword.setText(keyword);
            performSearch(keyword);
        });
    }

    /**
     * ส่งคำค้นหาวัตถุดิบไปยังหน้าแสดงผลลัพธ์ {@link SearchResultActivity}
     *
     * @param keyword ข้อความคีย์เวิร์ดที่ต้องการค้นหา
     */
    private void performSearch(String keyword) {
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra(SearchResultActivity.EXTRA_KEYWORD, keyword);
        startActivity(intent);
    }
}
