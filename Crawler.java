import java.io.*;                           // ByteArrayInputStream, FileOutputStream, IOException, InputStream, OutputStream;
import java.nio.file.*;                     // Files, Path, Paths
import org.jsoup.nodes.*;                   // Document, Element
import java.net.URI;
import org.jsoup.Jsoup;

public class Crawler {
  private final String url;                 // フェッチ先URL
  private final Settings settings;          // 設定
  private final FetchFiles fetcher;         // Fetchするためのインスタンス

    public Crawler(String url, Settings settings) {
      this.url = url;
      this.settings = settings;
      fetcher = new FetchFiles(settings);
    }

    public void crawl() throws Exception {

      Document doc = fetchDocument();
      Parser parser = new Parser(doc);

      processStylesheets(parser);
      processScripts(parser);
      processImages(parser);
      processIcons(parser);

      saveHtml(doc);
    }

    protected Document fetchDocument() throws IOException {
      return Jsoup.connect(url).get();
    }

    protected void processStylesheets(Parser parser) throws Exception {
      Path cssDir = settings.getSaveDirectory().resolve("css");
      Files.createDirectories(cssDir);

      for (Element link : parser.selectStylesheets()) {
        String href = link.absUrl("href");
        if (!fetcher.isDownloadable(href)) continue;

        Path p = Paths.get(new URI(href).getPath());
        if (p.getFileName() == null) continue;
        String filename = p.getFileName().toString();

        Path dest = cssDir.resolve(filename);
        fetcher.download(href, dest);

        parser.rewriteAttribute(link, "href", "css/" + filename);
      }
    }

    protected void processScripts(Parser parser) throws Exception {
      Path jsDir = settings.getSaveDirectory().resolve("js");
      Files.createDirectories(jsDir);

      for (Element script : parser.selectScripts()) {
        String href = script.absUrl("src");
        if (!fetcher.isDownloadable(href)) continue;

        Path p = Paths.get(new URI(href).getPath());
        if (p.getFileName() == null) continue;
        String filename = p.getFileName().toString();

        Path dest = jsDir.resolve(filename);
        fetcher.download(href, dest);

        parser.rewriteAttribute(script, "src", "js/" + filename);
      }
    }

    protected void processImages(Parser parser) throws Exception {
      Path imgDir = settings.getSaveDirectory().resolve("img");
      Files.createDirectories(imgDir);

      for (Element img : parser.selectImages()) {
        String href = img.absUrl("src");
        if (!fetcher.isDownloadable(href)) continue;

        Path p = Paths.get(new URI(href).getPath());
        if (p.getFileName() == null) continue;
        String filename = p.getFileName().toString();

        Path dest = imgDir.resolve(filename);
        fetcher.download(href, dest);

        parser.rewriteAttribute(img, "src", "img/" + filename);
      }
    }

    protected void processIcons(Parser parser) throws Exception {
      Path iconDir = settings.getSaveDirectory().resolve("icon");
      Files.createDirectories(iconDir);

      for (Element link : parser.selectIcons()) {
        String href = link.absUrl("href");
        if (!fetcher.isDownloadable(href)) continue;

        Path p = Paths.get(new URI(href).getPath());
        if (p.getFileName() == null) continue;
        String filename = p.getFileName().toString();

        Path dest = iconDir.resolve(filename);
        fetcher.download(href, dest);

        parser.rewriteAttribute(link, "href", "icon/" + filename);
      }
    }

    protected void saveHtml(Document document) throws IOException {
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
    try {
      // 1. 設定生成
      Settings settings = new Settings(Paths.get("www.rakuten.co.jp"));
      settings.setDebug(true);

      // 2. クローラ生成
      String url = "https://www.rakuten.co.jp/";
      Crawler crawler = new Crawler(url, settings);

      // 3. 実行
      crawler.crawl();

      System.out.println("Crawling finished.");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
