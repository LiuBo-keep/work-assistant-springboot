package com.work.assistant.biz.tool.model.convert;

import com.work.assistant.biz.tool.metadata.ToolCategorySaveRequest;
import com.work.assistant.biz.tool.model.ToolCategory;
import org.springframework.stereotype.Component;

/**
 * 工具收藏分类转换器
 *
 * @author work-assistant
 */
@Component
public class ToolCategoryConvert {

  /**
   * 从新增请求创建实体
   */
  public ToolCategory fromCreateRequest(ToolCategorySaveRequest request) {
    ToolCategory category = new ToolCategory();
    // 新增时 id 必须为 null，让 @GeneratedValue 触发 UUID 生成；前端可能传空串，这里统一归一化为 null
    category.setId(blankToNull(request.getId()));
    apply(category, request);
    return category;
  }

  private String blankToNull(String value) {
    return (value == null || value.trim().isEmpty()) ? null : value;
  }

  /**
   * 将请求字段写入实体（新增/修改共用）
   */
  public void apply(ToolCategory category, ToolCategorySaveRequest request) {
    category.setName(request.getName());
    category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
  }
}
