package josch.services.extraction.jsi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.RequiredPolicies;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import josch.model.dto.SettingsDto;
import josch.model.enums.EDatabaseSystems;
import josch.model.enums.ESystemConstants;
import josch.persistency.factory.AbstractClientFactory;
import josch.persistency.interfaces.IClient;
import josch.services.interfaces.IDatabaseService;
import josch.services.interfaces.IExtractionService;
import josch.services.persistency.DatabaseService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This {@code JsiExtractionService} class implements JSON Schema extraction using the JSON Schema
 * Inferrer (JSI). It is being recommended by the official JSON Schema Website <a
 * href="https://json-schema.org/implementations.html#from-data">json-schema.org</a>.
 *
 * @author Kai Dauberschmidt
 */
public class JsiExtractionService implements IExtractionService {

  /** The system's settings. */
  private final SettingsDto SETTINGS;

  /** Constructs a new ExtractionService with given settings */
  public JsiExtractionService(SettingsDto settings) {
    this.SETTINGS = settings;
  }

  /** {@inheritDoc} */
  @Override
  public String getJsonSchema() {
    ExtractionUtils extractionUtils = new ExtractionUtils();
    List<JsonNode> samples = extractionUtils.sendFilesToExtractionService(SETTINGS);

    // Create an Inferrer.
    JsonSchemaInferrer inferrer =
            JsonSchemaInferrer.newBuilder()
                    .setSpecVersion(SpecVersion.DRAFT_04)
                    .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
                    .build();

    JsonNode current;

    // Process the list and return the result.
    try {
      JsonNode schema = inferrer.inferForSamples(samples);
      // MongoDB specifics: remove oid property.
      if (SETTINGS.getDbms().equals(EDatabaseSystems.MONGO)) {
        current = schema.get("properties");
        current = current.get("_id"); // All documents in mongo have this.
        if (current.isObject()) {
          ((ObjectNode) current).remove("properties");
          ((ObjectNode) current).remove("required");
        }
      }
      return schema.toString();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      return ESystemConstants.ERROR.getValue() + e.getMessage();
    }
  }

  @Override
  public String getValidator() {
    String schema = getJsonSchema();
    if (schema.contains(ESystemConstants.ERROR.getValue())) {
      return schema;
    }
    IDatabaseService service = new DatabaseService(SETTINGS);
    return service.generateValidator(schema);
  }
}
