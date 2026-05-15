import BaseService from '../../baseservice'

class SystemService extends BaseService {
  getConfig() {
    return this.post(`/api/v1/hrms/notify-config/query`)
  }

  save(data) {
    return super.post(`/api/v1/hrms/notify-config/save`, {
      data: data
    })
  }

  testNotify(data) {
    return super.post(`/api/v1/notify/test`, {
      data: data
    })
  }
}

export default new SystemService()
