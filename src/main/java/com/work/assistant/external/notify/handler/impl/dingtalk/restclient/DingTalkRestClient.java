package com.work.assistant.external.notify.handler.impl.dingtalk.restclient;

import com.work.assistant.external.notify.handler.impl.dingtalk.metadata.DingTalkSendRequest;
import com.work.assistant.external.notify.handler.impl.dingtalk.metadata.DingTalkSendResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 钉钉机器人 Retrofit 客户端。
 *
 * @author work-assistant
 */
public interface DingTalkRestClient {

  /**
   * 发送信息
   *
   * @param request 请求信息
   * @return 结果
   */
  @POST("robot/send")
  Call<DingTalkSendResponse> send(@Query("access_token") String accessToken,
                                  @Body DingTalkSendRequest request);


}
