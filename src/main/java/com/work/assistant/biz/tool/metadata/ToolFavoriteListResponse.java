package com.work.assistant.biz.tool.metadata;

import java.util.List;
import lombok.Data;

/**
 * 工具收藏全量响应（分类 + 收藏列表，一次性返回给前端）
 *
 * @author work-assistant
 */
@Data
public class ToolFavoriteListResponse {

  /**
   * 分类列表（按排序号升序）
   */
  private List<ToolCategoryResponse> categories;

  /**
   * 收藏列表（按排序号升序、创建时间倒序）
   */
  private List<ToolFavoriteResponse> favorites;
}
