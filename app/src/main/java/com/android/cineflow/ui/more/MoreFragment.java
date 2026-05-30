package com.android.cineflow.ui.more;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import androidx.viewpager2.widget.ViewPager2;

import com.android.cineflow.MainActivity;
import com.android.cineflow.R;
import com.android.cineflow.data.auth.AuthManager;
import com.android.cineflow.ui.admin.AdminDashboardActivity;
import com.android.cineflow.ui.auth.LoginActivity;
import com.android.cineflow.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoreFragment extends BaseFragment {

    private MoreAdapter moreAdapter;
    private static final int REQUEST_LOGIN = 1001;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_more;
    }

    @Override
    protected void initViews(View view) {
        ListView lvMore = view.findViewById(R.id.lv_more);
        moreAdapter = new MoreAdapter(requireContext(), new ArrayList<>());
        lvMore.setAdapter(moreAdapter);

        lvMore.setOnItemClickListener((parent, v, position, id) -> {
            MoreSection section = (MoreSection) moreAdapter.getItem(position);
            if (section == null) return;

            switch (section.getType()) {
                case MoreSection.TYPE_ADMIN_ENTRY:
                    startActivity(new Intent(requireContext(), AdminDashboardActivity.class));
                    break;
                case MoreSection.TYPE_LOGOUT:
                    handleSignButton();
                    break;
            }
        });
    }

    @Override
    protected void initData() {
        buildSections();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN && resultCode == getActivity().RESULT_OK) {
            buildSections();
        }
    }

    private void buildSections() {
        List<MoreSection> data = new ArrayList<>();
        AuthManager authManager = AuthManager.getInstance();
        boolean isLoggedIn = authManager != null && authManager.isLoggedIn();

        // 0. Admin Panel (only if user is admin)
        if (isLoggedIn && authManager.isAdmin()) {
            data.add(new MoreSection(MoreSection.TYPE_ADMIN_ENTRY, null, null));
        }

        // 1. Header (shows "Đăng nhập" or username)
        String headerTitle = isLoggedIn ? authManager.getUsername() : null;
        data.add(new MoreSection(MoreSection.TYPE_HEADER_LOGIN, headerTitle, null));

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

        // 6. Sign In / Sign Out button (always visible)
        boolean isLogged = authManager != null && authManager.isLoggedIn();
        data.add(new MoreSection(MoreSection.TYPE_LOGOUT, isLogged ? "Sign Out" : "Sign In", null));

        moreAdapter.setData(data);
    }

    private void handleSignButton() {
        AuthManager auth = AuthManager.getInstance();
        boolean isLoggedIn = auth != null && auth.isLoggedIn();

        if (isLoggedIn) {
            performSignOut();
        } else {
            startActivityForResult(
                    new Intent(requireContext(), LoginActivity.class),
                    REQUEST_LOGIN);
        }
    }

    private void performSignOut() {
        AuthManager auth = AuthManager.getInstance();
        if (auth != null) auth.clearSession();
        buildSections();

        // Navigate to Home tab
        if (getActivity() instanceof MainActivity) {
            ViewPager2 viewPager = ((MainActivity) getActivity()).findViewById(R.id.view_pager);
            if (viewPager != null) {
                viewPager.setCurrentItem(0, false);
            }
        }
    }
}
