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

import astli.db.EntityServiceFactory;
import astli.pojo.ASTLIOptions;
import astli.extraction.FeatureExtractor;
import org.apache.commons.cli.*;
import org.jf.util.ConsoleUtil;

import java.util.Locale;
import java.util.stream.Stream;
import astli.match.MatchAlgorithm;
import astli.learn.LearnAlgorithm;
import astli.match.SetupLogger;
import astli.pojo.PackageHierarchy;
import java.sql.SQLException;
import java.util.logging.Level;
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
        
        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);
        
        ASTLIOptions astliOptions;
        
        try {
            astliOptions = parseOptions(args);
        } catch(RuntimeException ex) {
            printUsage();
            return;
        }
        
        try {
            Stream<PackageHierarchy> packages = new FeatureExtractor(astliOptions).
                    extractPackageHierarchies();
            
            AndroidLibIDAlgorithm alg;
            
            if(MatchAlgorithm.class.equals(astliOptions.algorithm)) {
                alg = new MatchAlgorithm(packages, astliOptions);
            } else {
                alg = new LearnAlgorithm(packages, astliOptions);
            }
            
            alg.run();
            
        } catch (Exception ex) {
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

        formatter.printHelp("java -jar astli.jar [options]+ <dex-file>",
                "find library dependencies in android apk", OPTIONS, "");
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static void buildOptions() {
        
        Option helpOption = OptionBuilder.withLongOpt("help")
                .withDescription("Prints the help message then exits.")
                .create("?");
        
        Option printOption = OptionBuilder.withLongOpt("print-setup")
                .withDescription("Print setup in libraries.")
                .create("s");

        Option matchingOption = OptionBuilder.withLongOpt("matching")
                .withDescription("Find similar packages from given .apk file "
                        + "in the set of learned packages.")
                .create("m");
        
        Option evalOption = OptionBuilder.withLongOpt("evaluation-mode")
                .withDescription("Activates evaluation mode (default: production mode). "
                        + "Use with --matching.")
                .create("e");
        
        Option mappingFileOption = OptionBuilder.withLongOpt("mapping-file")
                .hasArg()
                .withArgName("MAPPING_FILE")
                .withDescription("Replace class and package "
                        + "names with the names specified in the mapping file. "
                        + "For format, see Proguard documentation. "
                        + "Use with --matching.")
                .create("f");
        
        Option algorithmOption = OptionBuilder.withLongOpt("matching-algorithm-setup")
                .hasArg()
                .withArgName("ID")
                .withDescription("Choose a matching algorithm setup. "
                        + "Use with --matching.")
                .create("a");

        Option learningOption = OptionBuilder.withLongOpt("learning")
                .hasArg()
                .withArgName("GROUP_ID:ARTIFACT_ID:VERSION")
                .withDescription("Learn a library (.jar) and store to database.")
                .create("l");

        OPTIONS.addOption(helpOption);
        OPTIONS.addOption(printOption);
        OPTIONS.addOption(learningOption);
        OPTIONS.addOption(matchingOption);
        OPTIONS.addOption(mappingFileOption);
        OPTIONS.addOption(algorithmOption);
        OPTIONS.addOption(evalOption);

    }
    
    private static ASTLIOptions parseOptions(String[] args) {
        
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(OPTIONS, args);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }

        ASTLIOptions astliOptions = new ASTLIOptions();

        String[] remainingArgs = commandLine.getArgs();
        Option[] clOptions = commandLine.getOptions();

        for (int i=0; i<clOptions.length; i++) {
            Option option = clOptions[i];
            String opt = option.getOpt();

            switch (opt.charAt(0)) {
                    
                case '?':
                    throw new RuntimeException();
                    
                case 's': 
                    {
                        try {
                            new SetupLogger(EntityServiceFactory.createService(), astliOptions).logSetup();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    throw new RuntimeException();
                    
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
                    
                case 'e':
                    astliOptions.isInEvaluationMode = true;
                    break;
                    
                case 'a':
                    String algId = commandLine.getOptionValue('a');
                    if(!AlgIDDecoder.decode(algId, astliOptions)) {
                        throw new RuntimeException();
                    }
                    break;
                    
                default:
                    assert false;
            }
        }
                    
        if (remainingArgs.length != 1) {
            throw new RuntimeException();
        }
        
        astliOptions.setFileName(remainingArgs[0]);
        return astliOptions;
        
    }
}
