package com.blankj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.lang.reflect.Method;

/**
 * 1.功能相当于,点击了 应用程序信息 里面的 清楚缓存按钮，而非 清除数据
 *
 * 2.功能相当于,删除了/data/data/packageName/cache 文件夹里面所有的东西
 *
 * 3.需要权限 <uses-permission android:name="android.permission.CLEAR_APP_CACHE" />
 */
public class CacheClearHelper {

    @SuppressLint("PrivateApi")
    public static void clearCache(Context context) {

        try {
            Class IPackageDataObserver= Class.forName("android.content.pm.IPackageDataObserver");
            Class Stub= Class.forName("android.content.pm.IPackageDataObserver$Stub");
//            InvocationHandler mHandler = new InvocationHandler(){
//
//                @Override
//                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                    System.out.println("doing callback...");
//                    return null;
//                }
//            };
//            Object mObj = Proxy.newProxyInstance(IPackageDataObserver.getClassLoader(), new Class[] { Stub },mHandler);

            PackageManager packageManager = context.getPackageManager();
            Method localMethod = packageManager.getClass().getMethod("freeStorageAndNotify", Long.TYPE, IPackageDataObserver);
            Long localLong = getEnvironmentSize() - 1L;
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = localLong;
            LogUtils.v("构造函数的数量="+Stub.getConstructors().length);
            localMethod.invoke(packageManager, localLong, IPackageDataObserver);
            LogUtils.v("清除成功！！！");

            StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long getEnvironmentSize() {
        File localFile = Environment.getDataDirectory();
        long l1;
        if (localFile == null)
            l1 = 0L;
        while (true) {
            String str = localFile.getPath();
            StatFs localStatFs = new StatFs(str);
            long l2 = localStatFs.getBlockSize();
            l1 = localStatFs.getBlockCount() * l2;
            return l1;
        }

    }
}