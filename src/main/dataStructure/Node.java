package main.dataStructure;

import main.lexicalAnalyzer.NodeType;

import java.util.Set;

public class Node{

    private char icon;
    private int postion;
    private NodeType type;
    private boolean nullable;
    private Set<Integer> first;
    private Set<Integer> last;
    private Set<Integer> follow;
    private Node right;
    private Node left;
    public int end;
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