package com.example.educode.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educode.R;
import com.example.educode.models.DetectionResult;

import java.util.List;

public class DetectionsAdapter extends RecyclerView.Adapter<DetectionsAdapter.ViewHolder> {

    private Context context;
    private List<DetectionResult> detectionList;

    public DetectionsAdapter(Context context, List<DetectionResult> detectionList) {
        this.context = context;
        this.detectionList = detectionList;
    }

    public void updateList(List<DetectionResult> newList) {
        this.detectionList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetectionResult result = detectionList.get(position);

        holder.signName.setText(result.getSignName());
        holder.dateText.setText(result.getFormattedDate());
        holder.confidenceText.setText(result.getFormattedConfidence());

        // Load image dynamically based on resource name
        int resId = context.getResources().getIdentifier(
                result.getSignImage(), "drawable", context.getPackageName());

        if (resId != 0) {
            holder.signImage.setImageResource(resId);
        } else {
            // Fallback if image not found
            holder.signImage.setImageResource(R.drawable.ic_danger);
        }
    }

    @Override
    public int getItemCount() {
        return detectionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView signImage;
        TextView signName, dateText, confidenceText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            signImage = itemView.findViewById(R.id.signImageView);
            signName = itemView.findViewById(R.id.signNameTextView);
            dateText = itemView.findViewById(R.id.dateTextView);
            confidenceText = itemView.findViewById(R.id.confidenceTextView);
        }
    }
}