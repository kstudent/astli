package astli.features;

/**
 *
 * @author chri
 */
public enum NodeType {
    MTH("method"),
    VRT("virtual"),
    DRC("direct"), 
    SGN("signature"),
    PAR("parameter"),
    LOC("local");
    
    private final String name;

    private NodeType(String name) {
        this.name  = name;
    }

    public String getName() {
        return name;
    }
}
