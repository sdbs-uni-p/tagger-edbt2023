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

public class SpothExtractionService extends AbstractShellService implements IExtractionService {

    /**
     * The system's settings.
     */
    private final SettingsDto SETTINGS;

    private final TaggerExtractionDto taggerExtractionDto;

    public SpothExtractionService(SettingsDto settingsDto, TaggerExtractionDto taggerExtractionDto) {
        SETTINGS = settingsDto;
        this.taggerExtractionDto = taggerExtractionDto;
    }

    @Override
    public String getJsonSchema() throws IOException {
        ExtractionUtils extractionUtils = new ExtractionUtils();
        List<JsonNode> samples = extractionUtils.sendFilesToExtractionService(SETTINGS);

        String path = SETTINGS.getToolsPath() + File.separator + "temp";
        for (int k = 0; k < samples.size(); ++k) {
            File file = new File(path + File.separator + k + ".json");
            FileWriter fw = new FileWriter(file);
            fw.write(samples.get(k).toString());
            fw.close();
        }

        File file = new File(path + File.separator + "spoth_in.json");
        FileWriter fw = new FileWriter(file);
        fw.write("[");
        fw.write(System.lineSeparator());
        for (int k = 0; k < samples.size(); ++k) {
            fw.write(samples.get(k).toString());
            if (k != samples.size() - 1) {
                fw.write(",");
            }
            fw.write(System.lineSeparator());
        }
        fw.write("]");
        fw.close();

        // these arguments are called by spoth
        String[] spothArguments = new String[3];
        spothArguments[0] = path + File.separator + "spoth_in.json";
        spothArguments[1] = "log";
        spothArguments[2] = path + File.separator + "spoth_out.json";

        // Resolve the directory to execute in.
        File directory = Paths.get(SETTINGS.getToolsPath() + File.separator + "Spoth").toFile();

        // get spoth schema here
        //executeCommand(cmd.toArray(String[]::new), directory);
        String pathToSpoth = SETTINGS.getToolsPath() +
               File.separator + "Spoth";
        ProcessBuilder pb = new ProcessBuilder(SETTINGS.getJava8Path(),
                "-jar", "JsonExtractor.jar",
                spothArguments[0], spothArguments[1], spothArguments[2]);
        pb.directory(new File(pathToSpoth + File.separator));
        pb.redirectErrorStream(true);
        //pb.redirectInput(ProcessBuilder.Redirect.DISCARD);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Cmd Response: " + line);
        }
        p.getInputStream().close();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        p.destroy();

        String[] arguments = new String[7];
        arguments[0] = "spoth";
        // give schema as string
        arguments[1] = path + File.separator + "spoth_out.json";
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


    // Resolve the directory to execute in.
        String pathToExe = SETTINGS.getToolsPath() +
                File.separator + "Tagger" + File.separator + "Tagger-main" + File.separator + "dist" + File.separator;
        ProcessBuilder builder = new ProcessBuilder(pathToExe + File.separator + experiment, "spoth", arguments[1], arguments[2],
                arguments[3], arguments[4], arguments[5], arguments[6]);
        builder.directory(new File(pathToExe));
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
                System.out.println(shellLine);
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

    public static String convertStreamToStr(InputStream is) throws IOException {

        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        }
        else {
            return "";
        }
    }
}
