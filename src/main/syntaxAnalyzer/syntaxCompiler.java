package main.syntaxAnalyzer;

import main.dataStructure.LR1Item;
import main.dataStructure.Production;
import main.dataStructure.V;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Stack;
import java.util.LinkedList;

public class syntaxCompiler {

    private final String epsilon="epsilon";
    private final String startIcon="$L";
    private final String endIcon="$R";
    private final String leadTo="->";
    private Map<Integer,String> originalCFG;
    private Map<String,V> allMark;
    private ArrayList<V> sortedMark;
    private Set<String> allVTName;
    private Set<String> allVNName;
    private Set<String> allProductionName;//之后不要再用
    private String startMark;
    private String originalStart;
    private Stack<String> stateStack;
    private Stack<String> markStack;
    private ArrayList<String> input;
    private Map<String,Set<LR1Item>> stateToSet;//C
    private Map<String,Map<String,String>> stateChange;
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
            int i=1;
            while(s!=null){
                originalCFG.put(i,s);
                s=reader.readLine();
                i++;
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
        for(String s:originalCFG.values()){
            String temp[]=s.split(leadTo);
            allVNName.add(temp[0]);
        }
        for(String s:originalCFG.values()){
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
                originalStart=s;
                break;
            }
        }
        V v=new V("CC",VType.VN);
        Production p=new Production();
        ArrayList<V> temp=p.getList();
        temp.add(allMark.get(originalStart));
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

    private Set<LR1Item> closure(Set<LR1Item> lr1){
        int oldsize=lr1.size();
        int newsize=lr1.size();
        boolean haveBeta=true;

        do{
            oldsize=newsize;
            for(LR1Item item:lr1){
                int pos=item.getPointPos();
                V b=item.getRight().get(pos);
                V beta=new V("",VType.VT);
                if(pos==item.getRight().size()-1){
                    haveBeta=false;
                }
                else{
                    beta=item.getRight().get(pos+1);
                }
                Set<String> terminal= new HashSet<String>();
                if(haveBeta){
                    terminal=beta.getFirst();
                    if(beta.getFirst().contains(epsilon)){
                        terminal.remove(epsilon);
                        terminal.add(item.getMark());
                    }
                }
                else{
                    terminal.add(item.getMark());
                }
                for(Production p:b.getPros()){
                    ArrayList<V> list=p.getList();
                    for(String s:terminal){
                        boolean alreadyHave=false;
                        LR1Item lr1Item=new LR1Item(b,list,0,s);
                        for(LR1Item lr1Item11:lr1){
                            if(lr1Item11.toString().equals(lr1Item.toString())){
                                alreadyHave=true;
                                break;
                            }
                        }
                        if(alreadyHave){
                            continue;
                        }
                        else{
                            lr1.add(lr1Item);
                        }
                    }
                }

            }
            newsize=lr1.size();
        }while(oldsize!=newsize);
        return lr1;
    }

    private Set<LR1Item> Goto(String state,String path){
        Set<LR1Item> original=stateToSet.get(state);
        Set<LR1Item> result=new HashSet<LR1Item>();
        for(LR1Item lr1Item:original){
            int pos=lr1Item.getPointPos();
            if(pos<lr1Item.getRight().size()){
                if(lr1Item.getRight().get(pos).getName().equals(path)){
                    LR1Item newlr1=new LR1Item(lr1Item.getLeft(),lr1Item.getRight(),pos+1,lr1Item.getMark());
                    result.add(newlr1);
                }
            }
        }
        return closure(result);
    }

    private void getItems(){
        String pre="I";
        int count=0;
        V v1=allMark.get(startMark);
        V v2=allMark.get(originalStart);
        ArrayList<V> firstList=new ArrayList<V>();
        firstList.add(v2);
        LR1Item item1=new LR1Item(v1,firstList,0,endIcon);
        Set<LR1Item> firstSet=new HashSet<LR1Item>();
        firstSet.add(item1);
        String name=pre+count;
        Set<LR1Item> first=closure(firstSet);
        stateToSet.put(name,first);
        count++;
        int oldSize=stateToSet.size();
        int newSize=stateToSet.size();
        do{
            oldSize=newSize;
            Map<String,Map<String,String>> map=new HashMap<String,Map<String,String>>();
            for(Map.Entry<String,Set<LR1Item>> entry:stateToSet.entrySet()){
                Map<String,String> map1=new HashMap<String,String>();
                for(String path:allMark.keySet()){
                    Set<LR1Item> news=Goto(entry.getKey(),path);
                    if(news.size()>0&&!stateToSet.values().contains(news)){
                        name=pre+count;
                        count++;
                        stateToSet.put(name,news);
                        map1.put(path,name);
                    }
                }
                map.put(entry.getKey(),map1);
            }
            newSize=stateToSet.size();
        }while(oldSize!=newSize);

    }
}
