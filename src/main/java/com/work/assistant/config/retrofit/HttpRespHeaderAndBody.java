package com.work.assistant.config.retrofit;

import java.util.Map;
import lombok.Data;

/**
 * @author aidan.liu
 */
@Data
public class HttpRespHeaderAndBody<T> {

  private Integer httpStatusCode;
  private Map<String,String> headerMap;

  /**
   * 注意，当泛型为『Void』,body = null.
   */
  private T body;
}
