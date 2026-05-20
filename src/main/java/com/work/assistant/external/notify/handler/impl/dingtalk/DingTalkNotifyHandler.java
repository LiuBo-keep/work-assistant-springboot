package com.work.assistant.external.notify.handler.impl.dingtalk;

import com.work.assistant.config.retrofit.RetrofitClientProvider;
import com.work.assistant.config.retrofit.RetrofitClientResponseHelper;
import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.handler.NotifyConfigProvider;
import com.work.assistant.external.notify.handler.NotifyHandler;
import com.work.assistant.external.notify.handler.impl.dingtalk.enums.DingTalkMsgType;
import com.work.assistant.external.notify.handler.impl.dingtalk.helper.DingTalkHelper;
import com.work.assistant.external.notify.handler.impl.dingtalk.metadata.DingTalkSendRequest;
import com.work.assistant.external.notify.handler.impl.dingtalk.metadata.DingTalkSendResponse;
import com.work.assistant.external.notify.handler.impl.dingtalk.restclient.DingTalkRestClient;
import com.work.assistant.external.notify.model.NotifyConfigInfo;
import com.work.assistant.external.notify.model.NotifyRequest;
import com.work.assistant.external.notify.model.NotifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Retrofit;


@Slf4j
@Component
public class DingTalkNotifyHandler implements NotifyHandler {

  private final NotifyConfigProvider notifyConfigProvider;
  private final RetrofitClientProvider retrofitClientProvider;
  private final RetrofitClientResponseHelper retrofitClientResponseHelper;

  public DingTalkNotifyHandler(NotifyConfigProvider notifyConfigProvider, RetrofitClientProvider retrofitClientProvider,
                               RetrofitClientResponseHelper retrofitClientResponseHelper) {
    this.notifyConfigProvider = notifyConfigProvider;
    this.retrofitClientProvider = retrofitClientProvider;
    this.retrofitClientResponseHelper = retrofitClientResponseHelper;
  }


  @Override
  public NotifyType getNotifyType() {
    return NotifyType.DING_TALK;
  }

  @Override
  public NotifyResponse notify(NotifyRequest request) {


    NotifyConfigInfo notifyConfig = notifyConfigProvider.getNotifyConfig(NotifyType.DING_TALK);
    if (ObjectUtils.isEmpty(notifyConfig)) {
      return NotifyResponse.builder().success(false).message("").build();
    }

    DingTalkSendRequest dingTalkSendRequest = new DingTalkSendRequest();
    dingTalkSendRequest.setMsgtype(DingTalkMsgType.MARKDOWN);
    dingTalkSendRequest.setMarkdown(new DingTalkSendRequest.Markdown());
    dingTalkSendRequest.getMarkdown().setTitle(request.getTitle());
    dingTalkSendRequest.getMarkdown().setText(request.getContent());
    dingTalkSendRequest.setAt(new DingTalkSendRequest.AtWho());
    dingTalkSendRequest.getAt().setIsAtAll(false);

    String dingTalkUrl = notifyConfig.getDingTalkUrl();

    try {
      Retrofit retrofit = retrofitClientProvider.httpForJson(DingTalkHelper.getDingTalkDomain(dingTalkUrl));
      DingTalkRestClient dingTalkRestClient = retrofit.create(DingTalkRestClient.class);
      Call<DingTalkSendResponse> responseCall = dingTalkRestClient.send(DingTalkHelper.getDingTalkToken(dingTalkUrl), dingTalkSendRequest);
      DingTalkSendResponse dingTalkSendResponse =
          retrofitClientResponseHelper.processResponse(responseCall, "sendDingTalk");
      log.info("【钉钉推送通知】请求信息:[{}],响应信息:[{}]", dingTalkSendRequest.toString(),
          dingTalkSendResponse.toString());
    } catch (Exception e) {

    }
    return NotifyResponse.builder().success(true).build();
  }
}
