package com.work.assistant.biz.tool.model.convert;

import com.work.assistant.biz.tool.metadata.ToolFavoriteSaveRequest;
import com.work.assistant.biz.tool.model.ToolFavorite;
import org.springframework.stereotype.Component;

/**
 * 工具收藏转换器
 *
 * @author work-assistant
 */
@Component
public class ToolFavoriteConvert {

  /**
   * 从新增请求创建实体
   */
  public ToolFavorite fromCreateRequest(ToolFavoriteSaveRequest request) {
    ToolFavorite favorite = new ToolFavorite();
    // 新增时 id 必须为 null，让 @GeneratedValue 触发 UUID 生成；前端可能传空串，这里统一归一化为 null
    favorite.setId(blankToNull(request.getId()));
    favorite.setClickCount(0);
    apply(favorite, request);
    return favorite;
  }

  private String blankToNull(String value) {
    return (value == null || value.trim().isEmpty()) ? null : value;
  }

  /**
   * 将请求字段写入实体（新增/修改共用）
   */
  public void apply(ToolFavorite favorite, ToolFavoriteSaveRequest request) {
    favorite.setName(request.getName());
    favorite.setUrl(request.getUrl());
    favorite.setIcon(request.getIcon());
    favorite.setDescription(request.getDescription());
    // categoryId 为空串时归一化为 null，避免外键引用空字符串导致约束冲突
    favorite.setCategoryId(blankToNull(request.getCategoryId()));
    favorite.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
  }
}
