package main;

import dataStructure.Node.java;
import dataStructure.DFA.java;
import NodeType.java;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HaehMap;

public class LexCompiler{

    private Map posToNode;
    private String RE;
    private String prefix;
    private DFA dfa;
    private Node root;

    public DFA makeDFA(String input){
        input=input+"#";
        this.RE=input;
        prefixTransform();
        makeTree();

        dfa=new DFA();
        dfa.Dstates=new HashMap<String,Set<Integer>>();
        firstpos(this.root);
        Set initSet=this.root.getFirst();
        dfa.Dstates.put("I0",initSet);
        do{

        }
        return null;
    }

    private Node makeTree(){
        return null;
    }

    private String prefixTransform(){
        return null;
    }

    private void nullable(Node n){
        if(n.isLeaf()){
            if(n.getIcon()==null){
                n.setNullable(true);
            }
            n.setNullable(false);
        }
        if(n.getType()==NodeType.OR){
            n.setNullable(nullable(n.getLeft())||nullable(n.getRight()));
        }
        if(n.getType()==NodeType.CAT){
            n.setNullable(nullable(n.getLeft()&&nullable(n.getRight())));
        }
        if(n.getType()==NodeType.STAR){
            n.setNullable(true);
        }
        n.setNullable(true);
    }

    private void firstpos(Node n){
        Set<Integer> result=new HashSet();
        if(n.isLeaf()){
            if(n.getIcon()==null){
                n.setFirst(null);
                return;
            }
            result.add(n.getPos());
        }
        if(n.getType()==NodeType.OR){
            result=firstpos(n.getLeft());
            result.retainAll(n.getRight());
        }
        if(n.getType()==NodeType.CAT){
            if nullable(n.getLeft()){
                result=firstpos(n.getLeft());
                result.retainAll(n.getRight());
            }
            else{
                result=firstpos(n.getLeft());
            }
        }
        if(n.getType()==NodeType.STAR){
            result=firstpos(n.getLeft());
        }
        n.setFirst(result);
    }

    private Set lastpos(Node n){
        Set<Integer> result=new HashSet();
        if(n.isLeaf()){
            if(n.getIcon()==null){
                n.setLast(null);
                return;
            }
            result.add(n.getPos());
        }
        if(n.getType()==NodeType.OR){
            result=lastpos(n.left());
            result.retainAll(n.right());
        }
        if(n.getType()==NodeType.CAT){
            if nullable(n.right()){
                result=lastpos(n.left());
                result.retainAll(n.right());
            }
            else{
                result=lastpos(n.right());
            }
        }
        if(n.getType()==NodeType.STAR){
            result=lastpos(n.left());
        }
        n.setLast(result);
    }

    private void followpos(Node n){
        if(n.getType()==NodeType.CAT){
            Set toAdd = firstpos(n.getRight());
            Set host = lastpos(n.getLeft());
            for(int i:host){
                posToNode.get(i).setFollow(posToNode.get(i).getFollow.retainAll(toAdd));
            }
        }
        if(n.getType()==NodeType.STAR){
            Set toAdd=firstpos(n);
            Set host=lastpos(n);
            for(int i:host){
                posToNode.get(i).setFollow(posToNode.get(i).getFollow.retainAll(toAdd));
            }
        }
    }
}