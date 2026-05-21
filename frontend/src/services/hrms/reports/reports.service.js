import BaseService from '../../baseservice'

class ReportsService extends BaseService {

  getCardRecords(pageInfo, data) {
    return this.post(
      `/api/v1/hrms/card-record/page?pageNumber=${pageInfo.page}&pageSize=${pageInfo.size}`,
      {
        data: data
      }
    )
  }

  refreshCardRecord(data) {
    return this.post(`/api/v1/hrms/card-record/refresh`, {
      data: data
    })
  }
}

export default new ReportsService()
