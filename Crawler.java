import java.io.*;                           // ByteArrayInputStream, FileOutputStream, IOException, InputStream, OutputStream;
import java.nio.file.*;                     // Files, Path, Paths
import java.util.stream.Collectors;
import org.jsoup.nodes.*;                   // Document, Element
import java.net.URI;
import org.jsoup.Jsoup;

public class Crawler {
  private final String url;                 // フェッチ先URL
  private final Settings settings;          // 設定
  private final Fetcher fetcher;            // Fetchするためのインスタンス

  public Crawler(String url, Settings settings) {
    this.url = url;
    this.settings = settings;
    this.fetcher = new Fetcher(settings);
  }

  public void crawl() throws Exception {
    crawl(settings.getDepth());
  }

  private void crawl(int depth) throws Exception {
    Document doc = fetchDocument();
    if (doc == null) {
      return;
    }
    Parser parser = new Parser(doc, settings);
    processStylesheets(parser);
    processScripts(parser);
    processImages(parser);
    processIcons(parser);
    if (depth > 0) {
      for (Element element : parser.getHtml()) {
        try{
          String url = element.absUrl("href");
          Path newSaveDirectory = settings.getSaveDirectory().resolve(String.valueOf(Math.abs(url.hashCode())));
          Settings newSettings = new Settings(newSaveDirectory, depth - 1);
          newSettings.setDebug(settings.isDebug());
          Crawler crawler = new Crawler(url, newSettings);
          crawler.crawl(depth - 1);
          if (settings.getSaveDirectory().getParent() != null) {
            Path parentIndex = settings.getSaveDirectory().resolve("index.html").getParent().relativize(settings.getSaveDirectory().getParent().resolve("index.html"));
            element.attr("href", "./" + parentIndex.toString());
          } else {
            Path relativePath = settings.getSaveDirectory().relativize(newSaveDirectory.resolve("index.html"));
            element.attr("href", "./" + relativePath.toString());
          }
        } catch (Exception e) {
          if (settings.isDebug()) {
            System.err.println("Failed to crawl: " + element.absUrl("href"));
            e.printStackTrace();
          }
        }
      }
    }
    processHtml(doc);
  }

  protected Document fetchDocument() throws IOException {
    String result = fetcher.download(url, settings.getSaveDirectory().resolve("index.html"));
    if (result == null) {
      return null;
    }
    return Jsoup.parse(
      Files.lines(settings.getSaveDirectory().resolve("index.html"))
      .collect(Collectors.joining(System.getProperty("line.separator"))),
      url
    );
  }

  protected void processStylesheets(Parser parser) throws Exception {
    Path cssDir = settings.getSaveDirectory().resolve("stylesheets");
    Files.createDirectories(cssDir);

    for (Element link : parser.selectStylesheets()) {
      String href = link.absUrl("href");
      if (!fetcher.isDownloadable(href)) continue;

      Path p = Paths.get(new URI(href).getPath());
      if (p.getFileName() == null) continue;
      String filename = p.getFileName().toString();

      Path dest = cssDir.resolve(filename);
      String downloadedPath = fetcher.download(href, dest);

      parser.rewriteAttribute(link, "href", downloadedPath);
    }
  }

  protected void processScripts(Parser parser) throws Exception {
    Path jsDir = settings.getSaveDirectory().resolve("scripts");
    Files.createDirectories(jsDir);

    for (Element script : parser.selectScripts()) {
      String href = script.absUrl("src");
      if (!fetcher.isDownloadable(href)) continue;

      Path p = Paths.get(new URI(href).getPath());
      if (p.getFileName() == null) continue;
      String filename = p.getFileName().toString();

      Path dest = jsDir.resolve(filename);
      String downloadedPath = fetcher.download(href, dest);

      parser.rewriteAttribute(script, "src", downloadedPath);
    }
  }

  protected void processImages(Parser parser) throws Exception {
    Path imgDir = settings.getSaveDirectory().resolve("images");
    Files.createDirectories(imgDir);

    for (Element img : parser.selectImages()) {
      String href = img.absUrl("src");
      if (!fetcher.isDownloadable(href)) continue;

      Path p = Paths.get(new URI(href).getPath());
      if (p.getFileName() == null) continue;
      String filename = p.getFileName().toString();

      Path dest = imgDir.resolve(filename);
      String downloadedPath = fetcher.download(href, dest);

      parser.rewriteAttribute(img, "src", downloadedPath);
    }
  }

  protected void processIcons(Parser parser) throws Exception {
    Path iconDir = settings.getSaveDirectory().resolve("icons");
    Files.createDirectories(iconDir);

    for (Element link : parser.selectIcons()) {
      String href = link.absUrl("href");
      if (!fetcher.isDownloadable(href)) continue;

      Path p = Paths.get(new URI(href).getPath());
      if (p.getFileName() == null) continue;
      String filename = p.getFileName().toString();

      Path dest = iconDir.resolve(filename);
      String downloadedPath = fetcher.download(href, dest);

      parser.rewriteAttribute(link, "href", downloadedPath);
    }
  }

  protected void processHtml(Document document) throws IOException {
    Path htmlPath = settings.getSaveDirectory().resolve("index.html");
    try (
      InputStream in = new ByteArrayInputStream(document.html().getBytes(settings.getDefaultCharSet()));
      OutputStream out = new FileOutputStream(htmlPath.toString())
    ) {
      fetcher.copy(in, out);
    }
  }

  // テストコード
  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java Crawler <url>");
      return;
    }
    try {
      // 1. 設定生成
      Settings settings = new Settings(Paths.get("rakuten"), 1);
      settings.setDebug(true);

      // 2. クローラ生成
      String url = args[0];
      Crawler crawler = new Crawler(url, settings);

      // 3. 実行
      crawler.crawl();

      System.out.println("Crawling finished.");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
