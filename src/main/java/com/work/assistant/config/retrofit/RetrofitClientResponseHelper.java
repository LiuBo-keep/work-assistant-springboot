package com.work.assistant.config.retrofit;

import com.work.assistant.common.exception.AppErrorCode;
import com.work.assistant.common.exception.WorkAssistantException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author aidan.liu
 */
@Slf4j
@Component
public class RetrofitClientResponseHelper {

  public <T> T processResponse(Call<T> responseCall, String serviceName) throws WorkAssistantException {

    return this.getHeaderAndBody(responseCall, serviceName, null).getBody();
  }


  public <T> HttpRespHeaderAndBody<T> getHeaderAndBody(Call<T> responseCall, String serviceName, String... headerKeys) throws
      WorkAssistantException {

    HttpRespHeaderAndBody<T> respHeaderAndBody = new HttpRespHeaderAndBody<>();

    // request http rest
    Response<T> repos;
    try {
      repos = responseCall.execute();
    } catch (Exception e) {
      log.error("RequestApi- {} -failed.", serviceName, e);
      throw new WorkAssistantException(AppErrorCode.INTERNAL_SERVER_ERROR, "request[" + serviceName + "]failed,details:" + e.getMessage());
    }

    // process http status code
    respHeaderAndBody.setHttpStatusCode(repos.code());
    if (repos.isSuccessful()) {

      // process body
      if (repos.body() != null) {

        respHeaderAndBody.setBody(repos.body());
      } else {
        respHeaderAndBody.setBody(null);
      }

      // process header
      if (ArrayUtils.isNotEmpty(headerKeys)) {
        Map<String, String> headerMap = new HashMap<>(5);

        for (String headerKey : headerKeys) {
          String value = repos.headers().get(headerKey);
          headerMap.put(headerKey, value);
        }
        respHeaderAndBody.setHeaderMap(headerMap);
      }

      return respHeaderAndBody;

    } else {
      // no success, process error msg
      String exMsg = this.processError(repos, serviceName);
      throw new WorkAssistantException(AppErrorCode.INTERNAL_SERVER_ERROR, exMsg);
    }
  }

  private <T> String processError(Response<T> repos, String serviceName) {

    String exMsg;
    try (ResponseBody errorBody = repos.errorBody()) {
      if (errorBody != null) {

        // body is not null
        try {
          exMsg = errorBody.string();
        } catch (IOException e) {
          log.error("{}--processErrorBodyFailed,details:", serviceName, e);
          exMsg = "process[" + serviceName + "] error-result failed,status code:" + repos.code();
        }

      } else {
        // body is null
        exMsg = "request[" + serviceName + "] failed,status code:" + repos.code();
      }
    }
    return exMsg;
  }
}
