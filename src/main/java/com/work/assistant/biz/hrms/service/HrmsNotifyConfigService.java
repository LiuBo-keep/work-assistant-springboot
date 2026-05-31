package com.work.assistant.biz.hrms.service;

import com.work.assistant.biz.hrms.metadata.HrmsNotifyConfigQueryResponse;
import com.work.assistant.biz.hrms.metadata.HrmsNotifyConfigSaveRequest;
import com.work.assistant.biz.hrms.metadata.convert.ToHrmsNotifyConfigQueryResponseConvert;
import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import com.work.assistant.biz.hrms.model.hrms.convert.HrmsNotifyConfigConvert;
import com.work.assistant.biz.hrms.repository.HrmsNotifyConfigRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * hr通知配置服务
 *
 * @author aidan.liu
 */
@Service
public class HrmsNotifyConfigService {

  private final HrmsNotifyConfigConvert hrmsNotifyConfigConvert;
  private final HrmsNotifyConfigRepository hrmsNotifyConfigRepository;
  private final ToHrmsNotifyConfigQueryResponseConvert hrmsNotifyConfigQueryResponseConvert;

  public HrmsNotifyConfigService(HrmsNotifyConfigConvert hrmsNotifyConfigConvert, HrmsNotifyConfigRepository hrmsNotifyConfigRepository,
                                 ToHrmsNotifyConfigQueryResponseConvert hrmsNotifyConfigQueryResponseConvert) {
    this.hrmsNotifyConfigConvert = hrmsNotifyConfigConvert;
    this.hrmsNotifyConfigRepository = hrmsNotifyConfigRepository;
    this.hrmsNotifyConfigQueryResponseConvert = hrmsNotifyConfigQueryResponseConvert;
  }

  /**
   * 新增或者修改
   *
   */
  @Transactional(rollbackOn = Exception.class)
  public void save(HrmsNotifyConfigSaveRequest request) {
    HrmsNotifyConfig hrmsNotifyConfig;
    if (request.getId() == null) {
       hrmsNotifyConfig = hrmsNotifyConfigConvert.fromHrmsNotifyConfigCreateRequest(request);
    }else {
       hrmsNotifyConfig = hrmsNotifyConfigRepository.findById(request.getId()).orElse(null);
       if (ObjectUtils.isEmpty(hrmsNotifyConfig)){
           return;
       }
       hrmsNotifyConfigConvert.fromHrmsNotifyConfigCreateRequest(hrmsNotifyConfig,request);
    }

    hrmsNotifyConfigRepository.save(hrmsNotifyConfig);
  }

  /**
   * 查询
   */
  public HrmsNotifyConfigQueryResponse query() {
    HrmsNotifyConfigQueryResponse response = new HrmsNotifyConfigQueryResponse();
    List<HrmsNotifyConfig> hrmsNotifyConfigList = hrmsNotifyConfigRepository.findAll();
    if (CollectionUtils.isNotEmpty(hrmsNotifyConfigList)) {
      HrmsNotifyConfig first = hrmsNotifyConfigList.getFirst();
      response = hrmsNotifyConfigQueryResponseConvert.fromHrmsNotifyConfig(first);
    }
    return response;
  }
}
