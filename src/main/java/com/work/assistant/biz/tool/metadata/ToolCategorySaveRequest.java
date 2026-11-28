package com.work.assistant.biz.tool.metadata;

import lombok.Data;

/**
 * 工具收藏分类新增/修改请求
 *
 * @author work-assistant
 */
@Data
public class ToolCategorySaveRequest {

  /**
   * 主键（新增时为空）
   */
  private String id;

  /**
   * 分类名称
   */
  private String name;

  /**
   * 排序号（越小越靠前）
   */
  private Integer sortOrder;
}
