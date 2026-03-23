import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class ParseResult {
  private final Document document;
  private final Elements images;
  private final Elements scripts;
  private final Elements styles;
  private final Elements anchor;

  public ParseResult(Document document, Elements images, Elements scripts, Elements styles, Elements anchor) {
    this.document = document;
    this.images = images;
    this.scripts = scripts;
    this.styles = styles;
    this.anchor = anchor;
  }

  public Document getDocument() {
    return document;
  };

  public Elements getImages() {
    return images;
  };

  public Elements getScripts() {
    return scripts;
  };

  public Elements getStyles() {
    return styles;
  };

  public Elements getAnchorTags() {
    return anchor;
  };

  public static void main(String[] args) {
    Document doc = Jsoup.parse("<html><head><link rel=\"stylesheet\" href=\"a.css\"></head><body><img src=\"a.png\"><script src=\"a.js\"></script><a href=\"/x\">x</a></body></html>", "https://example.com");
    ParseResult result = new ParseResult(
        doc,
        doc.select("img[src]"),
        doc.select("script[src]"),
        doc.select("link[rel=stylesheet][href]"),
        doc.select("a[href]"));
    System.out.println("images=" + result.getImages().size());
    System.out.println("scripts=" + result.getScripts().size());
    System.out.println("styles=" + result.getStyles().size());
    System.out.println("links=" + result.getAnchorTags().size());
  }
}