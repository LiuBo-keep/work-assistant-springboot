import BaseService from '../baseservice'

class UserService extends BaseService {

  login(data) {
    return this.post(`/api/v1/token`, {
      data: data
    })
  }
}

export default new UserService()
