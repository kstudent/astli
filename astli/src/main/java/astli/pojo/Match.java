package astli.pojo;

import java.util.List;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class Match {
    
    private final List<Match.Item> items;
    private final PackageHierarchy apkH;

    public Match(List<Match.Item> items, PackageHierarchy apkH) {
        this.items = items;
        this.apkH = apkH;
    }

    public List<Match.Item> getItems() {
        return items;
    }

    public PackageHierarchy getApkH() {
        return apkH;
    }
    
    public static class Item {
        
        private final double score; 
        private final int particularity;
        private final String packageName;
        private final String libName;

        public Item(double score, PackageHierarchy lib) {
            this.score = score;
            this.packageName = lib.getName();
            this.particularity = lib.getParticularity();
            this.libName = lib.getLib();
        }

        public int getParticularity() {
            return particularity;
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
