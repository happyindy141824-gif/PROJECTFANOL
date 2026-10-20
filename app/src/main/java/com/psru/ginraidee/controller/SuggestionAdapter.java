package com.psru.ginraidee.controller;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psru.ginraidee.R;
import com.psru.ginraidee.model.AiSuggestion;
import com.psru.ginraidee.model.Food;
import com.psru.ginraidee.model.FoodManager;
import com.psru.ginraidee.model.HealthyFood;

import java.util.ArrayList;
import java.util.List;

/**
 * ตัวเชื่อมระหว่างข้อมูล {@link AiSuggestion} กับ layout {@code item_food.xml}
 *
 * <p>อยู่ในชั้น Controller เพราะหน้าที่ของมันคือ "เอาข้อมูลจาก Model ไปใส่ View"
 * ตัวมันเองไม่ได้เก็บกฎทางธุรกิจอะไรเลย</p>
 */
public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    private final List<AiSuggestion> suggestions = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Food food);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /** แทนที่ข้อมูลทั้งชุดแล้วสั่งวาดใหม่ */
    public void submit(List<AiSuggestion> newSuggestions) {
        suggestions.clear();
        if (newSuggestions != null) {
            suggestions.addAll(newSuggestions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        holder.bind(suggestions.get(position), itemClickListener);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {

        private final TextView emojiTextView;
        private final TextView nameTextView;
        private final TextView detailTextView;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiTextView = itemView.findViewById(R.id.tvItemEmoji);
            nameTextView = itemView.findViewById(R.id.tvItemName);
            detailTextView = itemView.findViewById(R.id.tvItemDetail);
        }

        void bind(AiSuggestion suggestion, OnItemClickListener listener) {
            emojiTextView.setText(suggestion.getEmoji());
            nameTextView.setText(suggestion.getFoodName());

            StringBuilder detail = new StringBuilder(suggestion.getReason());
            Food matched = suggestion.getMatchedFood();
            if (matched != null) {
                detail.append("\n")
                        .append(matched.getCalories())
                        .append(" แคลอรี่ • ")
                        .append(matched.getTag());
            } else if (suggestion.getCalories() > 0) {
                detail.append("\n~")
                        .append(suggestion.getCalories())
                        .append(" แคลอรี่ • เมนูแนะนำจาก AI");
            }
            detailTextView.setText(detail.toString());

            itemView.setOnClickListener(v -> {
                Food foodToPass = suggestion.getMatchedFood();
                if (foodToPass == null) {
                    foodToPass = FoodManager.getInstance().findByName(suggestion.getFoodName());
                }
                if (foodToPass == null && suggestion.getFoodName() != null) {
                    // เมนูใหม่จาก AI ที่ไม่มีในคลัง ใช้แคลอรี่/วัตถุดิบ/วิธีทำที่ AI ส่งมา
                    int calories = suggestion.getCalories() > 0 ? suggestion.getCalories() : 350;
                    String ingredients = suggestion.getIngredients().isEmpty()
                            ? "วัตถุดิบแนะนำจาก AI ออนไลน์" : suggestion.getIngredients();
                    String steps = suggestion.getSteps().isEmpty()
                            ? "1. เตรียมวัตถุดิบตามความเหมาะสมของเมนู\n2. ปรุงรสตามความชอบ\n3. จัดเสิร์ฟความอร่อยร้อน ๆ"
                            : suggestion.getSteps();
                    foodToPass = new HealthyFood(
                            suggestion.getFoodName(),
                            70.0,
                            calories,
                            "",
                            suggestion.getReason(),
                            80,
                            false
                    ).withDetails(ingredients, steps);
                }
                if (foodToPass != null) {
                    if (listener != null) {
                        listener.onItemClick(foodToPass);
                    } else {
                        Intent intent = new Intent(v.getContext(), RecipeActivity.class);
                        intent.putExtra(RecipeActivity.EXTRA_FOOD, foodToPass);
                        v.getContext().startActivity(intent);
                    }
                }
            });
        }
    }
}
