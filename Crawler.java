import java.net.URI;
import java.util.*;             // List, Map, Set, ArrayList, Deque, HashSet, Collections
import java.util.concurrent.*;  // ConcurrentLinkedDeque, CopyOnWriteArrayList, ConcurrentHashMap, ExecutorService, Executors, Future

import org.jsoup.nodes.Element;

public class Crawler {

  private final Settings settings;              // 設定
  private final Fetcher fetcher;                // Fetchするためのインスタンス
  private final PathResolver resolver;          // パスを解決するためのインスタンス
  private final Saver saver;                    // ファイルを保存するためのインスタンス
  private final Parser parser;                  // パースするためのインスタンス
  private final List<Process> processes;        // 処理を行うためのインスタンス
  private final Set<String> visited;            // 訪問済みURLのリスト
  private final Set<String> inProgress;         // 処理中のURLのリスト
  private final Deque<SiteNode> queue;          // クロール対象URLのキュー
  private final SiteGraph siteGraph;            // サイトグラフ
  private final UrlNormalizer normalizer;       // URLを正規化するためのインスタンス
  private final Set<String> missingUrls;        // 見つからないURLのリスト
  private final ExecutorService pageExecutor;   // ページ処理用のExecutor
  private final ExecutorService assetExecutor;  // アセット処理用のExecutor

  public Crawler(Settings settings) {
    this.settings = settings;
    this.fetcher = new Fetcher(settings);
    this.resolver = new PathResolver(settings);
    this.saver = new Saver(settings);
    this.parser = new HtmlParser(settings);
    this.processes = new CopyOnWriteArrayList<>();
    this.visited = ConcurrentHashMap.newKeySet();
    this.inProgress = ConcurrentHashMap.newKeySet();
    this.queue = new ConcurrentLinkedDeque<>();
    this.siteGraph = new SiteGraph();
    this.normalizer = new UrlNormalizer();
    this.missingUrls = ConcurrentHashMap.newKeySet();
    this.pageExecutor = Executors.newFixedThreadPool(settings.getPageBatchSize());
    this.assetExecutor = Executors.newFixedThreadPool(settings.getAssetConcurrency());
    this.processes.add(new StyleSheetProcess(fetcher, resolver, settings));
    this.processes.add(new ScriptProcess(fetcher, resolver, settings));
    this.processes.add(new ImageProcess(fetcher, resolver, settings));
  }

  public void crawl(String url) {
    crawl(url, settings.getDepth());
  }

  public void shutdown() {
    pageExecutor.shutdown();
    assetExecutor.shutdown();
  }

  private void crawl(String url, int depth) {
    // URLがnullまたは空白の場合は終了
    if (url == null || url.isBlank()) {
      return;
    }

    // URLを正規化
    String startUrl = normalizer.normalize(url);
    // 正規化の結果、URLがダウンロード対象外と判断された場合は終了
    if (startUrl == null) {
      if (settings.isDebug()) {
        System.out.println("[Crawler]: " + "start url skipped by normalizer=" + url);
      }
      return;
    }

    // URLのオリジンを取得
    String startOrigin = getOrigin(startUrl);
    if (settings.isDebug()) {
      System.out.println("[Crawler]: " + "crawl start url=" + startUrl + ", depth=" + depth);
    }

    visited.clear();
    inProgress.clear();
    queue.clear();
    missingUrls.clear();
    queue.addLast(new SiteNode(startUrl, 0));
    Map<String, Integer> retryCounts = new ConcurrentHashMap<>();

    int batchSize = settings.getPageBatchSize();
    List<Future<Void>> allFutures = new ArrayList<>();

    while (!queue.isEmpty()) {
      // キューからバッチサイズ分のURLを取得
      List<SiteNode> batch = new ArrayList<>();
      for (int i = 0; i < batchSize; i++) {
        SiteNode candidate = queue.pollLast();
        if (candidate == null) {
          break;
        }
        batch.add(candidate);
      }

      // バッチが空の場合は次のループへ
      if (batch.isEmpty()) {
        continue;
      }

      // バッチ内の各ページをスレッドプールへ
      for (SiteNode node : batch) {
        allFutures.add(pageExecutor.submit(new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            processPageNode(node, startOrigin, depth, retryCounts);
            return null;
          }
        }));
      }
    }

    // すべてのページ処理が完了するまで待機
    for (Future<Void> future : allFutures) {
      try {
        future.get();
      } catch (Exception e) {
        if (settings.isDebug()) {
          System.out.println("[Crawler]: " + "page task failed message=" + e.getMessage());
        }
      }
    }
  }

  // 1ページの処理
  private void processPageNode(SiteNode node, String startOrigin, int depth, Map<String, Integer> retryCounts) {
    // 指定した深さを超えている場合はスキップ
    if (node.getDepth() > depth) {
      if (settings.isDebug()) {
        System.out.println("[Crawler]: " + "skip by depth url=" + node.getUrl());
      }
      return;
    }

    // すでに訪問済みのURLはスキップ
    if (visited.contains(node.getUrl())) {
      if (settings.isDebug()) {
        System.out.println("[Crawler]: " + "skip visited url=" + node.getUrl());
      }
      return;
    }

    // クロール対象URLが最大ページ数に達している場合は終了
    if (visited.size() >= settings.getMaxPages()) {
      if (settings.isDebug()) {
        System.out.println("[Crawler]: " + "stop by maxPages=" + settings.getMaxPages());
      }
      return;
    }

    // すでに処理中のURLはスキップ
    if (!inProgress.add(node.getUrl())) {
      if (settings.isDebug()) {
        System.out.println("[Crawler]: " + "skip inflight url=" + node.getUrl());
      }
      return;
    }

    try {
      // URLをFetch
      FetchResult fetchResult = fetcher.fetch(node.getUrl());
      int status = fetchResult.getStatusCode();

      // HTTPステータスコードがBot判定の場合はリトライする
      if (status == 403 || status == 429) {

        // リトライ回数が設定値未満の場合はキューに再追加して次のノードへ
        int currentRetry = retryCounts.getOrDefault(node.getUrl(), 0);
        if (currentRetry < settings.getRetryCount()) {
          retryCounts.put(node.getUrl(), currentRetry + 1);
          if (settings.isDebug()) {
            System.out.println("[Crawler]: " + "requeue by status=" + status + ", url=" + node.getUrl() + ", retry=" + (currentRetry + 1));
          }

          // リトライ前に設定された秒数分待機
          if (settings.getRetryBackoffMillis() > 0) {
            try {
              Thread.sleep(settings.getRetryBackoffMillis());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return;
            }
          }
          queue.addFirst(node);
        }
        return;
      }
      retryCounts.remove(node.getUrl());

      // 訪問済みならスキップ
      if (!visited.add(node.getUrl())) {
        return;
      }
      if (settings.isDebug()) {
        System.out.println("[Crawler]: " + "visit url=" + node.getUrl() + ", visited=" + visited.size());
      }

      // HTML以外はスキップ
      if (!fetchResult.isHtml()) {
        if (settings.isDebug()) {
          System.out.println("[Crawler]: " + "skip non-html url=" + node.getUrl() + ", contentType=" + fetchResult.getContentType());
        }
        return;
      }

      // HTMLをパース
      ParseResult parseResult;
      try {
        parseResult = parser.parse(fetchResult);
      } catch (Exception e) {
        if (settings.isDebug()) {
          System.out.println("[Crawler]: " + "parse failed url=" + node.getUrl() + ", message=" + e.getMessage());
        }
        return;
      }

      // 各プロセスを並列実行
      List<Future<Set<String>>> processFutures = new ArrayList<>();
      for (Process process : processes) {
        processFutures.add(assetExecutor.submit(new Callable<Set<String>>() {
          @Override
          public Set<String> call() throws Exception {
            return process.execute(parseResult);
          }
        }));
      }

      // すべてのプロセスが完了するまで待機
      for (int i = 0; i < processFutures.size(); i++) {
        try {
          Set<String> missing = processFutures.get(i).get();
          if (missing != null && !missing.isEmpty()) {
            missingUrls.addAll(missing);
          }
        } catch (Exception e) {
          if (settings.isDebug()) {
            System.out.println("[Crawler]: " + "process failed message=" + e.getMessage());
          }
        }
      }

      // nodeのURLを起点としたリンクをサイトグラフに追加
      String currentHtmlPath = resolver.resolveHtmlPath(node.getUrl());
      for (Element a : parseResult.getAnchorTags()) {
        String href = a.attr("href");
        String normalized = normalizer.normalize(href, node.getUrl());
        if (normalized == null) {
          if (settings.isDebug()) {
            System.out.println("[Crawler]: " + "skip link by normalizer href=" + href);
          }
          continue;
        }
        siteGraph.addEdge(node.getUrl(), normalized);

        // リンクのオリジンを取得して、同一オリジンの場合はHTML内のリンクを書き換える
        String linkedOrigin = getOrigin(normalized);
        boolean sameOrigin = startOrigin.equals(linkedOrigin);
        if (sameOrigin) {
          String linkedHtmlPath = resolver.resolveHtmlPath(normalized);
          a.attr("href", resolver.relativize(currentHtmlPath, linkedHtmlPath));
        }

        // 同一オリジンのリンクのみクロール対象とする設定の場合は、オリジンが異なるリンクをスキップする 
        if (settings.isSameOriginForPageLinks() && !sameOrigin) {
          if (settings.isDebug()) {
            System.out.println("[Crawler]: " + "skip by origin link=" + normalized);
          }
          continue;
        }

        // すでに訪問済みでないかつ次の深さが設定値以下の場合はキューに追加
        if (!visited.contains(normalized) && node.getDepth() + 1 <= settings.getDepth()) {
          queue.addLast(new SiteNode(normalized, node.getDepth() + 1));
          if (settings.isDebug()) {
            System.out.println("[Crawler]: " + "push link=" + normalized + ", nextDepth=" + (node.getDepth() + 1));
          }
        }
      }
      if (settings.isDebug()) {
        System.out.println("[Crawler]: " + "links summary base=" + node.getUrl() + ", total=" + parseResult.getAnchorTags().size());
      }

      // 保存処理
      try {
        saver.saveHtml(currentHtmlPath, parseResult.getDocument().outerHtml());
        if (settings.isDebug()) {
          System.out.println("[Crawler]: " + "saved html path=" + currentHtmlPath);
        }
      } catch (Exception e) {
        if (settings.isDebug()) {
          System.out.println("[Crawler]: " + "save html failed url=" + node.getUrl() + ", message=" + e.getMessage());
        }
      }

    // URLの処理が終わったらinProgressから削除
    } finally {
      inProgress.remove(node.getUrl());
    }
  }

  // URLのオリジンを取得する
  private String getOrigin(String url) {
    URI uri = URI.create(url);
    String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
    String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
    return scheme + "://" + host;
  }

  // おまけ : 見つからないURLのリストを取得する
  public Set<String> getMissingUrls() {
    return Collections.unmodifiableSet(new HashSet<>(missingUrls));
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java Crawler <url>");
      return;
    }
    Settings settings = new Settings();
    settings.setDebug(true);
    settings.setDepth(1);
    settings.setPageBatchSize(64);
    settings.setAssetConcurrency(64);
    Crawler crawler = new Crawler(settings);
    try {
      crawler.crawl(args[0]);
      System.out.println("missingUrls=" + crawler.getMissingUrls().size());
    } finally {
      crawler.shutdown();
    }
  }
}