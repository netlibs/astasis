package astasis;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;


public final class AriClient {
  
  private static final Logger log = LoggerFactory.getLogger(AriClient.class);
  private static final Escaper queryEscaper = new PercentEscaper("_-!.~'()*,;:$?/[]@", true);

  private final HttpClient client;
  private final URI base;

  AriClient(HttpClient client, URI base) {
    this.client = client;
    this.base = base;
  }


  private static URI makeUri(String path, Map<String, String> query) {
    try {
      return new URI(
        null,
        null,
        path,
        query.entrySet().stream().map(e -> queryEscaper.escape(e.getKey()) + "=" + queryEscaper.escape(e.getValue())).collect(Collectors.joining("&")),
        null);
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  record AriDeleteCommand<T> (String path, Map<String, String> args, BodyHandler<T> bodyHandler) {
  }

  record AriPostCommand<T> (String path, Map<String, String> args, BodyPublisher publisher, BodyHandler<T> bodyHandler) {
  }

  //

  public <T> CompletableFuture<T> invoke(AriDeleteCommand<T> command) {
    HttpRequest request =
      HttpRequest.newBuilder()
        .uri(this.base.resolve(makeUri(command.path(), command.args())))
        .DELETE()
        .timeout(Duration.ofSeconds(3))
        .build();
    return client.sendAsync(request, command.bodyHandler()).thenApply(HttpResponse::body);
  }

  //
  public <T> CompletableFuture<T> invoke(AriPostCommand<T> command) {
    HttpRequest request =
      HttpRequest.newBuilder()
        .uri(this.base.resolve(makeUri(command.path(), command.args())))
        .header("content-type", "application/json;charset=utf-8")
        .POST(command.publisher())
        .timeout(Duration.ofSeconds(3))
        .build();
    return client.sendAsync(request, command.bodyHandler()).thenApply(HttpResponse::body);
  }
  //

  public <T> CompletableFuture<HttpResponse<T>> POST(URI path, BodyPublisher bodyPublisher, BodyHandler<T> bodyHandler) {
    HttpRequest request =
      HttpRequest.newBuilder()
        .uri(this.base.resolve(path))
        .POST(bodyPublisher)
        .timeout(Duration.ofSeconds(3))
        .build();
    return client.sendAsync(request, bodyHandler);
  }

  public <T> CompletableFuture<HttpResponse<T>> GET(URI path, BodyHandler<T> bodyHandler) {
    HttpRequest request =
      HttpRequest.newBuilder()
        .uri(this.base.resolve(path))
        .GET()
        .timeout(Duration.ofSeconds(3))
        .build();
    return client.sendAsync(request, bodyHandler);
  }


}
