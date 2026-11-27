package com.work.assistant.biz.hrms.repository;

import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author aidan.liu
 */
@Repository
public interface HrmsNotifyConfigRepository extends JpaRepository<HrmsNotifyConfig, String> {

  /**
   * 获取当前启用的通知配置。
   *
   * <p>约定：系统仅维护一条配置记录，取第一条并校验启用状态。
   *
   * @return 启用中的配置；无配置或未启用时返回 {@code null}
   */
  default HrmsNotifyConfig findEnabledConfig() {
    List<HrmsNotifyConfig> configs = findAll();
    if (configs.isEmpty()) {
      return null;
    }
    HrmsNotifyConfig config = configs.getFirst();
    if (Boolean.FALSE.equals(config.getEnabled())) {
      return null;
    }
    return config;
  }
}
