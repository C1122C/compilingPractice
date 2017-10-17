package main;

import dataStructure.Node.java;
import dataStructure.DFA.java;
import NodeType.java;
import java.util.ArrayList;

public class LexCompiler{

    public Node makeTree(){
        return null;
    }

    private String prefixTransform(){
        return null;
    }

    private boolean nullable(Node n){
        if(n.isLeaf()){
            if(n.getIcon()==null){
                return true;
            }
            return false;
        }
        if(n.getType()==NodeType.OR){
            return nullable(n.left()||nullable(n.right()));
        }
        if(n.getType()==NodeType.CAT){
            return nullable(n.left()&&nullable(n.right()));
        }
        if(n.getType()==NodeType.STAR){
            return true;
        }
        return false;
    }

    private ArrayList<Integer> firstpos(Node n){
        ArrayList<Integer> result=new ArrayList<Integer>();
        if(n.isLeaf()){
            if(n.getIcon()==null){
                return null;
            }
            result.append(n.getPos());
            return result;
        }
        if(n.getType()==NodeType.OR){
            return nullable(n.left()||nullable(n.right()));
        }
        if(n.getType()==NodeType.CAT){
            return nullable(n.left()&&nullable(n.right()));
        }
        if(n.getType()==NodeType.STAR){
            return true;
        }
        return false;
    }

    private ArrayList<Integer> lastpos(Node n){
        return null;
    }

    private ArrayList<Integer> followpos(int p){
        return null;
    }
}