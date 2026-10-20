package com.psru.ginraidee.model;

/** เมนูเน้นความเร็ว — Inheritance จาก {@link Food} */
public class FastFood extends Food {

    private int prepTimeMin;     // เวลาที่ใช้เตรียม/รอ (นาที)
    private boolean hasDelivery; // สั่งเดลิเวอรี่ได้ไหม

    public FastFood(String foodName, double price, int calories, String imageUrl,
                    String description, int prepTimeMin, boolean hasDelivery) {
        super(foodName, price, calories, imageUrl, description);
        this.prepTimeMin = prepTimeMin;
        this.hasDelivery = hasDelivery;
    }

    public int getPrepTimeMin() {
        return prepTimeMin;
    }

    public void setPrepTimeMin(int prepTimeMin) {
        this.prepTimeMin = prepTimeMin;
    }

    public boolean hasDelivery() {
        return hasDelivery;
    }

    public void setHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }

    // ราคา, เวลาเตรียม (prepTimeMin) และสถานะเดลิเวอรี่ (hasDelivery)
    // ไม่เอามาแสดงในหน้าผลลัพธ์แล้ว แต่ field ยังอยู่ครบเผื่อใช้ในอนาคต
    @Override
    public String getInfo() {
        return getFoodName() + "\n"
                + getDescription() + "\n"
                + getCalories() + " แคลอรี่";
    }

    @Override
    public String getTag() {
        return getCategory().getDisplayName();
    }

    @Override
    public FoodCategory getCategory() {
        return FoodCategory.FAST;
    }

    @Override
    public String getEmoji() {
        return "⚡";
    }
}
