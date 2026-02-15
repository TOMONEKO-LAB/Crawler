import java.nio.file.*;          // Path, Paths

public class Settings {
  private boolean debug;                    // デバッグモード
  private int bufferSize;                   // バッファサイズ
  private int depth;                        // 深さ
  private String defaultExtension;          // デフォルト拡張子
  private String defaultCharSet;            // デフォルト文字コード
  private Path saveDirectory;               // 保存先ディレクトリ

  public Settings() {
    this.debug = false;
    this.bufferSize = 512;
    this.depth = 0;
    this.defaultExtension = ".jpg";
    this.defaultCharSet = "UTF-8";
    this.saveDirectory = Paths.get("output");
  }

  public Settings(Path saveDirectory) {
    this();
    this.saveDirectory = saveDirectory;
  }

  public Settings(String saveDirectory) {
    this();
    this.saveDirectory = Paths.get(saveDirectory);
  }

  public Settings(Path saveDirectory, int depth) {
    this();
    this.saveDirectory = saveDirectory;
    this.depth = depth;
  }

  public Settings(String saveDirectory, int depth) {
    this();
    this.saveDirectory = Paths.get(saveDirectory);
    this.depth = depth;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public void setDefaultExtension(String defaultExtension) {
    this.defaultExtension = defaultExtension;
  }

  public void setDefaultCharSet(String defaultCharSet) {
    this.defaultCharSet = defaultCharSet;
  }

  public void setSaveDirectory(Path saveDirectory) {
    this.saveDirectory = saveDirectory;
  }

  public boolean isDebug() {
    return debug;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  public int getDepth() {
    return depth;
  }

  public String getDefaultExtension() {
    return defaultExtension;
  }

  public String getDefaultCharSet() {
    return defaultCharSet;
  }

  public Path getSaveDirectory() {
    return saveDirectory;
  }

}
