package com.amos.codebook3.MyUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class AppRestartHelper {

    /**
     * 重启应用
     *
     * @param context 上下文
     * @param delay   延迟时间（毫秒）
     */
    public static void restartApp(Context context, long delay) {
        // 获取当前应用的启动意图
        Intent restartIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (restartIntent != null) {
            restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        // 使用 Handler 延迟启动应用
        new Handler().postDelayed(() -> {
            if (restartIntent != null) {
                context.startActivity(restartIntent); // 启动应用
            }
            // 杀掉当前应用进程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }, delay);
    }
}
