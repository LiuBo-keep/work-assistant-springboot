package com.work.assistant.biz.tool.metadata;

import lombok.Data;

/**
 * 工具收藏分类响应
 *
 * @author work-assistant
 */
@Data
public class ToolCategoryResponse {

  /**
   * 主键
   */
  private String id;

  /**
   * 分类名称
   */
  private String name;

  /**
   * 排序号
   */
  private Integer sortOrder;
}
