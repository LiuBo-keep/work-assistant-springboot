package com.work.assistant.common.crypto;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RSA 密钥对管理器。
 *
 * <p>负责密码传输加解密所用的 RSA 密钥对的生成、持久化与加载：
 * <ul>
 *   <li>首次启动时生成一对密钥并写入本地文件（私钥 + 公钥各一个文件）；</li>
 *   <li>后续启动直接加载既有密钥，保证历史密文始终可解；</li>
 *   <li>公钥通过接口下发给客户端，私钥仅服务端持有。</li>
 * </ul>
 *
 * @author work-assistant
 */
@Component
public class RsaKeyManager {

  private static final Logger log = LoggerFactory.getLogger(RsaKeyManager.class);

  /**
   * 密钥文件存储目录，默认 {@code ./data/rsa}，可通过配置覆盖。
   */
  @Value("${work.assistant.rsa.key-dir:./data/rsa}")
  private String keyDir;

  private PublicKey publicKey;
  private PrivateKey privateKey;

  @PostConstruct
  public synchronized void init() {
    try {
      Path dir = Path.of(keyDir);
      Files.createDirectories(dir);
      Path pubFile = dir.resolve("public.pem");
      Path priFile = dir.resolve("private.pem");

      if (Files.exists(priFile) && Files.exists(pubFile)) {
        privateKey = RsaCipherUtils.privateKeyFromBase64(
            Files.readString(priFile, StandardCharsets.UTF_8).trim());
        publicKey = RsaCipherUtils.publicKeyFromBase64(
            Files.readString(pubFile, StandardCharsets.UTF_8).trim());
        log.info("RSA key pair loaded from {}", dir.toAbsolutePath());
      } else {
        KeyPair keyPair = RsaCipherUtils.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        Files.writeString(priFile, RsaCipherUtils.privateKeyToBase64(privateKey),
            StandardCharsets.UTF_8);
        Files.writeString(pubFile, RsaCipherUtils.publicKeyToBase64(publicKey),
            StandardCharsets.UTF_8);
        log.info("RSA key pair generated and persisted to {}", dir.toAbsolutePath());
      }
    } catch (Exception e) {
      throw new IllegalStateException("failed to initialize RSA key manager", e);
    }
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  /**
   * 公钥 Base64（X.509），用于下发客户端。
   */
  public String getPublicKeyBase64() {
    return RsaCipherUtils.publicKeyToBase64(publicKey);
  }
}
