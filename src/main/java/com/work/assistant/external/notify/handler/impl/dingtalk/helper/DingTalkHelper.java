package com.work.assistant.external.notify.handler.impl.dingtalk.helper;

/**
 * 钉钉 Webhook 地址解析助手。
 *
 * <p>从钉钉机器人 Webhook 地址中解析出域名与访问令牌（access_token）。
 *
 * @author work-assistant
 */
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