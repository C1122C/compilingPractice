package main;

import dataStructure.Node;
import dataStructure.DFA;
import NodeType.java;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HaehMap;
import java.util.Stack;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedReader;

public class LexCompiler{

    private Map posToNode;
    private String RE;
    private String prefix;
    private DFA dfa;
    private Node root;
    private Set alphabet;
    private Set atom;
    private int part;
    private boolean copy;
    private boolean RE;
    private boolean trans;

    public LexCompiler(){
        atom=new HashSet<char>();
        char add[]={'\\','^','$','*','+','?','{','}','.','(',')',':','[',']','|'};
        for(char c:add){
            atom.add(c);
        }
        part=1;
        copy=false;
        RE=false;
        trans=false;
    }

    public void readGrammar(File file){
        ArrayList<Character> content=new ArrayList<Character>();
        try{
            FileInputStream fr=new FileInputStream(file);
            BufferedReader reader=new BufferedReader(new InputStreamReader(fr,"UTF-8"));
            while(char c=reader.read()){
                content.add(c);
            }
            reader.close();
        }catch (Exception e){
            System.out.println("not find file");
        }

        try{
            File out=new File("E:\\IdeaProjects\\compilingPractice\\src\\output\\out.java");

        }catch(NullPointerException e){
            System.out.println("the pathname argument is null");
        }

    }

    public DFA makeDFA(String input){
        input=input+"#";
        this.RE=input;
        prefixTransform();
        makeTree();

        dfa=new DFA();
        dfa.Dstates=new HashMap<String,Set<Integer>>();
        dfa.Dtran=new HashMap<String,Map<String,Set<Integer>>>();

        Stack<Node> nstack = new Stack<Node>();
        Node current = root;
        for(;;){
            while(current != null){
                nstack.push(current);
                current = current.getLeft();
            }
            if(!nstack.empty()){
                current = nstack.pop();
                nullable(current);
                firstpos(current);
                lastpos(current);
                followpos(current);
                current = current.getRight();
            }
            else{
                break;
            }
        }

        dfa.Dstates.put("notMarked",root.getFirst());
        String pre = "I";
        int i = 0;
        while(dfa.Dstates.containsKey("notMarked")){
            Set temp=dfa.Dstates.get("notMarked");
            String name=pre+i;
            dfa.Dstates.put(name,temp);
            i++;
            for(char c:alphabet){
                Set newS = new HashSet<Integer>();
                for(int num:temp){
                    if(posToNode.get(num).getIcon()==c){
                        if(!newS.empty){
                            newS.retainAll(followpos(posToNode.get(num)));
                        }
                        else{
                            newS.addAll(followpos(posToNode.get(num)));
                        }
                    }
                }
                if(!dfa.Dstates.containsValue(newS)){
                    dfa.Dstates.replace("notMarked",newS);
                }
                else{
                    dfa.Dstates.remove("notMarked");
                }

                if(dfa.Dtran.contaisKey(name)){
                    dfa.Dstates.get(name).put(c+"",newS);
                }
                else{
                    Map add = new HashMap<String,Set<Integer>>();
                    add.put(c+"",newS);
                    dfa.Dstates.put(name,add);
                }
            }

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

    private void lastpos(Node n){
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
            return;
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