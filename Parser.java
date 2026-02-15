import java.util.*;                         // ArrayList, List
import org.jsoup.nodes.*;                   // Document, Element
import org.jsoup.select.Elements;

public class Parser {
  private final Document document;          // ダウンロード対象のHTMLドキュメント
  private final FetchFiles fetcher;         // Fetchするためのインスタンス

  public Parser(Document document, Settings settings) throws Exception {
    if (document == null) {
      throw new IllegalArgumentException("document must not be null");
    }
    this.document = document;
    this.fetcher = new FetchFiles(settings);
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
    for (Element element : selectDownloadableElements()) {
      if (element.hasAttr("src")) {
        attrUrl = element.absUrl("src");
      } else if (element.hasAttr("href")) {
        attrUrl = element.absUrl("href");
      } else if (element.hasAttr("content")) {
        attrUrl = element.absUrl("content");
      }
      if (!attrUrl.isEmpty() && fetcher.getContentType(attrUrl).startsWith("text/html") && !urls.contains(element)) {
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
