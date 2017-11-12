package main.syntaxAnalyzer;


import main.lexicalAnalyzer.LexCompiler;

public class main {
    public static void main(String args[]){
        LexCompiler lex=new LexCompiler();
        lex.getLex("lexFile");
    }
}