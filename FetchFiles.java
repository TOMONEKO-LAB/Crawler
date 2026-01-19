import java.io.*;               // IOException, InputStream, OutputStream
import java.nio.file.*;         // Path, Files
import java.net.URI;

public class FetchFiles {

  private final Settings settings;          // 設定

  public FetchFiles(Settings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("settings must not be null");
    }
    this.settings = settings;
  }

  public void download(String url, Path destination) throws IOException {
    Files.createDirectories(destination.getParent());
    try (
      InputStream in = URI.create(url).toURL().openStream();
      OutputStream out = new FileOutputStream(destination.toString())
    ) {
      copy(in, out);
    }

    if (settings.isDebug()) {
      System.out.println("Save: " + destination);
    }
  }

  public String resolveExtension(String url) {
    int lastDot = url.lastIndexOf('.');
    if (lastDot == -1) {
      return settings.getDefaultExtension();
    }
    String extension = url.substring(lastDot);
    return extension.replaceAll("(\\?.*)", "");
  }

  public boolean isDownloadable(String url) {
    return !(url.contains("#") || url.isEmpty());
  }

  protected void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[settings.getBufferSize()];
    int n;
    while ((n = in.read(buffer)) != -1) {
      out.write(buffer, 0, n);
    }
  }
}
