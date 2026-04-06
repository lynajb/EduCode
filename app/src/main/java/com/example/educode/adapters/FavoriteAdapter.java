package com.example.educode.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.educode.R;
import com.example.educode.models.Sign;
import com.example.educode.ui.learn.SignDetailActivity;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavViewHolder> {

    private Context context;
    private List<Sign> favList;
    private String username;
    private OnRemoveListener removeListener;

    public interface OnRemoveListener {
        void onRemove(int signId, int position);
    }

    public FavoriteAdapter(Context context, List<Sign> favList, String username, OnRemoveListener listener) {
        this.context = context;
        this.favList = favList;
        this.username = username;
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new FavViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
        Sign sign = favList.get(position);
        holder.tvName.setText(sign.getName());

        int resId = context.getResources().getIdentifier(sign.getImageName(), "drawable", context.getPackageName());
        if (resId == 0) resId = R.mipmap.ic_launcher;
        holder.imgSign.setImageResource(resId);

        // Clic sur l'item -> Détails
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SignDetailActivity.class);
            intent.putExtra("SIGN_ID", sign.getId());
            intent.putExtra("USERNAME", username);
            context.startActivity(intent);
        });

        // Clic sur poubelle -> Supprimer
        holder.btnDelete.setOnClickListener(v -> {
            removeListener.onRemove(sign.getId(), position);
        });
    }

    @Override
    public int getItemCount() { return favList.size(); }

    public static class FavViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgSign;
        ImageButton btnDelete;

        public FavViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFavName);
            imgSign = itemView.findViewById(R.id.imgFavIcon);
            btnDelete = itemView.findViewById(R.id.btnFavDelete);
        }
    }
}