package com.work.assistant.biz.user.model;

/**
 * 系统用户角色。
 *
 * <p>普通用户仅可见「工作 / 效率 / 工具」三个业务目录；
 * 超管用户可见全部目录（含「系统」目录下的系统监控、备份管理、用户管理）。
 *
 * @author work-assistant
 */
public enum UserRole {

  /**
   * 普通用户。
   */
  NORMAL,

  /**
   * 超级管理员。
   */
  ADMIN
}
