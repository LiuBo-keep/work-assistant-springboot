package com.work.assistant.biz.tool.metadata.convert;

import com.work.assistant.biz.tool.metadata.ToolCategoryResponse;
import com.work.assistant.biz.tool.model.ToolCategory;
import org.springframework.stereotype.Component;

/**
 * 工具收藏分类 → 响应转换器
 *
 * @author work-assistant
 */
@Component
public class ToToolCategoryResponseConvert {

  public ToolCategoryResponse from(ToolCategory category) {
    ToolCategoryResponse response = new ToolCategoryResponse();
    response.setId(category.getId());
    response.setName(category.getName());
    response.setSortOrder(category.getSortOrder());
    return response;
  }
}
