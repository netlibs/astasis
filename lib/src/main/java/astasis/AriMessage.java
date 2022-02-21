package astasis;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record AriMessage(ObjectNode properties) {

  public String type() {
    return properties.path("type").textValue();
  }

  public String toString() {
    return String.format("[%s]: %s", type(), properties);
  }

}
