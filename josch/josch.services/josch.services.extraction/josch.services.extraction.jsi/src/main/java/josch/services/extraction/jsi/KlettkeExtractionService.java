package josch.services.extraction.jsi;

import com.fasterxml.jackson.databind.JsonNode;
import josch.model.dto.SettingsDto;
import josch.model.dto.TaggerExtractionDto;
import josch.model.enums.ESystemConstants;
import josch.services.interfaces.AbstractShellService;
import josch.services.interfaces.IDatabaseService;
import josch.services.interfaces.IExtractionService;
import josch.services.persistency.DatabaseService;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KlettkeExtractionService extends AbstractShellService implements IExtractionService {

    /**
     * The system's settings.
     */
    private final SettingsDto SETTINGS;

    private final TaggerExtractionDto taggerExtractionDto;

    public KlettkeExtractionService(SettingsDto settings, TaggerExtractionDto taggerExtractionDto) {
        SETTINGS = settings;
        this.taggerExtractionDto = taggerExtractionDto;
    }

    @Override
    public String getJsonSchema() throws IOException {
        ExtractionUtils extractionUtils = new ExtractionUtils();
        List<JsonNode> samples = extractionUtils.sendFilesToExtractionService(SETTINGS);

        String pathToExe = SETTINGS.getToolsPath() +
                File.separator + "Tagger" + File.separator + "Tagger-main" + File.separator + "dist" + File.separator;

        String[] arguments = new String[7];
        arguments[0] = "klettke";

        String path = SETTINGS.getToolsPath() + File.separator + "temp" + File.separator;
        for (int k = 0; k < samples.size(); ++k) {
            File file = new File(path + File.separator + k + ".json");
            FileWriter fw = new FileWriter(file);
            fw.write(samples.get(k).toString());
            fw.close();
        }

        arguments[1] = path;

        arguments[2]  = String.valueOf(taggerExtractionDto.getSampleSize());

        arguments[3] = "none";

        arguments[4] = String.valueOf(taggerExtractionDto.isIgnoreConstantAttributes());

        arguments[5] = String.valueOf(taggerExtractionDto.isIgnoreUnique());

        arguments[6] = String.valueOf(taggerExtractionDto.isUseUnion());

        String experiment = "";
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            experiment = "experiments.exe";
        } else if (os.toLowerCase().contains("mac")) {
            experiment = "experiments";
        } else {
            experiment = "experiments_unix";
        }

        ProcessBuilder builder = new ProcessBuilder(pathToExe + File.separator + experiment, "klettke", arguments[1], arguments[2],
                arguments[3], arguments[4], arguments[5], arguments[6]);
        builder.directory(new File(pathToExe));
        Process process = builder.start();
        return getMessage(process);
    }

    @Override
    public String getValidator() {
        String schema;
        try {
            schema = getJsonSchema();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (schema.contains(ESystemConstants.ERROR.getValue())) {
            return schema;
        }
        IDatabaseService service = new DatabaseService(SETTINGS);
        return service.generateValidator(schema);
    }

    @Override
    protected String getMessage(Process p)  {
        // Read the shell until the process is finished or an exception occurred.
        String shellLine;
        BufferedReader shellReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        do {
            try {
                shellLine = shellReader.readLine();
                if (shellLine.contains("{")) {
                    System.out.println(shellLine);
                    return shellLine;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ESystemConstants.ERROR.getValue() + e.getMessage();
            }
        } while (!(shellLine.contains("Validation of input JSON document against produced JSON Schema passed")));
        return null;
    }

}
