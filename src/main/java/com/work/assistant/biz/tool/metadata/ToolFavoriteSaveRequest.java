package com.work.assistant.biz.tool.metadata;

import lombok.Data;

/**
 * 工具收藏新增/修改请求
 *
 * @author work-assistant
 */
@Data
public class ToolFavoriteSaveRequest {

  /**
   * 主键（新增时为空）
   */
  private String id;

  /**
   * 工具名称
   */
  private String name;

  /**
   * 工具网址
   */
  private String url;

  /**
   * 图标地址（可选）
   */
  private String icon;

  /**
   * 描述
   */
  private String description;

  /**
   * 分类 id（可为空）
   */
  private String categoryId;

  /**
   * 排序号
   */
  private Integer sortOrder;
}
