package com.psru.ginraidee.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * ตัวจัดการคลังเมนูอาหารทั้งหมด — เป็น "สมอง" หลักของฝั่ง Model
 *
 * <p><b>ออกแบบตามหลักการ OOP:</b></p>
 * <ul>
 *   <li><b>Single Responsibility Principle (SRP):</b> แยกงานสุ่ม/ค้นหา/กรอง ออกมาจาก {@link Food}
 *       เนื่องจากเป็นหน้าที่ของคลังเมนู ไม่ใช่งานของอาหารจานเดียว</li>
 *   <li><b>Singleton Pattern:</b> มีอินสแตนซ์เดียวทั้งแอป เพื่อให้ทุก Activity ทำงานกับข้อมูลชุดเดียวกัน</li>
 * </ul>
 */
public class FoodManager {

    /** อินสแตนซ์เดียวของ FoodManager (Singleton Pattern) */
    private static FoodManager instance;

    /** รายการคลังเมนูอาหารทั้งหมด */
    private final List<Food> foodList;

    /** ตัวสุ่มตัวเลขแบบสุ่ม */
    private final Random random;

    /** เมนูที่สุ่มได้ล่าสุด ใช้ป้องกันไม่ให้กด "หมุนใหม่" แล้วสุ่มได้จานเดิมซ้ำติดกัน */
    private Food lastPicked;

    /**
     * คอนสตรัคเตอร์แบบ Private ป้องกันไม่ให้คลาสภายนอกสร้างวัตถุผ่าน new
     * สั่งโหลดเมนูเริ่มต้นจาก {@link FoodRepository#createDefaultFoods()}
     */
    private FoodManager() {
        this.foodList = new ArrayList<>(FoodRepository.createDefaultFoods());
        this.random = new Random();
    }

    /** คอนสตรัคเตอร์สำหรับ Unit Test ให้สามารถส่งรายการอาหารทดสอบเข้ามาได้ */
    FoodManager(List<Food> foods, Random random) {
        this.foodList = new ArrayList<>(foods);
        this.random = random;
    }

    /**
     * ดึงอินสแตนซ์ Singleton ของ FoodManager (สร้างครั้งเดียวแบบ Thread-safe)
     *
     * @return อินสแตนซ์ของ FoodManager
     */
    public static synchronized FoodManager getInstance() {
        if (instance == null) {
            instance = new FoodManager();
        }
        return instance;
    }

    // ------------------------------------------------------------------
    // เมธอดอ่านข้อมูลคลังเมนู
    // ------------------------------------------------------------------

    /**
     * ดึงสำเนาของรายการอาหารทั้งหมด (คืนค่า Copy ป้องกันคลาสภายนอกดัดแปลง List หลัก)
     *
     * @return List ของวัตถุ Food ทั้งหมด
     */
    public List<Food> getFoodList() {
        return new ArrayList<>(foodList);
    }

    /**
     * ดึงจำนวนเมนูอาหารทั้งหมดในคลัง
     *
     * @return จำนวนจานอาหารที่มี
     */
    public int size() {
        return foodList.size();
    }

    // ------------------------------------------------------------------
    // เมธอดจัดการรายการโปรด (Favorites)
    // ------------------------------------------------------------------

    /**
     * สลับสถานะรายการโปรด (กดสลับ ชอบ <-> ไม่ชอบ)
     *
     * @param food วัตถุอาหารที่ต้องการสลับสถานะ
     * @return สถานะโปรดใหม่หลังสลับ (true: เป็นรายการโปรด, false: ไม่ใช่รายการโปรด)
     */
    public boolean toggleFavorite(Food food) {
        if (food == null) {
            return false;
        }
        boolean newState = !food.isFavorite();
        food.setFavorite(newState);
        for (Food item : foodList) {
            if (item.getFoodName().equalsIgnoreCase(food.getFoodName())) {
                item.setFavorite(newState);
                break;
            }
        }
        return newState;
    }

    /**
     * ดึงรายการอาหารโปรดทั้งหมดที่ผู้ใช้กดหัวใจบันทึกไว้
     *
     * @return List ของวัตถุ Food ที่เป็นรายการโปรด
     */
    public List<Food> getFavoriteFoods() {
        List<Food> favorites = new ArrayList<>();
        for (Food food : foodList) {
            if (food.isFavorite()) {
                favorites.add(food);
            }
        }
        return favorites;
    }

    // ------------------------------------------------------------------
    // เมธอดสุ่มเมนูอาหาร (Random Logic)
    // ------------------------------------------------------------------

    /** สุ่มจากเมนูอาหารทั้งหมดโดยไม่มีเงื่อนไขกรอง */
    public Food randomFood() {
        return randomFood(FoodCategory.ALL, FoodFilter.NONE);
    }

    /** สุ่มเฉพาะเมนูในหมวดหมู่อาหารที่เลือก */
    public Food randomFood(FoodCategory category) {
        return randomFood(category, FoodFilter.NONE);
    }

    /**
     * สุ่มเมนูอาหารในหมวดที่เลือก + ผ่านเงื่อนไขตัวกรอง (งบประมาณ, แคลอรี่, มังสวิรัติ)
     *
     * @param category หมวดหมู่อาหารที่ต้องการสุ่ม
     * @param filter   เงื่อนไขการกรอง
     * @return วัตถุ Food ที่สุ่มได้ หรือ null หากไม่มีเมนูที่ผ่านเงื่อนไข
     */
    public Food randomFood(FoodCategory category, FoodFilter filter) {
        List<Food> candidates = findCandidates(category, filter);
        if (candidates.isEmpty()) {
            return null;
        }

        // ป้องกันสุ่มได้จานเดิมซ้ำติดกัน หากมีตัวเลือกมากกว่า 1 จาน
        if (candidates.size() > 1 && lastPicked != null) {
            candidates.remove(lastPicked);
        }

        Food picked = candidates.get(random.nextInt(candidates.size()));
        lastPicked = picked;
        return picked;
    }

    // ------------------------------------------------------------------
    // เมธอดค้นหาและกรองข้อมูล (Search & Filter Logic)
    // ------------------------------------------------------------------

    /**
     * ค้นหารายชื่อเมนูอาหารทั้งหมดที่เข้าเงื่อนไขหมวดหมู่และตัวกรอง
     *
     * @param category หมวดหมู่อาหาร
     * @param filter   เงื่อนไขตัวกรอง
     * @return List ของวัตถุ Food ที่ผ่านเงื่อนไข
     */
    public List<Food> findCandidates(FoodCategory category, FoodFilter filter) {
        List<Food> candidates = filterByType(category);
        if (filter == null || filter.isEmpty()) {
            return candidates;
        }
        List<Food> passed = new ArrayList<>();
        for (Food food : candidates) {
            if (filter.matches(food)) {
                passed.add(food);
            }
        }
        return passed;
    }

    /**
     * กรองรายการอาหารตามหมวดหมู่ (อาศัยหลัก <b>Polymorphism</b> ในการเรียก food.getCategory())
     *
     * @param category หมวดหมู่อาหารที่ต้องการกรอง
     * @return List ของวัตถุ Food ที่อยู่ในหมวดหมู่นั้น
     */
    public List<Food> filterByType(FoodCategory category) {
        if (category == null || category == FoodCategory.ALL) {
            return getFoodList();
        }
        List<Food> result = new ArrayList<>();
        for (Food food : foodList) {
            if (food.getCategory() == category) {
                result.add(food);
            }
        }
        return result;
    }

    /** กรองรายการอาหารตามเงื่อนไขตัวกรอง (ราคา, แคลอรี่, มังสวิรัติ) */
    public List<Food> filterBy(FoodFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return getFoodList();
        }
        List<Food> result = new ArrayList<>();
        for (Food food : foodList) {
            if (filter.matches(food)) {
                result.add(food);
            }
        }
        return result;
    }

    /**
     * ค้นหาอาหารจากวัตถุดิบหรือชื่อเมนู (ค้นหาคำที่เกี่ยวข้องกันโดยไม่สนตัวพิมพ์เล็ก/ใหญ่)
     *
     * @param keyword ข้อความคีย์เวิร์ดที่ต้องการค้นหา
     * @return List ของวัตถุ Food ที่ตรงกับคำค้นหา
     */
    public List<Food> searchFood(String keyword) {
        List<Food> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return result;
        }
        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        for (Food food : foodList) {
            String name = food.getFoodName().toLowerCase(Locale.ROOT);
            String desc = food.getDescription() == null
                    ? "" : food.getDescription().toLowerCase(Locale.ROOT);
            String ing = food.getIngredients() == null
                    ? "" : food.getIngredients().toLowerCase(Locale.ROOT);
            if (name.contains(needle) || desc.contains(needle) || ing.contains(needle)) {
                result.add(food);
            }
        }
        return result;
    }

    /**
     * ค้นหาเมนูอาหารที่ชื่อตรงหรือใกล้เคียงที่สุด 1 จาน (ใช้จับคู่คำตอบจาก AI กับคลังในเครื่อง)
     *
     * @param name ชื่ออาหารที่ต้องการค้นหา
     * @return วัตถุ Food ที่เจอ หรือ null หากไม่พบ
     */
    public Food findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String needle = name.trim().toLowerCase(Locale.ROOT);

        // รอบที่ 1: ค้นหาชื่อที่ตรงเป๊ะ 100%
        for (Food food : foodList) {
            if (food.getFoodName().toLowerCase(Locale.ROOT).equals(needle)) {
                return food;
            }
        }

        // รอบที่ 2: ค้นหาชื่อที่มีคำนั้นซ่อนอยู่
        for (Food food : foodList) {
            if (food.getFoodName().toLowerCase(Locale.ROOT).contains(needle)) {
                return food;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // เมธอด เพิ่ม / แก้ไข / ลบ ข้อมูลอาหาร
    // ------------------------------------------------------------------

    /** เพิ่มเมนูอาหารใหม่เข้าคลัง */
    public void addFood(Food food) {
        if (food != null) {
            foodList.add(food);
        }
    }

    /**
     * แก้ไขข้อมูลอาหาร โดยใช้ชื่ออาหาร (foodName) เป็นตัวอ้างอิง
     *
     * @param food วัตถุอาหารที่มีข้อมูลอัปเดตใหม่
     * @return true หากแก้ไขสำเร็จ, false หากไม่พบเมนูดังกล่าว
     */
    public boolean editFood(Food food) {
        if (food == null) {
            return false;
        }
        for (int i = 0; i < foodList.size(); i++) {
            if (foodList.get(i).getFoodName().equals(food.getFoodName())) {
                foodList.set(i, food);
                return true;
            }
        }
        return false;
    }

    /**
     * ลบเมนูอาหารออกจากคลัง
     *
     * @param food วัตถุอาหารที่ต้องการลบ
     * @return true หากลบสำเร็จ
     */
    public boolean deleteFood(Food food) {
        if (food == null) {
            return false;
        }
        if (food.equals(lastPicked)) {
            lastPicked = null;
        }
        return foodList.remove(food);
    }
}
