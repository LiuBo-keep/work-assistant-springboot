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

  get role() {
    return localStorage.getItem('hrms.role') || ''
  }

  setRole(role) {
    localStorage.setItem('hrms.role', role)
  }

  get displayName() {
    return localStorage.getItem('hrms.displayName') || ''
  }

  setDisplayName(displayName) {
    localStorage.setItem('hrms.displayName', displayName)
  }

  clearAuth() {
    localStorage.removeItem('hrms.accessToken')
    localStorage.removeItem('hrms.username')
    localStorage.removeItem('hrms.role')
    localStorage.removeItem('hrms.displayName')
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
