import java.util.*; // Set, HashSet

import org.jsoup.Jsoup;
import org.jsoup.nodes.*; // Element, Document

public class ImageProcess extends Process {
  private final UrlNormalizer normalizer; // URLを正規化するためのインスタンス

  public ImageProcess(Fetcher fetcher, PathResolver resolver, Settings settings) {
    super(fetcher, resolver, settings);
    this.normalizer = new UrlNormalizer();
  }

  // img[src]とlink[rel*=icon][href]の処理
  public Set<String> execute(ParseResult result) throws Exception {
    Document doc = result.getDocument();
    String baseUrl = doc.location();
    String parentPath = resolver.resolveHtmlPath(baseUrl);
    Set<String> missing = new HashSet<>();

    // img[src]の処理
    for (Element element : result.getImages()) {
      handleAsset(element, "src", "images", baseUrl, parentPath, missing);
    }

    // link[rel*=icon][href]の処理
    for (Element icon : doc.select("link[rel*=icon][href]")) {
      handleAsset(icon, "href", "icons", baseUrl, parentPath, missing);
    }
    if (settings.isDebug()) {
      System.out.println("[ImageProcess]: " + "base=" + baseUrl + ", missing=" + missing.size());
    }
    return missing;
  }

  private void handleAsset(Element element, String attr, String dir, String baseUrl, String parentPath, Set<String> missing) throws Exception {
    String url = element.attr(attr);
    String normalizedUrl = normalizer.normalize(url, baseUrl);

    // URLが正規化できない場合はスキップ
    if (normalizedUrl == null) {
      return;
    }

    // fetchしてパスの書き換え
    FetchResult fetchResult = fetcher.fetch(normalizedUrl);
    String localPath = resolver.resolveAssetPath(parentPath, normalizedUrl, dir, fetchResult.getContentType());
    element.attr(attr, resolver.relativize(parentPath, localPath));

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

  public static void main(String[] args) throws Exception {
    Settings settings = new Settings();
    ImageProcess process = new ImageProcess(new Fetcher(settings), new PathResolver(settings), settings);
    Document doc = Jsoup.parse("<html><head></head><body></body></html>", "https://example.com");
    ParseResult result = new ParseResult(doc, doc.select("img[src]"), doc.select("script[src]"), doc.select("link[rel=stylesheet][href]"), doc.select("a[href]"));
    process.execute(result);
    System.out.println("ImageProcess smoke test done");
  }
}