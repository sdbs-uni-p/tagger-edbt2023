import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import josch.model.dto.CollectionDto;
import josch.model.dto.ConnectionInfoDto;
import josch.model.dto.SettingsDto;
import josch.model.dto.TaggerExtractionDto;
import josch.model.enums.EDatabaseSystems;
import josch.services.extraction.jsi.FrozzaExtractionService;
import josch.services.extraction.jsi.KlettkeExtractionService;
import josch.services.extraction.jsi.SpothExtractionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaggedUnionExtractionExperiments {

    private FrozzaExtractionService frozzaExtractionService;

    private SpothExtractionService spothExtractionService;

    private KlettkeExtractionService klettkeExtractionService;

    SettingsDto settingsDto = new SettingsDto();

    TaggerExtractionDto taggerExtractionDto = new TaggerExtractionDto();

    CollectionDto collectionDto = new CollectionDto();

    @BeforeAll
    // Please replace with your connection info if you want to use it
    void setup() {
        settingsDto.setDbms(EDatabaseSystems.MONGO);
        settingsDto.setFrozzaPath("/Users/vgi/Documents/schema-inference/approaches/frozza");
        settingsDto.setToolsPath("/Users/vgi/Documents/BT-Valentin-Gittinger-code/Josch/tools");
        settingsDto.setJava8Path("/Users/vgi/Library/Java/JavaVirtualMachines/corretto-1.8.0_352/Contents/Home/bin/java");
        ConnectionInfoDto connectionInfoDto = new ConnectionInfoDto();
        connectionInfoDto.setUrl("mongodb://localhost:27017/test");
        connectionInfoDto.setTimeout(3000);
        settingsDto.setConnectionInfo(connectionInfoDto);

        frozzaExtractionService = new FrozzaExtractionService(settingsDto, taggerExtractionDto);
        frozzaExtractionService.startServer();
    }

    void factory(boolean setIgnoreConstant, boolean setIgnoreUnique, int taggersample,
                 String collection, boolean setUseUnion) throws IOException {
        TaggerExtractionDto taggerExtractionDto = new TaggerExtractionDto();
        taggerExtractionDto.setIgnoreConstantAttributes(setIgnoreConstant);
        taggerExtractionDto.setIgnoreUnique(setIgnoreUnique);
        taggerExtractionDto.setUseUnion(setUseUnion);
        taggerExtractionDto.setSampleSize(taggersample);
        taggerExtractionDto.setMethod("absolute");
        taggerExtractionDto.setSize(1);
        collectionDto.setName(collection);
        settingsDto.setCollection(collectionDto);
        settingsDto.setExtraction(taggerExtractionDto);
        klettkeExtractionService = new KlettkeExtractionService(settingsDto, taggerExtractionDto);
        String schema = klettkeExtractionService.getJsonSchema();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        schema = gson.toJson(JsonParser.parseString(schema));
        File file = new File("/Users/vgi/Documents/BT-Valentin-Gittinger-code/Josch/repro/schemas" + '/' + "klettke_" +
                 collection + setIgnoreConstant + setIgnoreUnique + taggersample + setUseUnion + ".json");
        FileWriter fw = new FileWriter(file);
        fw.write(schema);
        fw.close();
        spothExtractionService = new SpothExtractionService(settingsDto, taggerExtractionDto);
        schema = spothExtractionService.getJsonSchema();
        gson = new GsonBuilder().setPrettyPrinting().create();
        schema = gson.toJson(JsonParser.parseString(schema));
        file = new File("/Users/vgi/Documents/BT-Valentin-Gittinger-code/Josch/repro/schemas" + '/' + "spoth_"
                + collection + setIgnoreConstant + setIgnoreUnique + taggersample + setUseUnion + ".json");
        fw = new FileWriter(file);
        fw.write(schema);
        fw.close();
        frozzaExtractionService = new FrozzaExtractionService(settingsDto, taggerExtractionDto);
        schema = frozzaExtractionService.getJsonSchema();
        gson = new GsonBuilder().setPrettyPrinting().create();
        schema = gson.toJson(JsonParser.parseString(schema));
        file = new File("/Users/vgi/Documents/BT-Valentin-Gittinger-code/Josch/repro/schemas" + '/' + "frozza_" +
                collection + setIgnoreConstant + setIgnoreUnique + taggersample + setUseUnion + ".json");
        fw = new FileWriter(file);
        fw.write(schema);
        fw.close();
    }

    void wikidata(boolean setIgnoreConstant, boolean setIgnoreUnique, int taggersample,
                  String collection, boolean setUseUnion) throws IOException {
        TaggerExtractionDto taggerExtractionDto = new TaggerExtractionDto();
        taggerExtractionDto.setIgnoreConstantAttributes(setIgnoreConstant);
        taggerExtractionDto.setIgnoreUnique(setIgnoreUnique);
        taggerExtractionDto.setUseUnion(setUseUnion);
        taggerExtractionDto.setSampleSize(taggersample);
        taggerExtractionDto.setMethod("absolute");
        taggerExtractionDto.setSize(1);
        collectionDto.setName(collection);
        settingsDto.setCollection(collectionDto);
        settingsDto.setExtraction(taggerExtractionDto);
        klettkeExtractionService = new KlettkeExtractionService(settingsDto, taggerExtractionDto);
        String schema = klettkeExtractionService.getJsonSchema();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        schema = gson.toJson(JsonParser.parseString(schema));
        File file = new File("/Users/vgi/Documents/BT-Valentin-Gittinger-code/Josch/repro/schemas_2" + '/' + "klettke_" +
                collection + setIgnoreConstant + setIgnoreUnique + taggersample + setUseUnion + ".json");
        FileWriter fw = new FileWriter(file);
        fw.write(schema);
        fw.close();
        spothExtractionService = new SpothExtractionService(settingsDto, taggerExtractionDto);
        schema = spothExtractionService.getJsonSchema();
        gson = new GsonBuilder().setPrettyPrinting().create();
        schema = gson.toJson(JsonParser.parseString(schema));
        file = new File("/Users/vgi/Documents/BT-Valentin-Gittinger-code/Josch/repro/schemas_2" + '/' + "spoth_"
                + collection + setIgnoreConstant + setIgnoreUnique + taggersample + setUseUnion + ".json");
        fw = new FileWriter(file);
        fw.write(schema);
        fw.close();
    }

    @Test
    void test1() throws IOException {
        int counter = 1;
        int taggersample = 10;
        String collection = "mapGermany";
        System.out.println("Starting computation ...");
        factory(true, true, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(true, true, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        factory(true, false, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(true, false, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        factory(false, true, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(false, false, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(false, false, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        factory(false, true, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        collection = "wikidata";
        factory(true, true, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(true, true, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        factory(true, false, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(true, false, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        collection = "yelp_academic_dataset_business";
        factory(true, true, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(true, true, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        factory(true, false, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(true, false, taggersample, collection, false);
        System.out.println(counter + " reached");
        counter++;
        factory(false, true, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(false, false, taggersample, collection, true);
        System.out.println(counter + " reached");
        counter++;
        factory(false, false, taggersample, collection, false);System.out.println(counter + " reached");
        counter++;
        factory(false, true, taggersample, collection, false);
    }

    @Test
    void test2() throws IOException {
        int taggersample = 10;
        String collection = "transportation"; // https://catalog.data.gov/dataset/monthly-transportation-statistics
        factory(true, false, taggersample, collection, false);
        factory(true, false, taggersample, collection, true);
        factory(true, true, taggersample, collection, false);
        factory(true, true, taggersample, collection, true);
    }

    @Test
    void test3() throws IOException {
        int taggersample = 10;
        String collection = "wikidata";
        wikidata(true, true, taggersample, collection, true);
        wikidata(true, false, taggersample, collection, true);
        wikidata(true, true, taggersample, collection, false);
        wikidata(true, false, taggersample, collection, false);
    }

    @Test
    void test4() throws IOException {
        int taggersample = 10;
        String collection = "wikidata";
        factory(false, true, taggersample, collection, false);
    }

    @Test
    void test5() throws IOException {
        int taggersample = 10;
        String collection = "wikidata";
        factory(false, false, taggersample, collection, false);
    }
}


