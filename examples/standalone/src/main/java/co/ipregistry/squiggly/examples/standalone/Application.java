package co.ipregistry.squiggly.examples.standalone;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import co.ipregistry.squiggly.Squiggly;
import co.ipregistry.squiggly.examples.standalone.model.Issue;

import java.io.File;

public class Application {

    public static void main(String[] args) throws Exception {
        Object model = null;
        String filter = null;

        if (args.length == 0) {
            model = Issue.findAll();
            filter = "**";
        } else if (args.length == 1) {
            model = Issue.findAll();
            filter = args[0];
        } else if (args.length == 2) {
            model = JsonMapper.builder().build().readValue(new File(args[0]), Object.class);
            filter = args[1];
        } else {
            printUsage();
            System.exit(1);
        }

        ObjectMapper objectMapper = Squiggly.init(JsonMapper.builder().build(), filter);
        objectMapper.writeValue(System.out, model);
    }

    private static void printUsage() {
        String prefix = "./gradlew run --args";
        String usage = "Usage:\n" +
                prefix + '\n' +
                prefix + " " + "'filter'\n" +
                prefix + " " + "'json-file filter'\n";

        System.out.println(usage);
    }
}
