package com.work.assistant.common.security;

import com.work.assistant.common.constant.CryptoConstants;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 轻量登录令牌工具。
 *
 * <p>不引入额外依赖，基于 JDK 内置 HmacSHA256 实现签名令牌。
 * 令牌格式：{@code payloadBase64.signatureHex}，其中 payload 为
 * JSON 形式的 {@code {"userName": "...", "role": "...", "expireAt": <毫秒时间戳>}}。
 *
 * <p>设计要点：
 * <ul>
 *   <li>签名密钥由系统内 AES 密码派生，保证与服务端绑定。</li>
 *   <li>令牌含过期时间，鉴权时校验有效性，防止长期复用。</li>
 *   <li>令牌携带用户角色，供鉴权拦截器与前端做目录级权限控制。</li>
 *   <li>避免在令牌中存放敏感明文，仅存用户名、角色与过期时间。</li>
 * </ul>
 *
 * @author aidan.liu
 */
public final class TokenUtils {

  /**
   * 令牌有效期。
   */
  public static final Duration TOKEN_TTL = Duration.ofHours(12);

  private static final String HMAC_ALGORITHM = "HmacSHA256";

  private TokenUtils() {
  }

  /**
   * 签发令牌。
   *
   * @param userName 用户名
   * @param role     用户角色（写入令牌，供权限控制）
   * @return 令牌字符串
   */
  public static String issueToken(String userName, String role) {
    long expireAt = System.currentTimeMillis() + TOKEN_TTL.toMillis();
    String payload = "{\"userName\":\"" + escape(userName) + "\","
        + "\"role\":\"" + escape(role == null ? "" : role) + "\","
        + "\"expireAt\":" + expireAt + "}";
    String payloadBase64 = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    return payloadBase64 + "." + sign(payloadBase64);
  }

  /**
   * 校验并解析令牌，返回令牌携带的用户信息。
   *
   * @param token 令牌
   * @return 解析出的用户信息（含用户名与角色）；无效或过期返回 {@code null}
   */
  public static TokenInfo verify(String token) {
    if (token == null || token.isEmpty()) {
      return null;
    }
    int dot = token.lastIndexOf('.');
    if (dot <= 0 || dot == token.length() - 1) {
      return null;
    }
    String payloadBase64 = token.substring(0, dot);
    String signature = token.substring(dot + 1);

    // 常量时间比较签名，降低时序侧信道风险。
    if (!constantTimeEquals(sign(payloadBase64), signature)) {
      return null;
    }

    try {
      String payload = new String(
          Base64.getUrlDecoder().decode(payloadBase64),
          StandardCharsets.UTF_8);
      Map<String, Object> map = parsePayload(payload);
      long expireAt = ((Number) map.get("expireAt")).longValue();
      if (System.currentTimeMillis() > expireAt) {
        return null;
      }
      TokenInfo info = new TokenInfo();
      info.userName = (String) map.get("userName");
      info.role = (String) map.get("role");
      return info;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 校验并解析令牌，仅返回用户名。
   *
   * @param token 令牌
   * @return 解析出的用户名；无效或过期返回 {@code null}
   */
  public static String verifyAndGetUserName(String token) {
    TokenInfo info = verify(token);
    return info == null ? null : info.userName;
  }

  /**
   * 令牌解析结果（用户名 + 角色）。
   */
  public static class TokenInfo {
    private String userName;
    private String role;

    public String getUserName() {
      return userName;
    }

    public String getRole() {
      return role;
    }
  }

  private static String sign(String data) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(CryptoConstants.SERVER_SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
      byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        sb.append(String.format("%02x", b & 0xff));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("failed to sign token", e);
    }
  }

  private static boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null) {
      return false;
    }
    byte[] ba = a.getBytes(StandardCharsets.UTF_8);
    byte[] bb = b.getBytes(StandardCharsets.UTF_8);
    if (ba.length != bb.length) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < ba.length; i++) {
      result |= ba[i] ^ bb[i];
    }
    return result == 0;
  }

  private static Map<String, Object> parsePayload(String payload) {
    // 极简 JSON 解析，仅处理本工具签发的固定结构。
    Map<String, Object> map = new HashMap<>();
    String s = payload.trim();
    if (!s.startsWith("{") || !s.endsWith("}")) {
      throw new IllegalArgumentException("invalid payload");
    }
    String body = s.substring(1, s.length() - 1);
    String[] pairs = body.split(",");
    for (String pair : pairs) {
      int colon = pair.indexOf(':');
      if (colon <= 0) {
        continue;
      }
      String key = pair.substring(0, colon).trim();
      String value = pair.substring(colon + 1).trim();
      key = key.replace("\"", "");
      if (value.startsWith("\"")) {
        value = value.substring(1, value.length() - 1);
        map.put(key, value);
      } else {
        map.put(key, Long.parseLong(value));
      }
    }
    return map;
  }

  private static String escape(String s) {
    return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
