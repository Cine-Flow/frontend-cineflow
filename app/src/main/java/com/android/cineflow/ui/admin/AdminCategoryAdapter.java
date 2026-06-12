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

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter
        extends RecyclerView.Adapter<AdminCategoryAdapter.CategoryViewHolder> {

    /**
     * Mirrors the backend {@code categories} row. {@code filmCount} is derived from the
     * {@code film_categories} junction on the server, not stored on the row.
     */
    public static class MockCategory {
        public Integer id;            // SERIAL — null for unsaved
        public String name;
        public String description;    // nullable
        public int filmCount;         // derived; read-only

        public MockCategory(Integer id, String name, String description, int filmCount) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.filmCount = filmCount;
        }
    }

    public interface OnCategoryActionListener {
        void onEditCategory(MockCategory category);
        void onDeleteCategory(MockCategory category);
    }

    private List<MockCategory> categories = new ArrayList<>();
    private final OnCategoryActionListener listener;

    public AdminCategoryAdapter(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<MockCategory> list) {
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
    public int getItemCount() { return categories.size(); }

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

        void bind(MockCategory c) {
            tvId.setText("#" + (c.id == null ? "—" : c.id));
            tvName.setText(c.name);

            if (c.description != null && !c.description.isEmpty()) {
                tvDescription.setText(c.description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setText("No description");
                tvDescription.setVisibility(View.VISIBLE);
            }

            tvFilmCount.setText(c.filmCount + " " + (c.filmCount == 1 ? "film" : "films"));
            int countColor = c.filmCount == 0
                    ? R.color.text_tertiary : R.color.brand_primary;
            GradientDrawable countBg = new GradientDrawable();
            countBg.setCornerRadius(24f);
            countBg.setStroke(1, itemView.getContext().getColor(countColor));
            countBg.setColor(itemView.getContext().getColor(R.color.surface_secondary));
            tvFilmCount.setBackground(countBg);
            tvFilmCount.setTextColor(itemView.getContext().getColor(countColor));

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditCategory(c);
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteCategory(c);
            });
        }
    }
}
