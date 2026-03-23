import java.nio.file.*; // Path, Paths
import java.util.*;     // ArrayList, Collections, List

public class Settings {
  private boolean debug;                            // デバッグモード
  private boolean rotateUserAgents;                 // User-Agentをローテーションするか
  private boolean sameOriginForPageLinks;           // ページ内のリンクは同一オリジンに限定するか
  private int depth;                                // 深さ
  private int timeout;                              // HTTP通信でのタイムアウト秒数
  private int maxPages;                             // クロールする最大ページ数
  private int retryCount;                           // Bot判定をもらった時のリトライ回数
  private int bufferSize;                           // バッファサイズ
  private int userAgentIndex;                       // User-Agentのインデックス
  private int pageBatchSize;                        // ページごとの並列処理数
  private int assetConcurrency;                     // アセットごとの並列処理数
  private int requestDelayMillis;                   // リクエスト間の遅延 : ms
  private int retryBackoffMillis;                   // リトライ時のバックオフ : ms
  private String userAgent;                         // 使用するUser-Agent
  private String defaultCharSet;                    // デフォルト文字コード
  private String defaultExtension;                  // デフォルト拡張子
  private Path saveDirectory;                       // 保存先ディレクトリ
  private List<String> userAgents;                  // User-Agentのリスト

  public Settings() {
    this.debug = false;
    this.rotateUserAgents = true;
    this.sameOriginForPageLinks = true;
    this.depth = 0;
    this.timeout = 20;
    this.maxPages = 500;
    this.retryCount = 1;
    this.bufferSize = 8192;
    this.userAgentIndex = 0;
    this.pageBatchSize = 8;
    this.assetConcurrency = 3;
    this.requestDelayMillis = 0;
    this.retryBackoffMillis = 500;
    this.userAgent = null;
    this.defaultCharSet = "UTF-8";
    this.defaultExtension = "bin";
    this.saveDirectory = Paths.get("output");
    this.userAgents = new ArrayList<>(List.of(
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0",
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 14.4; rv:124.0) Gecko/20100101 Firefox/124.0",
      "Mozilla/5.0 (X11; Linux i686; rv:124.0) Gecko/20100101 Firefox/124.0",
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Safari/605.1.15",
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.2420.81"
    ));
  }

  public boolean isDebug() {
    return debug;
  }

  public boolean isRotateUserAgents() {
    return rotateUserAgents;
  }

  public boolean isSameOriginForPageLinks() {
    return sameOriginForPageLinks;
  }

  public int getDepth() {
    return depth;
  }

  public int getTimeout() {
    return timeout;
  }

  public int getMaxPages() {
    return maxPages;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public int getPageBatchSize() {
    return pageBatchSize;
  }

  public int getAssetConcurrency() {
    return assetConcurrency;
  }

  public int getRequestDelayMillis() {
    return requestDelayMillis;
  }

  public int getRetryBackoffMillis() {
    return retryBackoffMillis;
  }

  public String getUserAgent() {
    if (userAgent != null && !userAgent.isBlank()) {
      return userAgent;
    }
    if (userAgents == null || userAgents.isEmpty()) {
      return "Mozilla/5.0";
    }
    if (!rotateUserAgents) {
      return userAgents.get(0);
    }
    String value = userAgents.get(userAgentIndex % userAgents.size());
    userAgentIndex = (userAgentIndex + 1) % userAgents.size();
    return value;
  }

  public String getDefaultCharSet() {
    return defaultCharSet;
  }

  public String getDefaultExtension() {
    return defaultExtension;
  }

  public Path getSaveDirectory() {
    return saveDirectory;
  }

  public List<String> getUserAgents() {
    return Collections.unmodifiableList(userAgents);
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public void setRotateUserAgents(boolean rotateUserAgents) {
    this.rotateUserAgents = rotateUserAgents;
  }

  public void setSameOriginForPageLinks(boolean sameOriginForPageLinks) {
    this.sameOriginForPageLinks = sameOriginForPageLinks;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public void setMaxPages(int maxPages) {
    this.maxPages = maxPages;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public void setPageBatchSize(int pageBatchSize) {
    this.pageBatchSize = Math.max(1, pageBatchSize);
  }

  public void setAssetConcurrency(int assetConcurrency) {
    this.assetConcurrency = Math.max(1, assetConcurrency);
  }

  public void setRequestDelayMillis(int requestDelayMillis) {
    this.requestDelayMillis = requestDelayMillis;
  }

  public void setRetryBackoffMillis(int retryBackoffMillis) {
    this.retryBackoffMillis = retryBackoffMillis;
  }

  public void setUserAgent(String userAgent) {
    if (userAgent == null || userAgent.isBlank()) {
      this.userAgent = null;
      return;
    }
    this.userAgent = userAgent;
  }

  public void setDefaultCharSet(String defaultCharSet) {
    this.defaultCharSet = defaultCharSet;
  }

  public void setDefaultExtension(String defaultExtension) {
    this.defaultExtension = defaultExtension;
  }

  public void setSaveDirectory(Path saveDirectory) {
    this.saveDirectory = saveDirectory;
  }

  public void setUserAgents(List<String> userAgents) {
    if (userAgents == null || userAgents.isEmpty()) {
      return;
    }
    this.userAgents = new ArrayList<>(userAgents);
    this.userAgentIndex = 0;
  }

  public static void main(String[] args) {
    Settings settings = new Settings();
    System.out.println("depth=" + settings.getDepth());
    System.out.println("pageConcurrency=" + settings.getPageBatchSize());
    System.out.println("assetConcurrency=" + settings.getAssetConcurrency());
    System.out.println("saveDirectory=" + settings.getSaveDirectory());
    System.out.println("ua1=" + settings.getUserAgent());
    System.out.println("ua2=" + settings.getUserAgent());
  }
}