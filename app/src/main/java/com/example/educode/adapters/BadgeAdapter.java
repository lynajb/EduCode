package com.example.educode.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educode.R;
import com.example.educode.models.Badge;
import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private Context context;
    private List<Badge> badgeList;

    public BadgeAdapter(Context context, List<Badge> badgeList) {
        this.context = context;
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badgeList.get(position);
        holder.tvName.setText(badge.getName());
        holder.tvDesc.setText(badge.getDescription());

        // Charger l'image (assure-toi d'avoir des drawables nommés badge_bronze, etc.)
        // Sinon utilise des étoiles par défaut
        int resId = context.getResources().getIdentifier(
                badge.getIconName(), // This returns "badge_bronze", etc.
                "drawable",
                context.getPackageName()
        );
        if (resId == 0) {
            resId = android.R.drawable.star_big_on;
        }
        holder.imgIcon.setImageResource(resId);
        if (badge.isUnlocked()) {
            // UNLOCKED: Remove tint, restore opacity, add color
            holder.imgIcon.clearColorFilter(); // Removes the gray tint
            holder.imgIcon.setAlpha(1.0f);     // Full opacity
            holder.cardView.setCardElevation(8f); // Add shadow

            holder.tvStatus.setText("DÉBLOQUÉ");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            // LOCKED: Apply gray tint and lower opacity
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0); // Black and white
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.imgIcon.setColorFilter(filter);

            holder.imgIcon.setAlpha(0.5f);
            holder.cardView.setCardElevation(0f); // Flat

            holder.tvStatus.setText("VERROUILLÉ");
            holder.tvStatus.setTextColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() { return badgeList.size(); }

    public static class BadgeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvStatus;
        ImageView imgIcon;
        CardView cardView;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBadgeName);
            tvDesc = itemView.findViewById(R.id.tvBadgeDesc);
            tvStatus = itemView.findViewById(R.id.tvBadgeStatus);
            imgIcon = itemView.findViewById(R.id.imgBadgeIcon);
            cardView = itemView.findViewById(R.id.cardBadgeItem);
        }
    }
}