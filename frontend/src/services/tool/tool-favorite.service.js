import BaseService from '../baseservice'

/**
 * ToolFavoriteService — 工具收藏服务
 * 对接后端 /api/v1/tool/... 接口
 */
class ToolFavoriteService extends BaseService {

  /** 全量查询（分类 + 收藏） */
  list() {
    return this.get('/api/v1/tool/list')
  }

  /** 新增/修改分类 */
  saveCategory(data) {
    return this.post('/api/v1/tool/category/save', { data })
  }

  /** 删除分类 */
  deleteCategory(id) {
    return this.delete(`/api/v1/tool/category/${id}`)
  }

  /** 新增/修改收藏 */
  saveFavorite(data) {
    return this.post('/api/v1/tool/favorite/save', { data })
  }

  /** 删除收藏 */
  deleteFavorite(id) {
    return this.delete(`/api/v1/tool/favorite/${id}`)
  }

  /** 记录点击 */
  click(id) {
    return this.post(`/api/v1/tool/favorite/${id}/click`)
  }

  /** 根据网址解析网页名称与图标 */
  resolve(url) {
    return this.get('/api/v1/tool/resolve', { params: { url } })
  }
}

export default new ToolFavoriteService()
