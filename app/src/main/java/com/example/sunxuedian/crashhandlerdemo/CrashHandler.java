package com.example.sunxuedian.crashhandlerdemo;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 单例模式
 * 捕抓APP奔溃异常
 * Created by sunxuedian on 2017/7/28.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler{

    private static CrashHandler mInstance;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;
    private String mDirPath;
    private HashMap<String, String> mCrashInfo = new HashMap<>();//保存捕抓到的信息
    private CrashUploader mCrashUploader;

    private CrashHandler(String dirPath){
        mDirPath = dirPath;
        File mDirectory = new File(mDirPath);
        if (!mDirectory.exists()){
            mDirectory.mkdirs();
        }
    }

    public static CrashHandler getInstance(String dirPath){
        if (mInstance == null){
            synchronized (CrashHandler.class){
                if (mInstance == null){
                    mInstance = new CrashHandler(dirPath);
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        //保存默认的异常处理handler
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //将该类设置为系统的异常处理handler
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void setCrashUploader(CrashUploader crashUploader){
        this.mCrashUploader = crashUploader;
    }

    /**
     * 处理异常信息
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!catchException(throwable) && mDefaultHandler != null){
            mDefaultHandler.uncaughtException(thread, throwable);
        }else {
            killProcess();
        }
    }

    /**
     * 处理异常
     * @param throwable
     * @return
     */
    private boolean catchException(Throwable throwable){
        if (throwable == null){
            return false;
        }
        mCrashInfo.put(CrashInfo.BUILD_INFO, collectBuildInfo());
        mCrashInfo.put(CrashInfo.EXCEPTION_INFO, collectExceptionInfos(throwable));
        saveCrashInfo2File();
        if (mCrashUploader != null){
            mCrashUploader.uploadCrashInfo(mCrashInfo);
        }
        return true;
    }

    /**
     * 从系统属性中提取设备硬件和版本信息
     * @return
     */
    private String collectBuildInfo(){
        StringBuffer buffer = new StringBuffer();
        //利用发射原理
        Field[] mField = Build.class.getDeclaredFields();
        // 迭代Build的字段key-value 此处的信息主要是为了在服务器端手机各种版本手机报错的原因
        for (Field field: mField){
            try {
                field.setAccessible(true);
                buffer.append(field.getName());
                buffer.append(": ");
                buffer.append(field.get("").toString());
                buffer.append('\t');
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }

    /**
     * 获取捕获异常的信息
     *
     * @param ex
     */
    private String collectExceptionInfos(Throwable ex) {
        Writer mWriter = new StringWriter();
        PrintWriter mPrintWriter = new PrintWriter(mWriter);
        ex.printStackTrace(mPrintWriter);
        ex.printStackTrace();
        Throwable mThrowable = ex.getCause();
        // 迭代栈队列把所有的异常信息写入writer中
        while (mThrowable != null) {
            mThrowable.printStackTrace(mPrintWriter);
            // 换行 每个个异常栈之间换行
            mPrintWriter.append("\r\n");
            mThrowable = mThrowable.getCause();
        }
        // 记得关闭
        mPrintWriter.close();
        return mWriter.toString();
    }

    private void saveCrashInfo2File(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("手机信息：\n");
        buffer.append(mCrashInfo.get(CrashInfo.BUILD_INFO) + '\n');
        buffer.append("异常信息：\n");
        buffer.append(mCrashInfo.get(CrashInfo.EXCEPTION_INFO));
        Log.i(getClass().getSimpleName(), buffer.toString());

        Date date = new Date(System.currentTimeMillis());
        String time = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss").format(date);
        String fileName = "mCrashLog-" + time + ".txt";
        File mDirectory = new File(mDirPath);
        if (!mDirectory.exists()){
            mDirectory.mkdirs();
        }
        try {
            FileOutputStream fo = new FileOutputStream(mDirPath + File.separator + fileName);
            fo.write(buffer.toString().getBytes());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void killProcess(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序崩溃了！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Log.e("CrashHandler:", "CrashHandler.InterruptedException--->" + ex.toString());
        }
        Process.killProcess(Process.myPid());
        System.exit(1);
    }


    /**
     * 定义的接口，实现异常上传
     */
    interface CrashUploader{
        void uploadCrashInfo(HashMap<String, String> crashInfo);
    }
}
