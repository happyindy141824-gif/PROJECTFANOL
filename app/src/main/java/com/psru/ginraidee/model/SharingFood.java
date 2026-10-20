package com.psru.ginraidee.model;

/** เมนูสายปาร์ตี้-ครอบครัว กินร่วมกันหลายคน — Inheritance จาก {@link Food} */
public class SharingFood extends Food {

    private int servingSize;  // กินได้กี่คน
    private boolean isSpicy;  // เผ็ดไหม

    public SharingFood(String foodName, double price, int calories, String imageUrl,
                       String description, int servingSize, boolean isSpicy) {
        super(foodName, price, calories, imageUrl, description);
        this.servingSize = servingSize;
        this.isSpicy = isSpicy;
    }

    public int getServingSize() {
        return servingSize;
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public boolean isSpicy() {
        return isSpicy;
    }

    public void setSpicy(boolean spicy) {
        this.isSpicy = spicy;
    }

    // ราคาไม่เอามาแสดงในหน้าผลลัพธ์แล้ว แต่ field getPrice() ยังอยู่ครบ
    @Override
    public String getInfo() {
        return getFoodName() + "\n"
                + getDescription() + "\n"
                + "เหมาะกับ " + servingSize + " คน"
                + (isSpicy ? " • เผ็ด" : " • ไม่เผ็ด")
                + "\n" + getCalories() + " แคลอรี่ (ทั้งจาน)";
    }

    @Override
    public String getTag() {
        return isSpicy ? "แชร์กันได้ • เผ็ด" : "แชร์กันได้";
    }

    @Override
    public FoodCategory getCategory() {
        return FoodCategory.SHARING;
    }

    @Override
    public String getEmoji() {
        return "🍲";
    }
}
