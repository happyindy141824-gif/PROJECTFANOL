package com.psru.ginraidee.model;

import java.io.Serializable;

/**
 * ผลลัพธ์ 1 รายการที่ Gemini แนะนำกลับมา
 *
 * <p>ถ้าชื่อเมนูที่ AI ตอบมาตรงกับเมนูในคลัง offline ของเรา
 * {@link #getMatchedFood()} จะไม่เป็น null และเอาข้อมูลจริง (ราคา/แคลอรี่) มาแสดงได้</p>
 */
public class AiSuggestion implements Serializable {

    private final String foodName;
    private final String reason;
    private final Food matchedFood;

    // ข้อมูลที่ AI ประมาณมาให้ ใช้ตอนเป็นเมนูใหม่ที่ไม่มีในคลัง offline
    private final int calories;
    private final String ingredients;
    private final String steps;

    public AiSuggestion(String foodName, String reason, Food matchedFood) {
        this(foodName, reason, matchedFood, 0, "", "");
    }

    public AiSuggestion(String foodName, String reason, Food matchedFood,
                        int calories, String ingredients, String steps) {
        this.foodName = foodName;
        this.reason = reason;
        this.matchedFood = matchedFood;
        this.calories = calories;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getReason() {
        return reason;
    }

    public Food getMatchedFood() {
        return matchedFood;
    }

    public int getCalories() {
        return calories;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public boolean hasMatchedFood() {
        return matchedFood != null;
    }

    /** อีโมจิของเมนู ถ้าจับคู่กับคลัง offline ได้ก็ใช้ของจริง ถ้าไม่ได้ใช้ค่ากลาง */
    public String getEmoji() {
        return matchedFood != null ? matchedFood.getEmoji() : "🤖";
    }
}
