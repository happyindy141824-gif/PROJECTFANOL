package com.psru.ginraidee.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.psru.ginraidee.R;
import com.psru.ginraidee.controller.FavoriteActivity;
import com.psru.ginraidee.controller.IngredientSearchActivity;
import com.psru.ginraidee.controller.MainActivity;
import com.psru.ginraidee.controller.MoreActivity;
import com.psru.ginraidee.controller.WeeklyPlannerActivity;

public final class BottomNavHelper {

    private BottomNavHelper() {}

    public static void setup(Activity activity, int currentNavId) {
        View navHome = activity.findViewById(R.id.navHome);
        View navFavorite = activity.findViewById(R.id.navFavorite);
        View navSearch = activity.findViewById(R.id.navSearch);
        View navWeekly = activity.findViewById(R.id.navWeekly);
        View navMore = activity.findViewById(R.id.navMore);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!(activity instanceof MainActivity)) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navFavorite != null) {
            navFavorite.setOnClickListener(v -> {
                if (!(activity instanceof FavoriteActivity)) {
                    Intent intent = new Intent(activity, FavoriteActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navSearch != null) {
            navSearch.setOnClickListener(v -> {
                if (!(activity instanceof IngredientSearchActivity)) {
                    Intent intent = new Intent(activity, IngredientSearchActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navWeekly != null) {
            navWeekly.setOnClickListener(v -> {
                if (!(activity instanceof WeeklyPlannerActivity)) {
                    Intent intent = new Intent(activity, WeeklyPlannerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navMore != null) {
            navMore.setOnClickListener(v -> {
                if (!(activity instanceof MoreActivity)) {
                    Intent intent = new Intent(activity, MoreActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }
    }
}
