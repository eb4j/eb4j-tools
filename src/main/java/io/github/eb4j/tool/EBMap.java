package io.github.eb4j.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import io.github.eb4j.ext.UnicodeMap;
import io.github.eb4j.tool.appendix.AltDef;
import io.github.eb4j.tool.appendix.Appendix;
import io.github.eb4j.tool.appendix.Range;
import io.github.eb4j.tool.appendix.SubAppendix;

/**
 * @author Hiroshi Miura
 */
@CommandLine.Command(name = "map", mixinStandardHelpOptions = true, description = "Generate furoku YAML definition " +
        "from EBWin map data")
public class EBMap implements Callable<Integer> {

    private static final String EPWING_TYPE = "EPWING";

    @CommandLine.Parameters(description="source map file")
    File sourceMap;

    @CommandLine.Parameters(description="destination yaml file")
    File yamlFile;

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        if (!sourceMap.isFile()) {
            System.out.println("Source file not found.");
            return 1;
        }
        if (yamlFile.exists()) {
            System.out.println("Destination file is exist, data will be removed.");
            boolean ignore = yamlFile.delete();
        }
        UnicodeMap unicodeMap = new UnicodeMap(sourceMap);
        Appendix appendix = constructData(unicodeMap);
        generateYaml(appendix);
        return 0;
    }

    protected Appendix constructData(UnicodeMap unicodeMap) throws Exception {
        AltDef narrow = generateAltDef(unicodeMap.getNarrowMap());
        AltDef wide = generateAltDef(unicodeMap.getWideMap());
        SubAppendix subBook = new SubAppendix("name", true, "JISX0208", "", narrow, wide);
        List<SubAppendix> subAppendixList = new ArrayList<>();
        subAppendixList.add(subBook);
        Appendix appendix = new Appendix("title", EPWING_TYPE, subAppendixList);
        return appendix;
    }

    protected AltDef generateAltDef(Map<Integer, String> map) {
        int maxRange =
                map.keySet().stream().max(Comparator.naturalOrder()).orElseThrow();
        int minRange =
                map.keySet().stream().min(Comparator.naturalOrder()).orElseThrow();
        Map<String, String> targetMap =
                map.keySet().stream().collect(Collectors.toMap(key -> String.format("%04X", key), map::get));
        return new AltDef(new Range(minRange, maxRange), targetMap);
    }

    protected void generateYaml(Appendix appendix) throws IOException {
        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);
        try (FileOutputStream fos = new FileOutputStream(yamlFile)) {
            SequenceWriter sw = mapper.writerWithDefaultPrettyPrinter().writeValues(fos);
            sw.write(appendix);
        }
    }
}
