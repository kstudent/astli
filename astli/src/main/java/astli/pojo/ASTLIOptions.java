package astli.pojo;

import astli.find.CandidateFinder;
import astli.find.FindByNameOrNeedle;
import astli.learn.LearnAlgorithm;
import astli.main.AndroidLibIDAlgorithm;
import astli.match.MatchAlgorithm;
import astli.score.PackageMatcher;
import astli.score.SimilarityMatcher;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTLIOptions {
    
    public Class<? extends AndroidLibIDAlgorithm> algorithm = LearnAlgorithm.class;
    public String mvnIdentifier = "";
    public String mappingFile = "";
    public String inputFileName = "";
    public String obfLvl = "";
    public String apkName = "";

    public boolean isInEvaluationMode = false;
    
    //paramters for matching phase
    public Class<? extends PackageMatcher> matcher = SimilarityMatcher.class;
    public Class<? extends CandidateFinder> finder = FindByNameOrNeedle.class;
    public double packageAcceptanceThreshold = 0.5d;
    public int minimumNeedleParticularity = 12;    
    public int maxNeedleAmount = 10;
    public int minimumPackageParticularity = 80;
    
    public ASTLIOptions() {
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

    public String getSetup() {
        return "{" + "minimumPackageParticularity=" + minimumPackageParticularity 
                + ", packageMatcher=" + matcher + ", finder=" + finder 
                + ", packageAcceptanceThreshold=" + packageAcceptanceThreshold 
                + ", minimumNeedleParticularity=" + minimumNeedleParticularity 
                + ", maxNeedleAmount=" + maxNeedleAmount + '}';
    }
    
}
