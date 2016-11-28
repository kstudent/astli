package astli.extraction;

import astli.pojo.NodeType;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnegative;

public class Node {
    
    private final List<Node> children;
    private final NodeType type;
    private final String signature; 

    private Node(NodeType type, String signature) {
        this.type = type;
        this.children = new LinkedList<>();
        this.signature = signature;
    }
    
    public Node(NodeType type) {
        this(type, "");
    }
    
    public Node(String signature) {
        this(NodeType.SGN, signature);
    }

    public String getSignature() {
        return signature;
    }
    
    public NodeType getType() {
        return type;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return toString(0);
    }
    
    private String toString(@Nonnegative int level) {
        StringBuilder indentation = new StringBuilder();
        for(int i = 0; i < level; i++) {
            indentation.append("  ");
        }
        
        StringBuilder s = new StringBuilder(indentation.toString() + type.getName());
        
        if(!signature.isEmpty()) {
            s.append(": ").append(signature);
        }
        
        s.append("\n");
        
        for (Node child : children) {
            s.append(child.toString(level + 1));
        }
        
        return s.toString(); 
    }
}
