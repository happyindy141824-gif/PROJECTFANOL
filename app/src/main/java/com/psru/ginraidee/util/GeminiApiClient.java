package com.psru.ginraidee.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * ที่คิดไว้คือเป็นตัวเรียก Gemini REST API ตรง ๆ ด้วย HttpURLConnection
 *
 * <p>ตั้งใจไม่ใช้ library เสริม (Retrofit/OkHttp) เพื่อให้เห็นขั้นตอนครบ
 * และลด dependency ของโปรเจค</p>
 *
 * <p><b>คลาสนี้ต้องเรียกจาก background thread เท่านั้น</b>
 * ถ้าเรียกบน main thread แอปจะ crash ด้วย NetworkOnMainThreadException
 * — ผู้ที่จัดการเรื่อง thread ให้คือ AiSuggestController</p>
 */
public final class GeminiApiClient {

    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final int TIMEOUT_MS = 20000;

    private GeminiApiClient() {
    }

    /**
     * ส่ง prompt ไปให้ Gemini แล้วบังคับให้ตอบกลับเป็น JSON array
     * ตาม schema: [{ "foodName", "reason", "calories", "ingredients", "steps" }]
     *
     * @return ข้อความ JSON ที่โมเดลตอบกลับมา
     * @throws IOException ถ้าเน็ตมีปัญหา หรือ API ตอบ error
     */
    public static String suggestMenus(String apiKey, String model, String prompt) throws IOException {
        HttpURLConnection connection = null;
        try {
            String fullUrl = String.format(ENDPOINT_TEMPLATE, model) + "?key=" + apiKey;
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("x-goog-api-key", apiKey);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoOutput(true);

            byte[] body = buildRequestBody(prompt).getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body);
            }

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                String error = readStream(connection.getErrorStream());
                throw new IOException("Gemini API ตอบกลับ HTTP " + status + " : " + error);
            }

            String response = readStream(connection.getInputStream());
            return extractText(response);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * สร้าง JSON body ของ request
     * จุดสำคัญคือ responseMimeType + responseSchema ที่บังคับให้โมเดลตอบเป็น JSON
     * จะได้ parse เข้า object ง่าย ไม่ต้องมานั่งตัดข้อความเอง
     */
    private static String buildRequestBody(String prompt) {
        try {
            JSONObject part = new JSONObject().put("text", prompt);
            JSONObject content = new JSONObject()
                    .put("role", "user")
                    .put("parts", new JSONArray().put(part));

            JSONObject itemProperties = new JSONObject()
                    .put("foodName", new JSONObject().put("type", "STRING"))
                    .put("reason", new JSONObject().put("type", "STRING"))
                    .put("calories", new JSONObject().put("type", "INTEGER"))
                    .put("ingredients", new JSONObject().put("type", "STRING"))
                    .put("steps", new JSONObject().put("type", "STRING"));

            JSONObject items = new JSONObject()
                    .put("type", "OBJECT")
                    .put("properties", itemProperties)
                    .put("required", new JSONArray().put("foodName").put("reason")
                            .put("calories").put("ingredients").put("steps"));

            JSONObject schema = new JSONObject()
                    .put("type", "ARRAY")
                    .put("items", items);

            JSONObject generationConfig = new JSONObject()
                    .put("temperature", 1.0)
                    .put("responseMimeType", "application/json")
                    .put("responseSchema", schema);

            return new JSONObject()
                    .put("contents", new JSONArray().put(content))
                    .put("generationConfig", generationConfig)
                    .toString();
        } catch (JSONException e) {
            // ไม่ควรเกิด เพราะ key ทุกตัวเราใส่เอง
            throw new IllegalStateException("สร้าง JSON request ไม่สำเร็จ", e);
        }
    }

    /** ดึงข้อความคำตอบออกจากโครงสร้าง response ของ Gemini */
    private static String extractText(String rawResponse) throws IOException {
        try {
            JSONObject root = new JSONObject(rawResponse);
            JSONArray candidates = root.optJSONArray("candidates");
            if (candidates == null || candidates.length() == 0) {
                throw new IOException("Gemini ไม่ได้ส่งคำตอบกลับมา");
            }
            JSONArray parts = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts");

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                text.append(parts.getJSONObject(i).optString("text", ""));
            }
            return text.toString();
        } catch (JSONException e) {
            throw new IOException("อ่านคำตอบจาก Gemini ไม่ได้: " + e.getMessage(), e);
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
