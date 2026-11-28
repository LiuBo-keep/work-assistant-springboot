import BaseService from '../baseservice'

/**
 * BackupService — 数据库备份服务
 * 对接后端 /api/v1/backup/... 接口
 */
class BackupService extends BaseService {

  /** 备份记录列表 */
  list() {
    return this.get('/api/v1/backup/list')
  }

  /** 可备份的表名清单 */
  tables() {
    return this.get('/api/v1/backup/tables')
  }

  /** 立即执行一次备份（tables 为空则全量备份，否则部分备份） */
  backup(tables) {
    const params = {}
    if (tables && tables.length) {
      params.tables = tables
    }
    return this.post('/api/v1/backup/backup', { params })
  }

  /** 导入备份文件 */
  importSql(file) {
    const formData = new FormData()
    formData.append('file', file)
    return this.http.post('/api/v1/backup/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }

  /** 下载备份文件（返回 blob） */
  download(id) {
    return this.http.get('/api/v1/backup/download/' + id, {
      responseType: 'blob'
    })
  }

  /** 删除备份记录 */
  remove(id) {
    return this.delete('/api/v1/backup/' + id)
  }
}

export default new BackupService()
