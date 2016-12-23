package astli.pojo;

import astli.db.Method;
import astli.db.Clazz;
import astli.db.Package;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchy {
    
    private final String name;
    private final Map<String, Map<String, Fingerprint>> classes = new HashMap<>();
    private final String lib;
    private List<List<String>> signatureTable;
    private List<List<Fingerprint>> printTable;
    private List<String> classTable;
    
    private int computedParticularity = -1;

    public PackageHierarchy(Package pckg) {
        this(pckg.getName(), pckg.getLibrary().getName());
        
        for(Clazz clazz : pckg.getClazzes()) {
            Map<String, Fingerprint> methods = new HashMap<>();
            for(Method method : clazz.getMethods()) {
                methods.put(method.getName(), new Fingerprint(method));
            }
            addMethods(clazz.getName(), methods);
        }
    }
    
    public PackageHierarchy(String name) {
        this(name, "");
    }
    
    public PackageHierarchy(String name, String lib) {
        this.name = name;
        this.lib = lib;
    }

    public synchronized void addMethods(String className, Map<String, Fingerprint> prints) {
        
        if(prints.isEmpty()) {
            return;
        }
        
        if(classes.containsKey(className)) {
            classes.get(className).putAll(prints);
        }
        
        classes.put(className, prints);
        resetMembers();
    }

    public String getName() {
        return name;
    }
    
    public Set<String> getClassNames() {
        return classes.keySet();
    }
    
    public Map<String, Fingerprint> getMethodsByClassName(String className) {
        Map<String, Fingerprint> methods = classes.get(className);
        return (methods == null) 
                ? new HashMap<String, Fingerprint>() : methods; 
    } 
    
    public String getClassNameByMethod(Fingerprint method) {
        for(String className : classes.keySet()) {
            Map<String, Fingerprint> methods = classes.get(className);
            
            if(methods.containsValue(method)) {
                return className;
            }
        }
        
        throw new RuntimeException("Method " + method.getName() + " not in hierarchy");
    }
    
    public int getClassesSize() {
        return classes.size();
    }
    
    private void fillTables() {
        signatureTable = new ArrayList<>();
        printTable = new ArrayList<>();
        classTable = new ArrayList<>();
        
        classes.keySet().stream()
                .peek(clssName -> classTable.add(clssName))
                .map(clssName -> classes.get(clssName))
                .forEachOrdered(methods -> {
                    ArrayList<Fingerprint> printsOfClass = new ArrayList<>(methods.values()); 
                    printTable.add(printsOfClass);
                    signatureTable.add(printsOfClass.stream()
                            .map(print -> print.getSignature())
                            .collect(Collectors.toList()));
            
        });
    }
    
    public synchronized List<List<String>> getSignatureTable() {
        if(signatureTable == null) {
            fillTables();
        }
        return signatureTable;
    } 
    
    public synchronized List<List<Fingerprint>> getPrintTable() {
        if(printTable == null) {
            fillTables();
        }
        return printTable;
    } 
    
    public synchronized List<String> getClassTable() {
        if(classTable == null) {
            fillTables();
        }
        return classTable;
    }
    
    public synchronized int getParticularity() {
        
        if(computedParticularity < 0) {
            computedParticularity = computeParticularity();
        }
        
        return computedParticularity;
    }

    public String getLib() {
        return lib;
    }
    
    private int computeParticularity() {
        return classes.values().stream()
                .flatMap(prints -> prints.values().stream())
                .mapToInt(method -> method.getParticularity())
                .sum();
    }
    
    private void resetMembers() {
        this.signatureTable = null;
        this.printTable = null;
        this.computedParticularity = -1;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(); 
        
        builder.append(name).append(":").append("\n");
        
        classes.keySet().stream()
            .forEach(clss -> builder.append("- ").append(clss).append("\n"))
        ;
        
        return builder.toString();
    }
    
}
