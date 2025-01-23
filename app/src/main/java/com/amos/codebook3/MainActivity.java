package com.amos.codebook3;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.amos.codebook3.MyUtil.MyUtil;
import com.amos.codebook3.data.SPCollection;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 主界面
 */
public class MainActivity extends AppCompatActivity {
    private static final String PREF_NAME = "settings";
    private NavController navController;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences preferences;
    private View rootLayout;

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

        // 设置导航
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigation, navController);
        }

        // 设置键盘显示状态监听器
        setupKeyboardVisibilityListener();
    }

    /**
     * 设置键盘显示状态监听器
     */
    private void setupKeyboardVisibilityListener() {
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootLayout.getWindowVisibleDisplayFrame(r);

            int screenHeight = rootLayout.getRootView().getHeight();
            int visibleHeight = r.height();
            int heightDiff = screenHeight - visibleHeight;

            // 判断键盘是否显示（阈值为 100dp 转换为像素）
            boolean isKeyboardVisible = heightDiff > getResources().getDisplayMetrics().density * 100;

            if (isKeyboardVisible) {
                hideNavigationBar();
            } else {
                showNavigationBar();
            }
        });
    }

    /**
     * 隐藏导航栏
     */
    private void hideNavigationBar() {
        if (bottomNavigation != null && bottomNavigation.getVisibility() == View.VISIBLE) {
            bottomNavigation.setVisibility(View.GONE);
        }
    }

    /**
     * 显示导航栏
     */
    private void showNavigationBar() {
        if (bottomNavigation != null && bottomNavigation.getVisibility() == View.GONE) {
            bottomNavigation.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    /**
     * 初始化数据库相关的 SharedPreferences
     */
    private void initDataBase() {
        SharedPreferences preferences = getSharedPreferences(SPCollection.FN_DATABASE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        if (preferences.getInt(SPCollection.KN_DB_COUNT, -1) == -1) {
            editor.putInt(SPCollection.KN_DB_COUNT, 65536);
        }
        if (preferences.getString(SPCollection.KN_DB_NAME, null) == null) {
            int dbCount = preferences.getInt(SPCollection.KN_DB_COUNT, 65536);
            editor.putString(SPCollection.KN_DB_NAME, "codebook" + dbCount + ".db");
        }
        if (preferences.getInt(SPCollection.KN_DB_VERSION, -1) == -1) {
            editor.putInt(SPCollection.KN_DB_VERSION, 1);
        }
        // 首次启动无密钥，自动生成随机密钥
        if (preferences.getInt(SPCollection.KN_DB_KEY_IS_SAVED, -1) == -1) {
            editor.putInt(SPCollection.KN_DB_KEY_IS_SAVED, 1);
            editor.putString(SPCollection.KN_DB_KEY, MyUtil.generateRandomKey(16));
        }

        editor.apply();
    }
}
