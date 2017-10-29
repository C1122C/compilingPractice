package main.lexicalAnalyzer;
import main.dataStructure.Node;
import main.lexicalAnalyzer.LexCompiler;
import main.lexicalAnalyzer.LexCompiler;
import main.dataStructure.DFA;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.util.Stack;

public class main{
    public static void main(String args[]){
        LexCompiler lex=new LexCompiler();
        lex.re.put("id","(cc)|(cl)|(ccj)");
        lex.re.put("letter","a|b|c");
        DFA s1=lex.makeDFA("r1","{id}|a*({letter}c)",1);
        DFA s2=lex.makeDFA("r2","0|1|2|3|4|5",2);
        DFA s3=lex.makeDFA("r3","a|(b|c)*ac",3);

        for(Map.Entry<String,Set<Integer>> entry:s3.Dstates.entrySet()){
            System.out.println("FOR state "+entry.getKey()+" : ");
            for(int i:entry.getValue()){
                System.out.print(i+", ");
            }
            System.out.print('\n');
        }
        for(Map.Entry<String,Map<Character,String>> entry:s3.UsableDtran.entrySet()){
            System.out.println("FOR state "+entry.getKey()+" : ");
            for(Map.Entry<Character,String> en:entry.getValue().entrySet()){
                System.out.println("with "+en.getKey()+" : "+en.getValue());
            }
        }
    }
}