import java.io.*;               // IOException, InputStream, OutputStream
import java.nio.file.*;         // Path, Files
import java.net.*;              // HttpURLConnection, URI

public class Fetcher {

  private final Settings settings;          // 設定

  public Fetcher(Settings settings) {
    if (settings == null) {
      throw new IllegalArgumentException("settings must not be null");
    }
    this.settings = settings;
  }

  public String download(String url, Path destination) throws IOException {
    Files.createDirectories(destination.getParent());
    HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
    if (connection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
      return null;
    }
    if (connection.getContentEncoding() != null && !connection.getContentEncoding().equals(settings.getDefaultCharSet())) {
      settings.setDefaultCharSet(connection.getContentEncoding());
    }
    String extension = resolveExtension(connection.getHeaderField("Content-Type"));
    String basename = destination.getFileName().toString();
    if (basename.lastIndexOf('.') == -1) {
      destination = destination.resolveSibling(basename + extension);
    } else if (connection.getURL().getQuery() != null &&  !connection.getContentType().contains("text/html")) {
      String query = connection.getURL().getQuery();
      destination = destination.resolveSibling(basename.substring(0, basename.lastIndexOf('.')) + query.hashCode() + extension);
    }
    try (
      InputStream in = connection.getInputStream();
      OutputStream out = new FileOutputStream(destination.toString())
    ) {
      copy(in, out);
    }

    if (settings.isDebug()) {
      System.out.println("Downloaded: " + url);
      System.out.println("Save: " + destination);
    }
    return destination.toString().replaceAll(settings.getSaveDirectory().toString(), ".");
  }

  private String resolveExtension(String contentType) {
    switch (contentType.split(";")[0].trim()) {
      case "application/octet-stream": return ".exe";
      case "application/json": return ".json";
      case "application/pdf": return ".pdf";
      case "application/vnd.ms-excel": return ".xls";
      case "application/rsds": return ".rsd";
      case "application/rsd+xml": return ".xml";
      case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return ".xlsx";
      case "application/vnd.ms-powerpoint": return ".ppt";
      case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return ".pptx";
      case "application/msword": return ".doc";
      case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return ".docx";
      case "application/zip": return ".zip";
      case "application/x-lzh": return ".lzh";
      case "application/x-tar": return ".tar";
      case "application/gzip": return ".gz";

      case "text/plain": return ".txt";
      case "text/csv": return ".csv";
      case "text/html": return ".html";
      case "text/css": return ".css";
      case "text/javascript": return ".js";
      case "text/xml": return ".xml";

      case "image/jpeg": return ".jpg";
      case "image/png": return ".png";
      case "image/gif": return ".gif";
      case "image/bmp": return ".bmp";
      case "image/x-ms-bmp": return ".bmp";
      case "image/svg+xml": return ".svg";
      case "image/tiff": return ".tiff";

      case "audio/mpeg": return ".mp3";
      case "audio/wav": return ".wav";
      case "audio/midi": return ".midi";

      case "video/mp4": return ".mp4";
      case "video/mpeg": return ".mpeg";
      case "video/avi": return ".avi";
      case "video/3gp": return ".3gp";

      default: return settings.getDefaultExtension();
    }
  }

  public boolean isDownloadable(String url) throws Exception{
    if (url.contains("#") || url.isEmpty()) {
      return false;
    }
    try {
      HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
      if (settings.isDebug()) {
        System.out.println("Checking if URL is downloadable: " + url);
        System.out.println("Content-Type: " + connection.getHeaderField("Content-Type"));
      }
      return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    } catch (Exception e) {
      return false;
    }
  }

  public String getContentType(String url) throws Exception {
    try {
      HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
      return connection.getHeaderField("Content-Type");
    } catch (Exception e) {
      return null;
    }
  }

  protected void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[settings.getBufferSize()];
    int n;
    while ((n = in.read(buffer)) != -1) {
      out.write(buffer, 0, n);
    }
  }
}
