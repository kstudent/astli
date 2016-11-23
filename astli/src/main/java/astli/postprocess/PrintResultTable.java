package astli.postprocess;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import astli.pojo.Match;
import astli.pojo.PackageHierarchy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PrintResultTable implements PostProcessor {

    private static final Logger LOG = LogManager.getLogger();
    private static final NumberFormat FRMTR = new DecimalFormat("#0");

    private static final int APKPCOLW = 30;
    private static final int CODEW    =  8;
    private static final int LIBPCOLW = 30;
    private static final int LIBCOLW  = 30;
    private static final int SCOREW   =  6;
    
    @Override
    public void init() {
        LOG.info( buildSeparator() + "\n"
                + trimToLength("APK Package", APKPCOLW)  + " | "
                + trimToLength("Size", CODEW) + " | "
                + trimToLength("Lib Package", LIBPCOLW) + " | "
                + trimToLength("Library ", LIBCOLW) + " | "
                + trimToLength("Score", SCOREW) + "\n"
                + buildSeparator()
        );
    }
    
    @Override
    public void process(Match result) {
        LOG.info(buildResultLines(result.getApkH(), result.getItems()) + "\n" + buildSeparator());
    }

    @Override
    public void done() {
    }

    private String buildResultLines(PackageHierarchy apk, List<Match.Item> items) {
        
        StringBuilder lines = new StringBuilder();
        String apkName = apk.getName();
        int apkCodeSize = apk.getEntropy();
        
        if(items.isEmpty()) {
            lines
                .append(trimToLength(apkName, APKPCOLW)).append(" | ")
                .append(trimToLength(Integer.toString(apkCodeSize), CODEW)).append(" | ")
                .append("<no match>"); 
        } else {
            lines.append(buildLine(apkName, apkCodeSize, items.get(0)));

            for(int i = 1; i < items.size(); i++) {
                lines.append("\n").append(buildLine("", 0, items.get(i)));
            }
        } 
        
        return lines.toString();
    }
    
    private StringBuilder buildLine(String apkP, int code, Match.Item item) {
        
        String codeString = (code > 0) ? Integer.toString(code) : ""; 
        
        return new StringBuilder(trimToLength(apkP, APKPCOLW)).append(" | ")
            .append(trimToLength(codeString, CODEW)).append(" | ")
            .append(trimToLength(item.getPackage(), LIBPCOLW)).append(" | ")
            .append(trimToLength(item.getLib(), LIBCOLW)).append(" | ")
            .append(trimToLength(FRMTR.format(item.getScore() * 100), SCOREW));
    } 
    
    private String buildSeparator() {
        return  StringUtils.rightPad("", APKPCOLW, "-") + "-+-"
                + StringUtils.rightPad("", CODEW, "-") + "-+-"
                + StringUtils.rightPad("", LIBPCOLW, "-") + "-+-"
                + StringUtils.rightPad("", LIBCOLW, "-") + "-+-"
                + StringUtils.rightPad("", SCOREW, "-")
        ;
                
    }
    
    private String trimToLength(String src, int length) {
        return StringUtils.rightPad(src.substring(0, Math.min(length, src.length())), length);
    }
    
}
