# 🍜 Gin Rai Dee? — มื้อนี้กินอะไรดี?

แอปพลิเคชัน Android ช่วยตัดสินใจเลือกเมนูอาหาร เขียนด้วยภาษา Java
ออกแบบตามหลัก **OOP + MVC**

> วิชา: การเขียนโปรแกรมคอมพิวเตอร์ขั้นสูง — อาจารย์วนารัตน์ จุฬพันธ์ทอง
> สาขาวิศวกรรมคอมพิวเตอร์ มหาวิทยาลัยราชภัฏพิบูลสงคราม (PSRU)

---

## เปิดโปรเจคยังไง

1. เปิด Android Studio → **File → Open** → เลือกโฟลเดอร์นี้
2. รอ Gradle sync จบ (ครั้งแรกจะโหลด dependency สักพัก)
3. (ถ้าจะใช้ AI) คัดลอก `local.properties.example` เป็น `local.properties`
   แล้วใส่ `GEMINI_API_KEY` — ดูวิธีใน [`docs/AI_SETUP_GUIDE.txt`](docs/AI_SETUP_GUIDE.txt)
   (ไม่ใส่ก็ได้ แอปจะใช้โหมดออฟไลน์แทน)
4. กด **Run ▶** เลือก emulator หรือมือถือจริง (ต้องเป็น Android 7.0 / API 24 ขึ้นไป)

---

## โครงสร้างไฟล์

```
app/src/main/java/com/psru/ginraidee/
├── model/                        ← ชั้น Model (ข้อมูล + กฎการทำงาน)
│   ├── Food.java                 abstract class — คลาสแม่ของอาหารทุกจาน
│   ├── HealthyFood.java          อาหารเพื่อสุขภาพ
│   ├── FastFood.java             อาหารเน้นความเร็ว
│   ├── SharingFood.java          อาหารสายปาร์ตี้-ครอบครัว
│   ├── DessertFood.java          อาหารประเภทของหวาน-มื้อเบา ๆ
│   ├── FoodCategory.java         enum หมวดความอยาก
│   ├── FoodFilter.java           เงื่อนไขการกรอง (ราคา/แคลอรี่/มังสวิรัติ)
│   ├── FoodManager.java          ตรรกะ สุ่ม/ค้นหา/กรอง/เพิ่ม-แก้-ลบ/รายการโปรด
│   ├── FoodRepository.java       คลังเมนู offline 40 จาน
│   ├── FavoriteStore.java        บันทึกรายการโปรดลงเครื่อง (SharedPreferences)
│   └── AiSuggestion.java         ผลลัพธ์ 1 รายการจาก AI
│
├── controller/                   ← ชั้น Controller (รับ input เชื่อม Model กับ View)
│   ├── MainActivity.java         หน้าแรก
│   ├── ChoiceActivity.java       เลือกหมวด
│   ├── RandomActivity.java       หมุนสุ่ม + ตั้งเงื่อนไข
│   ├── ResultActivity.java       แสดงผลลัพธ์ + ปุ่มดูร้านในแผนที่
│   ├── AiActivity.java           หน้าคุยกับ AI
│   ├── AiSuggestController.java  ตรรกะเรียก Gemini + แปลงผลลัพธ์
│   ├── SuggestionAdapter.java    เอาข้อมูลใส่ RecyclerView
│   ├── FavoriteActivity.java     รายการอาหารโปรด
│   ├── IngredientSearchActivity.java  ค้นหาเมนูจากวัตถุดิบ
│   ├── SearchResultActivity.java แสดงผลการค้นหา
│   ├── RecipeActivity.java       สูตร/วิธีทำ + ปุ่มดูร้านในแผนที่
│   ├── MenuCatalogActivity.java  รายการเมนูทั้งหมด
│   ├── WeeklyPlannerActivity.java  วางแผนมื้ออาหารรายสัปดาห์
│   └── MoreActivity.java         เมนูเพิ่มเติม (ไปหน้า ตั้งค่า/วิธีใช้/เกี่ยวกับ)
│
├── view/                         ← หน้าจอข้อมูลทั่วไป
│   ├── SettingsActivity.java     ตั้งค่า
│   ├── HowToActivity.java        วิธีใช้งาน
│   └── AboutActivity.java        เกี่ยวกับแอป
│
└── util/
    ├── GeminiApiClient.java      ยิง HTTP ไป Gemini API
    └── BottomNavHelper.java      จัดการแถบเมนูด้านล่าง

app/src/main/res/layout/          ← ชั้น View (XML ล้วน ไม่มี logic)
├── activity_main.xml             activity_choice.xml
├── activity_random.xml           activity_result.xml
├── activity_ai.xml               activity_favorite.xml
├── activity_ingredient_search.xml  activity_search_result.xml
├── activity_recipe.xml           activity_menu_catalog.xml
├── activity_weekly_planner.xml   activity_more.xml
├── activity_settings.xml         activity_how_to.xml
├── activity_about.xml            include_bottom_nav.xml
└── item_food.xml
```

รายละเอียดการออกแบบ + Class Diagram อยู่ที่ [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)

---

## หลัก OOP 4 ข้อ อยู่ตรงไหนบ้าง

| หลักการ | อยู่ที่ไหน | เปิดไฟล์ไหนดู |
|---|---|---|
| **Abstraction** | `Food` เป็น abstract class สร้าง object ตรง ๆ ไม่ได้ | `model/Food.java` |
| **Inheritance** | คลาสลูก 4 ตัว `extends Food` | `model/HealthyFood.java` และอีก 3 ไฟล์ |
| **Polymorphism** | เรียก `getInfo()` / `getTag()` ตัวเดียว แต่ได้ผลต่างกันตามคลาสลูก | `controller/ResultActivity.showFood()` |
| **Encapsulation** | field เป็น `private` ทุกตัว เข้าถึงผ่าน getter/setter | `model/Food.java` |

---

## ขอบเขตของโปรเจค (Scope)

ทำงาน **offline เป็นหลัก** ข้อมูลอาหารเก็บในเครื่องทั้งหมด

### ฟีเจอร์หลัก (ตามโครงร่างที่เสนออาจารย์)

| สถานะ | ฟีเจอร์ |
|---|---|
| ✅ | เลือกหมวดความอยาก → สุ่มเมนู |
| ✅ | Filter ตามเงื่อนไข (ราคา / แคลอรี่ / มังสวิรัติ) |
| ✅ | AI แนะนำเมนูจากคำอธิบายของผู้ใช้ (Gemini API) |

### ฟีเจอร์เสริมที่เพิ่มระหว่างพัฒนา

| สถานะ | ฟีเจอร์ |
|---|---|
| ✅ | รายการอาหารโปรด (บันทึกในเครื่อง) |
| ✅ | ค้นหาเมนูจากวัตถุดิบ |
| ✅ | ดูสูตร/วิธีทำ |
| ✅ | รายการเมนูทั้งหมด (Menu Catalog) |
| ✅ | วางแผนมื้ออาหารรายสัปดาห์ (Weekly Planner) |
| ✅ | ปุ่มเปิด Google Maps ดูร้านแนะนำของเมนูนั้น (ส่งคำค้นผ่าน Intent ไม่ใช้ GPS / Places API) |

### ฟีเจอร์ที่ตัดออก

| สถานะ | ฟีเจอร์ |
|---|---|
| ❌ | ค้นหาร้านใกล้เคียงจาก GPS / Google Places API |
| ❌ | แนะนำเมนูตามกระแสโซเชียล |
| ❌ | โหมดกลุ่มแบบ real-time |

**จุดที่ต่อ internet คือหน้า AI และปุ่มเปิดแผนที่** ที่เหลือทำงานได้แม้ปิดเน็ต

---

## ทีมพัฒนา

นักศึกษาสาขาวิศวกรรมคอมพิวเตอร์ มหาวิทยาลัยราชภัฏพิบูลสงคราม จำนวน 5 คน

| ชื่อ | รหัสนักศึกษา |
|---|---|
| นาย ณัฐกรณ์ น้อยเลิศบุญ | 6812247007 |
| นาย ธีรภัทร ทองมี | 6812247014 |
| นาย จิรครินทร์ ปาผล | 6812247040 |
| นาย ณัฐวัฒน์ คงแพง | 6812247041 |
| นาย ปัณณพัฒน์ นิพัทธ์คุณ | 6812247047 |

## เครื่องมือหลัก

Claude Code, ChatGPT, Gemini