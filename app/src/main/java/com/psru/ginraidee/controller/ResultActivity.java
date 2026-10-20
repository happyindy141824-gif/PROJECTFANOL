package com.psru.ginraidee.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.psru.ginraidee.R;
import com.psru.ginraidee.model.Food;
import com.psru.ginraidee.model.FoodCategory;
import com.psru.ginraidee.model.FoodFilter;
import com.psru.ginraidee.model.FoodManager;

/**
 * หน้าแสดงผลลัพธ์เมนูอาหาร (Story Board หน้าที่ 4)
 *
 * <p>บทบาทในตามสถาปัตยกรรม MVC:</p>
 * <ul>
 *   <li>เป็น <b>Controller</b> — รับวัตถุ {@link Food} จาก Intent แล้วสั่ง UI แสดงผลข้อมูล</li>
 *   <li>จุดที่เห็น <b>Polymorphism</b> ชัดที่สุดอยู่ในเมธอด {@link #showFood(Food)} —
 *       เราเรียก {@code food.getInfo()} เหมือนกันทุกครั้ง
 *       แต่ข้อความที่ออกมาต่างกันตามว่า object จริง ๆ เป็นคลาสลูกตัวไหน
 *       โดยที่ Activity นี้ไม่ต้องรู้เลยว่าเป็นคลาสอะไร</li>
 * </ul>
 */
public class ResultActivity extends AppCompatActivity {

    /** คีย์สำหรับส่งรับวัตถุ Food ผ่าน Intent */
    public static final String EXTRA_FOOD = "extra_food";

    /** คีย์สำหรับส่งรับหมวดหมู่อาหาร (FoodCategory) ผ่าน Intent */
    public static final String EXTRA_CATEGORY = "extra_category";

    /** คีย์สำหรับส่งรับเงื่อนไขการสุ่ม (FoodFilter) ผ่าน Intent */
    public static final String EXTRA_FILTER = "extra_filter";

    /** อินสแตนซ์ Singleton ของ Model สำหรับจัดการคลังเมนูอาหาร */
    private final FoodManager foodManager = FoodManager.getInstance();

    // ------------------------------------------------------------------
    // คอมโพเนนต์การแสดงผลบน View (Layout Widgets)
    // ------------------------------------------------------------------

    /** แสดงรูปอีโมจิประจำประเภทอาหาร */
    private TextView emojiTextView;

    /** แสดงชื่อเมนูอาหาร */
    private TextView foodNameTextView;

    /** แสดงป้ายกำกับสั้นๆ เช่น "สุขภาพดี • มังสวิรัติ" */
    private TextView tagTextView;

    /** แสดงรายละเอียดคุณลักษณะของอาหาร */
    private TextView infoTextView;

    /** ปุ่มกดเพิ่ม/ยกเลิกรายการโปรด (รูปหัวใจ) */
    private MaterialButton favoriteButton;

    /** แสดงรายการวัตถุดิบหลัก */
    private TextView ingredientsTextView;

    /** แสดงขั้นตอนวิธีทำแบบย่อ */
    private TextView recipeTextView;

    // ------------------------------------------------------------------
    // สถานะและข้อมูลที่ใช้อยู่ในหน้านี้ (State)
    // ------------------------------------------------------------------

    /** หมวดหมู่อาหารที่เลือกสุ่มมาจากหน้าก่อนหน้า */
    private FoodCategory category;

    /** เงื่อนไขการกรอง (งบประมาณ, แคลอรี่, มังสวิรัติ) */
    private FoodFilter filter;

    /** วัตถุเมนูอาหารที่กำลังแสดงผลอยู่ในปัจจุบัน */
    private Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // 1. เชื่อมต่อตัวแปร View กับ ID ในไฟล์ XML (findViewById)
        emojiTextView = findViewById(R.id.tvEmoji);
        foodNameTextView = findViewById(R.id.tvFoodName);
        tagTextView = findViewById(R.id.tvTag);
        infoTextView = findViewById(R.id.tvInfo);
        favoriteButton = findViewById(R.id.btnFavorite);
        ingredientsTextView = findViewById(R.id.tvIngredients);
        recipeTextView = findViewById(R.id.tvRecipe);

        // 2. อ่านค่าหมวดหมู่และตัวกรองที่ส่งมาจาก Intent
        category = FoodCategory.fromName(getIntent().getStringExtra(EXTRA_CATEGORY));
        filter = readFilter();

        // 3. อ่านวัตถุ Food ที่สุ่มได้มาจากหน้าก่อนหน้า
        Food food = readFood();
        if (food == null) {
            // กันเหนียว: กรณีเปิดหน้านี้โดยไม่มีข้อมูลติดมา ให้สั่ง Model สุ่มใหม่ทันที
            food = foodManager.randomFood(category, filter);
        }

        // 4. นำข้อมูลอาหารไปแสดงผลบนหน้าจอ
        showFood(food);

        // 5. ผูก Event Listener เมื่อผู้ใช้กดปุ่มเพิ่ม/ลบเมนูโปรด
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // 6. ผูก Event Listener เมื่อผู้ใช้กดปุ่ม "หมุนใหม่"
        Button spinAgainButton = findViewById(R.id.btnSpinAgain);
        spinAgainButton.setOnClickListener(v -> spinAgain());

        // 7. ผูก Event Listener ปุ่ม "ค้นหาร้านใน Map" — เปิดค้นหาร้านอาหารใน Google Maps
        findViewById(R.id.btnViewLocation).setOnClickListener(v -> openMapsLocation());

        // 8. ผูก Event Listener ปุ่ม "กลับหน้าแรก"
        findViewById(R.id.btnHome).setOnClickListener(v -> goHome());
    }

    /**
     * นำวัตถุ Food มาแสดงผลบนองค์ประกอบต่าง ๆ ของหน้าจอ View
     * <p>อาศัยหลัก <b>Polymorphism</b> ในการเรียก {@code food.getInfo()}, {@code food.getTag()} และ {@code food.getEmoji()}</p>
     *
     * @param food วัตถุเมนูอาหารที่ต้องการนำมาแสดงผล
     */
    private void showFood(Food food) {
        this.currentFood = food;
        if (food == null) {
            // กรณีสุ่มไม่พบเมนูที่ตรงตามเงื่อนไข ให้แสดงข้อความแจ้งเตือน
            emojiTextView.setText("😅");
            foodNameTextView.setText(R.string.result_empty_title);
            tagTextView.setText("");
            infoTextView.setText(R.string.random_no_match);
            ingredientsTextView.setText("ไม่ระบุวัตถุดิบ");
            recipeTextView.setText("ไม่ระบุวิธีทำ");
            favoriteButton.setText("🤍");
            return;
        }

        // ดึงข้อมูลผ่านเมธอดของ Food (Polymorphism ทำงานอัตโนมัติตามคลาสลูก)
        emojiTextView.setText(food.getEmoji());
        foodNameTextView.setText(food.getFoodName());
        tagTextView.setText(food.getTag());
        infoTextView.setText(food.getInfo());
        ingredientsTextView.setText(food.getIngredients());
        recipeTextView.setText(food.getRecipe());

        // อัปเดตไอคอนหัวใจตามสถานะรายการโปรด
        updateFavoriteButton(food);
    }

    /**
     * สลับสถานะรายการโปรด (กดสลับ ชอบ <-> ไม่ชอบ)
     * สั่งงานไปยัง Model (FoodManager) แล้วอัปเดตไอคอนหัวใจบน View
     */
    private void toggleFavorite() {
        if (currentFood == null) {
            return;
        }
        boolean isFav = foodManager.toggleFavorite(currentFood);
        updateFavoriteButton(currentFood);
        String message = isFav ? "บันทึกเข้าเมนูโปรดแล้ว ❤️" : "ยกเลิกเมนูโปรดแล้ว 🤍";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * อัปเดตสัญลักษณ์ปุ่มหัวใจตามสถานะโปรดของอาหาร
     *
     * @param food วัตถุอาหารที่ต้องการตรวจสอบสถานะโปรด
     */
    private void updateFavoriteButton(Food food) {
        if (food != null && food.isFavorite()) {
            favoriteButton.setText("❤️");
        } else {
            favoriteButton.setText("🤍");
        }
    }

    /** ปุ่ม "หมุนใหม่" — สั่ง Model สุ่มใหม่ในหมวดและเงื่อนไขเดิม */
    private void spinAgain() {
        Food next = foodManager.randomFood(category, filter);
        if (next == null) {
            Toast.makeText(this, R.string.random_no_match, Toast.LENGTH_LONG).show();
            return;
        }
        showFood(next);
    }

    /**
     * ปุ่ม "ค้นหาร้านใน Map" — เปิดแอปพลิเคชัน Google Maps เพื่อค้นหาร้านอาหารใกล้เคียง
     * หากเครื่องไม่มีแอป Google Maps จะสลับไปเปิดบนเว็บเบราว์เซอร์แทน
     */
    private void openMapsLocation() {
        if (currentFood == null) {
            return;
        }
        String query = "ร้าน " + currentFood.getFoodName() + " พิษณุโลก";
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // แผนสำรอง: เปิดค้นหาผ่านเว็บ Google Maps หากไม่มีแอป
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query)));
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "เปิดแผนที่ไม่ได้: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /** กลับไปหน้าแรก โดยเคลียร์ Activity ที่ค้างอยู่ใน Stack ทิ้งทั้งหมด */
    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /** อ่านวัตถุ Food จาก Intent Extra ที่ส่งมาจากหน้าก่อนหน้า */
    @SuppressWarnings("deprecation")
    private Food readFood() {
        Object extra = getIntent().getSerializableExtra(EXTRA_FOOD);
        return extra instanceof Food ? (Food) extra : null;
    }

    /** อ่านวัตถุ FoodFilter จาก Intent Extra ที่ส่งมาจากหน้าก่อนหน้า */
    @SuppressWarnings("deprecation")
    private FoodFilter readFilter() {
        Object extra = getIntent().getSerializableExtra(EXTRA_FILTER);
        return extra instanceof FoodFilter ? (FoodFilter) extra : FoodFilter.NONE;
    }
}
