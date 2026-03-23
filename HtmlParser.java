import java.nio.charset.*;  // Charset, StandardCharsets

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HtmlParser extends Parser {

  public HtmlParser(Settings settings) {
    super(settings);
  }

  // パース処理
  public ParseResult parse(FetchResult result) throws Exception {
    Document doc = buildDocument(result);
    ParseResult parsed = new ParseResult(
        doc,
        selectImages(doc),
        selectScripts(doc),
        selectStyles(doc),
        selectAnchorTags(doc)
    );
    if (settings.isDebug()) {
      System.out.println("[HtmlParser]: " + "parsed url=" + result.getUrl()
        + ", images=" + parsed.getImages().size()
        + ", scripts=" + parsed.getScripts().size()
        + ", styles=" + parsed.getStyles().size()
        + ", links=" + parsed.getAnchorTags().size());
    }
    return parsed;
  };

  // Fetchした内容からDocument作成
  private Document buildDocument(FetchResult result) {

    // Content-Typeヘッダからcharsetを取得
    String charsetName = result.getCharset() == null ? settings.getDefaultCharSet() : result.getCharset();
    Charset charset;
    try {
      charset = Charset.forName(charsetName);
    } catch (Exception e) {
      charset = Charset.forName(settings.getDefaultCharSet());
    }
    if (settings.isDebug()) {
      System.out.println("[HtmlParser]: " + "buildDocument charset=" + charset.name() + ", url=" + result.getUrl());
    }

    // HTMLを文字列として読み込んでDocument作成
    String html = new String(result.getBody(), charset);
    return Jsoup.parse(html, result.getUrl());
  };

  // img[src]
  private Elements selectImages(Document doc) {
    return doc.select("img[src]");
  };

  // script[src]
  private Elements selectScripts(Document doc) {
    return doc.select("script[src]");
  };

  // link[rel=stylesheet][href]
  private Elements selectStyles(Document doc) {
    return doc.select("link[rel=stylesheet][href]");
  };

  // a[href]
  private Elements selectAnchorTags(Document doc) {
    return doc.select("a[href]");
  };

  public static void main(String[] args) throws Exception {
    Settings settings = new Settings();
    HtmlParser parser = new HtmlParser(settings);
    String html = "<html><head><link rel=\"stylesheet\" href=\"s.css\"></head><body><img src=\"i.png\"><script src=\"a.js\"></script><a href=\"/next\">n</a></body></html>";
    FetchResult fetchResult = new FetchResult(
        "https://example.com/index.html",
        200,
        html.getBytes(StandardCharsets.UTF_8),
        "text/html; charset=UTF-8",
        "UTF-8");
    ParseResult result = parser.parse(fetchResult);
    System.out.println("images=" + result.getImages().size());
    System.out.println("scripts=" + result.getScripts().size());
    System.out.println("styles=" + result.getStyles().size());
    System.out.println("links=" + result.getAnchorTags().size());
  }
}