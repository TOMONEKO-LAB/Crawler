import org.jsoup.nodes.*;                   // Document, Element
import org.jsoup.select.Elements;

public class Parser {
  private final Document document;          // ダウンロード対象のHTMLドキュメント

  public Parser(Document document) {
    if (document == null) {
      throw new IllegalArgumentException("document must not be null");
    }
    this.document = document;
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
