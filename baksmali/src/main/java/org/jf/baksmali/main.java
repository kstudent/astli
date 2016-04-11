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

package org.jf.baksmali;

import com.google.common.collect.Lists;
import org.apache.commons.cli.*;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.DexFileFactory.MultipleDexFilesException;
import org.jf.dexlib2.analysis.InlineMethodResolver;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.dexbacked.OatFile.OatDexFile;
import org.jf.util.ConsoleUtil;
import org.jf.util.SmaliHelpFormatter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class main {

    public static final String VERSION;

    private static final Options basicOptions;
    private static final Options debugOptions;
    private static final Options options;

    static {
        options = new Options();
        basicOptions = new Options();
        debugOptions = new Options();
        buildOptions();

        InputStream templateStream = baksmali.class.getClassLoader().getResourceAsStream("baksmali.properties");
        if (templateStream != null) {
            Properties properties = new Properties();
            String version = "(unknown)";
            try {
                properties.load(templateStream);
                version = properties.getProperty("application.version");
            } catch (IOException ex) {
                // ignore
            }
            VERSION = version;
        } else {
            VERSION = "[unknown version]";
        }
    }

    /**
     * This class is uninstantiable.
     */
    private main() {
    }

    /**
     * Run!
     */
    public static void main(String[] args) throws IOException {
        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);
        
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine;

        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            usage();
            return;
        }

        baksmaliOptions options = new baksmaliOptions();

        boolean disassemble = true;
        boolean setBootClassPath = false;

        String[] remainingArgs = commandLine.getArgs();
        Option[] clOptions = commandLine.getOptions();

        for (int i=0; i<clOptions.length; i++) {
            Option option = clOptions[i];
            String opt = option.getOpt();

            switch (opt.charAt(0)) {
                case 'v':
                    version();
                    return;
                case '?':
                    while (++i < clOptions.length) {
                        if (clOptions[i].getOpt().charAt(0) == '?') {
                            usage(true);
                            return;
                        }
                    }
                    usage(false);
                    return;

                case 'y':
                    options.aliFingerprintAPK = true;
                    break;
                case 'z':
                    options.mvnIdentifier = commandLine.getOptionValue('z');
                    options.aliFingerprintJAR = true;
                    break;
                case 'Z':
                    options.mappingFile = commandLine.getOptionValue('Z');
                    options.isObfuscated = true;
                    break;
                case 'a':
                    int algId = Integer.parseInt(commandLine.getOptionValue('a'));
                    if(algId < 1 || algId > 5) {
                        usage();
                        return;
                    }
                    options.algorithmID = algId;
                    if(algId == 5) {
                        options.allowRepeatedMatching = false;
                    }
                    
                    break;
                case 'j':
                    options.jobs = Integer.parseInt(commandLine.getOptionValue("j"));
                    break;
                default:
                    assert false;
            }
        }
                    
        if (remainingArgs.length != 1) {
            usage();
            return;
        }
        
        if (options.jobs <= 0) {
            options.jobs = Runtime.getRuntime().availableProcessors();
            if (options.jobs > 6) {
                options.jobs = 6;
            }
        }

        String inputDexFileName = remainingArgs[0];

        File dexFileFile = new File(inputDexFileName);
        if (!dexFileFile.exists()) {
            System.err.println("Can't find the file " + inputDexFileName);
            System.exit(1);
        }
        
        if (options.aliFingerprintJAR) {
            String[] dxArgs = {"--dex", "--output=" + inputDexFileName + ".dex", inputDexFileName};
            com.android.dx.command.Main.main(dxArgs);
            dexFileFile = new File(inputDexFileName + ".dex");
        }

        //Read in and parse the dex file
        DexBackedDexFile dexFile = null;
        try {
            dexFile = DexFileFactory.loadDexFile(dexFileFile, options.dexEntry, options.apiLevel, options.experimental);
        } catch (MultipleDexFilesException ex) {
            System.err.println(String.format("%s contains multiple dex files. You must specify which one to " +
                    "disassemble with the -e option", dexFileFile.getName()));
            System.err.println("Valid entries include:");
            for (OatDexFile oatDexFile: ex.oatFile.getDexFiles()) {
                System.err.println(oatDexFile.filename);
            }
            System.exit(1);
        }

        if (dexFile.hasOdexOpcodes()) {
            if (!options.deodex) {
                System.err.println("Warning: You are disassembling an odex file without deodexing it. You");
                System.err.println("won't be able to re-assemble the results unless you deodex it with the -x");
                System.err.println("option");
                options.allowOdex = true;
            }
        } else {
            options.deodex = false;
        }

        if (!setBootClassPath && (options.deodex || options.registerInfo != 0 || options.normalizeVirtualMethods)) {
            if (dexFile instanceof DexBackedOdexFile) {
                options.bootClassPathEntries = ((DexBackedOdexFile)dexFile).getDependencies();
            } else {
                options.bootClassPathEntries = getDefaultBootClassPathForApi(options.apiLevel,
                        options.experimental);
            }
        }

        if (options.customInlineDefinitions == null && dexFile instanceof DexBackedOdexFile) {
            options.inlineResolver =
                    InlineMethodResolver.createInlineMethodResolver(
                            ((DexBackedOdexFile)dexFile).getOdexVersion());
        }

        boolean errorOccurred = false;
        if (disassemble) {
            errorOccurred = !baksmali.disassembleDexFile(dexFile, options);
        }

        if (errorOccurred) {
            System.exit(1);
        }
    }

    /**
     * Prints the usage message.
     */
    private static void usage(boolean printDebugOptions) {
        SmaliHelpFormatter formatter = new SmaliHelpFormatter();
        int consoleWidth = ConsoleUtil.getConsoleWidth();
        if (consoleWidth <= 0) {
            consoleWidth = 80;
        }

        formatter.setWidth(consoleWidth);

        formatter.printHelp("java -jar baksmali.jar [options] <dex-file>",
                "/opt/smali/baksmali. srsly. disassembles and/or dumps a dex file", basicOptions, printDebugOptions?debugOptions:null);
    }

    private static void usage() {
        usage(false);
    }

    /**
     * Prints the version message.
     */
    protected static void version() {
        System.out.println("androidlibid " + VERSION);
        System.out.println("Copyright...?");
        System.out.println("Licence...?");
        System.exit(0);
    }

    @SuppressWarnings("AccessStaticViaInstance")
    private static void buildOptions() {
        Option versionOption = OptionBuilder.withLongOpt("version")
                .withDescription("prints the version then exits")
                .create("v");

        Option helpOption = OptionBuilder.withLongOpt("help")
                .withDescription("prints the help message then exits. Specify twice for debug options")
                .create("?");

        Option aliFingerprintAPK = OptionBuilder.withLongOpt("ali-apk")
                .withDescription("try to identify libraries in an android app (.apk)")
                .create("y");

        Option aliFingerprintJAR = OptionBuilder.withLongOpt("ali-jar")
                .hasArg()
                .withArgName("GROUP_ID:ARTIFACT_ID:VERSION")
                .withDescription("fingerprint a library (.jar) and store to database")
                .create("z");
        
        Option verifyObfuscationWithMapping = OptionBuilder.withLongOpt("verify-obfuscation-with-mapping-file")
                .hasArg()
                .withArgName("mapping-file")
                .withDescription("uses the given proguard mapping file to verify"
                        + " the quality of the matching algorithm (can only be "
                        + "used together with --ali-apk option)")
                .create("Z");
        
        Option algorithmOption = OptionBuilder.withLongOpt("algorithm")
                .hasArg()
                .withArgName("algorithm-ID")
                .withDescription("choose matching algorithm (can only be "
                        + "used together with --ali-apk option):\n"
                        + "1: vector-difference on package level\n"
                        + "2: vector-difference on class level\n"
                        + "3: vector-difference on method level\n"
                        + "4: inclusion-strategy with method needle, repeated matching (of classes and methods) allowed\n"
                        + "5: inclusion-strategy with method needle, repeated matching (of classes and methods) disabled\n")
                .create("a");
        
        Option jobsOption = OptionBuilder.withLongOpt("jobs")
                .withDescription("The number of threads to use. Defaults to the number of cores available, up to a " +
                        "maximum of 6")
                .hasArg()
                .withArgName("NUM_THREADS")
                .create("j");

        basicOptions.addOption(versionOption);
        basicOptions.addOption(helpOption);
        basicOptions.addOption(aliFingerprintAPK);
        basicOptions.addOption(aliFingerprintJAR);
        basicOptions.addOption(verifyObfuscationWithMapping);
        basicOptions.addOption(algorithmOption);
        basicOptions.addOption(jobsOption);

        for (Object option: basicOptions.getOptions()) {
            options.addOption((Option)option);
        }
        for (Object option: debugOptions.getOptions()) {
            options.addOption((Option)option);
        }
    }

    @Nonnull
    private static List<String> getDefaultBootClassPathForApi(int apiLevel, boolean experimental) {
        if (apiLevel < 9) {
            return Lists.newArrayList(
                    "/system/framework/core.jar",
                    "/system/framework/ext.jar",
                    "/system/framework/framework.jar",
                    "/system/framework/android.policy.jar",
                    "/system/framework/services.jar");
        } else if (apiLevel < 12) {
            return Lists.newArrayList(
                    "/system/framework/core.jar",
                    "/system/framework/bouncycastle.jar",
                    "/system/framework/ext.jar",
                    "/system/framework/framework.jar",
                    "/system/framework/android.policy.jar",
                    "/system/framework/services.jar",
                    "/system/framework/core-junit.jar");
        } else if (apiLevel < 14) {
            return Lists.newArrayList(
                    "/system/framework/core.jar",
                    "/system/framework/apache-xml.jar",
                    "/system/framework/bouncycastle.jar",
                    "/system/framework/ext.jar",
                    "/system/framework/framework.jar",
                    "/system/framework/android.policy.jar",
                    "/system/framework/services.jar",
                    "/system/framework/core-junit.jar");
        } else if (apiLevel < 16) {
            return Lists.newArrayList(
                    "/system/framework/core.jar",
                    "/system/framework/core-junit.jar",
                    "/system/framework/bouncycastle.jar",
                    "/system/framework/ext.jar",
                    "/system/framework/framework.jar",
                    "/system/framework/android.policy.jar",
                    "/system/framework/services.jar",
                    "/system/framework/apache-xml.jar",
                    "/system/framework/filterfw.jar");
        } else if (apiLevel < 21) {
            // this is correct as of api 17/4.2.2
            return Lists.newArrayList(
                    "/system/framework/core.jar",
                    "/system/framework/core-junit.jar",
                    "/system/framework/bouncycastle.jar",
                    "/system/framework/ext.jar",
                    "/system/framework/framework.jar",
                    "/system/framework/telephony-common.jar",
                    "/system/framework/mms-common.jar",
                    "/system/framework/android.policy.jar",
                    "/system/framework/services.jar",
                    "/system/framework/apache-xml.jar");
        } else { // api >= 21
            // TODO: verify, add new ones?
            return Lists.newArrayList(
                    "/system/framework/core-libart.jar",
                    "/system/framework/conscrypt.jar",
                    "/system/framework/okhttp.jar",
                    "/system/framework/core-junit.jar",
                    "/system/framework/bouncycastle.jar",
                    "/system/framework/ext.jar",
                    "/system/framework/framework.jar",
                    "/system/framework/telephony-common.jar",
                    "/system/framework/voip-common.jar",
                    "/system/framework/ims-common.jar",
                    "/system/framework/mms-common.jar",
                    "/system/framework/android.policy.jar",
                    "/system/framework/apache-xml.jar");
        }
    }
}
