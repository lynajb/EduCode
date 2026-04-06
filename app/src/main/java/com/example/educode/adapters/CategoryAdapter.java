package com.example.educode.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educode.R;
import com.example.educode.models.Category;
import com.example.educode.ui.learn.SignListActivity;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private String currentUsername;

    // The gradient colors
    private final String[] colors = {
            "#FF5722", "#FF7043", "#FF8A65", "#FFAB91",
            "#FFCCBC", "#FFD180", "#FFE0B2", "#FFF3E0"
    };

    public CategoryAdapter(Context context, List<Category> categoryList, String username) {
        this.context = context;
        this.categoryList = categoryList;
        this.currentUsername = username;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvName.setText(category.getName());

        // 1. Calculate the color for this specific item
        String colorHex = colors[position % colors.length];

        // Apply it to the card background
        holder.layoutBackground.setBackgroundColor(Color.parseColor(colorHex));

        // Text color logic (Dark text for light backgrounds)
        if (position < 3) {
            holder.tvName.setTextColor(Color.WHITE);
        } else {
            holder.tvName.setTextColor(Color.parseColor("#333333"));
        }

        int resId = context.getResources().getIdentifier(category.getIconName(), "drawable", context.getPackageName());
        if (resId == 0) resId = R.mipmap.ic_launcher;
        holder.imgIcon.setImageResource(resId);

        // Click Listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SignListActivity.class);
            intent.putExtra("CAT_ID", category.getId());
            intent.putExtra("CAT_NAME", category.getName());
            intent.putExtra("USERNAME", currentUsername);

            // --- NEW: Pass the color to the next activity ---
            intent.putExtra("CAT_COLOR", colorHex);
            // ------------------------------------------------

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgIcon;
        LinearLayout layoutBackground;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCatName);
            imgIcon = itemView.findViewById(R.id.imgCatIcon);
            layoutBackground = itemView.findViewById(R.id.layoutCategoryBackground);
        }
    }
}