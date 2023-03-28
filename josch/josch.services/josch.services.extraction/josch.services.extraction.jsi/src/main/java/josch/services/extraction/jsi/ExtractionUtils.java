package josch.services.extraction.jsi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import josch.model.dto.SettingsDto;
import josch.model.dto.TaggerExtractionDto;
import josch.persistency.factory.AbstractClientFactory;
import josch.persistency.interfaces.IClient;

import java.io.File;
import java.util.*;

public class ExtractionUtils {

    public List<JsonNode> sendFilesToExtractionService(SettingsDto SETTINGS) {

        Thread shutdownHook = new Thread(() -> {
            String path = SETTINGS.getToolsPath() + File.separator + "temp";
            Arrays.stream(Objects.requireNonNull(new File(path).listFiles())).forEach(File::delete);
            FrozzaExtractionService frozzaExtractionService
                   = new FrozzaExtractionService(SETTINGS, new TaggerExtractionDto());
            try {
                frozzaExtractionService.stopServer();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        ObjectMapper mapper = new ObjectMapper();
        IClient client = AbstractClientFactory.getClient(SETTINGS);

        // Get the amount of documents.
        String method = SETTINGS.getExtraction().getMethod();
        int size = SETTINGS.getExtraction().getSize();
        if (method.equals("relative")) {
            double percent = (double) size / 100;
            long amountDocuments = SETTINGS.getCollection().getCount();
            size = (int) (amountDocuments * percent);
        }
        if (method.equals("absolute")) {
            size = size - 1;
        }
        List<JsonNode> samples = new ArrayList<>();
        Iterator<String> it = client.getDocumentIterator(SETTINGS.getCollection().getName(), true);
        String doc;
        JsonNode current;

        /* Tracks the memory because large collections might easily exceed the memory limits. */
        Runtime jvm = Runtime.getRuntime();
        long allocatedMemory;
        long freeMemory;
        boolean hasMemoryLeft;

        // The reserve half the maximum available memory of the JVM.
        long maxMemory = jvm.maxMemory();
        long reservedMemory = (long) (maxMemory * 0.5);
        System.out.println(
                "Reserving for processing: " + (reservedMemory / Math.pow(1024, 3)) + " GB");

        // Add the documents to the sample if they still fit into the memory.
        int i = 0;
        do {
            // Check the memory state.
            allocatedMemory = jvm.totalMemory() - jvm.freeMemory() + reservedMemory;
            freeMemory = maxMemory - allocatedMemory;
            hasMemoryLeft = freeMemory > 0 | jvm.totalMemory() + reservedMemory < maxMemory;

            // Add the documents to the sample list.
            try {
                doc = it.next();
                current = mapper.readTree(doc);
                samples.add(current);
                i++;
            } catch (JsonProcessingException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        } while (it.hasNext() && i <= size && hasMemoryLeft);


        if (!hasMemoryLeft) {
            System.out.println("System reaching memory limits:");
            System.out.println("Total Memory (GB): " + (jvm.totalMemory() / Math.pow(1024,3)));
            System.out.println("Free Memory (MB): " + (jvm.freeMemory() / Math.pow(1024, 2)));
            System.out.println("JSI couldn't sample all documents because the memory has reached its " +
                    "limits. it sampled " + i + " documents instead.");
        }
        return samples;
    }
}
