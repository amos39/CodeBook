package com.amos.codebook3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
// 不再需要 Handler
import android.util.Log; // 如果需要调试可以保留
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.amos.codebook3.MyUtil.MyUtil;
import com.amos.codebook3.data.SPCollection;
import com.amos.codebook3.ui.home.HomeFragment; // 确保导入路径正确
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// 重新实现 Fragment 交互接口
public class MainActivity extends AppCompatActivity
        implements HomeFragment.OnFragmentInteractionListener {

    private static final String PREF_NAME = "settings";
    private NavController navController;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences preferences;
    private View rootLayout;
    private int bottomNavHeight;
    private HideBottomViewOnScrollBehavior<BottomNavigationView> bottomNavBehavior; // 用于动画

    // --- 手动控制的状态变量 ---
    private boolean isKeyboardVisible = false; // 键盘是否可见
    private boolean isHiddenByScroll = false; // 是否因滚动而隐藏
    // 定义一个滚动阈值，避免微小滚动触发隐藏/显示 (像素值，根据需要调整)
    private static final int SCROLL_THRESHOLD = 2;
    // --- ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 SharedPreferences
        preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 初始化 databaseInfo
        initDataBase();

        // 初始化视图
        bottomNavigation = findViewById(R.id.bottom_navigation);
        rootLayout = findViewById(android.R.id.content);

        // 获取 BottomNavigationView 的 Behavior 实例 (即使不用它自动滚动，也可以用它的动画方法)
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) bottomNavigation.getLayoutParams();
        // 确保 XML 中仍然有 behavior 或者在这里手动添加，如果需要 slideUp/slideDown 动画
        // 如果你把 BottomNavigationView 的 app:layout_behavior 也移除了，这里会是 null
        if (params.getBehavior() instanceof HideBottomViewOnScrollBehavior) {
            bottomNavBehavior = (HideBottomViewOnScrollBehavior<BottomNavigationView>) params.getBehavior();
        } else {
            // 如果找不到 Behavior 但我们又想用它的方法，需要处理
            Log.w("MainActivity", "HideBottomViewOnScrollBehavior not found on BottomNavigationView. Using View.animate() for fallback.");
            // 你可以考虑在这里手动创建一个并设置给 params，或者就接受使用备用动画
        }


        // 设置导航
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigation, navController);
        } else {
            Log.e("MainActivity", "NavHostFragment not found!");
        }

        // 设置键盘显示状态监听器
        setupKeyboardVisibilityListener();

        // 获取底部导航栏高度 (在布局完成后)
        bottomNavigation.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // 确保获取到有效高度
                        if (bottomNavigation.getHeight() > 0 && bottomNavHeight == 0) {
                            bottomNavHeight = bottomNavigation.getHeight();
                            // 根据初始状态调整 Padding (通常初始是可见的)
                            adjustFragmentPadding(bottomNavHeight);
                            // 移除监听器，避免重复执行
                            bottomNavigation.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            // 根据初始状态更新一次导航栏可见性（虽然初始一般是可见的）
                            updateNavigationBarVisibility();
                        }
                    }
                });
    }

    /**
     * 设置键盘显示状态监听器
     */
    private void setupKeyboardVisibilityListener() {
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            // r will be populated with the coordinates of your view area that are visible.
            rootLayout.getWindowVisibleDisplayFrame(r);

            int screenHeight = rootLayout.getRootView().getHeight();
            // r.bottom is the position above soft keypad or device button.
            // heightDiff = screen height - display area height
            int heightDiff = screenHeight - r.bottom; // 使用 r.bottom 更准确

            // 判断键盘是否显示（阈值为 100dp 转换为像素）
            boolean keyboardCurrentlyVisible = heightDiff > getResources().getDisplayMetrics().density * 100;

            // 仅在状态改变时更新
            if (keyboardCurrentlyVisible != isKeyboardVisible) {
                isKeyboardVisible = keyboardCurrentlyVisible;
                // Log.d("MainActivity", "Keyboard visible: " + isKeyboardVisible);
                updateNavigationBarVisibility(); // 更新导航栏状态
            }
        });
    }

    // --- 实现 HomeFragment.OnFragmentInteractionListener 接口方法 ---
    @Override
    public void onScrollEvent(int scrollY) {
        // 根据滚动Y值和阈值判断是否应该隐藏
        boolean shouldHideDueToScroll = scrollY > SCROLL_THRESHOLD;

        // 仅在状态改变时更新
        if (shouldHideDueToScroll != isHiddenByScroll) {
            isHiddenByScroll = shouldHideDueToScroll;
            // Log.d("MainActivity", "Hidden by scroll: " + isHiddenByScroll + " (scrollY: " + scrollY + ")");
            updateNavigationBarVisibility(); // 更新导航栏状态
        }
        // 备注：你之前的代码是 scrollY > 0 和 scrollY <= 0。
        // 使用阈值可以提供一个小的“死区”，防止在顶部附近微小滚动时频繁切换。
        // 如果你想用滚动的 delta (dy) 来判断方向会更精确，需要 Fragment 传递 dy 值。
    }
    // --- ---

    /**
     * 根据键盘和滚动状态，统一更新导航栏的可见性
     */
    private synchronized void updateNavigationBarVisibility() {
        // 确保 bottomNavHeight 已经初始化
        if (bottomNavHeight == 0 && bottomNavigation.getHeight() > 0) {
            bottomNavHeight = bottomNavigation.getHeight();
        }
        // 如果高度未知，则不执行更新
        if (bottomNavHeight == 0 && !isKeyboardVisible) {
            // 如果是因为键盘弹出导致高度为0，还是需要隐藏
            // 但通常在键盘弹出前高度应该已经确定
            // 简单的处理是如果高度未知且非键盘场景，则不更新
            // return; // 酌情保留或移除此行检查
        }


        // 决定是否应该隐藏：键盘弹出 或 因滚动而隐藏
        boolean shouldBeHidden = isKeyboardVisible || isHiddenByScroll;

        // 获取当前实际的可见状态
        boolean isCurrentlyVisible = bottomNavigation.getVisibility() == View.VISIBLE;

        // Log.d("MainActivity", "Update check: shouldBeHidden=" + shouldBeHidden + ", isCurrentlyVisible=" + isCurrentlyVisible);

        // 如果应该隐藏，但当前是可见的 -> 执行隐藏
        if (shouldBeHidden && isCurrentlyVisible) {
            // Log.d("MainActivity", "Hiding Navigation Bar (setting GONE)");
            hideNavigationBar(); // 调用 GONE 版本的 hide
        }
        // 如果不应该隐藏，但当前是隐藏状态 (GONE) -> 执行显示
        else if (!shouldBeHidden && !isCurrentlyVisible) {
            // Log.d("MainActivity", "Showing Navigation Bar (setting VISIBLE)");
            showNavigationBar(); // 调用 VISIBLE 版本的 show
        }
    }


    /**
     * 隐藏导航栏 (设置为 GONE)
     */
    private void hideNavigationBar() {
        if (bottomNavigation.getVisibility() == View.VISIBLE) { // 避免重复设置
            bottomNavigation.setVisibility(View.GONE);
            adjustFragmentPadding(0); // 隐藏时移除 Fragment 底部 padding
            // Log.d("MainActivity", "NavBar set to GONE");
        }
    }

    /**
     * 显示导航栏 (设置为 VISIBLE)
     */
    private void showNavigationBar() {
        if (bottomNavigation.getVisibility() == View.GONE) { // 避免重复设置
            bottomNavigation.setVisibility(View.VISIBLE);
            // 确保高度已获取，否则 adjustFragmentPadding 会使用 0
            if (bottomNavHeight == 0 && bottomNavigation.getHeight() > 0) {
                bottomNavHeight = bottomNavigation.getHeight();
            }
            adjustFragmentPadding(bottomNavHeight); // 显示时恢复 Fragment 底部 padding
            // Log.d("MainActivity", "NavBar set to VISIBLE");
        }
    }


    /**
     * 调整 Fragment 容器的底部内边距
     * @param paddingBottom 新的底部内边距
     */
    private void adjustFragmentPadding(int paddingBottom) {
        View fragmentContainer = findViewById(R.id.nav_host_fragment);
        if (fragmentContainer != null) {
            // 确保 padding 不为负
            final int finalPaddingBottom = Math.max(0, paddingBottom);
            fragmentContainer.setPadding(
                    fragmentContainer.getPaddingLeft(),
                    fragmentContainer.getPaddingTop(),
                    fragmentContainer.getPaddingRight(),
                    finalPaddingBottom
            );
            // Log.d("MainActivity", "Adjusted paddingBottom to: " + finalPaddingBottom);
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * 处理向上导航事件
     */
    @Override
    public boolean onSupportNavigateUp() {
        // 确保 navController 已初始化
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * 初始化数据库相关的 SharedPreferences
     */
    private void initDataBase() {
        SharedPreferences preferences = getSharedPreferences(SPCollection.FN_DATABASE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // 使用 containsKey 检查更可靠
        if (!preferences.contains(SPCollection.KN_DB_COUNT)) {
            editor.putInt(SPCollection.KN_DB_COUNT, 65536);
        }
        if (!preferences.contains(SPCollection.KN_DB_NAME)) {
            int dbCount = preferences.getInt(SPCollection.KN_DB_COUNT, 65536); // 获取 count 值
            editor.putString(SPCollection.KN_DB_NAME, "codebook" + dbCount + ".db");
        }
        if (!preferences.contains(SPCollection.KN_DB_VERSION)) {
            editor.putInt(SPCollection.KN_DB_VERSION, 1);
        }
        // 假设之前用 -1 表示不存在
        if (!preferences.contains(SPCollection.KN_DB_KEY_IS_SAVED)) {
            editor.putInt(SPCollection.KN_DB_KEY_IS_SAVED, 1); // 1 表示已保存 (true)
            editor.putString(SPCollection.KN_DB_KEY, MyUtil.generateRandomKey(16)); // 生成随机密钥
        }

        editor.apply(); // 异步应用更改
    }
}