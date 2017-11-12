package main.syntaxAnalyzer;

import main.dataStructure.Production;
import main.dataStructure.V;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Stack;

public class syntaxCompiler {

    private final String epsilon="epsilon";
    private final String startIcon="$L";
    private final String endIcon="$R";
    private final String leadTo="->";
    private ArrayList<String> originalCFG;
    private Map<String,V> allMark;
    private ArrayList<V> sortedMark;
    private Set<String> allVTName;
    private Set<String> allVNName;
    private Set<String> allProductionName;//之后不要再用
    private String startMark;
    private Stack<String> stateStack;
    private Stack<String> markStack;
    private ArrayList<String> input;
    private Map<String,Map<String,String>> action;
    private Map<String,Map<String,String>> GOTO;

    public void syntax(String Cpath,String Fpath){
        readCFG(Cpath);
        readFile(Fpath);
        resolveCFG();
        getStartMark();
        sort();
        for(int i=sortedMark.size()-1;i>=0;i--){
            V temp=first(sortedMark.get(i));
            sortedMark.set(i,temp);
            allMark.replace(sortedMark.get(i).getName(),temp);
        }
        for(int i=0;i<sortedMark.size();i++){
            V temp=follow(sortedMark.get(i));
            sortedMark.set(i,temp);
            allMark.replace(sortedMark.get(i).getName(),temp);
        }
    }

    private void readCFG(String path){
        try{
            FileInputStream fr=new FileInputStream(path);
            BufferedReader reader=new BufferedReader(new InputStreamReader(fr,"UTF-8"));
            String s;
            s=reader.readLine();
            while(s!=null){
                originalCFG.add(s);
                s=reader.readLine();
            }
            reader.close();
        }catch (Exception e){
            System.out.println("not find CFG file");
        }
    }

    private void readFile(String path){
        try{
            FileInputStream fr=new FileInputStream(path);
            BufferedReader reader=new BufferedReader(new InputStreamReader(fr,"UTF-8"));
            String s=reader.readLine();
            while(s!=null){
                s=s+endIcon;
                input.add(s);
                s=reader.readLine();
            }
            reader.close();
        }catch (Exception e){
            System.out.println("not find file");
        }
    }

    private void resolveCFG(){
        for(String s:originalCFG){
            String temp[]=s.split(leadTo);
            allVNName.add(temp[0]);
        }
        for(String s:originalCFG){
            String temp[]=s.split(leadTo);
            if(allMark.keySet().contains(temp[0])){
                V original=allMark.get(temp[0]);
                Set<Production> result=original.getPros();
                Production toAdd=resolvePro(temp[1]);
                result.add(toAdd);
                original.setPros(result);
                allMark.replace(temp[0],original);
            }
            else{
                V vn=new V(temp[0],VType.VN);
                Set<Production> result=new HashSet<Production>();
                Production toAdd=resolvePro(temp[1]);
                result.add(toAdd);
                vn.setPros(result);
                allMark.put(temp[0],vn);
            }
        }
    }

    private Production resolvePro(String s){
        char check[]=s.toCharArray();
        Production result=new Production();
        ArrayList<V> l=new ArrayList<V>();
        String vt="";
        V v;
        for(int i=0;i<check.length;i++){
            String c=check[i]+"";
            if(!allVNName.contains(c)){
                vt=vt+c;
            }
            else{
                if(vt.length()>0){
                    if(allVTName.contains(vt)){
                        v=allMark.get(vt);
                    }
                    else{
                        v=new V(vt,VType.VT);
                        allVTName.add(vt);
                        allMark.put(vt,v);
                        allProductionName.add(vt);
                    }
                    l.add(v);
                    vt="";
                }
                if(allMark.keySet().contains(c)){
                    l.add(allMark.get(c));
                }
                else{
                    v=new V(c,VType.VN);
                    allMark.put(c,v);
                    l.add(v);
                }
                allProductionName.add(c);
            }
        }
        if(vt.length()>0){
            if(allVTName.contains(vt)){
                v=allMark.get(vt);
            }
            else{
                v=new V(vt,VType.VT);
                allVTName.add(vt);
                allMark.put(vt,v);
                allProductionName.add(vt);
            }
            l.add(v);
        }
        result.setList(l);
        return result;
    }

    private void sort(){
        ArrayList<String> link=new ArrayList<String>();
        sortedMark.add(allMark.get(startMark));
        link.addAll(allMark.get(startMark).getProsInString());
        while(link.size()>0){
            for(int i=0;i<link.size();i++){
                boolean ok=true;
                String check=link.get(i);
                for(int j=0;j<link.size();j++){
                    if(j!=i){
                        if(allMark.get(link.get(j)).getProsInString().contains(check)){
                            ok=false;
                            break;
                        }
                    }
                }
                if(ok){
                    sortedMark.add(allMark.get(check));
                    link.remove(i);
                    link.addAll(allMark.get(check).getProsInString());
                    break;
                }
            }
        }
    }

    private void getStartMark(){
        for(String s:allVNName){
            if(!allProductionName.contains(s)){
                startMark=s;
                break;
            }
        }
        V v=new V("CC",VType.VN);
        Production p=new Production();
        ArrayList<V> temp=p.getList();
        temp.add(allMark.get(startMark));
        p.setList(temp);
        Set<Production> result=v.getPros();
        result.add(p);
        v.setPros(result);
        allMark.put("CC",v);
        allVNName.add("CC");
        startMark="CC";

    }

    private V first(V v){
        Set result=v.getFirst();
        if(v.getType()==VType.VT){
            result.add(v.getName());
        }
        else{
            if(v.isHaveEpsilon()){
                result.add(epsilon);
            }
            for(Production production:v.getPros()){
                int i=0;
                ArrayList<V> check=production.getList();
                while(i<check.size()&&check.get(i).getFirst().contains(epsilon)){
                    Set<String> temp=check.get(i).getFirst();
                    temp.remove(epsilon);
                    result.addAll(temp);
                    i++;
                }
                if(i==check.size()&&check.get(i).getFirst().contains(epsilon)){
                    result.add(epsilon);
                }
            }
        }
        v.setFirst(result);
        return v;
    }

    private V follow(V v){
        Set<String> result=new HashSet<String>();
        if(v.getName().equals(startMark)){
            result.add(endIcon);
        }
        else{
            for(V vv:allMark.values()){
                Set<Production> p=vv.getPros();
                for(Production prod:p){
                    for(int i=0;i<prod.getList().size();i++){
                        if(prod.getList().get(i).getName().equals(v.getName())){
                            if(i==prod.getList().size()-1){
                                result.addAll(vv.getFollow());
                            }
                            else{
                                V behind=prod.getList().get(i+1);
                                Set<String> toadd=behind.getFirst();
                                if(toadd.contains(epsilon)){
                                    result.addAll(vv.getFollow());
                                    toadd.remove(epsilon);
                                }
                                result.addAll(toadd);
                            }
                        }
                    }
                }
            }
        }
        v.setFollow(result);
        return v;
    }
}
