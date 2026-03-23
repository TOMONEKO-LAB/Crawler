import java.net.http.*;  // HttpClient, HttpRequest, HttpResponse
import java.net.URI;
import java.time.Duration;
import java.io.IOException;

public class Fetcher {
  private final Settings settings;  // 設定
  private final HttpClient client;  // HTTPクライアント

  static int THREAD_ERROR = 600;  // スレッドエラー
  static int UNKNOWN_ERROR = 601;  // 不明なエラー
  static int RETRY_EXHAUSTED = 602; // リトライ回数超過

  public Fetcher(Settings settings) {
    this.settings = settings;
    this.client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(settings.getTimeout()))
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  }

  // フェッチ処理
  public FetchResult fetch(String url) {

    // リトライ回数の設定
    int maxRetry = settings.getRetryCount();
    if (settings.isDebug()) {
      System.out.println("[Fetcher]: " + "fetch start url=" + url + ", maxRetry=" + maxRetry);
    }

    // リトライループ
    for (int attempt = 0; attempt <= maxRetry; attempt++) {

      // User-Agentの設定
      String userAgent = settings.getUserAgent();
      if (settings.isDebug()) {
        System.out.println("[Fetcher]: " + "attempt=" + attempt + ", ua=" + userAgent);
      }

      // HTTPリクエスト作成
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .timeout(Duration.ofSeconds(settings.getTimeout()))
          .header("User-Agent", userAgent)
          .GET()
          .build();

      try {
        // HTTPリクエスト送信
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        // ステータスコード取得
        int status = response.statusCode();
        if (settings.isDebug()) {
          System.out.println("[Fetcher]: " + "response status=" + status + ", attempt=" + attempt);
        }

        // Bot判定の場合はリトライ
        if ((status == 429 || status == 403) && attempt < maxRetry) {
          if (settings.isDebug()) {
            System.out.println("[Fetcher]: " + "retry by status=" + status + ", nextAttempt=" + (attempt + 1));
          }

          // スレッドでの待機処理
          if (!sleepBackoff()) {
            return new FetchResult(url, THREAD_ERROR, new byte[0], null, null);
          }
          continue;
        }

        return new FetchResult(
            url,
            status,
            response.body(),
            response.headers().firstValue("Content-Type").orElse(null),
            null
        );

      // HTTP通信がうまくいかなかった場合
      } catch (IOException e) {
        if (settings.isDebug()) {
          System.out.println("[Fetcher]: " + "io error url=" + url + ", message=" + e.getMessage());
        }
        return new FetchResult(url, UNKNOWN_ERROR, new byte[0], null, null);
      } catch (InterruptedException e) {
        if (settings.isDebug()) {
          System.out.println("[Fetcher]: " + "interrupted url=" + url);
        }
        Thread.currentThread().interrupt();
        return new FetchResult(url, THREAD_ERROR, new byte[0], null, null);
      }
    }

    // リトライ回数を超過した場合
    return new FetchResult(url, RETRY_EXHAUSTED, new byte[0], null, null);
  }

  // リトライするための待機処理
  private boolean sleepBackoff() {

    // リトライのバックオフ時間が0以下の場合
    if (settings.getRetryBackoffMillis() <= 0) {
      return true;
    }
    if (settings.isDebug()) {
      System.out.println("[Fetcher]: " + "sleep backoff ms=" + settings.getRetryBackoffMillis());
    }
    try {
      Thread.sleep(settings.getRetryBackoffMillis());
      return true;

    // スレッドが中断された場合
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  public static void main(String[] args) {
    Settings settings = new Settings();
    Fetcher fetcher = new Fetcher(settings);
    String url = args.length > 0 ? args[0] : "https://example.com";
    FetchResult result = fetcher.fetch(url);
    System.out.println("url=" + result.getUrl());
    System.out.println("status=" + result.getStatusCode());
    System.out.println("contentType=" + result.getContentType());
    System.out.println("bytes=" + result.getBody().length);
  }
}
