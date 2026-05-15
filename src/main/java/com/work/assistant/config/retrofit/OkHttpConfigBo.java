package com.work.assistant.config.retrofit;

import lombok.Builder;
import lombok.Data;

/**
 * @author aidan.liu
 */
@Builder
@Data
public class OkHttpConfigBo {

  private RetrofitConverterType retrofitConverterType;
  private String baseUrl;

  @Builder.Default
  private boolean retryOnConnectionFailure = true;

  /**
   * 单位：秒
   */
  @Builder.Default
  private int readTimeout = 25;

  /**
   * 单位：秒
   */
  @Builder.Default
  private int connectTimeout = 25;

  /**
   * 单位：秒
   */
  @Builder.Default
  private int writeTimeout = 25;

  private byte[] certificateInfo;
  private String certificatePwd;

  @Builder.Default
  private boolean enableSepUrlInterceptor = false;
}
