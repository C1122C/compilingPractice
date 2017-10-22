package main;

import java.util.Map;
import java.util.HashMap;
import dataStructure.IDetail;
import dataStructure.NumDetail;
import java.util.ArrayList;
import java.io.FileInputStream;

public class CodeScript{

    private Map IDTable;
    private Map NumTable;
    private int lexBegin;
    private int forword;
    private ArrayList chars;

    public CodeScript(){
        IDTable=new HashMap<String,IDetail>();
        NumTable=new HashMap<String,Integer>();
        lexBegin=0;
        forword=0;
        chars=new ArrayList<Character>();
    }

    public void readFile(String name){
        try{
            FileInputStream stream=new FileInputStream(name);
            while(char c=stream.read()){
                chars.add(c);
            }
        }catch (FileNotFoundException e){
            System.out.println("Sorry,we can not find a file with the given path.");
        }
    }

    public String getNextToken(){

    }

    public String InstallID(String name){
        IDetail idetail=new IDetail(lexBegin,forword);
        IDTable.put(name,idetail);
        return name;
    }

    public String InstallNum(String name,int v){
        NumDetail numdetail=new NumDetail(lexBegin,forword,v;
        NumTable.put(name,numdetail);
        return name;
    }
}