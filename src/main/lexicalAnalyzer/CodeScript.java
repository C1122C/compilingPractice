package main.lexicalAnalyzer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import main.dataStructure.IDetail;
import main.dataStructure.NumDetail;
import java.io.FileNotFoundException;

public class CodeScript{

    private Map IDTable;
    private Map NumTable;
    private int lexBegin;
    private int forword;
    private int begin;
    private int length;
    private ArrayList chars;
    private boolean canOut;
    private boolean canNotGo;
    private ArrayList<String> re;
    private String currentState;
    private Map map1;
    private Map stateToRE;
    private Map codeMap;

    public CodeScript(){
        IDTable=new HashMap<String,IDetail>();
        NumTable=new HashMap<String,Integer>();
        lexBegin=0;
        forword=0;
        chars=new ArrayList<Character>();
        canOut=false;
        re=new ArrayList<String>();
        canNotGo=false;
        currentState="I0";

    }

    public void readFile(String name){
        try{
            FileReader reader=new FileReader(name);
            FileInputStream stream=new FileInputStream(name);
            int in=stream.read();
            while(in!=-1){
                char c=(char)(in);
                chars.add(c);
            }
        }catch (FileNotFoundException e){
            System.out.println("Sorry,we can not find a file with the given path.");
        }catch(IOException e1){

        }
    }

    public void getNextToken(){
        currentState="I0";
        while(!canNotGo){
            Map router=(HashMap<Character,String>)map1.get(currentState);
            char path=(Character)chars.get(forword);
            if(stateToRE.containsKey(currentState)){
                canOut=true;
                re.add((String)stateToRE.get(currentState));
            }
            if(router.containsKey(path)){
                currentState=(String)router.get(path);
                forword++;
            }
            else{
                canNotGo=true;
                forword--;
            }
        }
        String gainedRE=re.get(re.size()-1);
        begin=lexBegin;
        length=forword-lexBegin;
        lexBegin=forword+1;
        forword=lexBegin;
        mapCode(gainedRE);
    }

    public void mapCode(String re){

    }

    public boolean end(){
        if(lexBegin==chars.size()-1){
            return true;
        }
        else{
            return false;
        }
    }

    public String InstallID(String name){
        IDetail idetail=new IDetail(lexBegin,forword);
        IDTable.put(name,idetail);
        return name;
    }

    public String InstallNum(String name,int v){
        NumDetail numdetail=new NumDetail(lexBegin,forword,v);
        NumTable.put(name,numdetail);
        return name;
    }
}