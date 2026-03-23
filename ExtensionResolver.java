public class ExtensionResolver {

  private final Settings settings;  // 設定

  public ExtensionResolver(Settings settings) {
    this.settings = settings;
  }

  // Content-Typeから拡張子を解決
  public String resolveExtension(String contentType) {
    switch (contentType.split(";")[0].trim()) {
      case "application/octet-stream": return "exe";
      case "application/json": return "json";
      case "application/pdf": return "pdf";
      case "application/vnd.ms-excel": return "xls";
      case "application/rsds": return "rsd";
      case "application/rsd+xml": return "xml";
      case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": return "xlsx";
      case "application/vnd.ms-powerpoint": return "ppt";
      case "application/vnd.openxmlformats-officedocument.presentationml.presentation": return "pptx";
      case "application/msword": return "doc";
      case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": return "docx";
      case "application/zip": return "zip";
      case "application/x-lzh": return "lzh";
      case "application/x-tar": return "tar";
      case "application/gzip": return "gz";

      case "text/plain": return "txt";
      case "text/csv": return "csv";
      case "text/html": return "html";
      case "text/css": return "css";
      case "text/javascript":
      case "application/javascript":
      case "application/x-javascript":
        return "js";
      case "text/xml": return "xml";

      case "image/png": return "png";
      case "image/jpeg": return "jpg";
      case "image/gif": return "gif";
      case "image/bmp":
      case "image/x-ms-bmp":
        return "bmp";
      case "image/webp": return "webp";
      case "image/svg+xml": return "svg";
      case "image/tiff": return "tiff";
      case "image/x-icon":
      case "image/vnd.microsoft.icon":
        return "ico";

      case "audio/mpeg": return "mp3";
      case "audio/wav": return "wav";
      case "audio/midi": return "midi";

      case "video/mp4": return "mp4";
      case "video/mpeg": return "mpeg";
      case "video/avi": return "avi";
      case "video/3gp": return "3gp";
      default: return settings.getDefaultExtension();
    }
  }

  public static void main(String[] args) {
    Settings settings = new Settings();
    ExtensionResolver resolver = new ExtensionResolver(settings);
    System.out.println(resolver.resolveExtension("image/png"));
    System.out.println(resolver.resolveExtension("application/javascript; charset=utf-8"));
    System.out.println(resolver.resolveExtension("unknown/type"));
  }
}
