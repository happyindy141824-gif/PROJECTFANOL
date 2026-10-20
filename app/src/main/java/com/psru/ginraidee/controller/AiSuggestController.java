package com.psru.ginraidee.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.psru.ginraidee.BuildConfig;
import com.psru.ginraidee.model.AiSuggestion;
import com.psru.ginraidee.model.Food;
import com.psru.ginraidee.model.FoodCategory;
import com.psru.ginraidee.model.FoodManager;
import com.psru.ginraidee.util.GeminiApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller ของฟีเจอร์ "แนะนำโดย AI"
 *
 * <p>หน้าที่:</p>
 * <ol>
 *   <li>รับข้อความที่ผู้ใช้พิมพ์มาจาก View ({@link AiActivity})</li>
 *   <li>ประกอบ prompt แล้วสั่ง {@link GeminiApiClient} ยิง API บน background thread</li>
 *   <li>แปลง JSON ที่ได้กลับมาเป็น {@link AiSuggestion} และจับคู่กับเมนูในคลัง offline</li>
 *   <li>ส่งผลลัพธ์กลับขึ้น main thread ให้ View เอาไปแสดง</li>
 * </ol>
 *
 * <p>สังเกตว่าคลาสนี้ไม่รู้จัก layout หรือ widget ใด ๆ เลย มันคุยกับ View ผ่าน
 * {@link Callback} เท่านั้น — นี่คือหัวใจของการแยกชั้นแบบ MVC</p>
 */
public class AiSuggestController {

    /** ช่องทางส่งผลลัพธ์กลับไปให้หน้าจอ */
    public interface Callback {
        /**
         * @param offlineNotice null ถ้าคำตอบมาจาก Gemini จริง
         *                      ถ้าไม่ null แปลว่าใช้โหมดออฟไลน์ และข้อความนี้บอกเหตุผล
         */
        void onSuccess(List<AiSuggestion> suggestions, String offlineNotice);

        void onError(String message);
    }

    private static final String TAG = "AiSuggestController";
    private static final int MAX_SUGGESTIONS = 3;

    private final FoodManager foodManager;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public AiSuggestController(FoodManager foodManager) {
        this.foodManager = foodManager;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /** ใส่ API key ไว้ใน local.properties หรือยัง */
    public boolean isApiKeyConfigured() {
        return BuildConfig.GEMINI_API_KEY != null && !BuildConfig.GEMINI_API_KEY.trim().isEmpty();
    }

    /**
     * ขอคำแนะนำเมนูจากคำอธิบายของผู้ใช้
     * ถ้ายังไม่ได้ตั้ง API key จะเปลี่ยนไปใช้การค้นหาในเครื่องแทน แอปจะได้ยังเดโมได้
     */
    public void suggest(String userInput, Callback callback) {
        if (userInput == null || userInput.trim().isEmpty()) {
            callback.onError("ช่วยพิมพ์บอกหน่อยว่าตอนนี้อยากกินแนวไหน");
            return;
        }

        final String question = userInput.trim();

        if (!isApiKeyConfigured()) {
            List<AiSuggestion> offline = suggestOffline(question);
            if (offline.isEmpty()) {
                callback.onError("ไม่พบเมนูที่เข้ากับคำค้นหา ลองระบุคำค้นหาใหม่อีกครั้งนะ");
            } else {
                callback.onSuccess(offline,
                        "⚠️ ยังไม่ได้ใส่ GEMINI_API_KEY ใน local.properties จึงใช้โหมดออฟไลน์ (ดู docs/AI_SETUP_GUIDE.txt)");
            }
            return;
        }

        executor.execute(() -> {
            try {
                String json = GeminiApiClient.suggestMenus(
                        BuildConfig.GEMINI_API_KEY,
                        BuildConfig.GEMINI_MODEL,
                        buildPrompt(question));
                List<AiSuggestion> suggestions = parseSuggestions(json);

                if (suggestions.isEmpty()) {
                    postError(callback, "AI ยังไม่มีเมนูแนะนำ ลองอธิบายเพิ่มอีกนิดนะ");
                } else {
                    mainHandler.post(() -> callback.onSuccess(suggestions, null));
                }
            } catch (Exception e) {
                // ต่อ AI ไม่ได้ก็ยังพอช่วยผู้ใช้ได้ด้วยการค้นในคลัง offline
                // แต่ต้องบอกผู้ใช้ด้วยว่าทำไม ไม่งั้นจะดูเหมือน AI ใช้ไม่ได้โดยไม่รู้สาเหตุ
                Log.e(TAG, "เรียก Gemini ไม่สำเร็จ", e);
                String reason = describeError(e);
                List<AiSuggestion> offline = suggestOffline(question);
                if (offline.isEmpty()) {
                    postError(callback, "เชื่อมต่อ AI ไม่สำเร็จ: " + reason);
                } else {
                    mainHandler.post(() -> callback.onSuccess(offline,
                            "⚠️ เชื่อมต่อ AI ไม่สำเร็จ (" + reason + ") จึงใช้โหมดออฟไลน์แทน"));
                }
            }
        });
    }

    /** ปิด thread pool ตอนหน้าจอถูกทำลาย ไม่งั้น thread ค้าง */
    public void shutdown() {
        executor.shutdown();
    }

    // ------------------------------------------------------------------
    // ส่วนประกอบภายใน
    // ------------------------------------------------------------------

    /**
     * ประกอบ prompt ให้ AI แนะนำเมนูอะไรก็ได้ตามที่ผู้ใช้อยากกิน
     * ไม่แนบรายชื่อเมนูในเครื่องไปแล้ว เพราะ AI จะวนเลือกแต่เมนูเดิม ๆ ในคลัง
     * ถ้าชื่อที่ได้กลับมาตรงกับคลัง offline ก็ยังจับคู่ได้เองใน parseSuggestions()
     */
    private String buildPrompt(String userInput) {
        return "คุณเป็นผู้ช่วยเลือกเมนูอาหารของแอป \"Gin Rai Dee?\" ตอบเป็นภาษาไทยเสมอ\n\n"
                + "สิ่งที่ผู้ใช้อยากกิน: \"" + userInput + "\"\n\n"
                + "ให้แนะนำเมนู " + MAX_SUGGESTIONS + " อย่างที่ไม่ซ้ำแนวกัน เรียงจากที่เหมาะที่สุด\n"
                + "- เลือกได้ทุกเมนูที่หากินได้จริงในไทย ทั้งอาหารไทย ญี่ปุ่น เกาหลี ตะวันตก ของหวาน ฯลฯ\n"
                + "- foodName ใช้ชื่อเมนูที่คนไทยเรียกกันทั่วไป ไม่ต้องมีคำอธิบายต่อท้าย\n"
                + "- reason เขียนสั้น ๆ ไม่เกิน 2 บรรทัด บอกว่าทำไมเมนูนี้ถึงเหมาะกับที่ผู้ใช้บอก\n"
                + "- calories คือแคลอรี่โดยประมาณต่อ 1 จาน (ตัวเลขอย่างเดียว)\n"
                + "- ingredients คือวัตถุดิบหลัก คั่นด้วยเครื่องหมายจุลภาค\n"
                + "- steps คือวิธีทำแบบย่อ 3-5 ขั้นตอน ขึ้นบรรทัดใหม่ทุกขั้นและนำหน้าด้วยเลขข้อ";
    }

    /** แปลง JSON array ที่ Gemini ตอบกลับมาเป็น list ของ object */
    private List<AiSuggestion> parseSuggestions(String json) throws Exception {
        List<AiSuggestion> suggestions = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return suggestions;
        }

        JSONArray array = new JSONArray(json.trim());
        for (int i = 0; i < array.length() && suggestions.size() < MAX_SUGGESTIONS; i++) {
            JSONObject item = array.getJSONObject(i);
            String name = item.optString("foodName", "").trim();
            String reason = item.optString("reason", "").trim();
            if (name.isEmpty()) {
                continue;
            }
            // จับคู่กับคลัง offline เพื่อเอาราคา/แคลอรี่จริงมาแสดง ถ้าไม่เจอก็ใช้ค่าที่ AI ประมาณมา
            suggestions.add(new AiSuggestion(name, reason, foodManager.findByName(name),
                    item.optInt("calories", 0),
                    item.optString("ingredients", "").trim(),
                    item.optString("steps", "").trim()));
        }
        return suggestions;
    }

    /**
     * แผนสำรองเมื่อไม่มี API key หรือเน็ตล่ม — ค้นจากคลังเมนูในเครื่องด้วยคำที่ผู้ใช้พิมพ์
     * ใช้ FoodManager.searchFood() ที่เขียนไว้แล้ว ไม่ต้องเขียนตรรกะซ้ำ
     */
    private List<AiSuggestion> suggestOffline(String userInput) {
        List<AiSuggestion> suggestions = new ArrayList<>();
        if (userInput == null || userInput.trim().isEmpty()) {
            return suggestions;
        }

        // 1. ค้นหาตามคำที่มีการเว้นวรรค
        for (String keyword : userInput.split("[\\s,]+")) {
            if (keyword.length() < 2) {
                continue;
            }
            for (Food food : foodManager.searchFood(keyword)) {
                if (suggestions.size() >= MAX_SUGGESTIONS) {
                    return suggestions;
                }
                if (!containsFood(suggestions, food)) {
                    suggestions.add(new AiSuggestion(food.getFoodName(),
                            "ค้นเจอในเมนูของแอปจากคำว่า \"" + keyword + "\" (โหมดออฟไลน์)", food));
                }
            }
        }

        // 2. ถ้ายังได้ไม่ครบ (เช่น ภาษาไทยที่พิมพ์ติดกันเป็นประโยคโดยไม่เว้นวรรค)
        String cleanInput = userInput.trim().toLowerCase();
        for (Food food : foodManager.getFoodList()) {
            if (suggestions.size() >= MAX_SUGGESTIONS) {
                break;
            }
            if (containsFood(suggestions, food)) {
                continue;
            }

            String foodName = food.getFoodName().toLowerCase();
            boolean match = cleanInput.contains(foodName);

            // ตรวจสอบคำสั้นในชื่ออาหาร เช่น "สลัด", "กะเพรา", "ส้มตำ", "ไก่"
            if (!match) {
                String[] parts = foodName.split("\\s+");
                for (String part : parts) {
                    if (part.length() >= 3 && cleanInput.contains(part)) {
                        match = true;
                        break;
                    }
                }
            }

            // ตรวจสอบคีย์เวิร์ดแนวรสชาติ / สไตล์อาหาร
            if (!match) {
                if ((cleanInput.contains("เผ็ด") || cleanInput.contains("แซ่บ"))
                        && (food.getTag().contains("เผ็ด") || food.getDescription().contains("เผ็ด") || food.getDescription().contains("แซ่บ"))) {
                    match = true;
                } else if ((cleanInput.contains("หวาน") || cleanInput.contains("ขนม") || cleanInput.contains("ล้างปาก") || cleanInput.contains("สดชื่น"))
                        && food.getCategory() == FoodCategory.DESSERT) {
                    match = true;
                } else if ((cleanInput.contains("สุขภาพ") || cleanInput.contains("คลีน") || cleanInput.contains("แคล") || cleanInput.contains("ผัก"))
                        && food.getCategory() == FoodCategory.HEALTHY) {
                    match = true;
                } else if ((cleanInput.contains("ด่วน") || cleanInput.contains("เร็ว") || cleanInput.contains("รีบ") || cleanInput.contains("ง่าย"))
                        && food.getCategory() == FoodCategory.FAST) {
                    match = true;
                } else if ((cleanInput.contains("ปาร์ตี้") || cleanInput.contains("เพื่อน") || cleanInput.contains("หม้อไฟ") || cleanInput.contains("ครอบครัว") || cleanInput.contains("สังสรรค์"))
                        && food.getCategory() == FoodCategory.SHARING) {
                    match = true;
                }
            }

            if (match) {
                suggestions.add(new AiSuggestion(food.getFoodName(),
                        "แนะนำสำหรับความต้องการ: \"" + userInput + "\" (โหมดออฟไลน์)", food));
            }
        }

        // 3. ถ้ายังไม่พบเมนูตรงกันเลย ให้สุ่มเมนูยอดนิยมมาให้
        if (suggestions.isEmpty()) {
            for (Food food : foodManager.getFoodList()) {
                if (suggestions.size() >= MAX_SUGGESTIONS) {
                    break;
                }
                suggestions.add(new AiSuggestion(food.getFoodName(),
                        "เมนูแนะนำยอดนิยม (โหมดออฟไลน์)", food));
            }
        }

        return suggestions;
    }

    private boolean containsFood(List<AiSuggestion> suggestions, Food food) {
        for (AiSuggestion suggestion : suggestions) {
            if (suggestion.getFoodName().equals(food.getFoodName())) {
                return true;
            }
        }
        return false;
    }

    /** แปลง exception เป็นข้อความที่ผู้ใช้อ่านแล้วรู้ว่าต้องแก้ตรงไหน */
    private String describeError(Exception e) {
        if (e instanceof UnknownHostException) {
            return "ไม่มีอินเทอร์เน็ต หรือ emulator ต่อเน็ตไม่ได้";
        }
        if (e instanceof SocketTimeoutException) {
            return "เน็ตช้าเกินไป หมดเวลารอ";
        }
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (e instanceof IOException) {
            if (msg.contains("API_KEY_INVALID") || msg.contains("API key not valid")) {
                return "API key ไม่ถูกต้อง ลองสร้างคีย์ใหม่";
            }
            if (msg.contains("HTTP 404")) {
                return "ไม่พบโมเดล \"" + BuildConfig.GEMINI_MODEL + "\" ลองเปลี่ยน GEMINI_MODEL";
            }
            if (msg.contains("HTTP 429")) {
                return "ใช้โควตาฟรีเกินแล้ว รอสักพักแล้วลองใหม่";
            }
            if (msg.contains("HTTP 403")) {
                return "คีย์นี้ไม่มีสิทธิ์ใช้ Gemini API";
            }
        }
        return msg.isEmpty() ? e.getClass().getSimpleName() : msg;
    }

    private void postError(Callback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
