package org.androidlibid.proto.pojo;

import java.util.List;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class Match {
    
    private final List<Match.Item> items;
    private final PackageHierarchy apkH;
    private final boolean packageInDB;

    public Match(List<Match.Item> items, PackageHierarchy apkH, boolean packageInDB) {
        this.items = items;
        this.apkH = apkH;
        this.packageInDB = packageInDB;
    }

    public boolean isPackageInDB() {
        return packageInDB;
    }

    public List<Match.Item> getItems() {
        return items;
    }

    public PackageHierarchy getApkH() {
        return apkH;
    }
    
    public static class Item {
        
        private final double score; 
        private final int entropy;
        private final String packageName;
        private final String libName;

        public Item(double score, PackageHierarchy lib) {
            this.score = score;
            this.packageName = lib.getName();
            this.entropy = lib.getEntropy();
            this.libName = lib.getLib();
        }

        public int getEntropy() {
            return entropy;
        }

        public String getPackage() {
            return packageName;
        }

        public double getScore() {
            return score;
        }

        public String getLib() {
            return libName;
        }
    }
    
}
