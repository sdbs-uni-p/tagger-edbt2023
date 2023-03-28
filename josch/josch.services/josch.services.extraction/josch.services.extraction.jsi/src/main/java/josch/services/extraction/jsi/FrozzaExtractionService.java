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

public class FrozzaExtractionService extends AbstractShellService implements IExtractionService {

    private final SettingsDto settingsDto;

    private final TaggerExtractionDto taggerExtractionDto;


    public void startServer() {
        String frozzaPath = settingsDto.getFrozzaPath();
        String base = "gradle";
        String command = ":frozza:start";
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(base);
        cmd.add(command);
        executeCommand(cmd.toArray(String[]::new), Paths.get(frozzaPath).toFile());
    }

    public void stopServer() {
        String frozzaPath = settingsDto.getFrozzaPath();
        String base = "gradle";
        String command = ":frozza:stop";
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(base);
        cmd.add(command);
        executeCommand(cmd.toArray(String[]::new), Paths.get(frozzaPath).toFile());
    }


    public FrozzaExtractionService(SettingsDto settings, TaggerExtractionDto taggerExtractionDto) {
        this.settingsDto = settings;
        this.taggerExtractionDto = taggerExtractionDto;
    }

    @Override
    public String getJsonSchema() throws IOException {
        ExtractionUtils extractionUtils = new ExtractionUtils();
        List<JsonNode> samples = extractionUtils.sendFilesToExtractionService(settingsDto);

        ArrayList<String> cmd = new ArrayList<>();

        String path = settingsDto.getToolsPath() + File.separator + "temp";
        for (int k = 0; k < samples.size(); ++k) {
            File file = new File(path + File.separator + k + ".json");
            FileWriter fw = new FileWriter(file);
            fw.write(samples.get(k).toString());
            fw.close();
        }


        String frozzaPath = settingsDto.getFrozzaPath();

        String base = "gradle";

        String run = "run";

        String c = "-Pfrozza.outputFile=" + path + File.separator + "frozza_out.json";

        String collection = settingsDto.getCollection().getName();

        String setCollection = "-Pfrozza.collectionName=" + collection;

        IDatabaseService service = new DatabaseService(settingsDto);
        String setDatabase = "-Pfrozza.dbName=" + service.getDatabase();

        cmd.add(base);
        cmd.add(c);
        cmd.add(setDatabase);
        cmd.add(setCollection);
        cmd.add(run);

        executeCommand(cmd.toArray(String[]::new), Paths.get(frozzaPath).toFile());

        cmd = new ArrayList<>();
        cmd.add("pipenv");
        cmd.add("run");
        cmd.add("python3");
        cmd.add("experiments.py");

        String[] arguments = new String[7];
        arguments[0] = "frozza";
        arguments[1] = path + File.separator + "frozza_out.json";
        arguments[2] = String.valueOf(taggerExtractionDto.getSampleSize());
        arguments[3] = path + File.separator;
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

        String pathToExe = settingsDto.getToolsPath() +
                File.separator + "Tagger" + File.separator + "Tagger-main" + File.separator + "dist" + File.separator;
        ProcessBuilder builder = new ProcessBuilder(pathToExe + File.separator + experiment, "frozza", arguments[1], arguments[2],
                arguments[3], arguments[4], arguments[5], arguments[6]);
        builder.directory(new File(pathToExe));
        builder.redirectErrorStream(true);

        Process process = builder.start();
        return getMessage(process);
    }

    @Override
    public String getValidator() {
        String schema = "";
        try {
            schema = getJsonSchema();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (schema.contains(ESystemConstants.ERROR.getValue())) {
            return schema;
        }
        IDatabaseService service = new DatabaseService(settingsDto);
        return service.generateValidator(schema);
    }

    @Override
    protected String getMessage(Process p) {
        // Read the shell until the process is finished or an exception occurred.
        String shellLine;
        BufferedReader shellReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        do {
            try {
                shellLine = shellReader.readLine();
                if (shellLine.contains("{")) {
                    return shellLine;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ESystemConstants.ERROR.getValue() + e.getMessage();
            }
        } while (!(shellLine.contains("Process finished with exit code 0")));
        return null;
    }
}
