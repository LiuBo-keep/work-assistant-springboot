package com.work.assistant.biz.tool.metadata;

import lombok.Data;

/**
 * 工具收藏响应
 *
 * @author work-assistant
 */
@Data
public class ToolFavoriteResponse {

  /**
   * 主键
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
   * 图标地址
   */
  private String icon;

  /**
   * 描述
   */
  private String description;

  /**
   * 分类 id
   */
  private String categoryId;

  /**
   * 排序号
   */
  private Integer sortOrder;

  /**
   * 点击次数
   */
  private Integer clickCount;
}
