package com.work.assistant.biz.tool.repository;

import com.work.assistant.biz.tool.model.ToolFavorite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 工具收藏仓储
 *
 * @author work-assistant
 */
@Repository
public interface ToolFavoriteRepository extends JpaRepository<ToolFavorite, String> {

  /**
   * 按分类 id 查找收藏
   *
   * @param categoryId 分类 id
   * @return 收藏列表
   */
  List<ToolFavorite> findByCategoryId(String categoryId);
}
