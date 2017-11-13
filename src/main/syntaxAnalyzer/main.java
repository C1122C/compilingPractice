package main.syntaxAnalyzer;


import main.syntaxAnalyzer.syntaxCompiler;

public class main {
    public static void main(String args[]){
        syntaxCompiler compiler=new syntaxCompiler();
        compiler.syntax("CFG","test");
    }
}