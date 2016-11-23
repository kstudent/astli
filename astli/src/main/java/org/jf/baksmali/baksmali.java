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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.jf.dexlib2.analysis.ClassPath;
import org.jf.dexlib2.analysis.CustomInlineMethodResolver;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.analysis.InlineMethodResolver;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.dexbacked.OatFile;
import org.jf.dexlib2.util.SyntheticAccessorResolver;

public class baksmali {
    
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<? extends ClassDef> disassembleDexFile(DexFile dexFile, final baksmaliOptions options) throws IOException {
        if (options.registerInfo != 0 || options.deodex || options.normalizeVirtualMethods) {
            try {
                Iterable<String> extraClassPathEntries;
                if (options.extraClassPathEntries != null) {
                    extraClassPathEntries = options.extraClassPathEntries;
                } else {
                    extraClassPathEntries = ImmutableList.of();
                }

                options.classPath = ClassPath.fromClassPath(options.bootClassPathDirs,
                        Iterables.concat(options.bootClassPathEntries, extraClassPathEntries), dexFile,
                        options.apiLevel, options.checkPackagePrivateAccess, options.experimental);

                if (options.customInlineDefinitions != null) {
                    options.inlineResolver = new CustomInlineMethodResolver(options.classPath,
                            options.customInlineDefinitions);
                }
            } catch (Exception ex) {
                throw new IOException("\n\nError occurred while loading boot class path files. Aborting.", ex);
            }
        }

        if (options.resourceIdFileEntries != null) {
            class PublicHandler extends DefaultHandler {
                String prefix = null;
                public PublicHandler(String prefix) {
                    super();
                    this.prefix = prefix;
                }

                public void startElement(String uri, String localName,
                        String qName, Attributes attr) throws SAXException {
                    if (qName.equals("public")) {
                        String type = attr.getValue("type");
                        String name = attr.getValue("name").replace('.', '_');
                        Integer public_key = Integer.decode(attr.getValue("id"));
                        String public_val = new StringBuffer()
                            .append(prefix)
                            .append(".")
                            .append(type)
                            .append(".")
                            .append(name)
                            .toString();
                        options.resourceIds.put(public_key, public_val);
                    }
                }
            };

            for (Entry<String,String> entry: options.resourceIdFileEntries.entrySet()) {
                try {
                    SAXParser saxp = SAXParserFactory.newInstance().newSAXParser();
                    String prefix = entry.getValue();
                    saxp.parse(entry.getKey(), new PublicHandler(prefix));
                } catch (ParserConfigurationException e) {
                    continue;
                } catch (SAXException e) {
                    continue;
                } catch (IOException e) {
                    continue;
                }
            }
        }

        File outputDirectoryFile = new File(options.outputDirectory);
        if (!outputDirectoryFile.exists()) {
            if (!outputDirectoryFile.mkdirs()) {
                LOGGER.error("Can't create the output directory " + options.outputDirectory);
                return null;
            }
        }
        
        //sort the classes, so that if we're on a case-insensitive file system and need to handle classes with file
        //name collisions, then we'll use the same name for each class, if the dex file goes through multiple
        //baksmali/smali cycles for some reason. If a class with a colliding name is added or removed, the filenames
        //may still change of course
        List<? extends ClassDef> classDefs = Ordering.natural().sortedCopy(dexFile.getClasses());
        
        if (!options.noAccessorComments) {
            options.syntheticAccessorResolver = new SyntheticAccessorResolver(dexFile.getOpcodes(), classDefs);
        }

        return classDefs;
    }

    public static DexBackedDexFile readInAndParseTheDexFile(File dexFileFile, baksmaliOptions options) throws IOException {
        DexBackedDexFile dexFile = null;
        try {
            dexFile = DexFileFactory.loadDexFile(dexFileFile, options.dexEntry, options.apiLevel, options.experimental);
        } catch (DexFileFactory.MultipleDexFilesException ex) {
            LOGGER.error(String.format("%s contains multiple dex files. You must specify which one to " +
                    "disassemble.", dexFileFile.getName()));
            LOGGER.error("Valid entries include:");
            for (OatFile.OatDexFile oatDexFile: ex.oatFile.getDexFiles()) {
                LOGGER.error(oatDexFile.filename);
            }
            throw new IOException(ex);
        } catch (org.jf.util.ExceptionWithContext ex) {
            LOGGER.error("{}", ex.toString());
            throw new IOException(ex);
        }

        if (dexFile.hasOdexOpcodes()) {
            if (!options.deodex) {
                LOGGER.error("Warning: You are disassembling an odex file without deodexing it. You");
                LOGGER.error("won't be able to re-assemble the results unless you deodex it with the -x");
                LOGGER.error("option");
                options.allowOdex = true;
            }
        } else {
            options.deodex = false;
        }

        if (options.deodex || options.registerInfo != 0 || options.normalizeVirtualMethods) {
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
        
        return dexFile;
    }
    
    @Nonnull
    public static List<String> getDefaultBootClassPathForApi(int apiLevel, boolean experimental) {
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
