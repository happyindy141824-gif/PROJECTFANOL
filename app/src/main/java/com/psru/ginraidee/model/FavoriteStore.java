package com.psru.ginraidee.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public final class FavoriteStore {

    private static final String PREF_NAME = "ginraidee_favorites";
    private static final String KEY_FAVORITES = "favorite_food_names";

    private FavoriteStore() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static Set<String> getNames(Context context) {
        Set<String> saved = getPrefs(context)
                .getStringSet(KEY_FAVORITES, new HashSet<>());

        return new HashSet<>(saved);
    }

    public static boolean isFavorite(Context context, String foodName) {
        return getNames(context).contains(foodName);
    }

    public static void saveFromManager(Context context) {
        Set<String> names = new HashSet<>();

        for (Food food : FoodManager.getInstance().getFavoriteFoods()) {
            names.add(food.getFoodName());
        }

        getPrefs(context).edit()
                .putStringSet(KEY_FAVORITES, names)
                .apply();
    }

    public static void loadIntoManager(Context context) {
        Set<String> names = getNames(context);

        for (Food food : FoodManager.getInstance().getFoodList()) {
            food.setFavorite(names.contains(food.getFoodName()));
        }
    }
}
