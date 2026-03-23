import java.net.URI;

public class UrlNormalizer {

  // URLを正規化
  public String normalize(String url) {
    return normalize(url, null);
  }

  // URLを正規化
  public String normalize(String url, String baseUrl) {
    if (!isDownloadable(url)) {
      return null;
    }
    try {
      URI resolved;
      if (baseUrl == null || baseUrl.isBlank()) {
        resolved = URI.create(url);
      } else {
        URI base = URI.create(baseUrl);
        resolved = base.resolve(url);
      }
      return resolved.normalize().toString();
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  // URLがダウンロード対象外かどうかを判定する
  public boolean isDownloadable(String url) {
    // URLがnullならダウンロード対象外
    if (url == null) {
      return false;
    }
    String value = url.trim();
    // URLが空文字列またはフラグメントの場合はダウンロード対象外
    if (value.isEmpty() || value.startsWith("#")) {
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    UrlNormalizer normalizer = new UrlNormalizer();
    System.out.println(normalizer.normalize("https://example.com/a/../b", null));
    System.out.println(normalizer.normalize("../img/logo.png", "https://example.com/a/index.html"));
    System.out.println(normalizer.isDownloadable("#fragment"));
  }
}