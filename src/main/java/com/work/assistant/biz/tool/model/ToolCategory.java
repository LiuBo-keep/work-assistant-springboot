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
 * 工具收藏分类
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "tool_category")
public class ToolCategory {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 分类名称
   */
  @Column(name = "name")
  private String name;

  /**
   * 排序号（越小越靠前）
   */
  @Column(name = "sort_order")
  private Integer sortOrder;

  /**
   * 创建时间
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Version
  @Column(name = "version_num")
  private Integer versionNum = 0;
}
