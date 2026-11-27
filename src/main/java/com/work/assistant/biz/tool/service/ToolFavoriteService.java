package com.work.assistant.biz.tool.service;

import com.work.assistant.biz.tool.metadata.ToolCategoryResponse;
import com.work.assistant.biz.tool.metadata.ToolCategorySaveRequest;
import com.work.assistant.biz.tool.metadata.ToolFavoriteListResponse;
import com.work.assistant.biz.tool.metadata.ToolFavoriteResponse;
import com.work.assistant.biz.tool.metadata.ToolFavoriteSaveRequest;
import com.work.assistant.biz.tool.metadata.convert.ToToolCategoryResponseConvert;
import com.work.assistant.biz.tool.metadata.convert.ToToolFavoriteResponseConvert;
import com.work.assistant.biz.tool.model.ToolCategory;
import com.work.assistant.biz.tool.model.ToolFavorite;
import com.work.assistant.biz.tool.model.convert.ToolCategoryConvert;
import com.work.assistant.biz.tool.model.convert.ToolFavoriteConvert;
import com.work.assistant.biz.tool.repository.ToolCategoryRepository;
import com.work.assistant.biz.tool.repository.ToolFavoriteRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 工具收藏服务
 *
 * @author work-assistant
 */
@Service
public class ToolFavoriteService {

  private final ToolCategoryRepository categoryRepository;
  private final ToolFavoriteRepository favoriteRepository;
  private final ToolCategoryConvert categoryConvert;
  private final ToolFavoriteConvert favoriteConvert;
  private final ToToolCategoryResponseConvert categoryResponseConvert;
  private final ToToolFavoriteResponseConvert favoriteResponseConvert;

  public ToolFavoriteService(ToolCategoryRepository categoryRepository,
                             ToolFavoriteRepository favoriteRepository,
                             ToolCategoryConvert categoryConvert,
                             ToolFavoriteConvert favoriteConvert,
                             ToToolCategoryResponseConvert categoryResponseConvert,
                             ToToolFavoriteResponseConvert favoriteResponseConvert) {
    this.categoryRepository = categoryRepository;
    this.favoriteRepository = favoriteRepository;
    this.categoryConvert = categoryConvert;
    this.favoriteConvert = favoriteConvert;
    this.categoryResponseConvert = categoryResponseConvert;
    this.favoriteResponseConvert = favoriteResponseConvert;
  }

  /**
   * 全量查询：分类 + 收藏列表
   */
  public ToolFavoriteListResponse list() {
    ToolFavoriteListResponse response = new ToolFavoriteListResponse();

    List<ToolCategory> categories = categoryRepository.findAll();
    categories.sort(Comparator.comparing(ToolCategory::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
    response.setCategories(categories.stream().map(categoryResponseConvert::from).toList());

    List<ToolFavorite> favorites = favoriteRepository.findAll();
    favorites.sort(Comparator
        .comparing(ToolFavorite::getSortOrder, Comparator.nullsLast(Integer::compareTo))
        .thenComparing(ToolFavorite::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
    response.setFavorites(favorites.stream().map(favoriteResponseConvert::from).toList());

    return response;
  }

  /**
   * 新增/修改分类
   */
  @Transactional(rollbackOn = Exception.class)
  public ToolCategoryResponse saveCategory(ToolCategorySaveRequest request) {
    if (isBlank(request.getName())) {
      throw new IllegalArgumentException("分类名称不能为空");
    }

    ToolCategory category;
    if (isBlank(request.getId())) {
      // 新增：校验重名
      if (categoryRepository.findByName(request.getName().trim()) != null) {
        throw new IllegalArgumentException("分类已存在：" + request.getName());
      }
      category = categoryConvert.fromCreateRequest(request);
      category.setCreatedAt(LocalDateTime.now());
    } else {
      category = categoryRepository.findById(request.getId()).orElse(null);
      if (category == null) {
        throw new IllegalArgumentException("分类不存在");
      }
      ToolCategory duplicated = categoryRepository.findByName(request.getName().trim());
      if (duplicated != null && !duplicated.getId().equals(category.getId())) {
        throw new IllegalArgumentException("分类已存在：" + request.getName());
      }
      categoryConvert.apply(category, request);
    }
    categoryRepository.save(category);
    return categoryResponseConvert.from(category);
  }

  /**
   * 删除分类（其下收藏的分类 id 置空）
   */
  @Transactional(rollbackOn = Exception.class)
  public void deleteCategory(String id) {
    if (isBlank(id)) {
      throw new IllegalArgumentException("分类 id 不能为空");
    }
    categoryRepository.findById(id).ifPresent(category -> {
      // 将归属该分类的收藏置为「未分类」
      List<ToolFavorite> favorites = favoriteRepository.findByCategoryId(id);
      for (ToolFavorite favorite : favorites) {
        favorite.setCategoryId(null);
      }
      favoriteRepository.saveAll(favorites);
      categoryRepository.delete(category);
    });
  }

  /**
   * 新增/修改收藏
   */
  @Transactional(rollbackOn = Exception.class)
  public ToolFavoriteResponse saveFavorite(ToolFavoriteSaveRequest request) {
    if (isBlank(request.getName())) {
      throw new IllegalArgumentException("工具名称不能为空");
    }
    if (isBlank(request.getUrl())) {
      throw new IllegalArgumentException("工具网址不能为空");
    }

    ToolFavorite favorite;
    if (isBlank(request.getId())) {
      favorite = favoriteConvert.fromCreateRequest(request);
      favorite.setCreatedAt(LocalDateTime.now());
    } else {
      favorite = favoriteRepository.findById(request.getId()).orElse(null);
      if (favorite == null) {
        throw new IllegalArgumentException("收藏不存在");
      }
      favoriteConvert.apply(favorite, request);
    }
    favoriteRepository.save(favorite);
    return favoriteResponseConvert.from(favorite);
  }

  /**
   * 删除收藏
   */
  @Transactional(rollbackOn = Exception.class)
  public void deleteFavorite(String id) {
    if (isBlank(id)) {
      throw new IllegalArgumentException("收藏 id 不能为空");
    }
    favoriteRepository.deleteById(id);
  }

  /**
   * 记录点击（打开工具），点击次数 +1
   */
  @Transactional(rollbackOn = Exception.class)
  public void click(String id) {
    if (isBlank(id)) {
      return;
    }
    favoriteRepository.findById(id).ifPresent(favorite -> {
      favorite.setClickCount((favorite.getClickCount() == null ? 0 : favorite.getClickCount()) + 1);
      favoriteRepository.save(favorite);
    });
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
