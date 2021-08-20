package io.github.eb4j.tool;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "eb",
        description = "EPWING ebook tools",
        mixinStandardHelpOptions = true,
        subcommands = {HelpCommand.class, EBDump.class, EBZip.class, EBInfo.class, EBAppendix.class},
        synopsisSubcommandLabel = "[dump|info|zip]",
        version = {"eb4j-tools",
                "Version " + Main.VERSION,
                "Copyright (c) 2002-2007 by Hisaya FUKUMOTO.",
                "Copyright (c) 2016 Hiroshi Miura, Aaron Madlon-Kay",
                "Copyright (c) 2020-2021 Hiroshi Miura"})
public class Main implements Runnable {

    public static final String VERSION = "2.1.0";

    @Override
    public void run() {
    }

    public static void main(final String... args) {
        CommandLine cmd = new CommandLine(new Main());
        int returnCode = cmd.execute(args);
        if (args.length == 0) {
            cmd.usage(System.out);
        }
        System.exit(returnCode);
    }

}
