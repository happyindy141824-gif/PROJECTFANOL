package com.psru.ginraidee.model;

/**
 * เมนูเพื่อสุขภาพ — Inheritance: สืบทอดจาก {@link Food}
 * และ Polymorphism: override getInfo() / getTag() / getCategory() / getEmoji()
 */
public class HealthyFood extends Food {

    private int nutriScore;       // คะแนนโภชนาการ 1-100 ยิ่งมากยิ่งดี
    private boolean isVegetarian; // เป็นเมนูมังสวิรัติหรือไม่

    public HealthyFood(String foodName, double price, int calories, String imageUrl,
                       String description, int nutriScore, boolean isVegetarian) {
        super(foodName, price, calories, imageUrl, description);
        this.nutriScore = nutriScore;
        this.isVegetarian = isVegetarian;
    }

    public int getNutriScore() {
        return nutriScore;
    }

    public void setNutriScore(int nutriScore) {
        this.nutriScore = nutriScore;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.isVegetarian = vegetarian;
    }

    @Override
    public String getInfo() {
        return getFoodName() + "\n"
                + getDescription() + "\n"
                + "คะแนนโภชนาการ " + nutriScore + "/100"
                + (isVegetarian ? " • เมนูมังสวิรัติ" : "")
                + "\n" + calorieText();
    }

    @Override
    public String getTag() {
        return isVegetarian ? "สุขภาพดี • มังสวิรัติ" : "สุขภาพดี";
    }

    @Override
    public FoodCategory getCategory() {
        return FoodCategory.HEALTHY;
    }

    @Override
    public String getEmoji() {
        return "🥗";
    }

    // ราคาไม่เอามาแสดงในหน้าผลลัพธ์แล้ว แต่ field getPrice() ยังอยู่ครบ
    // เผื่อใช้กับ FoodFilter หรือฟีเจอร์อื่นในอนาคต
    private String calorieText() {
        return getCalories() + " แคลอรี่";
    }
}
