package com.work.assistant.config.retrofit;

import static com.work.assistant.config.retrofit.RetrofitConverterType.JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.work.assistant.common.exception.WorkAssistantException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.jaxb.JaxbConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author aidan.liu
 */
@Slf4j
@Component
public class RetrofitClientProvider {

  private final ObjectMapper objectMapper;
  private final OkHttpClientProvider okHttpClientProvider;

  public RetrofitClientProvider(ObjectMapper objectMapper, OkHttpClientProvider okHttpClientProvider) {
    this.objectMapper = objectMapper;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  public Retrofit generateRetrofit(OkHttpConfigBo okHttpConfigBo) throws WorkAssistantException {

    Retrofit.Builder builder = new Retrofit.Builder();

    builder.baseUrl(okHttpConfigBo.getBaseUrl());

    switch (okHttpConfigBo.getRetrofitConverterType()) {
      case XML:
        builder.addConverterFactory(new NullOnEmptyConverterFactory())
            .addConverterFactory(JaxbConverterFactory.create());
        break;
      case SCALARS:
        builder.addConverterFactory(new NullOnEmptyConverterFactory())
            .addConverterFactory(ScalarsConverterFactory.create());
        break;
      case JSON:
      default:
        builder.addConverterFactory(new NullOnEmptyConverterFactory())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JacksonConverterFactory.create(objectMapper));
        break;
    }

    builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    builder.client(okHttpClientProvider.generateOkHttpClient(okHttpConfigBo));

    return builder.build();
  }

  /**
   * @see RetrofitClientProvider#generateRetrofit(OkHttpConfigBo)
   */
  public Retrofit httpForJson(String baseUrl) throws WorkAssistantException {

    OkHttpConfigBo okHttpConfigBo = OkHttpConfigBo.builder()
        .retrofitConverterType(JSON)
        .baseUrl(baseUrl)
        .build();
    return this.generateRetrofit(okHttpConfigBo);
  }
}
