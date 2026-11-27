package com.work.assistant.biz.tool.metadata;

import lombok.Data;

/**
 * 网址解析结果（自动提取网页名称与图标）
 *
 * @author work-assistant
 */
@Data
public class ToolMetadataResolveResponse {

  /**
   * 网页标题（作为工具名称）
   */
  private String name;

  /**
   * 网站图标地址（favicon），可能为空
   */
  private String icon;

  /**
   * 最终解析的网址（可能跟随重定向）
   */
  private String url;
}
