package com.work.assistant.biz.tool.metadata.convert;

import com.work.assistant.biz.tool.metadata.ToolFavoriteResponse;
import com.work.assistant.biz.tool.model.ToolFavorite;
import org.springframework.stereotype.Component;

/**
 * 工具收藏 → 响应转换器
 *
 * @author work-assistant
 */
@Component
public class ToToolFavoriteResponseConvert {

  public ToolFavoriteResponse from(ToolFavorite favorite) {
    ToolFavoriteResponse response = new ToolFavoriteResponse();
    response.setId(favorite.getId());
    response.setName(favorite.getName());
    response.setUrl(favorite.getUrl());
    response.setIcon(favorite.getIcon());
    response.setDescription(favorite.getDescription());
    response.setCategoryId(favorite.getCategoryId());
    response.setSortOrder(favorite.getSortOrder());
    response.setClickCount(favorite.getClickCount());
    return response;
  }
}
