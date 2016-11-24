/*
 * [The "BSD licence"]
 * Copyright (c) 2010 Ben Gruver (JesusFreke)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package astli.main;

import astli.pojo.ASTLIOptions;
import astli.extraction.FeatureExtractor;
import org.apache.commons.cli.*;
import org.jf.util.ConsoleUtil;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;
import astli.match.MatchAlgorithm;
import astli.match.MatchingProcess;
import astli.learn.LearnAlgorithm;
import astli.pojo.PackageHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class main {

    public static String VERSION = "1.0";

    private static final Options OPTIONS;
    
    private static final Logger LOGGER = LogManager.getLogger(main.class);

    static {
        OPTIONS = new Options();
        buildOptions();
    }

    private main() {
    }

    /**
     * Run!
     * @param args
     */
    public static void main(String[] args) {
        try {
            Locale locale = new Locale("en", "US");
            Locale.setDefault(locale);
            
            
            ASTLIOptions astliOptions = parseOptions(args);

            Stream<PackageHierarchy> packages = new FeatureExtractor(astliOptions).extractPackageHierarchies();
            
            AndroidLibIDAlgorithm alg;
            
            if(MatchAlgorithm.class.equals(astliOptions.algorithm)) {
                alg = new MatchAlgorithm(packages, astliOptions);
            } else {
                alg = new LearnAlgorithm(packages, astliOptions);
            }
            
            alg.run();
            
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }

    /**
     * Prints the usage message.
     */
    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        int consoleWidth = ConsoleUtil.getConsoleWidth();
        if (consoleWidth <= 0) {
            consoleWidth = 80;
        }

        formatter.setWidth(consoleWidth);

        formatter.printHelp("java -jar baksmali.jar [options] <dex-file>",
                "find library dependencies in android apk", OPTIONS, "");
    }

    /**
     * Prints the version message.
     */
    protected static void printVersion() {
        LOGGER.info("androidlibid " + VERSION);
        LOGGER.info("Copyright...?");
        LOGGER.info("Licence...?");
        System.exit(0);
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static void buildOptions() {
        Option versionOption = OptionBuilder.withLongOpt("version")
                .withDescription("prints the version then exits")
                .create("v");

        Option helpOption = OptionBuilder.withLongOpt("help")
                .withDescription("prints the help message then exits. "
                        + "Specify twice for debug options")
                .create("?");
        
        Option printOption = OptionBuilder.withLongOpt("print-setup")
                .withDescription("print setup in libraries")
                .create("s");

        Option matchingOption = OptionBuilder.withLongOpt("matching")
                .withDescription("compare from given .apk file with packages from db")
                .create("m");
        
        Option mappingFileOption = OptionBuilder.withLongOpt("mapping-file")
                .hasArg()
                .withArgName("MAPPING_FILE")
                .withDescription("Replace class and package "
                        + "names with the names specified in the mapping file. "
                        + "For format, see Proguard documentation. Makes sense "
                        + "to use together with --matching.")
                .create("f");

        Option learningOption = OptionBuilder.withLongOpt("learning")
                .hasArg()
                .withArgName("GROUP_ID:ARTIFACT_ID:VERSION")
                .withDescription("learn a library (.jar) and store to database.")
                .create("l");

        OPTIONS.addOption(versionOption);
        OPTIONS.addOption(helpOption);
        OPTIONS.addOption(matchingOption);
        OPTIONS.addOption(learningOption);
        OPTIONS.addOption(printOption);
        OPTIONS.addOption(mappingFileOption);

    }
    
    private static ASTLIOptions parseOptions(String[] args) {
        
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(OPTIONS, args);
        } catch (ParseException ex) {
            printUsage();
            return null;
        }

        ASTLIOptions astliOptions = new ASTLIOptions(MatchAlgorithm.class, MatchingProcess.class);

        String[] remainingArgs = commandLine.getArgs();
        Option[] clOptions = commandLine.getOptions();

        for (int i=0; i<clOptions.length; i++) {
            Option option = clOptions[i];
            String opt = option.getOpt();

            switch (opt.charAt(0)) {
                case 'v':
                    printVersion();
                    return null;
                case '?':
                    while (++i < clOptions.length) {
                        if (clOptions[i].getOpt().charAt(0) == '?') {
                            printUsage();
                            return null;
                        }
                    }
                    printUsage();
                    return null;

                case 'm':
                    astliOptions.algorithm = MatchAlgorithm.class;
                    break;
                    
                case 'f':
                    astliOptions.mappingFile = commandLine.getOptionValue('f');
                    break;
                    
                case 'l':
                    astliOptions.algorithm = LearnAlgorithm.class;
                    astliOptions.mvnIdentifier = commandLine.getOptionValue('l');
                    break;
                    
                case 'a':
                    int algId = Integer.parseInt(commandLine.getOptionValue('a'));
                    if(algId != 1) {
                        printUsage();
                        return null;
                    }
                    
                    //todo: add  other processes
                    astliOptions.process = MatchingProcess.class;
                    break;
                case 's': 
                    // astliOptions.algorithm = 
                    //todo!
                    break;
                default:
                    assert false;
            }
        }
                    
        if (remainingArgs.length != 1) {
            printUsage();
            return null;
        }
        
        astliOptions.setFileName(remainingArgs[0]);
        
        return astliOptions;
        
    }
}
