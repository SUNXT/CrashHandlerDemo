package com.example.sunxuedian.crashhandlerdemo;

import cn.bmob.v3.BmobObject;

/**
 * Created by sunxuedian on 2017/7/28.
 */

public class CrashInfo extends BmobObject {

    public static final String BUILD_INFO = "buildInfo";
    public static final String EXCEPTION_INFO = "exceptionInfo";

    private String appName;
    private String buildInfo;
    private String exceptionInfo;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getBuildInfo() {
        return buildInfo;
    }

    public void setBuildInfo(String buildInfo) {
        this.buildInfo = buildInfo;
    }

    public String getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }
}
