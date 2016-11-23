package astli.pojo;

import astli.main.AndroidLibIDAlgorithm;
import astli.match.MatchAlgorithm;
import astli.match.MatchingProcess;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTLIOptions {
    
    public Class<? extends AndroidLibIDAlgorithm> algorithm;
    public String  mvnIdentifier = "";
    public String  mappingFile = "";
    public String inputFileName = "";
    public Class<? extends MatchingProcess> process;
    public String obfLvl = "";
    public String apkName = "";

    public ASTLIOptions() {
        this.algorithm = MatchAlgorithm.class;
        this.process = MatchingProcess.class;
    }
    
    public void setFileName(String fileName) {
        String[] pieces = fileName.split("/");
        this.inputFileName  = fileName; 
        this.obfLvl    = pieces[pieces.length - 1];
        this.apkName   = (pieces.length > 1) ? pieces[pieces.length - 2] : "<unknown>";
    }
    
    public boolean isObfuscated() {
        return !("".equals(mappingFile));
    }
    
    public boolean isInMatchingPhase() {
        return algorithm.equals(MatchAlgorithm.class);
    }

    
}
