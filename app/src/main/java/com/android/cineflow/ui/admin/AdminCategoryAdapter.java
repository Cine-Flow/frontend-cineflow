package com.android.cineflow.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.cineflow.R;
import com.android.cineflow.data.network.dto.AdminCategoryDto;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter
        extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryActionListener {
        void onEditCategory(AdminCategoryDto category);
        void onDeleteCategory(AdminCategoryDto category);
    }

    private List<AdminCategoryDto> categories = new ArrayList<>();
    private final OnCategoryActionListener listener;

    public AdminCategoryAdapter(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<AdminCategoryDto> list) {
        this.categories = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_category, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvId;
        private final TextView tvName;
        private final TextView tvDescription;
        private final TextView tvFilmCount;
        private final ImageView btnEdit;
        private final ImageView btnDelete;

        CategoryViewHolder(View v) {
            super(v);
            tvId = v.findViewById(R.id.tv_category_id);
            tvName = v.findViewById(R.id.tv_category_name);
            tvDescription = v.findViewById(R.id.tv_category_description);
            tvFilmCount = v.findViewById(R.id.tv_film_count);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }

        void bind(AdminCategoryDto category) {
            Long filmCount = category.getFilmCount();
            tvId.setText("#" + (category.getId() == null ? "-" : category.getId()));
            tvName.setText(category.getName());
            tvDescription.setText(category.getDescription() != null && !category.getDescription().isEmpty()
                    ? category.getDescription()
                    : itemView.getContext().getString(R.string.admin_no_description));

            tvFilmCount.setText(filmCount + " " + (filmCount == 1 ? "film" : "films"));
            int countColor = filmCount == 0 ? R.color.text_tertiary : R.color.brand_primary;
            GradientDrawable countBg = new GradientDrawable();
            countBg.setCornerRadius(24f);
            countBg.setStroke(1, itemView.getContext().getColor(countColor));
            countBg.setColor(itemView.getContext().getColor(R.color.surface_secondary));
            tvFilmCount.setBackground(countBg);
            tvFilmCount.setTextColor(itemView.getContext().getColor(countColor));

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditCategory(category);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteCategory(category);
            });
        }
    }
}
