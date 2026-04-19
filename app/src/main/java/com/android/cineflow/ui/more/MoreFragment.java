package com.android.cineflow.ui.more;

import android.view.View;
import android.widget.ListView;

import com.android.cineflow.R;
import com.android.cineflow.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoreFragment extends BaseFragment {

    private MoreAdapter moreAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_more;
    }

    @Override
    protected void initViews(View view) {
        ListView lvMore = view.findViewById(R.id.lv_more);
        moreAdapter = new MoreAdapter(requireContext(), new ArrayList<>());
        lvMore.setAdapter(moreAdapter);
    }

    @Override
    protected void initData() {
        List<MoreSection> data = new ArrayList<>();
        
        // 1. Header
        data.add(new MoreSection(MoreSection.TYPE_HEADER_LOGIN, null, null));

        // 2. Download
        data.add(new MoreSection(MoreSection.TYPE_DOWNLOAD_BANNER, null, null));

        // 3. Action Grid
        data.add(new MoreSection(MoreSection.TYPE_ACTION_GRID, null, Arrays.asList(
                new MoreSection.MoreItem("Thư viện", R.drawable.ic_library, null),
                new MoreSection.MoreItem("Đổi mã quà tặng", R.drawable.ic_gift, null),
                new MoreSection.MoreItem("Sản phẩm yêu thích", R.drawable.ic_heart, null)
        )));

        // 4. Apps
        data.add(new MoreSection(MoreSection.TYPE_APPS_SECTION, "Ứng dụng", Arrays.asList(
                new MoreSection.MoreItem("VietjetAir", R.drawable.vietjetair_logo, null),
                new MoreSection.MoreItem("Vietnam Airlines", R.drawable.vn_airlines, null),
                new MoreSection.MoreItem("Nạp tiền điện thoại", R.drawable.card, null),
                new MoreSection.MoreItem("Khách hàng thân thiết", R.drawable.customer_logo, null)
        )));

        // 5. Help
        data.add(new MoreSection(MoreSection.TYPE_HELP_ITEM, "Trợ giúp", Arrays.asList(
                new MoreSection.MoreItem("Thông tin liên hệ", 0, null)
        )));

        moreAdapter.setData(data);
    }
}
