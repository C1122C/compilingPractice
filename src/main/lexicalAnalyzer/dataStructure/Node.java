package main;

import NodeType.java;

public class Node{

    private char icon;
    private int postion;
    private NodeType type;

    public boolean isLeaf(){
        return false;
    }
}