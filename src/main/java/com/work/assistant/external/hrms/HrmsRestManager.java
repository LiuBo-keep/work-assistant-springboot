package com.work.assistant.external.hrms;

import com.work.assistant.common.exception.WorkAssistantException;
import com.work.assistant.config.retrofit.RetrofitClientProvider;
import com.work.assistant.config.retrofit.RetrofitClientResponseHelper;
import com.work.assistant.external.hrms.client.HrmsRestClient;
import com.work.assistant.external.hrms.metadata.HrmsCardRecordResponse;
import com.work.assistant.external.hrms.parser.HrmsCardRecordParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * @author aidan.liu
 */
@Slf4j
@Component
public class HrmsRestManager {

  private final HrmsCardRecordParser hrmsCardRecordParser;
  private final RetrofitClientProvider retrofitClientProvider;
  private final RetrofitClientResponseHelper retrofitClientResponseHelper;


  public HrmsRestManager(RetrofitClientProvider retrofitClientProvider, RetrofitClientResponseHelper retrofitClientResponseHelper,
                         HrmsCardRecordParser hrmsCardRecordParser) {
    this.retrofitClientProvider = retrofitClientProvider;
    this.retrofitClientResponseHelper = retrofitClientResponseHelper;
    this.hrmsCardRecordParser = hrmsCardRecordParser;
  }

  public HrmsCardRecordResponse queryCardRecord(String hrmsUrl, String hrmsCookie) throws WorkAssistantException {
    Retrofit retrofit = retrofitClientProvider.httpForJson(StringUtils.stripEnd(hrmsUrl, "/") + "/");
    HrmsRestClient hrmsRestClient = retrofit.create(HrmsRestClient.class);
    Call<String> execute = hrmsRestClient.getCardRecord(hrmsUrl, hrmsCookie);
    String result = retrofitClientResponseHelper.processResponse(execute, "CardRecordClient");
    return hrmsCardRecordParser.parser(result);
  }
}
