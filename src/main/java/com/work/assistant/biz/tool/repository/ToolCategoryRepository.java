package com.work.assistant.biz.tool.repository;

import com.work.assistant.biz.tool.model.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 工具收藏分类仓储
 *
 * @author work-assistant
 */
@Repository
public interface ToolCategoryRepository extends JpaRepository<ToolCategory, String> {

  /**
   * 按名称精确查找分类
   *
   * @param name 分类名称
   * @return 匹配的分类，无则 {@code null}
   */
  ToolCategory findByName(String name);
}
