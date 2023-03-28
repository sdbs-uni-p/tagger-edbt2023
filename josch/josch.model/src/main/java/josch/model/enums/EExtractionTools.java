package josch.model.enums;

/**
 * This enum lists all extraction tools. These tools do have a concrete name stored as a string.
 *
 * @author Kai Dauberschmidt
 */
public enum EExtractionTools {

    /**
     * The Hackolade tool.
     */
    HACK("hackolade"),

    /**
     * The json-schema-inferrer
     */
    JSI("JSON Schema Inferrer"),

    /**
     * Approach by klettke with Tagger
     */
    KLETTKE("tagger klettke"),

    /**
     * Approach by Spoth with Tagger
     */
    SPOTH("spoth"),

    /**
     * Approach by Frozza with Tagger
     */
    FROZZA("frozza");



    /**
     * The concrete name of the containment tool
     */
    private final String NAME;

    /**
     * Constructs a containment tool with a given name.
     */
    EExtractionTools(String name) {
        this.NAME = name;
    }

    /**
     * Gets the correct tool with a given name.
     */
    public static EExtractionTools getTool(String name) {
        return switch (name.toLowerCase()) {
            case "hackolade" -> HACK;
            case "json schema inferrer", "ijs", "json-schema-inferrer" -> JSI;
            case "tagger klettke" -> KLETTKE;
            case "spoth" -> SPOTH;
            case "frozza" -> FROZZA;
            default -> throw new IllegalArgumentException("Extraction tool does not exist.");
        };
    }

    /**
     * Gets the concrete name of the containment tool.
     */
    public String getName() {
        return NAME;
    }

    /**
     * Returns the string representation of the containment tool.
     */
    @Override
    public String toString() {
        return getName();
    }
}
