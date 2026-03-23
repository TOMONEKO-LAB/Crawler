public abstract class Parser {
  protected final Settings settings;  // 設定

  public Parser(Settings settings) {
    this.settings = settings;
  }

  public abstract ParseResult parse(FetchResult result) throws Exception;
}