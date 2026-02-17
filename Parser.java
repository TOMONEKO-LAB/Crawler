import org.jsoup.nodes.*;                   // Document, Element
import org.jsoup.select.Elements;

public class Parser {
  private final Document document;          // ダウンロード対象のHTMLドキュメント
  private final Fetcher fetcher;            // Fetchするためのインスタンス
  private final Settings settings;          // 設定

  public Parser(Document document, Settings settings) throws Exception {
    if (document == null) {
      throw new IllegalArgumentException("document must not be null");
    }
    this.document = document;
    this.settings = settings;
    this.fetcher = new Fetcher(settings);
  }

  public Elements selectDownloadableElements() throws Exception {
    Elements elements = new Elements();
    for (Element element : document.getAllElements()) {
      if (element.hasAttr("src") && fetcher.isDownloadable(element.absUrl("src"))) {
        elements.add(element);
      }
      if (element.hasAttr("href") && fetcher.isDownloadable(element.absUrl("href"))) {
        elements.add(element);
      }
      if (element.hasAttr("content") && fetcher.isDownloadable(element.absUrl("content"))) {
        elements.add(element);
      }
    }
    return elements;
  }

  public Elements getHtml () throws Exception {
    Elements urls = new Elements();
    String attrUrl = "";
    for (Element element : document.select("a[href]")) {
      attrUrl = element.absUrl("href");
      if (attrUrl.contains("#")) {
        continue;
      }
      if (!attrUrl.isEmpty() && !urls.contains(element)) {
        if (settings.isDebug()) {
          System.out.println("Found downloadable HTML element: " + element);
        }
        urls.add(element);
      }
    }
    return urls;
  }

  public Elements selectStylesheets() {
    return document.select("link[rel=stylesheet]");
  }

  public Elements selectScripts() {
    return document.select("script[src]");
  };

  public Elements selectImages() {
    return document.select("img");
  }

  public Elements selectIcons() {
    Elements links = document.select("link");
    Elements icons = new Elements();
    for (Element link : links) {
      if (link.attr("rel").contains("icon")) {
        icons.add(link);
      }
    }
    return icons;
  }

  public void rewriteAttribute(Element element, String attribute, String newValue) {
    element.attr(attribute, newValue);
  }

}
