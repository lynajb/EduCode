package com.example.educode.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.educode.R;
import com.example.educode.data.DatabaseHelper; // <--- Import DatabaseHelper
import com.example.educode.models.Sign;
import com.example.educode.ui.learn.SignDetailActivity;

import java.util.List;

public class SignAdapter extends RecyclerView.Adapter<SignAdapter.SignViewHolder> {

    private Context context;
    private List<Sign> signList;
    private String currentUsername;

    public SignAdapter(Context context, List<Sign> signList, String username) {
        this.context = context;
        this.signList = signList;
        this.currentUsername = username;
    }

    @NonNull
    @Override
    public SignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sign, parent, false);
        return new SignViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull SignViewHolder holder, int position) {
        Sign sign = signList.get(position);
        holder.tvName.setText(sign.getName());

        int resId = context.getResources().getIdentifier(sign.getImageName(), "drawable", context.getPackageName());
        if (resId == 0) resId = R.mipmap.ic_launcher;
        holder.imgSign.setImageResource(resId);

        holder.itemView.setOnClickListener(v -> {
            // 1. INCREMENT THE MANUAL COUNTER
            DatabaseHelper db = new DatabaseHelper(context);
            db.incrementSignsLearned(currentUsername);
            db.close(); // Close to be safe

            // 2. OPEN DETAIL ACTIVITY
            Intent intent = new Intent(context, SignDetailActivity.class);
            intent.putExtra("SIGN_ID", sign.getId());
            intent.putExtra("USERNAME", currentUsername);
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return signList.size();
    }

    public static class SignViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgSign;

        public SignViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSignName);
            imgSign = itemView.findViewById(R.id.imgSign);
        }
    }
}