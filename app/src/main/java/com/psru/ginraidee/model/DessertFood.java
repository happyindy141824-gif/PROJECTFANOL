package com.psru.ginraidee.model;

/** ของหวาน / มื้อเบา ๆ — Inheritance จาก {@link Food} */
public class DessertFood extends Food {

    private int sweetLevel;  // ระดับความหวาน 1-5
    private boolean isCold;  // เป็นของเย็นไหม

    public DessertFood(String foodName, double price, int calories, String imageUrl,
                       String description, int sweetLevel, boolean isCold) {
        super(foodName, price, calories, imageUrl, description);
        this.sweetLevel = sweetLevel;
        this.isCold = isCold;
    }

    public int getSweetLevel() {
        return sweetLevel;
    }

    public void setSweetLevel(int sweetLevel) {
        this.sweetLevel = sweetLevel;
    }

    public boolean isCold() {
        return isCold;
    }

    public void setCold(boolean cold) {
        this.isCold = cold;
    }

    // ราคาไม่เอามาแสดงในหน้าผลลัพธ์แล้ว แต่ field getPrice() ยังอยู่ครบ
    @Override
    public String getInfo() {
        return getFoodName() + "\n"
                + getDescription() + "\n"
                + "ความหวาน " + sweetLevelBar()
                + (isCold ? " • เสิร์ฟเย็น" : " • เสิร์ฟอุ่น")
                + "\n" + getCalories() + " แคลอรี่";
    }

    @Override
    public String getTag() {
        return isCold ? "ของหวานเย็น" : "ของหวาน";
    }

    @Override
    public FoodCategory getCategory() {
        return FoodCategory.DESSERT;
    }

    @Override
    public String getEmoji() {
        return "🍰";
    }

    /** วาดแถบความหวานเป็นดาว เช่น sweetLevel = 3 -> "★★★☆☆" */
    private String sweetLevelBar() {
        StringBuilder bar = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            bar.append(i <= sweetLevel ? "★" : "☆");
        }
        return bar.toString();
    }
}
