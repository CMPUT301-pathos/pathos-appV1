package com.example.eventlottery.ui;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventlottery.R;
import com.example.eventlottery.admin.AdminBrowseImages;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hasratsinghchauhan
 * P.S do not change the contents of the file w/o informing/collaboratng (with)  the author.
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {

    private List<AdminBrowseImages.ImageData> images = new ArrayList<>();
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(AdminBrowseImages.ImageData image);
        void onDeleteClick(AdminBrowseImages.ImageData image);
    }

    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setImages(List<AdminBrowseImages.ImageData> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        AdminBrowseImages.ImageData image = images.get(position);
        holder.bind(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private ImageView btnDelete;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            btnDelete = itemView.findViewById(R.id.btnDeleteImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(images.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(images.get(getAdapterPosition()));
                }
            });
        }

       /* void bind(AdminBrowseImages.ImageData image) {
            Glide.with(itemView.getContext())
                    .load(image.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivImage);
        }*/
       void bind(AdminBrowseImages.ImageData image) {
           Glide.with(itemView.getContext())
                   .load(image.getImageUrl())
                   .placeholder(android.R.drawable.ic_menu_gallery)
                   .into(ivImage);

           // Optional: Show image type
           TextView tvImageType = itemView.findViewById(R.id.tvImageType);
           if (tvImageType != null) {
               tvImageType.setText(image.getType());
               tvImageType.setVisibility(View.VISIBLE);
           }
       }
    }
}

