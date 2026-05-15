import axios from 'axios'
import { ElMessage } from 'element-plus'

import browser from '../store/persistent/browser'
import i18n from '../i18n'
import router from '../router'

export default class BaseService {
  http

  constructor() {
    const config = {
      baseURL: '',
      timeout: 1000 * 60 * 60,
      headers: {
        'Content-Type': 'application/json; charset=utf-8'
      }
    }

    this.http = axios.create(config)

    this.http.interceptors.request.use(
      config => {
        if (browser.accessToken) {
          config.headers.Authorization = `Bearer ${browser.accessToken}`
        }

        return config
      },
      error => {
        return Promise.reject(error)
      }
    )

    this.http.interceptors.response.use(
      response => {
        if (
          response.status < 400 &&
          response.data?.error?.code
        ) {
          return {
            code: 500,
            error: response.data.error.message,
            errorCode: response.data.error.code
          }
        }

        return {
          code: response.status,
          data: response.data,
          contentType: response.headers['content-type']
        }
      },

      async error => {
        if (error.response) {
          const status = error.response.status

          if (status === 403) {
            ElMessage.error(i18n.global.t('无此权限'))

            return Promise.reject({
              code: 403,
              error: i18n.global.t('无此权限')
            })
          }

          if (status === 401) {
            if (browser.accessToken) {
              browser.setAccessToken('')

              router.push({
                path: '/login',
                query: {
                  redirect: router.currentRoute.value.fullPath
                }
              })
            }

            return Promise.reject({
              code: 401,
              error: '会话过期，请重新登陆'
            })
          }

          return Promise.reject({
            code: status,
            error:
              error.response.statusText ||
              error.response.data ||
              status
          })
        }

        return Promise.reject({
          code: 500,
          error: error.message || error.stack
        })
      }
    )
  }

  request(url, params = {}, method = 'get') {
    ElMessage.closeAll()

    return this.http({
      method,
      url,
      ...params
    }).catch(error => {
      console.error(error)

      if (axios.isCancel(error)) {
        return
      }

      return Promise.reject(error)
    })
  }

  get(url, params) {
    return this.request(url, params, 'get')
  }

  post(url, params) {
    return this.request(url, params, 'post')
  }

  put(url, params) {
    return this.request(url, params, 'put')
  }

  patch(url, params) {
    return this.request(url, params, 'patch')
  }

  delete(url, params) {
    return this.request(url, params, 'delete')
  }
}