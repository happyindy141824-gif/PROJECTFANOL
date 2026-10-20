package com.psru.ginraidee.model;

import java.io.Serializable;

/**
 * เงื่อนไขการกรองเมนู (ฟีเจอร์ "Filter ตามเงื่อนไข")
 * ค่า 0 หมายถึง "ไม่จำกัด"
 */
public class FoodFilter implements Serializable {

    public static final FoodFilter NONE = new FoodFilter(0, 0, false);

    private double maxPrice;
    private int maxCalories;
    private boolean vegetarianOnly;

    public FoodFilter() {
        this(0, 0, false);
    }

    public FoodFilter(double maxPrice, int maxCalories, boolean vegetarianOnly) {
        this.maxPrice = maxPrice;
        this.maxCalories = maxCalories;
        this.vegetarianOnly = vegetarianOnly;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getMaxCalories() {
        return maxCalories;
    }

    public void setMaxCalories(int maxCalories) {
        this.maxCalories = maxCalories;
    }

    public boolean isVegetarianOnly() {
        return vegetarianOnly;
    }

    public void setVegetarianOnly(boolean vegetarianOnly) {
        this.vegetarianOnly = vegetarianOnly;
    }

    /** ไม่ได้ตั้งเงื่อนไขอะไรเลยหรือเปล่า */
    public boolean isEmpty() {
        return maxPrice <= 0 && maxCalories <= 0 && !vegetarianOnly;
    }

    /**
     * เมนูจานนี้ผ่านเงื่อนไขไหม
     * สังเกตว่าเมธอดนี้เรียก {@code food.getPrice()} แบบ polymorphic
     * ใช้ได้กับคลาสลูกทุกตัวโดยไม่ต้องรู้ว่าเป็นคลาสอะไร
     */
    public boolean matches(Food food) {
        if (food == null) {
            return false;
        }
        if (maxPrice > 0 && food.getPrice() > maxPrice) {
            return false;
        }
        if (maxCalories > 0 && food.getCalories() > maxCalories) {
            return false;
        }
        if (vegetarianOnly) {
            return food instanceof HealthyFood && ((HealthyFood) food).isVegetarian();
        }
        return true;
    }
}
