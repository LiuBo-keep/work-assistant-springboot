import BaseService from '../baseservice'

class UserService extends BaseService {

  login(data) {
    return this.post(`/api/v1/token`, {
      data: data
    })
  }

  me() {
    return this.get('/api/v1/user/me')
  }

  list() {
    return this.get('/api/v1/user/list')
  }

  save(data) {
    return this.post('/api/v1/user/save', {
      data: data
    })
  }

  remove(id) {
    return this.delete(`/api/v1/user/${id}`)
  }

  changePassword(data) {
    return this.post('/api/v1/user/change-password', {
      data: data
    })
  }
}

export default new UserService()
