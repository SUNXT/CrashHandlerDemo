package com.example.sunxuedian.crashhandlerdemo;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by sunxuedian on 2017/7/28.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "5b30fc8414d43b08361f91f8353be5a6");
        final CrashHandler crashHandler = CrashHandler.getInstance(Environment.getExternalStorageDirectory().getPath() + File.separator + "CrashHandlerDemo");
        crashHandler.init(this);
        crashHandler.setCrashUploader(new CrashHandler.CrashUploader() {
            @Override
            public void uploadCrashInfo(HashMap<String, String> crashInfoMap) {
                CrashInfo crashInfo = new CrashInfo();
                crashInfo.setAppName("CrashHandler");
                crashInfo.setBuildInfo(crashInfoMap.get(CrashInfo.BUILD_INFO));
                crashInfo.setExceptionInfo(crashInfoMap.get(CrashInfo.EXCEPTION_INFO));
                crashInfo.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null){
                            Log.i("Bomb save", "上传报告成功！");
                        }else {
                            Log.e("Bomb save", "上传失败！");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
