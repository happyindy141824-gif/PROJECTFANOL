package com.psru.ginraidee.controller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.psru.ginraidee.R;
import com.psru.ginraidee.model.Food;
import com.psru.ginraidee.util.BottomNavHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * หน้าจอวางแผนเมนูอาหารรายสัปดาห์ (Weekly Meal Planner)
 *
 * <p>บทบาทตามสถาปัตยกรรม MVC: เป็น <b>Controller</b> จัดการตารางเมนูอาหาร 3 มื้อต่อวัน
 * (เช้า, กลางวัน, เย็น) คำนวณช่วงวันที่ของสัปดาห์ปัจจุบัน สัปดาห์ก่อนหน้า และสัปดาห์ถัดไป
 * พร้อมรองรับการเลือกเมนูอาหารจาก {@link MenuCatalogActivity}</p>
 */
public class WeeklyPlannerActivity extends AppCompatActivity {

    /** หัวข้อแสดงชื่อวันและวันที่เลือกปัจจุบัน */
    private TextView tvDayTitle;

    /** ข้อความแสดงชื่ออาหารมื้อเช้า */
    private TextView tvBreakfastName;

    /** ข้อความแสดงชื่ออาหารมื้อกลางวัน */
    private TextView tvLunchName;

    /** ข้อความแสดงชื่ออาหารมื้อเย็น */
    private TextView tvDinnerName;

    /** ข้อความแสดงช่วงวันที่ของสัปดาห์ เช่น "19 ต.ค. – 25 ต.ค. 2569" */
    private TextView tvWeekRange;

    /** ลำดับวันในสัปดาห์ที่กำลังเลือกอยู่ (0 = วันจันทร์ ... 6 = วันอาทิตย์) */
    private int currentDayIndex = 0;

    /** มื้ออาหารที่กำลังเลือกแก้ไข (0: มื้อเช้า, 1: มื้อกลางวัน, 2: มื้อเย็น) */
    private int editingMealSlot = 0;

    /** ชื่อวันภาษาไทยเรียงตามลำดับ 0 = จันทร์ ถึง 6 = อาทิตย์ */
    private static final String[] DAY_NAMES = {
            "วันจันทร์", "วันอังคาร", "วันพุธ", "วันพฤหัสบดี",
            "วันศุกร์", "วันเสาร์", "วันอาทิตย์"
    };

    /** ชื่อเดือนภาษาไทยแบบย่อ */
    private static final String[] THAI_MONTHS = {
            "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
            "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
    };

    /** รายการ Resource ID ของวงกลมวันที่บนแถบปฏิทิน 7 วัน */
    private int[] dayBadgeIds;

    /** วันจันทร์ของสัปดาห์ที่กำลังแสดงผลอยู่ (คำนวณจากวันที่จริงของเครื่อง) */
    private Calendar weekStart;

    /**
     * โครงสร้างข้อมูลเก็บตารางอาหารรายวัน โดยใช้วันที่ "yyyy-MM-dd" เป็น Key
     * จับคู่กับ Array ขนาด 3 ช่อง (Food[0]=เช้า, Food[1]=กลางวัน, Food[2]=เย็น)
     */
    private final Map<String, Food[]> weeklyPlan = new HashMap<>();

    /**
     * ตัวรอรับผลลัพธ์เมนูอาหารที่ถูกเลือกกลับมาจากหน้า {@link MenuCatalogActivity}
     */
    private final ActivityResultLauncher<Intent> catalogLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Food selectedFood = (Food) result.getData().getSerializableExtra(MenuCatalogActivity.EXTRA_SELECTED_FOOD);
                    if (selectedFood != null) {
                        // บันทึกเมนูอาหารลงในช่องมื้อที่กำลังแก้ไขอยู่
                        getMealsForSelectedDay()[editingMealSlot] = selectedFood;
                        // อัปเดตการแสดงผลบน View
                        updateMealDisplay();
                        String mealName = editingMealSlot == 0 ? "มื้อเช้า" : (editingMealSlot == 1 ? "มื้อกลางวัน" : "มื้อเย็น");
                        Toast.makeText(this, "อัปเดต " + mealName + " เรียบร้อย ❤️", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_planner);

        // 1. ปุ่มย้อนกลับ
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 2. เชื่อมต่อตัวแปร View กับ ID ใน XML Layout
        tvDayTitle = findViewById(R.id.tvDayTitle);
        tvBreakfastName = findViewById(R.id.tvBreakfastName);
        tvLunchName = findViewById(R.id.tvLunchName);
        tvDinnerName = findViewById(R.id.tvDinnerName);
        tvWeekRange = findViewById(R.id.tvWeekRange);

        // 3. กำหนด ID ของป้ายวงกลมวันที่ในสัปดาห์
        dayBadgeIds = new int[]{
                R.id.badgeMon, R.id.badgeTue, R.id.badgeWed,
                R.id.badgeThu, R.id.badgeFri, R.id.badgeSat, R.id.badgeSun
        };

        // 4. ผูก Event กดเลือกวันบนแถบปฏิทิน
        findViewById(R.id.dayMon).setOnClickListener(v -> selectDay(0));
        findViewById(R.id.dayTue).setOnClickListener(v -> selectDay(1));
        findViewById(R.id.dayWed).setOnClickListener(v -> selectDay(2));
        findViewById(R.id.dayThu).setOnClickListener(v -> selectDay(3));
        findViewById(R.id.dayFri).setOnClickListener(v -> selectDay(4));
        findViewById(R.id.daySat).setOnClickListener(v -> selectDay(5));
        findViewById(R.id.daySun).setOnClickListener(v -> selectDay(6));

        // 5. ปุ่มกดเพิ่มเมนูอาหาร
        MaterialButton btnAddMenu = findViewById(R.id.btnAddMenu);
        btnAddMenu.setOnClickListener(v -> openCatalogForSlot(1)); // ค่าเริ่มต้นเพิ่มมื้อกลางวัน

        // 6. ผูก Event เมื่อแตะช่องมื้ออาหารแต่ละมื้อ
        findViewById(R.id.mealBreakfast).setOnClickListener(v -> openCatalogForSlot(0));
        findViewById(R.id.mealLunch).setOnClickListener(v -> openCatalogForSlot(1));
        findViewById(R.id.mealDinner).setOnClickListener(v -> openCatalogForSlot(2));

        // 7. ปุ่มเลื่อนเปลี่ยนสัปดาห์
        findViewById(R.id.btnPrevWeek).setOnClickListener(v -> changeWeek(-1)); // สัปดาห์ก่อนหน้า
        findViewById(R.id.btnNextWeek).setOnClickListener(v -> changeWeek(1));  // สัปดาห์ถัดไป
        tvWeekRange.setOnClickListener(v -> goToToday());                         // แตะช่วงวันที่เพื่อกลับมาสัปดาห์ปัจจุบัน

        // 8. เริ่มต้นเปิดปฏิทินสัปดาห์ปัจจุบันและเลือกวันนี้ไว้
        goToToday();

        // 9. ตั้งค่าการทำงานของแถบเมนูด้านล่าง (Bottom Navigation)
        BottomNavHelper.setup(this, R.id.navWeekly);
    }

    /** เปิดสัปดาห์ปัจจุบัน และเลือกวันปัจจุบันเป็นค่าเริ่มต้น */
    private void goToToday() {
        Calendar today = Calendar.getInstance();
        weekStart = getMondayOf(today);
        renderWeek();
        selectDay(getDayIndex(today));
    }

    /**
     * เลื่อนสัปดาห์ไปข้างหน้า (+1) หรือย้อนหลัง (-1)
     *
     * @param offset จำนวนสัปดาห์ที่ต้องการเลื่อน
     */
    private void changeWeek(int offset) {
        weekStart.add(Calendar.WEEK_OF_YEAR, offset);
        renderWeek();
        selectDay(currentDayIndex);
    }

    /** คำนวณหาวันจันทร์ของสัปดาห์ที่วันที่นั้นสังกัดอยู่ */
    private static Calendar getMondayOf(Calendar date) {
        Calendar monday = (Calendar) date.clone();
        monday.set(Calendar.HOUR_OF_DAY, 0);
        monday.set(Calendar.MINUTE, 0);
        monday.set(Calendar.SECOND, 0);
        monday.set(Calendar.MILLISECOND, 0);
        monday.add(Calendar.DAY_OF_MONTH, -getDayIndex(date));
        return monday;
    }

    /** แปลงวันในสัปดาห์จากระบบ Android ให้เป็นดรรชนี (0 = จันทร์ ... 6 = อาทิตย์) */
    private static int getDayIndex(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }

    /** ดึงวัตถุ Calendar ตามลำดับวันในสัปดาห์ (0..6) */
    private Calendar getDate(int dayIndex) {
        Calendar date = (Calendar) weekStart.clone();
        date.add(Calendar.DAY_OF_MONTH, dayIndex);
        return date;
    }

    /** แปลงวันที่เป็นข้อความภาษาไทยย่อ เช่น "19 ต.ค." */
    private static String formatDate(Calendar date) {
        return date.get(Calendar.DAY_OF_MONTH) + " " + THAI_MONTHS[date.get(Calendar.MONTH)];
    }

    /** ตรวจสอบว่าวันที่สองค่าตรงกันในวันเดียวกันหรือไม่ */
    private static boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    /** เขียนตัวเลขวันที่ลงป้ายบนแถบปฏิทินทั้ง 7 วัน และอัปเดตช่วงวันที่สัปดาห์ (ปี พ.ศ.) */
    private void renderWeek() {
        for (int i = 0; i < dayBadgeIds.length; i++) {
            TextView badge = findViewById(dayBadgeIds[i]);
            badge.setText(String.valueOf(getDate(i).get(Calendar.DAY_OF_MONTH)));
        }
        Calendar sunday = getDate(6);
        int buddhistYear = sunday.get(Calendar.YEAR) + 543;
        tvWeekRange.setText(formatDate(weekStart) + " – " + formatDate(sunday) + " " + buddhistYear);
    }

    /** ดึงรายการเมนู 3 มื้อของวันที่เลือกอยู่ หากยังไม่มีข้อมูลจะสร้างขึ้นใหม่ */
    private Food[] getMealsForSelectedDay() {
        Calendar date = getDate(currentDayIndex);
        String key = String.format(Locale.US, "%04d-%02d-%02d",
                date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
        Food[] meals = weeklyPlan.get(key);
        if (meals == null) {
            meals = new Food[3];
            weeklyPlan.put(key, meals);
        }
        return meals;
    }

    /**
     * เลือกวันที่ต้องการดูข้อมูล พร้อมไฮไลต์สีส้มบนวงกลมปฏิทิน
     *
     * @param dayIndex ลำดับวัน (0 = จันทร์ ... 6 = อาทิตย์)
     */
    private void selectDay(int dayIndex) {
        currentDayIndex = dayIndex;
        Calendar selected = getDate(dayIndex);
        String title = DAY_NAMES[dayIndex] + " (" + formatDate(selected) + ")";
        if (isSameDay(selected, Calendar.getInstance())) {
            title += " • วันนี้";
        }
        tvDayTitle.setText(title);

        // อัปเดตสีไฮไลต์ของวงกลมวันที่บนแถบปฏิทิน
        for (int i = 0; i < dayBadgeIds.length; i++) {
            TextView badge = findViewById(dayBadgeIds[i]);
            if (badge != null) {
                if (i == dayIndex) {
                    badge.setBackgroundResource(R.drawable.bg_circle_button);
                    badge.setBackgroundTintList(getResources().getColorStateList(R.color.orange_primary, getTheme()));
                    badge.setTextColor(Color.WHITE);
                } else {
                    badge.setBackgroundResource(0);
                    badge.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
                }
            }
        }

        // อัปเดตการแสดงผลเมนูอาหาร 3 มื้อบน View
        updateMealDisplay();
    }

    /** อัปเดตชื่ออาหารและข้อความในแต่ละมื้อ (เช้า, กลางวัน, เย็น) บน View */
    private void updateMealDisplay() {
        Food[] meals = getMealsForSelectedDay();
        Food b = meals[0]; // มื้อเช้า
        Food l = meals[1]; // มื้อกลางวัน
        Food d = meals[2]; // มื้อเย็น

        if (b != null) {
            tvBreakfastName.setText(b.getEmoji() + "  " + b.getFoodName());
            tvBreakfastName.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        } else {
            tvBreakfastName.setText("➕ แตะเพื่อเลือกเมนูมื้อเช้า");
            tvBreakfastName.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        }

        if (l != null) {
            tvLunchName.setText(l.getEmoji() + "  " + l.getFoodName());
            tvLunchName.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        } else {
            tvLunchName.setText("➕ แตะเพื่อเลือกเมนูมื้อกลางวัน");
            tvLunchName.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        }

        if (d != null) {
            tvDinnerName.setText(d.getEmoji() + "  " + d.getFoodName());
            tvDinnerName.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
        } else {
            tvDinnerName.setText("➕ แตะเพื่อเลือกเมนูมื้อเย็น");
            tvDinnerName.setTextColor(getResources().getColor(R.color.text_secondary, getTheme()));
        }
    }

    /**
     * เปิดหน้าคลังเมนูอาหาร {@link MenuCatalogActivity} เพื่อเลือกอาหารใส่ลงมื้อที่ระบุ
     *
     * @param slot มื้อที่ต้องการแก้ไข (0: เช้า, 1: กลางวัน, 2: เย็น)
     */
    private void openCatalogForSlot(int slot) {
        editingMealSlot = slot;
        String mealName = slot == 0 ? "มื้อเช้า" : (slot == 1 ? "มื้อกลางวัน" : "มื้อเย็น");
        Toast.makeText(this, "เลือกเมนูสำหรับ " + mealName, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MenuCatalogActivity.class);
        intent.putExtra(MenuCatalogActivity.EXTRA_SELECT_MODE, true);
        catalogLauncher.launch(intent);
    }
}
