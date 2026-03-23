import java.util.Set;
import java.util.concurrent.Callable;

public abstract class Process implements Callable<Set<String>> {
  protected final Fetcher fetcher;
  protected final PathResolver resolver;
  protected final Saver saver;
  protected final Settings settings;
  private ParseResult currentResult;

  public Process(Fetcher fetcher, PathResolver resolver, Settings settings) {
    this.fetcher = fetcher;
    this.resolver = resolver;
    this.settings = settings;
    this.saver = new Saver(settings);
  }

  public void setParseResult(ParseResult result) {
    this.currentResult = result;
  }

  @Override
  public Set<String> call() throws Exception {
    if (currentResult == null) {
      throw new IllegalStateException("ParseResult is not set");
    }
    return execute(currentResult);
  }

  public abstract Set<String> execute(ParseResult result) throws Exception;
}