package com.work.assistant.biz.hrms.repository;

import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author aidan.liu
 */
@Repository
public interface HrmsNotifyConfigRepository extends JpaRepository<HrmsNotifyConfig, String> {

}
