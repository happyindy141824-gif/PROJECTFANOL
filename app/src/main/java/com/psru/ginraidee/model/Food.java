package com.psru.ginraidee.model;

import java.io.Serializable;

/**
 * คลาสแม่ของอาหารทุกจานในแอป
 *
 * <p>หลัก OOP ที่คลาสนี้แสดง:</p>
 * <ul>
 *   <li><b>Abstraction</b> — เป็น abstract class สร้าง object ตรง ๆ ไม่ได้
 *       ต้องสร้างผ่านคลาสลูกเสมอ</li>
 *   <li><b>Encapsulation</b> — field ทุกตัวเป็น private เข้าถึงผ่าน getter/setter</li>
 * </ul>
 *
 * <p>ข้อสำคัญ: คลาสนี้ทำหน้าที่ <b>เก็บข้อมูลอาหาร 1 จาน</b> เท่านั้น
 * ตรรกะอย่างการสุ่ม/ค้นหา/กรอง เป็นหน้าที่ของ {@link FoodManager} ไม่ใช่ของอาหาร 1 จาน</p>
 */
public abstract class Food implements Serializable {

    private String foodName;
    private double price;
    private int calories;
    private String imageUrl;
    private String description;
    private boolean favorite;
    private String ingredients;
    private String recipe;

    protected Food(String foodName, double price, int calories, String imageUrl, String description) {
        this.foodName = foodName;
        this.price = price;
        this.calories = calories;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // ---------- Encapsulation: getter / setter ----------

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getIngredients() {
        return ingredients != null && !ingredients.trim().isEmpty() ? ingredients : "ไม่ระบุวัตถุดิบ";
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getRecipe() {
        return recipe != null && !recipe.trim().isEmpty() ? recipe : "ไม่ระบุวิธีทำ";
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public Food withDetails(String ingredients, String recipe) {
        this.ingredients = ingredients;
        this.recipe = recipe;
        return this;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ---------- Abstraction + Polymorphism: คลาสลูกต้อง override ----------

    /** ข้อความบรรยายรายละเอียดของเมนู แต่ละหมวดเขียนไม่เหมือนกัน */
    public abstract String getInfo();

    /** ป้ายกำกับสั้น ๆ ของเมนู เช่น "สุขภาพดี" "ทำเร็ว" */
    public abstract String getTag();

    /**
     * หมวดของเมนู ใช้ตอน {@link FoodManager#filterByType(FoodCategory)}
     * คลาสลูกแต่ละตัวตอบคนละค่า
     */
    public abstract FoodCategory getCategory();

    /**
     * อีโมจิที่ใช้แทนรูปภาพ (แอปทำงาน offline จึงยังไม่โหลดรูปจาก {@link #getImageUrl()})
     * คลาสลูก override เพื่อให้แต่ละหมวดหน้าตาต่างกัน
     */
    public String getEmoji() {
        return "🍽";  // 🍽
    }

    @Override
    public String toString() {
        return getFoodName() + " (" + getTag() + ")";
    }
}
