package com.work.assistant.external.notify.handler.impl.pushplus;

import com.work.assistant.config.retrofit.RetrofitClientProvider;
import com.work.assistant.config.retrofit.RetrofitClientResponseHelper;
import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.handler.NotifyConfigProvider;
import com.work.assistant.external.notify.handler.NotifyHandler;
import com.work.assistant.external.notify.model.NotifyConfigInfo;
import com.work.assistant.external.notify.model.NotifyRequest;
import com.work.assistant.external.notify.model.NotifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * PushPlus 微信通知处理器。
 *
 * @author aidan.liu
 */
@Slf4j
@Component
public class PushPlusNotifyHandler implements NotifyHandler {

  private final NotifyConfigProvider notifyConfigProvider;
  private final RetrofitClientProvider retrofitClientProvider;
  private final RetrofitClientResponseHelper retrofitClientResponseHelper;

  public PushPlusNotifyHandler(NotifyConfigProvider notifyConfigProvider,
                               RetrofitClientProvider retrofitClientProvider,
                               RetrofitClientResponseHelper retrofitClientResponseHelper) {
    this.notifyConfigProvider = notifyConfigProvider;
    this.retrofitClientProvider = retrofitClientProvider;
    this.retrofitClientResponseHelper = retrofitClientResponseHelper;
  }

  @Override
  public NotifyType getNotifyType() {
    return NotifyType.PUSH_PLUS;
  }

  @Override
  public NotifyResponse notify(NotifyRequest request) {

    NotifyConfigInfo notifyConfig = notifyConfigProvider.getNotifyConfig(NotifyType.PUSH_PLUS);
    if (ObjectUtils.isEmpty(notifyConfig)
        || StringUtils.isAnyBlank(notifyConfig.getPushPlusUrl(), notifyConfig.getPushPlusToken())) {
      return NotifyResponse.builder().success(false).message("pushplus config missing").build();
    }

    PushPlusSendRequest sendRequest = new PushPlusSendRequest();
    sendRequest.setToken(notifyConfig.getPushPlusToken());
    sendRequest.setTitle(request.getTitle());
    sendRequest.setContent(request.getContent());
    sendRequest.setTemplate("markdown");

    try {
      Retrofit retrofit = retrofitClientProvider.httpForJson(
          StringUtils.stripEnd(notifyConfig.getPushPlusUrl(), "/") + "/");
      PushPlusRestClient client = retrofit.create(PushPlusRestClient.class);
      Call<PushPlusSendResponse> call = client.send(sendRequest);
      PushPlusSendResponse response = retrofitClientResponseHelper.processResponse(call, "sendPushPlus");
      log.info("【PushPlus推送通知】请求信息:[{}],响应信息:[{}]", sendRequest.toString(), response);

      boolean success = response != null && response.getCode() == 200;
      return NotifyResponse.builder()
          .success(success)
          .message(response == null ? "empty response" : response.getMsg())
          .build();
    } catch (Exception e) {
      log.error("【PushPlus推送通知】发送失败", e);
      return NotifyResponse.builder()
          .success(false)
          .message(e.getMessage())
          .build();
    }
  }
}
