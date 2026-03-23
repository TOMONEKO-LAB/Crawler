public class FetchResult {
  private final String url;
  private final int statusCode;
  private final byte[] body;
  private final String contentType;
  private final String charset;

  public FetchResult(String url, int statusCode, byte[] body, String contentType, String charset) {
    this.url = url;
    this.statusCode = statusCode;
    this.body = body;
    this.contentType = contentType;
    this.charset = charset;
  }

  public String getUrl() {
    return url;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public byte[] getBody() {
    return body;
  }

  public String getContentType() {
    return contentType;
  }

  public String getCharset() {
    return charset;
  };

  public boolean isHtml() {
    return contentType != null && contentType.contains("text/html");
  }

  public static void main(String[] args) {
    FetchResult result = new FetchResult(
        "https://example.com",
        200,
        "<html></html>".getBytes(),
        "text/html; charset=UTF-8",
        "UTF-8");
    System.out.println("url=" + result.getUrl());
    System.out.println("status=" + result.getStatusCode());
    System.out.println("isHtml=" + result.isHtml());
  }
}