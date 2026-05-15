package com.work.assistant.external.hrms.parser;

import com.work.assistant.external.hrms.metadata.HrmsCardRecordResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Slf4j
@Component
public class HrmsCardRecordParser {

  private static final String CARD_RECORD_KEY = "table[class=table table-bordered table-condensed]";

  public HrmsCardRecordResponse parser(String cardRecord) {
    Document doc = Jsoup.parse(cardRecord);

    if (doc.select(CARD_RECORD_KEY).isEmpty()) {
      log.info("查询HRMS刷卡记录 连接HRMS信息错误");
      return null;
    }

    HrmsCardRecordResponse hrmsCardRecordResponse = new HrmsCardRecordResponse();
    Elements rows = doc.select(CARD_RECORD_KEY).getFirst().select("tr");

    if (!rows.isEmpty()) {
      Elements elements = rows.get(1).select("td");
      hrmsCardRecordResponse.setEmployeeId(elements.get(0).text());
      hrmsCardRecordResponse.setCnName(elements.get(1).text());
      hrmsCardRecordResponse.setEnName(elements.get(2).text());
      hrmsCardRecordResponse.setClockInTime(elements.get(3).text());
      hrmsCardRecordResponse.setClockInType(elements.get(4).text());
      hrmsCardRecordResponse.setAttendanceLocation(elements.get(5).text());
      return hrmsCardRecordResponse;
    }
    return hrmsCardRecordResponse;
  }
}
