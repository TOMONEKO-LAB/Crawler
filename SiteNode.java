public class SiteNode {
  private String url; // URL
  private int depth;  // 深さ

  public SiteNode(String url, int depth) {
    this.url = url;
    this.depth = depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getDepth() {
    return depth;
  }

  public String getUrl() {
    return url;
  }
}
