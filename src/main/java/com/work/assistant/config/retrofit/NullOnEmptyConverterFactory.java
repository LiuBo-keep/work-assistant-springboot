package com.work.assistant.config.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author aidan.liu
 */
public class NullOnEmptyConverterFactory extends Converter.Factory {

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
    final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
    return (Converter<ResponseBody, Object>) body -> {
      if (body.contentLength() == 0) {
        return null;
      }
      return delegate.convert(body);
    };
  }
}
