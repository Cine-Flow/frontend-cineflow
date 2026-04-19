package com.android.cineflow.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.cineflow.R;
import com.android.cineflow.data.common.uimodel.ContentCard;
import com.android.cineflow.data.common.uimodel.HomeSection;

import java.util.List;

public class HomeAdapter extends BaseAdapter {

    public interface OnItemClickListener {
        void onItemClick(HomeSection section, ContentCard card);
    }

    private final Context context;
    private List<HomeSection> sections;
    private OnItemClickListener clickListener;

    public HomeAdapter(Context context, List<HomeSection> sections) {
        this.context = context;
        this.sections = sections;
    }

    public void setSections(List<HomeSection> newSections) {
        this.sections = newSections;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public int getCount() {
        return sections != null ? sections.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return sections.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3; // BANNER, SPORT_ROW, SECTION_ROW
    }

    @Override
    public int getItemViewType(int position) {
        int type = sections.get(position).getViewType();
        if (type == HomeSection.TYPE_BANNER) return 0;
        if (type == HomeSection.TYPE_SPORT_ROW) return 1;
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        HomeSection section = sections.get(position);
        
        if (convertView == null) {
            LayoutInflater inf = LayoutInflater.from(context);
            if (type == 0) {
                convertView = inf.inflate(R.layout.item_home_banner, parent, false);
            } else {
                convertView = inf.inflate(R.layout.item_home_section_row, parent, false);
            }
        }

        if (type == 0) {
            bindBanner(convertView, section);
        } else {
            bindSectionRow(convertView, section);
        }

        return convertView;
    }

    private void bindBanner(View view, HomeSection section) {
        // Vì RecyclerView không dùng được với BaseAdapter một cách trực tiếp trong scroll ngang 
        // mà vẫn muốn "giữ hoạt động", tôi sẽ dùng một LinearLayout nằm ngang giả lập hoặc 
        // giữ RecyclerView bên trong nhưng dùng BaseAdapter cho nó.
        // Tuy nhiên, RecyclerView CẦN RecyclerView.Adapter. 
        // Để giải quyết, tôi sẽ dùng phương pháp inflate view thủ công vào một LinearLayout ngang 
        // để thay thế RecyclerView ngang.
        
        LinearLayout container = view.findViewById(R.id.rv_banner_container); 
        if (container == null) return;
        
        container.removeAllViews();
        ContentRowAdapter childAdapter = new ContentRowAdapter(context, section.getItems(), ContentCard.STYLE_BANNER, 
            card -> { if (clickListener != null) clickListener.onItemClick(section, card); });
        
        for (int i = 0; i < childAdapter.getCount(); i++) {
            container.addView(childAdapter.getView(i, null, container));
        }
    }

    private void bindSectionRow(View view, HomeSection section) {
        TextView tvTitle = view.findViewById(R.id.tv_section_title);
        TextView tvAction = view.findViewById(R.id.tv_section_action);
        LinearLayout container = view.findViewById(R.id.rv_section_row_container);

        tvTitle.setText(section.getTitle());
        if (section.getActionLabel() != null) {
            tvAction.setVisibility(View.VISIBLE);
            tvAction.setText(section.getActionLabel());
        } else {
            tvAction.setVisibility(View.GONE);
        }

        int cardStyle = section.getViewType() == HomeSection.TYPE_SPORT_ROW
                ? ContentCard.STYLE_LANDSCAPE
                : ContentCard.STYLE_PORTRAIT;

        if (container != null) {
            container.removeAllViews();
            ContentRowAdapter childAdapter = new ContentRowAdapter(context, section.getItems(), cardStyle, 
                card -> { if (clickListener != null) clickListener.onItemClick(section, card); });
            for (int i = 0; i < childAdapter.getCount(); i++) {
                container.addView(childAdapter.getView(i, null, container));
            }
        }
    }
}
