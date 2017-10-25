package main.lexicalAnalyzer;
import main.dataStructure.Node;
import main.lexicalAnalyzer.LexCompiler;
import main.lexicalAnalyzer.LexCompiler;
import main.dataStructure.DFA;

import java.util.Stack;

public class main{
    public static void main(String args[]){
        LexCompiler lex=new LexCompiler();
        /*Node s1=lex.makeTree("{id}|a*({letter}c)");
        Node s2=lex.makeTree("0|1|2|3|4|5|6|7|8|9");
        Node s3=lex.makeTree("a|(b|c)*ac");
        Node current=s3;
        Stack<Node> nstack = new Stack<Node>();
        for(;;){
            while(current != null){
                nstack.push(current);
                current = current.getLeft();
            }
            if(!nstack.empty()){
                current = nstack.pop();
                System.out.println("R1: "+current.getIcon());
                current = current.getRight();
            }
            else{
                break;
            }
        }*/

    }
}