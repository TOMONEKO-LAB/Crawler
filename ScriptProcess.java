import java.util.*; // Set, HashSet

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

class ScriptProcess extends Process {
  private final UrlNormalizer normalizer; // URLを正規化するためのインスタンス

  public ScriptProcess(Fetcher fetcher, PathResolver resolver, Settings settings) {
    super(fetcher, resolver, settings);
    this.normalizer = new UrlNormalizer();
  }

  // script[src]の処理
  public Set<String> execute(ParseResult result) throws Exception {
    String baseUrl = result.getDocument().location();
    String parentPath = resolver.resolveHtmlPath(baseUrl);
    Set<String> missing = new HashSet<>();

    for (Element element : result.getScripts()) {
      String src = element.attr("src");
      String normalizedUrl = normalizer.normalize(src, baseUrl);

      // URLが正規化できない場合はスキップ
      if (normalizedUrl == null) {
        continue;
      }

      // fetchしてパスの書き換え
      FetchResult fecthResult = fetcher.fetch(normalizedUrl);
      String localPath = resolver.resolveAssetPath(parentPath, normalizedUrl, "scripts", fecthResult.getContentType());
      element.attr("src", resolver.relativize(parentPath, localPath));

      // HTTPステータスコードが200番台なら保存
      if (fecthResult.getStatusCode() >= 200 && fecthResult.getStatusCode() < 300) {
        try {
          saver.saveAssets(localPath, fecthResult.getBody());

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
      System.out.println("[ScriptProcess]: " + "base=" + baseUrl + ", missing=" + missing.size());
    }
    return missing;
  }

  public static void main(String[] args) throws Exception {
    Settings settings = new Settings();
    ScriptProcess process = new ScriptProcess(new Fetcher(settings), new PathResolver(settings), settings);
    org.jsoup.nodes.Document doc = Jsoup.parse("<html><head></head><body></body></html>", "https://example.com");
    ParseResult result = new ParseResult(doc, doc.select("img[src]"), doc.select("script[src]"), doc.select("link[rel=stylesheet][href]"), doc.select("a[href]"));
    process.execute(result);
    System.out.println("ScriptProcess smoke test done");
  }
}