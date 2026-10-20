package com.psru.ginraidee.model;

/**
 * หมวดความอยาก (Hunger Spectrum) — ตรงกับปุ่มในหน้า Choice Page
 * และตรงกับคลาสลูกของ {@link Food} หมวดละ 1 คลาส
 */
public enum FoodCategory {

    HEALTHY("เพื่อสุขภาพ", "🥗"),
    FAST("เน้นความเร็ว", "⚡"),
    SHARING("สายปาร์ตี้-ครอบครัว", "🍲"),
    DESSERT("ของหวาน-มื้อเบา ๆ", "🍰"),
    ALL("ทั้งหมด", "🎲");

    private final String displayName;
    private final String emoji;

    FoodCategory(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    /** แปลงชื่อ enum กลับเป็น object แบบปลอดภัย ถ้าไม่รู้จักให้ตอบ ALL */
    public static FoodCategory fromName(String name) {
        if (name != null) {
            for (FoodCategory category : values()) {
                if (category.name().equals(name)) {
                    return category;
                }
            }
        }
        return ALL;
    }
}
