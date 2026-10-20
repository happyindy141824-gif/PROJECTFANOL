# PROJECT_CONTEXT.md
> วางไฟล์นี้ไว้ที่ root ของโปรเจค Android Studio แล้วบอก Claude Code ว่า
> "อ่านไฟล์ PROJECT_CONTEXT.md ก่อนเริ่มงาน" ทุกครั้งที่เปิด session ใหม่

## ข้อมูลรายวิชา

- **รายวิชา**: การเขียนโปรแกรมคอมพิวเตอร์ขั้นสูง
- **อาจารย์ผู้สอน**: อาจารย์วนารัตน์ จุฬพันธ์ทอง
- **มหาวิทยาลัย**: มหาวิทยาลัยราชภัฏพิบูลสงคราม (PSRU)
- **โจทย์**: ทำแอปพลิเคชันบน Android ด้วยภาษา Java ออกแบบด้วยหลัก **OOP + MVC**

## ภาพรวมแอปพลิเคชัน

- **ชื่อแอพ**: Gin Rai Dee? (มื้อนี้กินอะไรดี?)
- **แนวคิด**: แอปช่วยตัดสินใจเลือกเมนูอาหาร แก้ปัญหาความลังเลก่อนกินแต่ละมื้อ
- **จุดขายหลัก**: ให้ผู้ใช้เลือกหมวดความอยาก (Hunger Spectrum) ก่อน แล้วค่อยสุ่มภายในหมวดนั้น แทนที่จะสุ่มมั่วๆ ไม่ตรงใจ

## Scope ที่ตัดสินใจแล้ว (สำคัญ — อย่าเพิ่มฟีเจอร์นอกนี้โดยไม่ถามก่อน)

**ทำงานแบบ offline เป็นหลัก** ข้อมูลอาหารเก็บในเครื่อง (local, ไม่ต้องพึ่ง GPS/Places API)

**จุดเดียวที่ต่อ internet คือ AI แนะนำเมนู** ผ่าน Gemini API (free tier พอสำหรับ scope นี้ ไม่ต้องกังวลเรื่อง rate limit)

### ฟีเจอร์ที่ตัดออกแล้ว (อาจารย์แนะนำให้เก็บเฉพาะ AI ที่เหลือให้ทำ offline)
- ❌ ค้นหาร้านใกล้เคียงจาก GPS / Google Places API
- ❌ แนะนำเมนูตามกระแสโซเชียล (TikTok/Instagram)
- ❌ โหมดกลุ่ม/ปาร์ตี้แบบ real-time multiplayer

### ฟีเจอร์ที่เก็บไว้
- ✅ เลือกหมวดความอยาก → สุ่มเมนู (offline)
- ✅ Filter ตามเงื่อนไข
- ✅ AI แนะนำเมนูจากคำอธิบายของผู้ใช้ (ผ่าน Gemini API)

## Story Board (ลำดับหน้าจอ)

1. **Main Page** — ปุ่ม Random เริ่มต้น
2. **Choice Page** — เลือกหมวด: เพื่อสุขภาพ / เน้นความเร็ว / สายปาร์ตี้-ครอบครัว / ของหวาน-มื้อเบาๆ / ทั้งหมด
3. **Randomization Page** — หมุนสุ่ม (มีปุ่ม "หมุน")
4. **Output Page** — แสดงผลลัพธ์ (มีปุ่ม "หมุนใหม่")
5. **แนะนำโดย AI Page** — พิมพ์คำอธิบายให้ AI แนะนำเมนู

## Class Diagram (OOP)

หลักการออกแบบ: **แยกข้อมูล (Food) ออกจากตรรกะการทำงาน (FoodManager)** — อย่าเอา method อย่าง `randomFood()`/`searchFood()`/`filterByType()` ไปใส่ในคลาส `Food` เพราะไม่ใช่หน้าที่ของอาหาร 1 จาน

```
«abstract» Food
- foodName : String
- price : double
- calories : int
- imageUrl : String
- description : String
+ getFoodName() : String
+ setFoodName(n) : void
+ getInfo() : String   {abstract}
+ getTag() : String    {abstract}

    ├── HealthyFood   (- nutriScore, - isVegetarian)
    ├── FastFood      (- prepTimeMin, - hasDelivery)
    ├── SharingFood   (- servingSize, - isSpicy)
    └── DessertFood   (- sweetLevel, - isCold)
        each overrides getInfo() and getTag()

FoodManager  (แยกจาก Food โดยเจตนา)
- foodList : List<Food>
+ randomFood() : Food
+ filterByType(t) : List<Food>
+ searchFood(kw) : List<Food>
+ addFood(f) / editFood(f) / deleteFood(f) : void
```

**หลักการ OOP ที่ต้องโชว์ให้ครบ 4 ข้อ**:
- Abstraction → `Food` เป็น abstract class
- Inheritance → คลาสลูก 4 ตัวสืบทอดจาก `Food`
- Polymorphism → แต่ละคลาสลูก override `getInfo()`/`getTag()` ต่างกัน
- Encapsulation → private field + getter/setter

คลาสลูกทั้ง 4 ตรงกับ 4 หมวดใน Choice Page พอดี — เวลาผู้ใช้เลือกหมวด ให้สร้าง object ของคลาสลูกที่ตรงกัน

## โครงสร้าง MVC

| ชั้น | หน้าที่ | ไฟล์ |
|---|---|---|
| **Model** | เก็บข้อมูล + กฎการทำงาน | `Food.java` (abstract), `HealthyFood.java`, `FastFood.java`, `SharingFood.java`, `DessertFood.java`, `FoodManager.java` |
| **View** | หน้าจอ (Layout XML เท่านั้น ห้ามมี logic) | `activity_main.xml`, `activity_choice.xml`, `activity_random.xml`, `activity_result.xml`, `activity_ai.xml`, `item_food.xml` |
| **Controller** | รับ input ผู้ใช้ เชื่อม Model กับ View | `MainActivity.java`, `ChoiceActivity.java`, `RandomActivity.java`, `ResultActivity.java`, `AiSuggestController.java` |

**ตัวอย่าง flow** (กดปุ่ม Random):
ผู้ใช้กดปุ่ม (View) → Controller รับ event → เรียก `FoodManager.randomFood()` (Model) → Model คืนค่า `Food` object → Controller สั่งให้ View แสดงผลที่ Output Page

## AI Integration (Gemini API)

- ใช้ **Gemini API free tier** (โมเดล Flash/Flash-Lite) — เพียงพอสำหรับ scope นี้ ไม่ต้องกังวล rate limit
- **ห้ามเปิด Cloud Billing บน Google Cloud project เดียวกับที่ใช้ทดสอบฟรี** เพราะจะทำให้ฟรีเทียร์หายไปทันที ทุกคำขอเริ่มคิดเงินตั้งแต่ token แรก ถ้าต้องการทดสอบแบบเสียเงินให้แยก project ใหม่
- Controller ที่รับผิดชอบส่วนนี้คือ `AiSuggestController.java`
- Flow: ผู้ใช้พิมพ์คำอธิบาย (เช่น "อยากกินเผ็ดๆ ทำเร็ว") → ส่ง prompt ไป Gemini API → รับผลลัพธ์กลับมา แสดงเป็นเมนูแนะนำ
- แนะนำให้ Gemini ตอบกลับเป็น **JSON** ที่ parse เข้า object ได้ง่าย แทนข้อความอิสระ

## Timeline (ประมาณ 8 สัปดาห์ / 2 เดือน)

| ช่วง | งาน |
|---|---|
| สัปดาห์ 1 | Class Diagram/MVC ให้สมบูรณ์ + เตรียมฐานข้อมูลอาหาร local |
| สัปดาห์ 2-3 | สร้าง UI ตาม Story Board |
| สัปดาห์ 4 | ระบบสุ่ม + filter (`FoodManager`) ให้ทำงานสมบูรณ์ |
| สัปดาห์ 5-6 | ต่อ Gemini API + debug |
| สัปดาห์ 7 | รวมระบบ ทดสอบ แก้ bug |
| สัปดาห์ 8 | สไลด์ คลิปวิดีโอสาธิต รายงาน |

**หมายเหตุ**: scope นี้เป็นการรวมไอเดียที่ตัดทอนแล้วรอบล่าสุด (ผ่านการเสนอโครงร่างกับอาจารย์แล้ว) หากเวลาช่วงพัฒนาจริงยังไม่พอ ให้ตัดจากลำดับนี้ก่อน:
1. AI (แนะนำให้เก็บไว้ — อาจารย์ระบุให้เก็บไว้โดยเฉพาะ)
2. ลด field/attribute ที่ไม่จำเป็นในแต่ละคลาสลูกได้ แต่อย่าลดจำนวนคลาสลูก (กระทบการโชว์ Inheritance/Polymorphism)

## ทีมพัฒนา

จัดทำโดยนักศึกษา สาขาวิศวกรรมคอมพิวเตอร์ มหาวิทยาลัยราชภัฏพิบูลสงคราม จำนวน 5 คน
