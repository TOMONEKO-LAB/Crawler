import java.util.*; // Set, HashSet

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

class StyleSheetProcess extends Process {
  private final UrlNormalizer normalizer; // URLを正規化するためのインスタンス

  public StyleSheetProcess(Fetcher fetcher, PathResolver resolver, Settings settings) {
    super(fetcher, resolver, settings);
    this.normalizer = new UrlNormalizer();
  }

  // link[rel=stylesheet][href]の処理
  public Set<String> execute(ParseResult result) throws Exception {
    String baseUrl = result.getDocument().location();
    String parentPath = resolver.resolveHtmlPath(baseUrl);
    Set<String> missing = new HashSet<>();

    for (Element element : result.getStyles()) {
      String href = element.attr("href");
      String normalizedUrl = normalizer.normalize(href, baseUrl);

      // URLが正規化できない場合はスキップ
      if (normalizedUrl == null) {
        continue;
      }

      // fetchしてパスの書き換え
      FetchResult fetchResult = fetcher.fetch(normalizedUrl);
      String localPath = resolver.resolveAssetPath(parentPath, normalizedUrl, "stylesheets", fetchResult.getContentType());
      element.attr("href", resolver.relativize(parentPath, localPath));

      // HTTPステータスコードが200番台なら保存
      if (fetchResult.getStatusCode() >= 200 && fetchResult.getStatusCode() < 300) {
        try {
          saver.saveAssets(localPath, fetchResult.getBody());

          // 保存に失敗した場合はmissingに追加
        } catch (Exception e) {
          missing.add(normalizedUrl);
        }

        // HTTPステータスコードが200番台以外ならmissingに追加
      } else {
        missing.add(normalizedUrl);
      }
    }
    if (settings.isDebug()) {
      System.out.println("[StyleSheetProcess]: " + "base=" + baseUrl + ", missing=" + missing.size());
    }
    return missing;
  }

  public static void main(String[] args) throws Exception {
    Settings settings = new Settings();
    StyleSheetProcess process = new StyleSheetProcess(new Fetcher(settings), new PathResolver(settings), settings);
    org.jsoup.nodes.Document doc = Jsoup.parse("<html><head></head><body></body></html>", "https://example.com");
    ParseResult result = new ParseResult(doc, doc.select("img[src]"), doc.select("script[src]"), doc.select("link[rel=stylesheet][href]"), doc.select("a[href]"));
    process.execute(result);
    System.out.println("StyleSheetProcess smoke test done");
  }
}