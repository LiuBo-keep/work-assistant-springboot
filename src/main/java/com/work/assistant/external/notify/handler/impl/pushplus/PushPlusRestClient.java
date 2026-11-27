package com.work.assistant.external.notify.handler.impl.pushplus;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * PushPlus 服务客户端。
 *
 * @author aidan.liu
 */
public interface PushPlusRestClient {

  @POST("send")
  Call<PushPlusSendResponse> send(@Body PushPlusSendRequest request);
}
