package io.github.eb4j.tool;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

/**
 * Main class for main command.
 */
@Command(name = "eb",
        description = "EPWING ebook tools",
        mixinStandardHelpOptions = true,
        subcommands = {HelpCommand.class, EBDump.class, EBZip.class, EBInfo.class, EBAppendix.class, Map2Yaml.class},
        synopsisSubcommandLabel = "[dump|info|zip]",
        version = {"eb4j-tools",
                "Version " + Main.VERSION,
                "Copyright (c) 2002-2007 by Hisaya FUKUMOTO.",
                "Copyright (c) 2016 Hiroshi Miura, Aaron Madlon-Kay",
                "Copyright (c) 2020-2021 Hiroshi Miura"})
public class Main implements Runnable {

    /**
     * Version string to show in command help.
     */
    public static final String VERSION = "2.1.0";

    /**
     * Dummy run function when running without subcommand.
     */
    @Override
    public void run() {
    }

    /**
     * Main function.
     * @param args command line arguments.
     */
    public static void main(final String... args) {
        CommandLine cmd = new CommandLine(new Main());
        int returnCode = cmd.execute(args);
        if (args.length == 0) {
            cmd.usage(System.out);
        }
        System.exit(returnCode);
    }

}
