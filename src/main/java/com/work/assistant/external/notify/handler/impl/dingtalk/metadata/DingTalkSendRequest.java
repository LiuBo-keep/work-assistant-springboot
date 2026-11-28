package com.work.assistant.external.notify.handler.impl.dingtalk.metadata;

import com.work.assistant.external.notify.handler.impl.dingtalk.enums.DingTalkMsgType;
import java.util.List;
import lombok.Data;

/**
 * 钉钉机器人发送请求体。
 *
 * @author work-assistant
 */
@Data
public class DingTalkSendRequest {

  private DingTalkMsgType msgtype;

  private Markdown markdown;

  private Text text;

  private AtWho at;

  @Data
  public static class Markdown {
    private String title;
    private String text;
  }

  @Data
  public static class Text {
    private String content;
  }

  @Data
  public static class AtWho {
    private List<String> atMobiles;
    private Boolean isAtAll;
  }
}
