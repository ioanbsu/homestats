package com.artigile.homestats;

import com.google.common.base.Preconditions;
import org.apache.commons.cli.*;


/**
 * @author ivanbahdanau
 */
@SuppressWarnings("AccessStaticViaInstance")
final class ArgsParser {


    /**
     * When this parameter passed help is printed and application should exit right away.
     */
    public static final Option HELP_OPTION = OptionBuilder
            .withDescription("Print this message")
            .withLongOpt("help")
            .create("h");


    public static final Option APP_MODE_OPTION = OptionBuilder
            .withDescription("App HTU21F - application reads data from real sensors(temp and humidity available)." +
                    "BMP085 - application reads data from real sensors(temp only available)." +
                    " DEV returns all fake data." +
                    " Default is DEV.")
            .withLongOpt("mode")
            .withArgName("MODE")
            .hasArg()
            .create("m");

    /**
     * Db host.
     */
    public static final Option DB_HOST_OPTION = OptionBuilder
            .withDescription("Database server url. Default is [localhost]")
            .withLongOpt("dbHost")
            .withArgName("DB_HOST")
            .hasArg()
            .create("d");

    /**
     * DB user password
     */
    public static final Option DB_PWD_OPTION = OptionBuilder
            .withDescription("Database password")
            .withLongOpt("dbPassword")
            .withArgName("DB_PASSWORD")
            .hasArg()
            .isRequired()
            .create("p");


    /**
     * DB user login name.
     */
    public static final Option DB_USER_OPTION = OptionBuilder
            .withDescription("Database user")
            .withLongOpt("dbUser")
            .withArgName("DB_USER")
            .hasArg()
            .isRequired()
            .create("u");

    /**
     * Application http port.
     */
    public static final Option APP_PORT_OPTION = OptionBuilder
            .withDescription("Application port.")
            .withLongOpt("appPort")
            .withArgName("APP_PORT")
            .hasArg()
            .create("a");

    /**
     * Application http port.
     */
    public static final Option PRINT_AND_EXIT = OptionBuilder
            .withDescription("Print sensors data and exit the application")
            .withLongOpt("print")
            .withArgName("PRINT")
            .create("i");


    /**
     * Parsed command line options.
     */
    private CommandLine parsedCmdLine;

    /**
     * Parsed command line options.
     */
    private CommandLine helpOptsCmdLine;

    /**
     * Available options that are parsed and recognized.
     */
    private static final Options AVAILABLE_ARGS = new Options()
            .addOption(APP_MODE_OPTION).addOption(DB_HOST_OPTION).addOption(DB_PWD_OPTION)
            .addOption(DB_USER_OPTION).addOption(APP_PORT_OPTION).addOption(PRINT_AND_EXIT);
    /**
     * Help options.
     */
    private static final Options HELP_OPTS = new Options().addOption(HELP_OPTION);

    /**
     * Constructor that parses all input arguments so later particular argument can be retrieved.
     *
     * @param args arguments array to parse
     * @throws ParseException happens when not expected parameters are passed
     */
    public ArgsParser(final String[] args) throws ParseException {
        //see more at: http://www.gnu.org/prep/standards/html_node/Command_002dLine-Interfaces.html
        Preconditions.checkArgument(args.length > 0, "Application requires input parameters.");
        CommandLineParser commandLineParser = new GnuParser();
        try {
            helpOptsCmdLine = commandLineParser.parse(HELP_OPTS, args);
        } catch (UnrecognizedOptionException e) {
            //when help option not present, parse application configuration parameters
            parsedCmdLine = commandLineParser.parse(AVAILABLE_ARGS, args);
        }
    }

    /**
     * @return tru is help parameter recognized.
     */
    public boolean isDisplayHelp() {
        return helpOptsCmdLine != null;
    }

    /**
     * @return parsed command line with all parsed input parameters that were passed to the app.
     */
    private CommandLine parsedParams() {
        return parsedCmdLine;
    }

    /**
     * Returns argument value by passed option.
     *
     * @param optionToParse the name of the option.
     * @return argument value or throws null pointer exception when arg is not found.
     */
    public String getString(final Option optionToParse) {
        return getString(optionToParse, null);
    }

    /**
     * Returns argument value by passed option with a default value.
     *
     * @param optionToParse the name of the option.
     * @param defaultValue  the value to return if none was specified.
     * @return argument value or throws null pointer exception when arg is not found.
     */
    public String getString(final Option optionToParse, final String defaultValue) {
        String paramName = optionToParse.getLongOpt();
        return parsedParams().getOptionValue(paramName, defaultValue);
    }

    public boolean argumentPassed(final Option checkIfPassed){
        return parsedParams().hasOption(checkIfPassed.getLongOpt());
    }


    /**
     * Displays help into console on how to use router app.
     */
    public static void printHelp() {
        Options options = new Options();
        for (Object ob : HELP_OPTS.getOptions()) {
            Option option = (Option) ob;
            options.addOption(option);
        }
        for (Object ob : AVAILABLE_ARGS.getOptions()) {
            Option option = (Option) ob;
            options.addOption(option);
        }
        new HelpFormatter().printHelp("java -jar router.jar",
                "The following options are available:", options, "", true);
    }


}

