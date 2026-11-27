package com.work.assistant.biz.tool.controller;

import com.work.assistant.biz.tool.metadata.ToolCategoryResponse;
import com.work.assistant.biz.tool.metadata.ToolCategorySaveRequest;
import com.work.assistant.biz.tool.metadata.ToolFavoriteListResponse;
import com.work.assistant.biz.tool.metadata.ToolFavoriteResponse;
import com.work.assistant.biz.tool.metadata.ToolFavoriteSaveRequest;
import com.work.assistant.biz.tool.metadata.ToolMetadataResolveResponse;
import com.work.assistant.biz.tool.service.ToolFavoriteService;
import com.work.assistant.biz.tool.service.ToolMetadataResolver;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工具收藏控制器
 *
 * @author work-assistant
 */
@RestController
@RequestMapping("/api/v1/tool")
public class ToolFavoriteController {

  private final ToolFavoriteService toolFavoriteService;
  private final ToolMetadataResolver toolMetadataResolver;

  public ToolFavoriteController(ToolFavoriteService toolFavoriteService,
                                ToolMetadataResolver toolMetadataResolver) {
    this.toolFavoriteService = toolFavoriteService;
    this.toolMetadataResolver = toolMetadataResolver;
  }

  /**
   * 全量查询（分类 + 收藏）
   */
  @GetMapping("/list")
  public ToolFavoriteListResponse list() {
    return toolFavoriteService.list();
  }

  /**
   * 根据网址解析网页名称与图标（用于新增收藏时自动填充）
   */
  @GetMapping("/resolve")
  public ToolMetadataResolveResponse resolve(@RequestParam("url") String url) {
    return toolMetadataResolver.resolve(url);
  }

  /**
   * 新增/修改分类
   */
  @PostMapping("/category/save")
  public ToolCategoryResponse saveCategory(@RequestBody ToolCategorySaveRequest request) {
    return toolFavoriteService.saveCategory(request);
  }

  /**
   * 删除分类
   */
  @DeleteMapping("/category/{id}")
  public void deleteCategory(@PathVariable("id") String id) {
    toolFavoriteService.deleteCategory(id);
  }

  /**
   * 新增/修改收藏
   */
  @PostMapping("/favorite/save")
  public ToolFavoriteResponse saveFavorite(@RequestBody ToolFavoriteSaveRequest request) {
    return toolFavoriteService.saveFavorite(request);
  }

  /**
   * 删除收藏
   */
  @DeleteMapping("/favorite/{id}")
  public void deleteFavorite(@PathVariable("id") String id) {
    toolFavoriteService.deleteFavorite(id);
  }

  /**
   * 记录点击
   */
  @PostMapping("/favorite/{id}/click")
  public void click(@PathVariable("id") String id) {
    toolFavoriteService.click(id);
  }
}
