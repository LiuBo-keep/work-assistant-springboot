package com.work.assistant.config.retrofit;

import com.work.assistant.common.exception.AppErrorCode;
import com.work.assistant.common.exception.WorkAssistantException;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.TlsVersion;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Slf4j
@Component
public class OkHttpClientProvider {

  /**
   * 下述都是表示秒。
   */
  private static final int DEFAULT_READ_25S_TIMEOUT = 25;

  private static final int DEFAULT_CONNECT_25S_TIMEOUT = 25;

  private static final int DEFAULT_WRITE_25S_TIMEOUT = 25;

  private static final int HTTP_MAX_120S_TIMEOUT = 120;

  private final ConnectionPool connectionPool;

  public OkHttpClientProvider(ConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  /**
   * 自签名证书相关
   */
  public static SSLSocketFactory createSslSocketFactory() {

    SSLSocketFactory ssfFactory = null;
    try {
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, new TrustManager[] {new TrustAllCerts()}, new SecureRandom());
      ssfFactory = sc.getSocketFactory();
    } catch (Exception ex) {
      log.error("", ex);
    }
    return ssfFactory;
  }

  /**
   * 请使用此方法构建okHttpClient
   * 注意：如果直接使用OkHttpClient，OKHTTP的Response必须手动关闭。
   * 注意：如果直接使用OkHttpClient，OKHTTP的Response必须手动关闭。
   * 注意：如果直接使用OkHttpClient，OKHTTP的Response必须手动关闭。
   */
  public OkHttpClient generateOkHttpClient(OkHttpConfigBo okHttpConfigBo) throws WorkAssistantException {

    // 构建处理tls相关对象
    HttpsTslConfig httpsTslConfig;
    if (okHttpConfigBo.getCertificateInfo() != null && StringUtils.isNoneBlank(okHttpConfigBo.getCertificatePwd())) {
      httpsTslConfig = this.generateTslConfig(okHttpConfigBo.getCertificateInfo(), okHttpConfigBo.getCertificatePwd());
    } else {
      httpsTslConfig = this.generateTslConfig(null, null);
    }

    // 开始构建OkHttpClient
    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();

    // SEP特殊的处理
    if (BooleanUtils.isTrue(okHttpConfigBo.isEnableSepUrlInterceptor())) {
      // 如果是sep，启用这个url的参数处理
      okHttpClientBuilder.addInterceptor((Interceptor.Chain chain) -> {
        Request originalRequest = chain.request();
        HttpUrl.Builder urlBuilder = originalRequest.url().newBuilder();
        String query = originalRequest.url().encodedQuery();
        if (query != null && query.length() > 0) {
          // query参数中部分有特殊含义, 不进行编码
          query = query.replace("%3D", "=");
          query = query.replace("%3B", ";");
          query = query.replace("%3A", ":");
          query = query.replace("%2C", ",");
          urlBuilder.query(query);
        }
        Request request = originalRequest.newBuilder()
            .url(urlBuilder.build())
            .build();
        return chain.proceed(request);
      });
    }

    // timeout相关的处理，最大不能超过2分钟（超过就是120s）。最小不能小于等于0（小于等于0就是25s）.
    if (okHttpConfigBo.getReadTimeout() > HTTP_MAX_120S_TIMEOUT) {
      okHttpClientBuilder.readTimeout(HTTP_MAX_120S_TIMEOUT, TimeUnit.SECONDS);
    } else if (okHttpConfigBo.getReadTimeout() <= 0) {
      okHttpClientBuilder.readTimeout(DEFAULT_READ_25S_TIMEOUT, TimeUnit.SECONDS);
    } else {
      okHttpClientBuilder.readTimeout(okHttpConfigBo.getReadTimeout(), TimeUnit.SECONDS);
    }

    if (okHttpConfigBo.getConnectTimeout() > HTTP_MAX_120S_TIMEOUT) {
      okHttpClientBuilder.connectTimeout(HTTP_MAX_120S_TIMEOUT, TimeUnit.SECONDS);
    } else if (okHttpConfigBo.getReadTimeout() <= 0) {
      okHttpClientBuilder.connectTimeout(DEFAULT_CONNECT_25S_TIMEOUT, TimeUnit.SECONDS);
    } else {
      okHttpClientBuilder.connectTimeout(okHttpConfigBo.getConnectTimeout(), TimeUnit.SECONDS);
    }

    if (okHttpConfigBo.getWriteTimeout() > HTTP_MAX_120S_TIMEOUT) {
      okHttpClientBuilder.writeTimeout(HTTP_MAX_120S_TIMEOUT, TimeUnit.SECONDS);
    } else if (okHttpConfigBo.getReadTimeout() <= 0) {
      okHttpClientBuilder.writeTimeout(DEFAULT_WRITE_25S_TIMEOUT, TimeUnit.SECONDS);
    } else {
      okHttpClientBuilder.writeTimeout(okHttpConfigBo.getWriteTimeout(), TimeUnit.SECONDS);
    }

    okHttpClientBuilder
        .connectionPool(connectionPool)
        .retryOnConnectionFailure(okHttpConfigBo.isRetryOnConnectionFailure())
        .sslSocketFactory(httpsTslConfig.getSslSocketFactory(), httpsTslConfig.getTrustManager())
        .hostnameVerifier(new TrustAllHostnameVerifier());

    return okHttpClientBuilder.build();
  }

  /**
   * 支持双向认证,可加缓存防止每次都重新构建证书信息.
   */
  public HttpsTslConfig generateTslConfig(byte[] certificateInfo, String certificatePwd) throws
      WorkAssistantException {
    try {
      // 有证书没密码就报错。
      if (certificateInfo != null && StringUtils.isBlank(certificatePwd)) {
        throw new WorkAssistantException(AppErrorCode.INTERNAL_SERVER_ERROR, "没有有效的双向证书的密码");
      }

      // 注意,这里都是okHttp的写法.参考OkHttpClient.Builder().sslSocketFactory方法的注释.
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
        throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
      }
      X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

      SSLContext sslContext = SSLContext.getInstance(TlsVersion.TLS_1_2.javaName());
      if (certificateInfo != null) {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        ByteArrayInputStream certificateInputStream = new ByteArrayInputStream(certificateInfo);
        char[] certificatePassword = certificatePwd.toCharArray();
        keyStore.load(certificateInputStream, certificatePassword);
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, certificatePassword);
        final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
        sslContext.init(keyManagers, new TrustManager[] {trustManager}, null);
      } else {
        sslContext.init(null, new TrustManager[] {new TrustAllCerts()}, new SecureRandom());
      }
      SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      return HttpsTslConfig.builder().sslSocketFactory(sslSocketFactory).trustManager(trustManager).build();
    } catch (Exception e) {
      e.printStackTrace();
      throw new WorkAssistantException(AppErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Builder
  @Data
  public static class HttpsTslConfig {
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;
  }

  /**
   * 实现HostnameVerifier接口
   */
  public static class TrustAllHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession session) {

      return true;
    }
  }

  /**
   * 实现X509TrustManager接口
   */
  public static class TrustAllCerts implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {

      return new X509Certificate[0];
    }
  }

}
