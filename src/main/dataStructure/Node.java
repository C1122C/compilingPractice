package main.dataStructure;

/**
 * 词法分析树节点实体类
 */

import main.lexicalAnalyzer.NodeType;
import java.util.Set;

public class Node{

    /*表示节点的实质内容*/
    private char icon;
    /*节点位置*/
    private int postion;
    /*节点类型*/
    private NodeType type;
    /*节点属性*/
    private boolean nullable;
    private Set<Integer> first;
    private Set<Integer> last;
    private Set<Integer> follow;
    /*右孩子*/
    private Node right;
    /*左孩子*/
    private Node left;
    /*节点所在树终止节点的位置*/
    public int end;
    /*如果这个节点代表的是epsilon，则这一属性为true*/
    public boolean isNull=true;

    public void setRight(Node right) {
        this.right = right;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public Node getLeft() {
        return left;
    }

    public void setIcon(char icon) {
        this.icon = icon;
    }

    public void setPostion(int postion) {
        this.postion = postion;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public void setFirst(Set<Integer> first) {
        this.first = first;
    }

    public void setLast(Set<Integer> last) {
        this.last = last;
    }

    public void setFollow(Set<Integer> follow) {
        this.follow = follow;
    }

    public int getPostion() {
        return postion;
    }

    public NodeType getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public Set<Integer> getFirst() {
        return first;
    }

    public Set<Integer> getLast() {
        return last;
    }

    public Set<Integer> getFollow() {
        return follow;
    }

    public char getIcon() {
        return icon;
    }

    public boolean isLeaf(){
        return false;
    }
}