package astasis;

import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.net.UrlEscapers;

import astasis.AriClient.AriDeleteCommand;
import astasis.AriClient.AriPostCommand;

public final class AriCommands {

  private static final ObjectMapper mapper =
    new JsonMapper()
      .registerModule(new Jdk8Module());

  public static final class Channels {

    @Value.Immutable
    @Value.Style(stagedBuilder = true, depluralize = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static interface CreateParams {

      @JsonProperty
      String endpoint();

      @JsonProperty
      String app();

      @JsonProperty
      Optional<String> appArgs();

      @JsonProperty
      Optional<String> channelId();

      @JsonProperty
      Optional<String> otherChannelId();

      @JsonProperty
      Optional<String> originator();

      @JsonProperty
      Optional<String> formats();

      @JsonProperty
      Map<String, String> variables();

    }

    public static AriPostCommand<String> create(Function<ImmutableCreateParams.EndpointBuildStage, ImmutableCreateParams.BuildFinal> b) {

      ObjectNode body = mapper.convertValue(b.apply(ImmutableCreateParams.builder()).build(), ObjectNode.class);

      return new AriPostCommand<>(
        "channels/create",
        Map.of(),
        BodyPublishers.ofString(body.toPrettyString()),
        BodyHandlers.ofString());
    }

    @Value.Immutable
    @Value.Style(stagedBuilder = true, depluralize = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static interface OriginateParams {

      @JsonProperty
      String endpoint();

      @JsonProperty
      String app();

      @JsonProperty
      Optional<String> appArgs();

      @JsonProperty
      Optional<String> channelId();

      @JsonProperty
      Optional<String> otherChannelId();

      @JsonProperty
      Optional<String> originator();

      @JsonProperty
      Optional<String> formats();

      @JsonProperty
      Optional<String> callerId();

      @JsonProperty
      OptionalInt timeout();

      @JsonProperty
      Map<String, String> variables();

    }

    public static AriPostCommand<String> originate(Function<ImmutableOriginateParams.EndpointBuildStage, ImmutableOriginateParams.BuildFinal> b) {
      ObjectNode body = mapper.convertValue(b.apply(ImmutableOriginateParams.builder()).build(), ObjectNode.class);
      return new AriPostCommand<>(
        "channels",
        Map.of(),
        BodyPublishers.ofString(body.toPrettyString()),
        BodyHandlers.ofString());
    }

    @Value.Immutable
    @Value.Style(stagedBuilder = true)
    public static interface DialParams {

      String channelId();

      String callerId();

      Duration timeout();

    }

    public static AriPostCommand<String> dial(String channelId, String caller, int timeout) {
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/dial",
        Map.of("caller", caller, "timeout", Integer.toString(timeout)),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    @Value.Immutable
    @Value.Style(stagedBuilder = true, depluralize = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static interface SetChannelVarParams {

      @JsonProperty
      String channelId();

      @JsonProperty
      String variable();

      @JsonProperty
      Optional<String> value();

    }

    public static AriPostCommand<String> setChannelVar(Function<ImmutableSetChannelVarParams.ChannelIdBuildStage, ImmutableSetChannelVarParams.BuildFinal> b) {
      var params = b.apply(ImmutableSetChannelVarParams.builder()).build();
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(params.channelId()) + "/variable",
        Map.of("variable", params.variable(), "value", params.value().orElse(null)),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    public static AriPostCommand<String> answer(String channelId) {
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/answer",
        Map.of(),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    public static AriPostCommand<String> ring(String channelId) {
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/ring",
        Map.of(),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    public static AriDeleteCommand<String> ringStop(String channelId) {
      return new AriDeleteCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/ring",
        Map.of(),
        BodyHandlers.ofString());
    }

    public static AriPostCommand<String> hold(String channelId) {
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/hold",
        Map.of(),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    public static AriDeleteCommand<String> unhold(String channelId) {
      return new AriDeleteCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/hold",
        Map.of(),
        BodyHandlers.ofString());
    }

    public static AriPostCommand<String> startMoh(String channelId) {
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/moh",
        Map.of(),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    public static AriDeleteCommand<String> stopMoh(String channelId) {
      return new AriDeleteCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/moh",
        Map.of(),
        BodyHandlers.ofString());
    }

    public static AriPostCommand<String> startSilence(String channelId) {
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/silence",
        Map.of(),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    public static AriDeleteCommand<String> stopSilence(String channelId) {
      return new AriDeleteCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId) + "/silence",
        Map.of(),
        BodyHandlers.ofString());
    }

    @Value.Immutable
    @Value.Style(stagedBuilder = true, depluralize = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static interface PlayParams {

      @JsonProperty
      String channelId();

      @JsonProperty
      String playbackId();

      @JsonProperty
      String media();

    }

    public static AriPostCommand<String> playWithId(Function<ImmutablePlayParams.ChannelIdBuildStage, ImmutablePlayParams.BuildFinal> b) {
      var params = b.apply(ImmutablePlayParams.builder()).build();
      return new AriPostCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(params.channelId()) + "/play/" + params.playbackId(),
        mapper.convertValue(params, new TypeReference<Map<String, String>>() {}),
        BodyPublishers.noBody(),
        BodyHandlers.ofString());
    }

    public static AriDeleteCommand<String> hangup(String channelId, String reason) {
      return new AriDeleteCommand<>(
        "channels/" + UrlEscapers.urlPathSegmentEscaper().escape(channelId),
        Map.of("reason", reason),
        BodyHandlers.ofString());
    }

  }

}
