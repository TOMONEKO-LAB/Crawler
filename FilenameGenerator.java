import java.net.URI;
import java.util.*;   // HashMap, HashSet, Map, Set

public class FilenameGenerator {
  private final Settings settings;
  private final ExtensionResolver extensionResolver;
  private final Map<String, Set<String>> usedNamesByScope;

  public FilenameGenerator(Settings settings) {
    this.settings = settings;
    this.extensionResolver = new ExtensionResolver(settings);
    this.usedNamesByScope = new HashMap<>();
  }

  // URLとContent-Typeからファイル名を生成
  public String generateAssetFileName(String url, String contentType, String scopeKey) {

    // URLからファイル名を抽出
    URI uri = URI.create(url);
    String path = uri.getPath();
    String fileName = extractFileName(path);

    // URLに含まれている既存の拡張子を除去
    String baseName = fileName;
    int lastDot = baseName.lastIndexOf('.');
    if (lastDot > 0) {
      baseName = baseName.substring(0, lastDot);
    }
    String extension = extensionResolver.resolveExtension(contentType);

    // クエリパラメータがある場合はハッシュを付与
    if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
      baseName = baseName + "_" + uri.getQuery().hashCode();
      if (settings.isDebug()) {
        System.out.println("[FilenameGenerator]: " + "query hash applied url=" + url + ", query=" + uri.getQuery());
      }
    }

    // 候補となるファイル名を生成
    String candidate = baseName + "." + extension;
    if (settings.isDebug()) {
      System.out.println("[FilenameGenerator]: " + "candidate scope=" + scopeKey + ", name=" + candidate);
    }
    return ensureUnique(scopeKey, candidate);
  }

  // 同一スコープ内でファイル名が重複している確認
  public String ensureUnique(String scopeKey, String filenameCandidate) {
    Set<String> used = usedNamesByScope.get(scopeKey);

    // スコープが未登録なら新規作成
    if (used == null) {
      used = new HashSet<>();
      usedNamesByScope.put(scopeKey, used);
    }

    // 候補が未使用ならそのまま採用
    if (!used.contains(filenameCandidate)) {
      used.add(filenameCandidate);
      return filenameCandidate;
    }

    // ファイル名と拡張子を抽出
    String base = extractFileName(filenameCandidate);
    String extension = "";
    int dot = filenameCandidate.lastIndexOf('.');
    if (dot > 0) {
      base = filenameCandidate.substring(0, dot);
      extension = filenameCandidate.substring(dot);
    }

    // 連番を付与して重複を回避
    int serialNumber = 1;
    String next;
    do {
      next = base + "_" + serialNumber + extension;
      serialNumber++;
    } while (used.contains(next));

    used.add(next);
    if (settings.isDebug()) {
      System.out.println("[FilenameGenerator]: " + "collision resolved scope=" + scopeKey + ", name=" + next);
    }
    return next;
  }

  // URLのパスからファイル名を抽出
  private String extractFileName(String path) {

    // パスがnull、空文字列、/で終わる場合
    if (path == null || path.isBlank() || path.endsWith("/")) {
      return "index";
    }

    // 最後の/以降をファイル名とする
    String name = path.substring(path.lastIndexOf('/') + 1);
    return name;
  }

  public static void main(String[] args) {
    Settings settings = new Settings();
    FilenameGenerator generator = new FilenameGenerator(settings);
    String scope = "example.com/scripts";
    System.out.println(generator.generateAssetFileName("https://example.com/load.php?a=1&b=2", "application/javascript", scope));
    System.out.println(generator.generateAssetFileName("https://example.com/load.php?b=2&a=1", "application/javascript", scope));
    System.out.println(generator.generateAssetFileName("https://example.com/load.php?a=1&b=2", "application/javascript", scope));
  }
}
