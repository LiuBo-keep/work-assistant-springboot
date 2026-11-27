package com.work.assistant.biz.tool.service;

import com.work.assistant.biz.tool.metadata.ToolMetadataResolveResponse;
import com.work.assistant.config.retrofit.OkHttpClientProvider;
import com.work.assistant.config.retrofit.OkHttpConfigBo;
import java.net.URI;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

/**
 * 网页元信息解析：根据网址抓取网页，提取标题与图标。
 *
 * @author work-assistant
 */
@Slf4j
@Service
public class ToolMetadataResolver {

  private final OkHttpClientProvider okHttpClientProvider;

  public ToolMetadataResolver(OkHttpClientProvider okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  /**
   * 解析指定网址，提取名称（网页标题）与图标（favicon）。
   *
   * @param url 目标网址，需以 http/https 开头
   * @return 解析结果，抓取失败时仅返回原始 url
   */
  public ToolMetadataResolveResponse resolve(String url) {
    ToolMetadataResolveResponse result = new ToolMetadataResolveResponse();
    result.setUrl(url);

    String normalized = normalize(url);
    if (normalized == null) {
      return result;
    }

    OkHttpClient client = buildClient();
    Request request = new Request.Builder()
        .url(normalized)
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36")
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        log.warn("抓取网页失败，status={}, url={}", response.code(), normalized);
        return result;
      }
      String contentType = response.header("Content-Type", "");
      if (contentType.startsWith("text/html") || contentType.startsWith("application/xhtml")) {
        ResponseBody body = response.body();
        if (body != null) {
          String html = body.string();
          parse(html, normalized, result);
        }
      }
      // 记录最终地址（跟随重定向后的地址）
      result.setUrl(response.request().url().toString());
    } catch (Exception e) {
      log.warn("解析网页元信息异常，url={}, error={}", normalized, e.getMessage());
    }
    return result;
  }

  /**
   * 从 HTML 中提取标题与图标
   */
  private void parse(String html, String baseUrl, ToolMetadataResolveResponse result) {
    Document doc = Jsoup.parse(html, baseUrl);

    // 标题：优先 og:title，其次 <title>，去掉站点名后缀
    String title = metaContent(doc, "og:title");
    if (isBlank(title)) {
      Element titleEl = doc.selectFirst("title");
      title = titleEl == null ? null : titleEl.text();
    }
    if (!isBlank(title)) {
      result.setName(cleanTitle(title));
    }

    // 图标：优先 icon shortcut/link，其次 apple-touch-icon，最后默认 /favicon.ico
    String icon = extractIcon(doc);
    if (!isBlank(icon)) {
      result.setIcon(icon);
    }
  }

  private String extractIcon(Document doc) {
    // 1. <link rel="icon" / "shortcut icon" / "apple-touch-icon">
    Element link = doc.selectFirst("link[rel~=(?i)icon]");
    if (link == null) {
      link = doc.selectFirst("link[rel~=(?i)shortcut]");
    }
    if (link == null) {
      link = doc.selectFirst("link[rel~=(?i)apple-touch-icon]");
    }
    if (link != null) {
      String href = link.absUrl("href");
      if (!isBlank(href)) {
        return href;
      }
    }
    // 2. 兜底默认 favicon.ico
    return null;
  }

  private String metaContent(Document doc, String property) {
    Element el = doc.selectFirst("meta[property=" + property + "]");
    if (el == null) {
      el = doc.selectFirst("meta[name=" + property + "]");
    }
    return el == null ? null : el.attr("content");
  }

  private OkHttpClient buildClient() {
    try {
      OkHttpConfigBo config = OkHttpConfigBo.builder()
          .retryOnConnectionFailure(true)
          .readTimeout(8)
          .connectTimeout(8)
          .writeTimeout(8)
          .build();
      return okHttpClientProvider.generateOkHttpClient(config);
    } catch (Exception e) {
      log.error("构建 OkHttpClient 失败", e);
      return new OkHttpClient.Builder()
          .connectTimeout(Duration.ofSeconds(8))
          .readTimeout(Duration.ofSeconds(8))
          .build();
    }
  }

  private String normalize(String url) {
    if (isBlank(url)) {
      return null;
    }
    String trimmed = url.trim();
    if (!trimmed.matches("(?i)^https?://.*")) {
      trimmed = "https://" + trimmed;
    }
    try {
      // 校验并规范化，确保是合法 http/https 地址
      URI uri = URI.create(trimmed);
      if (uri.getHost() == null) {
        return null;
      }
      return uri.toString();
    } catch (Exception e) {
      return null;
    }
  }

  private String cleanTitle(String title) {
    if (isBlank(title)) {
      return title;
    }
    String cleaned = title.trim();
    // 常见站点名分隔符： | - – — · _ 之后的站点名移除
    String[] separators = {" | ", " - ", " – ", " — ", " · ", " _ "};
    for (String sep : separators) {
      int idx = cleaned.indexOf(sep);
      if (idx > 0) {
        cleaned = cleaned.substring(0, idx).trim();
        break;
      }
    }
    return cleaned.isEmpty() ? title.trim() : cleaned;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
