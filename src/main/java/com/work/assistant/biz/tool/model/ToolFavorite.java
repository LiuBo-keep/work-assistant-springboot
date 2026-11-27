package com.work.assistant.biz.tool.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工具收藏
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "tool_favorite")
public class ToolFavorite {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 工具名称
   */
  @Column(name = "name")
  private String name;

  /**
   * 工具网址
   */
  @Column(name = "url")
  private String url;

  /**
   * 图标地址（可选）
   */
  @Column(name = "icon")
  private String icon;

  /**
   * 描述
   */
  @Column(name = "description")
  private String description;

  /**
   * 分类 id（可为空，表示未分类）
   */
  @Column(name = "category_id")
  private String categoryId;

  /**
   * 排序号（越小越靠前）
   */
  @Column(name = "sort_order")
  private Integer sortOrder;

  /**
   * 点击次数
   */
  @Column(name = "click_count")
  private Integer clickCount;

  /**
   * 创建时间
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Version
  @Column(name = "version_num")
  private Integer versionNum = 0;
}
