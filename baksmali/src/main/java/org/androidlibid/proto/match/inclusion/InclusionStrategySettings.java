package org.androidlibid.proto.match.inclusion;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class InclusionStrategySettings {
    
    private double methodAcceptThreshold;
    private double minimalMethodLengthForNeedleLookup;
    private double packageRejectThreshold;

    public InclusionStrategySettings() {
        //            this(0.9999d,  0.95d, 12);
        //            this(0.9999d,  0.90d, 12);
        //            this(0.9999d,  0.85d, 15);
        this(0.9999d, 12, 0.75d);
    } //            this(0.9999d,  0.95d, 12);
    //            this(0.9999d,  0.90d, 12);
    //            this(0.9999d,  0.85d, 15);

    public InclusionStrategySettings(double methodAcceptThreshold, double minimalMethodLengthForNeedleLookup, double packageRejectThreshold) {
        this.methodAcceptThreshold = methodAcceptThreshold;
        this.minimalMethodLengthForNeedleLookup = minimalMethodLengthForNeedleLookup;
        this.packageRejectThreshold = packageRejectThreshold;
    }

    public double getMethodAcceptThreshold() {  
        return methodAcceptThreshold;
    }

    public double getMinimalMethodLengthForNeedleLookup() {
        return minimalMethodLengthForNeedleLookup;
    }

    public double getPackageAcceptThreshold(double score) {
        if(score < 220) return 1.0;
        return Math.exp((- score / 4000) -2) + .8;
    }

    public double getPackageRejectThreshold() {
        return packageRejectThreshold;
    }

    @Override
    public String toString() {
        return "InclusionStrategySettings{" + "methodAcceptThreshold=" 
                + methodAcceptThreshold + ", minimalMethodLengthForNeedleLookup=" 
                + minimalMethodLengthForNeedleLookup + ", packageRejectThreshold=" 
                + packageRejectThreshold + '}';
    }
}
