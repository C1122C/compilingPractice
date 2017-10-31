package main.lexicalAnalyzer;
import main.dataStructure.Node;
import main.lexicalAnalyzer.LexCompiler;
import main.lexicalAnalyzer.LexCompiler;
import main.dataStructure.DFA;

import java.util.*;

public class main{
    public static void main(String args[]){
        LexCompiler lex=new LexCompiler();
        lex.getLex("lexFile");
        /*lex.re.put("id","{letter}+({letter}|{number})*");
        lex.re.put("letter","a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|v|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z");
        lex.re.put("num","0|1|2|3|4|5|6|7|8|9");
        lex.re.put("r1","{id}|a*({letter}c)");
        lex.re.put("r2","0|1|2|3|4|5");
        lex.re.put("r3","a|(b|c)*ac");
        DFA s1=lex.makeDFA("letter","a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|v|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z",1);
        DFA s2=lex.makeDFA("num","0|1|2|3|4|5|6|7|8|9",2);
        DFA s3=lex.makeDFA("id","{letter}+({letter}|{num})*",3);
        /*lex.REToDFA.put("r1",s1);
        lex.REToDFA.put("r2",s2);
        lex.REToDFA.put("r3",s3);
        lex.DFAmerge();*/

        /*for(Map.Entry<String,Set<Integer>> entry:s1.Dstates.entrySet()){
            System.out.println("FOR state "+entry.getKey()+" : ");
            for(int i:entry.getValue()){
                System.out.print(i+", ");
            }
            System.out.print('\n');
        }
        for(Map.Entry<String,Map<Character,String>> entry:s1.UsableDtran.entrySet()){
            System.out.println("FOR state "+entry.getKey()+" : ");
            for(Map.Entry<Character,String> en:entry.getValue().entrySet()){
                System.out.println("with "+en.getKey()+" : "+en.getValue());
            }
        }*/
    }
}