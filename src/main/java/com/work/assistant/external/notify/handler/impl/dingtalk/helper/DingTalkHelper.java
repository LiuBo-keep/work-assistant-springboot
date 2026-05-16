package com.work.assistant.external.notify.handler.impl.dingtalk.helper;

public class DingTalkHelper {


    public static String getDingTalkDomain(String dingTalkUrl) {
        int firstSlashIndex =
                dingTalkUrl.indexOf("/", dingTalkUrl.indexOf("//") + 2);
        return dingTalkUrl.substring(0, firstSlashIndex + 1);
    }


    public static String getDingTalkToken(String dingTalkUrl) {
        String tokenKey = "access_token=";

        int tokenIndex =
                dingTalkUrl.indexOf(tokenKey);

        return dingTalkUrl.substring(tokenIndex + tokenKey.length());
    }
}