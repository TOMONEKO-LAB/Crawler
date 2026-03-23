import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathResolver {
  private final Settings settings;                    // 設定
  private final FilenameGenerator filenameGenerator;  // ファイル名を生成するインスタンス

  public PathResolver(Settings settings) {
    this.settings = settings;
    this.filenameGenerator = new FilenameGenerator(settings);
  }

  // HTMLのURLを保存先のパスに変換する
  public String resolveHtmlPath(String url) {
    URI uri = URI.create(url);
    String host = uri.getHost() == null ? "unknown-host" : uri.getHost();
    String path = uri.getPath();

    // パスが空またはルートの場合
    if (path == null || path.isEmpty() || "/".equals(path)) {
      path = "/index.html";

    // パスが/で終わっている場合
    } else if (path.endsWith("/")) {
      path = path + "index.html";

    // パスの最後の部分に拡張子がない場合
    } else if (!path.substring(path.lastIndexOf('/') + 1).contains(".")) {
      path = path + "/index.html";
    }

    // 保存先のパスを生成する
    String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
    normalizedPath = normalizedPath.replace(":", "_cln_");
    String resolvedPath = Paths.get(settings.getSaveDirectory().toString(), host, normalizedPath)
      .toString()
      .replace('\\', '/');
    if (settings.isDebug()) {
      System.out.println("[PathResolver]: " + "resolveHtmlPath url=" + url + " -> " + resolvedPath);
    }
    return resolvedPath;
  }

  // アセットのURLを保存先のパスに変換する
  public String resolveAssetPath(String parentHtmlPath, String assetUrl, String assetType, String contentType) {
    Path htmlPath = Paths.get(parentHtmlPath).normalize();
    Path parentDir = htmlPath.getParent();

    // 親ディレクトリが存在しない場合は保存ディレクトリを親ディレクトリとする
    if (parentDir == null) {
      parentDir = Paths.get(settings.getSaveDirectory().toString());
    }

    // ファイル名を生成するためのスコープキーを作成する
    // スコープキーは親ディレクトリのパスとアセットの種類（assetType）を組み合わせたものとする
    String scopeKey = parentDir.toString().replace('\\', '/') + "/" + assetType;
    String fileName = filenameGenerator.generateAssetFileName(assetUrl, contentType, scopeKey);
    Path outputFile = parentDir.resolve(assetType).resolve(fileName);

    // 同名のファイルが存在する場合はファイル名をユニークにする
    while (Files.exists(outputFile)) {
      fileName = filenameGenerator.ensureUnique(scopeKey, fileName);
      outputFile = parentDir.resolve(assetType).resolve(fileName);
    }

    // Windows環境でのパス区切り文字を統一
    String resolvedPath = outputFile.toString().replace('\\', '/');
    if (settings.isDebug()) {
      System.out.println("[PathResolver]: " + "resolveAssetPath html=" + parentHtmlPath + ", asset=" + assetUrl + ", dir=" + assetType + " -> " + resolvedPath);
    }
    return resolvedPath;
  }

  // 親パスから子パスへの相対パスを計算する
  public String relativize(String parent, String child) {
    Path parentPath = Paths.get(parent).normalize();
    Path childPath = Paths.get(child).normalize();
    Path parentDir = parentPath.getParent();

    // 親ディレクトリが存在しない場合は子パスをそのまま返す
    if (parentDir == null) {
      return childPath.toString().replace('\\', '/');
    }

    // 相対パスを計算する
    String relativePath = parentDir.relativize(childPath).toString().replace('\\', '/');

    // 相対パスが./や../で始まらない場合は./を付与する
    if (!relativePath.startsWith(".") && !relativePath.startsWith("..")) {
      String outputFile = "./" + relativePath;
      if (settings.isDebug()) {
        System.out.println("[PathResolver]: " + "relativize parent=" + parent + ", child=" + child + " -> " + outputFile);
      }
      return outputFile;
    }
    if (settings.isDebug()) {
      System.out.println("[PathResolver]: " + "relativize parent=" + parent + ", child=" + child + " -> " + relativePath);
    }
    return relativePath;
  }

  public static void main(String[] args) {
    Settings settings = new Settings();
    PathResolver resolver = new PathResolver(settings);
    String html = resolver.resolveHtmlPath("https://example.com/about");
    String js = resolver.resolveAssetPath(html, "https://cdn.example.com/assets/app?v=1", "scripts", "application/javascript");
    System.out.println("html=" + html);
    System.out.println("asset=" + js);
    System.out.println("rel=" + resolver.relativize(html, js));
  }
}