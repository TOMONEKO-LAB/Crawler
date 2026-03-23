import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Saver {

  private final Settings settings;  // 設定

  public Saver(Settings settings) {
    this.settings = settings;
  }

  // アセットの保存
  public void saveAssets(String path, byte[] data) throws IOException {
    Path p = Paths.get(path);
    ensureParent(p);
    try (
      InputStream in = new ByteArrayInputStream(data);
      OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    ) {
      copy(in, out);
    }
    if (settings.isDebug()) {
      System.out.println("[Saver]: " + "save bytes path=" + path + ", size=" + data.length);
    }
  }

  // HTMLの保存
  public void saveHtml(String path, String html) throws IOException {
    Path p = Paths.get(path);
    ensureParent(p);
    try (
      InputStream in = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
      OutputStream out = Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    ) {
      copy(in, out);
    }
    if (settings.isDebug()) {
      System.out.println("[Saver]: " + "save html path=" + path + ", chars=" + html.length());
    }
  }

  // 書き込み処理
  private void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[settings.getBufferSize()];
    int n;
    while ((n = in.read(buffer)) != -1) {
      out.write(buffer, 0, n);
    }
  }

  // 親ディレクトリが存在しない場合は作成
  private void ensureParent(Path path) throws IOException {
    Path parent = path.getParent();
    if (parent != null && !Files.exists(parent)) {
      Files.createDirectories(parent);
    }
  }

  public static void main(String[] args) throws Exception {
    Settings settings = new Settings();
    Saver saver = new Saver(settings);
    String base = settings.getSaveDirectory().resolve("smoke").toString();
    saver.saveAssets(base + "/hello.txt", "hello".getBytes(StandardCharsets.UTF_8));
    saver.saveHtml(base + "/index.html", "<html><body>ok</body></html>");
    System.out.println("saved=" + base);
  }
}