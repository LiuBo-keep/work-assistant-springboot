class BrowserStore {

  get accessToken() {
    return localStorage.getItem('hrms.accessToken')
  }

  /**
   *
   * @param {string} accessToken 服务身份令牌
   */
  setAccessToken(accessToken) {
    localStorage.setItem('hrms.accessToken', accessToken)
  }

  setcCurrentTime(currentTime) {
    localStorage.setItem('hrms.currentTime', currentTime)
  }

  get username() {
    return localStorage.getItem('hrms.username') || ''
  }

  /**
   *
   * @param {string} tenant 保存输入用户名
   */
  setUsername(username) {
    localStorage.setItem('hrms.username', username)
  }

  get en() {
    return localStorage.getItem('hrms.en') || ''
  }

  get language() {
    return localStorage.getItem('hrms.language') || 'zh-CN'
  }

  setBaseURL(value) {
    localStorage.setItem('hrms.baseURL', value)
  }
}

export default new BrowserStore()
