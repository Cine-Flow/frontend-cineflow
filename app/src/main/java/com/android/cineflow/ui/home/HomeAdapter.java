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

import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

public class HomeAdapter extends BaseAdapter {

    public interface OnItemClickListener {
        void onItemClick(HomeSection section, ContentCard card);
    }

    /** Callback for the "Xem thêm" action label tap. */
    public interface OnSeeMoreClickListener {
        void onSeeMoreClick(HomeSection section);
    }

    private final Context context;
    private List<HomeSection> sections;
    private OnItemClickListener clickListener;
    private OnSeeMoreClickListener seeMoreListener;

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

    public void setOnSeeMoreClickListener(OnSeeMoreClickListener listener) {
        this.seeMoreListener = listener;
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
                convertView = inf.inflate(R.layout.item_home_banner_vp, parent, false);
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
        ViewPager2 viewPager = view.findViewById(R.id.vp_banner); 
        if (viewPager == null) return;
        
        BannerAdapter adapter = new BannerAdapter(context, section.getItems(), 
            card -> { if (clickListener != null) clickListener.onItemClick(section, card); });
        
        viewPager.setAdapter(adapter);
        
        // Auto-scroll logic
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (adapter.getItemCount() > 0) {
                    int nextItem = (viewPager.getCurrentItem() + 1) % adapter.getItemCount();
                    viewPager.setCurrentItem(nextItem, true);
                    handler.postDelayed(this, 3000); // 3 seconds
                }
            }
        };
        
        // Remove callbacks to avoid memory leaks or multiple runnables
        viewPager.setTag(handler); 
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(runnable, 3000);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacksAndMessages(null);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(runnable, 3000);
                }
            }
        });
    }
    private void bindSectionRow(View view, HomeSection section) {
        TextView tvTitle = view.findViewById(R.id.tv_section_title);
        TextView tvAction = view.findViewById(R.id.tv_section_action);
        LinearLayout container = view.findViewById(R.id.rv_section_row_container);

        if (section.getTitleResId() != 0) {
            tvTitle.setText(context.getString(section.getTitleResId()));
        } else {
            tvTitle.setText("");
        }

        if (section.getActionLabelResId() != 0) {
            tvAction.setVisibility(View.VISIBLE);
            tvAction.setText(context.getString(section.getActionLabelResId()));
            // Handle "Xem thêm" click
            tvAction.setOnClickListener(v -> {
                if (seeMoreListener != null) {
                    seeMoreListener.onSeeMoreClick(section);
                }
            });
        } else {
            tvAction.setVisibility(View.GONE);
            tvAction.setOnClickListener(null);
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
