package astasis;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Builder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public final class RxWebSocket extends Flowable<AriMessage> {

  private static final Logger log = LoggerFactory.getLogger(RxWebSocket.class);

  private final @NonNull WebSocket ws;
  private final @NonNull SocketState state;

  private static final JsonMapper mapper = new JsonMapper();

  private static final Function<CharSequence, AriMessage> textMapper = in -> {
    try {
      // if it's not an ObjectNode it's an error. ARI event API defines events as "JSON objects".
      return new AriMessage(mapper.readValue(in.toString(), ObjectNode.class));
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  };

  private RxWebSocket(@NonNull SocketState state, @NonNull WebSocket ws) {
    this.ws = ws;
    this.state = state;
  }

  @Override
  protected void subscribeActual(@NonNull Subscriber<? super AriMessage> subscriber) {

    state.subscriber = subscriber;

    subscriber.onSubscribe(new Subscription() {

      @Override
      public void request(long n) {
        // request more packets.
        ws.request(n);
      }

      @Override
      public void cancel() {
        log.debug("subscriber cancelled");
        ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
      }

    });

  }

  private static final class SocketState implements WebSocket.Listener {

    private @NonNull Subscriber<? super AriMessage> subscriber;
    private CompletableFuture<WebSocket> future;

    public SocketState(Builder webSocketBuilder, URI uri) {
      this.future = webSocketBuilder.buildAsync(uri, this);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
      // we don't request any data - wait for subscriber to request(n) on the flowable.
    }

    @Override
    public CompletionStage<Void> onText(WebSocket webSocket, CharSequence data, boolean last) {
      this.subscriber.onNext(textMapper.apply(data));
      return CompletableFuture.completedFuture(null);
    }

    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
      log.debug("closed with {}: {}", statusCode, reason);
      // webSocket.sendClose(CUSTOM_STATUS_CODE, CUSTOM_REASON);
      subscriber.onComplete();
      return new CompletableFuture<Void>();
    }

    public void onError(WebSocket webSocket, Throwable error) {
      log.warn("connection error", error.getMessage(), error);
      subscriber.onError(error);
    }

    // called once the supplier has disposed.
    public void dispose() {
    }

  }

  public static Single<RxWebSocket> newClientFactory(HttpClient httpClient, URI uri) {

    // single instance for all client instances.
    var webSocketBuilder = httpClient.newWebSocketBuilder();

    // new socket for each subscription.
    return Single.using(
      () -> new SocketState(webSocketBuilder, uri),
      state -> Single.fromCompletionStage(state.future).map(ws -> new RxWebSocket(state, ws)),
      state -> state.dispose(),
      false);

  }

}
