package com.work.assistant.external.hrms.client;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * hr接口客户端
 *
 * @author aidan.liu
 */
public interface HrmsRestClient {

  @POST()
  Call<String> getCardRecord(@Url String url, @Header("Cookie") String cookie);
}
