package josch.model.dto;

public class TaggerExtractionDto extends ExtractionDto{

    // The tagger sample size
    private float sampleSize;

    public float getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(float sampleSize) {
        this.sampleSize = sampleSize;
    }

    public boolean isIgnoreConstantAttributes() {
        return ignoreConstantAttributes;
    }

    public void setIgnoreConstantAttributes(boolean ignoreConstantAttributes) {
        this.ignoreConstantAttributes = ignoreConstantAttributes;
    }

    public boolean isIgnoreUnique() {
        return ignoreUnique;
    }

    public void setIgnoreUnique(boolean ignoreUnique) {
        this.ignoreUnique = ignoreUnique;
    }

    public boolean isUseUnion() {
        return useUnion;
    }

    public void setUseUnion(boolean useUnion) {
        this.useUnion = useUnion;
    }

    // The ignore constant flag
    private boolean ignoreConstantAttributes;

    // The ignore unique flag;
    private boolean ignoreUnique;

    //The use union flag
    private boolean useUnion;


}
