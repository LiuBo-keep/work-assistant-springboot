package com.work.assistant.biz.password.model;

import com.work.assistant.config.database.AesEncryptorConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 密码本条目。
 *
 * <p>密码由服务端存储，字段含义：
 * <ul>
 *   <li>{@code account} / {@code password}：账号与密码。密码使用服务端 AES 落库加密
 *       （{@link AesEncryptorConverter}），传输过程中则使用 RSA 非对称加密。</li>
 *   <li>{@code owner}：所属用户，取自鉴权令牌中的用户名，用于多用户数据隔离。</li>
 * </ul>
 *
 * @author work-assistant
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "password_entry")
public class PasswordEntry {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 条目名称（如 OA 系统、GitLab）
   */
  @Column(name = "name")
  private String name;

  /**
   * 分类
   */
  @Column(name = "category")
  private String category;

  /**
   * 账号 / 用户名
   */
  @Column(name = "account")
  private String account;

  /**
   * 密码（服务端 AES 落库加密）
   */
  @Convert(converter = AesEncryptorConverter.class)
  @Column(name = "password")
  private String password;

  /**
   * 网站地址（可选）
   */
  @Column(name = "url")
  private String url;

  /**
   * 备注（可选）
   */
  @Column(name = "remark")
  private String remark;

  /**
   * 所属用户（数据隔离）
   */
  @Column(name = "owner")
  private String owner;

  /**
   * 创建时间
   */
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * 更新时间
   */
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Version
  @Column(name = "version_num")
  private Integer versionNum = 0;
}
