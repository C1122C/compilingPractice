package main.syntaxAnalyzer;

import main.dataStructure.LR1Item;
import main.dataStructure.Production;
import main.dataStructure.V;

import java.io.*;
import java.util.*;
import java.util.Stack;

public class syntaxCompiler {

    private final String epsilon="epsilon";
    private final String startIcon="$L";
    private final String endIcon="$R";
    private final String leadTo="->";
    private final String acc="accept";
    private Map<Integer,String> originalCFG;
    private Map<String,V> allMark;
    private ArrayList<V> sortedMark;
    private Set<String> allVTName;
    private Set<String> allVNName;
    private Set<String> allProductionName;
    private String startMark;
    private ArrayList<String> input;
    private ArrayList<String> STState;
    private ArrayList<Set<LR1Item>> STSet;
    private Map<String,Map<String,String>> stateChange;
    private Map<String,Map<String,String>> action;
    private Map<String,Map<String,String>> GOTO;
    private ArrayList<String> output;

    public syntaxCompiler(){
        originalCFG=new HashMap<Integer,String>();
        allMark=new HashMap<String,V>();
        sortedMark=new ArrayList<V>();
        allVTName=new HashSet<String>();
        allVNName=new HashSet<String>();
        allProductionName=new HashSet<String>();
        startMark="";
        input=new ArrayList<String>();
        STState=new ArrayList<String>();
        STSet=new ArrayList<Set<LR1Item>>();
        stateChange=new HashMap<String,Map<String,String>>();
        action=new HashMap<String,Map<String,String>>();
        GOTO=new HashMap<String,Map<String,String>>();
        output=new ArrayList<String>();
    }

    public void syntax(String Cpath,String Fpath){
        readCFG(Cpath);
        readFile(Fpath);
        resolveCFG();
        getStartMark();

        for(Map.Entry<String,V> entry:allMark.entrySet()){
            V v=entry.getValue();
            V temp=first(v);
            temp=follow(v);
            allMark.replace(entry.getKey(),temp);
            /*System.out.print(entry.getKey()+" : ");
            for(String s:temp.getFollow()){
                System.out.print(s+",");
            }
            System.out.print("\n");*/
        }

        getItems();
        /*for(int i=0;i<STState.size();i++){
            System.out.println(STState.get(i)+" : ");
            for(LR1Item l:STSet.get(i)){
                System.out.println(l.toString());
            }
            System.out.println("------------------------------------------------");
        }*/
        /*for(Map.Entry<String,Map<String,String>> entry1:stateChange.entrySet()){
            System.out.println(entry1.getKey()+" : ");
            for(Map.Entry<String,String> entry2:entry1.getValue().entrySet()){
                System.out.println("-"+entry2.getKey()+"->"+entry2.getValue());
            }
        }
        System.out.println("------------------------------------------------");*/
        LRTable();
        /*for(Map.Entry<String,Map<String,String>> entry1:GOTO.entrySet()){
            System.out.println(entry1.getKey()+":");
            for(Map.Entry<String,String> entry2:entry1.getValue().entrySet()){
                System.out.println("-"+entry2.getKey()+"->"+entry2.getValue());
            }
        }*/
        getSquence();
        //outputFile();
    }

    private void readCFG(String path){
        try{
            FileInputStream fr=new FileInputStream(path);
            BufferedReader reader=new BufferedReader(new InputStreamReader(fr,"UTF-8"));
            String s;
            s=reader.readLine();
            int i=1;
            while(s!=null){
                if(i==1){
                    String l[]=s.split(leadTo);
                    String str="CC->"+l[0];
                    originalCFG.put(i,str);
                    i++;
                }
                originalCFG.put(i,s);
                s=reader.readLine();
                i++;
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
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
        allVTName.add(endIcon);
        for(String s:originalCFG.values()){
            String temp[]=s.split(leadTo);
            if(allMark.keySet().contains(temp[0])){
                V original=allMark.get(temp[0]);
                Set<Production> result=original.getPros();
                Production toAdd=resolvePro(temp[1],temp[0]);
                result.add(toAdd);
                original.setPros(result);
                allMark.replace(temp[0],original);
            }
            else{
                V vn=new V(temp[0],VType.VN);
                Set<Production> result=new HashSet<Production>();
                Production toAdd=resolvePro(temp[1],temp[0]);
                result.add(toAdd);
                vn.setPros(result);
                allMark.put(temp[0],vn);
            }
        }
    }

    private Production resolvePro(String s,String flag){
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
                if(!flag.equals(c)){
                    //System.out.println(flag+" "+c);
                    allProductionName.add(c);
                }
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

    private void getStartMark(){
        for(String s:allVNName){
            if(!allProductionName.contains(s)){
                //System.out.println("GET IN");
                startMark=s;
                break;
            }
        }
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
                do{
                    if(check.get(i).getName().equals(v.getName())&&i==0){
                        break;
                    }
                    if(!allMark.get(check.get(i).getName()).finishFirst){
                        allMark.replace(check.get(i).getName(),first(allMark.get(check.get(i).getName())));
                    }
                    Set<String> temp=allMark.get(check.get(i).getName()).getFirst();
                    if(i!=check.size()-1){
                        temp.remove(epsilon);
                    }
                    result.addAll(temp);
                    i++;
                }while(i<check.size()&&allMark.get(check.get(i).getName()).getFirst().contains(epsilon));

            }
        }
        v.setFirst(result);
        v.finishFirst=true;
        return v;
    }

    private V follow(V v){
        if(v.getType()==VType.VT){
            return v;
        }
        Set<String> result=v.getFollow();
        if(v.getName().equals(startMark)){
            result.add(endIcon);
        }
        else{
            for(Map.Entry<String,V> entry:allMark.entrySet()){
                V vv=entry.getValue();
                Set<Production> p=vv.getPros();
                for(Production prod:p){
                    for(int i=0;i<prod.getList().size();i++){
                        if(prod.getList().get(i).getName().equals(v.getName())){
                            if(i==prod.getList().size()-1){
                                if(!vv.getName().equals(v.getName())){
                                    if(!vv.finishfollow){
                                        //System.out.println(vv.getName());
                                        vv=follow(vv);
                                    }
                                    result.addAll(vv.getFollow());
                                }
                            }
                            else{
                                int j=1;
                                Set<String> toadd=new HashSet<String>();
                                do{
                                    V behind=prod.getList().get(i+j);
                                    if(behind.getType()==VType.VT){
                                        result.add(behind.getName());
                                        break;
                                    }
                                    j++;
                                    toadd=behind.getFirst();
                                    Set<String> st=toadd;
                                    st.remove(epsilon);
                                    result.addAll(st);
                                    if(j+i==prod.getList().size()&&toadd.contains(epsilon)){
                                        if(!vv.finishfollow){
                                            vv=follow(vv);
                                        }
                                        result.addAll(vv.getFollow());
                                    }
                                }while(toadd.contains(epsilon)&&(i+j<prod.getList().size()));
                                result.addAll(toadd);
                            }
                        }
                    }
                }
            }
        }
        v.setFollow(result);
        v.finishfollow=true;
        return v;
    }

    private Set<LR1Item> closure(Set<LR1Item> lr1){
        int oldsize=lr1.size();
        int newsize=lr1.size();
        ArrayList<LR1Item> gothrough=new ArrayList<LR1Item>();
        for(LR1Item l:lr1){
            gothrough.add(l);
        }
        do{
            oldsize=newsize;
            for(int i=0;i<gothrough.size();i++){
                LR1Item item=gothrough.get(i);
                //System.out.println(item.toString());
                int pos=item.getPointPos();
                boolean haveBeta=true;
                boolean end=false;
                V b=new V("",VType.VT);
                V beta=new V("",VType.VT);
                if(pos==item.getRight().size()){
                    end=true;
                    //System.out.println("IT`S END");
                }
                else{
                    b=item.getRight().get(pos);
                    if(pos==item.getRight().size()-1){
                        haveBeta=false;
                        //System.out.println("DO NOT HAVE BETA");
                    }
                    else{
                        beta=item.getRight().get(pos+1);
                        //System.out.println("WE GOT ALL");
                    }
                }

                Set<String> terminal= new HashSet<String>();
                if(!end){
                    //System.out.println("GET IN!");
                    if(haveBeta){
                        terminal=beta.getFirst();
                        if(beta.getFirst().contains(epsilon)){
                            terminal.remove(epsilon);
                            terminal.addAll(item.getMark());
                        }
                    }
                    else{
                        terminal.addAll(item.getMark());
                    }
                    //System.out.println("terminal is:");
                    for(String s:terminal){
                        //System.out.println(s);
                    }
                    for(Production p:b.getPros()){
                        ArrayList<V> list=p.getList();
                        for(String s:terminal){
                            boolean alreadyHave=false;
                            LR1Item lr1Item=new LR1Item(b,0);
                            lr1Item.setRight(list);
                            lr1Item.addMark(s);
                            //System.out.println("NEW ITEM: "+lr1Item.toString());
                            for(LR1Item lr1Item11:gothrough){
                                if(lr1Item11.toString().equals(lr1Item.toString())){
                                    //System.out.println("EQUAL "+lr1Item11.toString());
                                    alreadyHave=true;
                                    break;
                                }
                            }
                            if(alreadyHave){
                                continue;
                            }
                            else{
                                gothrough.add(lr1Item);
                                //System.out.println("ADD ONE!");
                            }
                        }
                    }
                }

            }
            //System.out.println("OK");
            newsize=gothrough.size();
        }while(oldsize!=newsize);
        lr1=new HashSet<LR1Item>();
        for(LR1Item l:gothrough){
            lr1.add(l);
        }
        return lr1;
    }

    private Set<LR1Item> Goto(String state,String path){
        Set<LR1Item> original=STSet.get(STState.indexOf(state));
        Set<LR1Item> result=new HashSet<LR1Item>();
        for(LR1Item lr1Item:original){
            int pos=lr1Item.getPointPos();
            if(pos<lr1Item.getRight().size()){
                if(lr1Item.getRight().get(pos).getName().equals(path)){
                    LR1Item newlr1=new LR1Item(lr1Item.getLeft(),pos+1);
                    newlr1.setRight(lr1Item.getRight());
                    newlr1.setMark(lr1Item.getMark());
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
        ArrayList<V> firstList=new ArrayList<V>();
        for(Production p:v1.getPros()){
            for(V v:p.getList()){
                firstList.add(v);
            }
        }
        LR1Item item1=new LR1Item(v1,0);
        item1.setRight(firstList);
        item1.addMark(endIcon);
        Set<LR1Item> firstSet=new HashSet<LR1Item>();
        //System.out.println(item1.toString());
        firstSet.add(item1);
        String name=pre+count;
        Set<LR1Item> first=closure(firstSet);
        STState.add(name);
        STSet.add(first);
        count++;
        int oldSize=STState.size();
        int newSize=STState.size();
        do{
            oldSize=newSize;
            for(int i=0;i<STState.size();i++){
                Map<String,String> map1=new HashMap<String,String>();
                for(String path:allMark.keySet()){
                    Set<LR1Item> news=Goto(STState.get(i),path);
                    if(news.size()>0){
                        boolean canAdd=true;
                        String statename="";
                        for(int j=0;j<STSet.size();j++){
                            statename=STState.get(j);
                            Set<LR1Item> set=STSet.get(j);
                            boolean eq=true;
                            if(set.size()!=news.size()){
                                continue;
                            }
                            for(LR1Item lr1Item:set){
                                boolean found=false;
                                for(LR1Item l1:news){
                                    if(l1.equals(lr1Item)){
                                        found=true;
                                        break;
                                    }
                                }
                                if(!found){
                                    eq=false;
                                    break;
                                }
                            }
                            if(eq){
                                canAdd=false;
                                break;
                            }
                        }
                        if(canAdd){
                            name=pre+count;
                            count++;
                            /*System.out.println("------------------------------------------------");
                            System.out.println(name+":");
                            for(LR1Item l:news){
                                System.out.println(l.toString());
                            }
                            System.out.println("------------------------------------------------");*/
                            for(LR1Item l:news){
                                String s[]=l.toString().split(",");
                                if(s[0].startsWith("CC")&&s[0].endsWith(".")){
                                    name="."+name;
                                    break;
                                }
                            }
                            STState.add(name);
                            STSet.add(news);
                            map1.put(path,name);
                        }
                        else{
                            map1.put(path,statename);
                        }
                    }
                }
                stateChange.put(STState.get(i),map1);
            }
            newSize=STState.size();
        }while(oldSize!=newSize);

    }

    private void LRTable(){
        for(Map.Entry<String,Map<String,String>> entry1:stateChange.entrySet()){
            String stateName=entry1.getKey();
            //System.out.println("NOW "+stateName+": ");
            if(stateName.startsWith(".")){
                stateName=stateName.substring(1);
                //System.out.println("CHANGE "+stateName);
                Map<String,String> tempMap=new HashMap<String,String>();
                tempMap.put(endIcon,acc);
                action.put(stateName,tempMap);
                continue;
            }
            for(Map.Entry<String,String> entry2:entry1.getValue().entrySet()){
                String path=entry2.getKey();
                String des= entry2.getValue();
                //System.out.println("GET PATH "+path);
                //System.out.println("GET DES "+des);
                Map<String,String> tempMap=new HashMap<String,String>();
                if(des.startsWith(".")){
                    des=des.substring(2);
                }
                else{
                    des=des.substring(1);
                }
                //System.out.println("NEW DES IS "+des);
                if(allVTName.contains(path)){
                    des="S"+des;
                    //System.out.println("ADD "+des+" TO ACTION");
                    tempMap.put(path,des);
                    if(action.keySet().contains(stateName)){
                        Map<String,String> temp1=action.get(stateName);
                        temp1.putAll(tempMap);
                        action.replace(stateName,temp1);
                    }
                    else{
                        action.put(stateName,tempMap);
                    }
                }
                else{
                    //System.out.println("ADD "+des+" TO GOTO");
                    tempMap.put(path,des);
                    if(GOTO.keySet().contains(stateName)){
                        Map<String,String> temp1=GOTO.get(stateName);
                        temp1.putAll(tempMap);
                        GOTO.replace(stateName,temp1);
                    }
                    else{
                        GOTO.put(stateName,tempMap);
                    }
                }
            }
        }
        for(int i=0;i<STState.size();i++){
            Set<LR1Item> set=STSet.get(i);
            String statename=STState.get(i);
            for(LR1Item l:set){
                if(l.getPointPos()==l.getRight().size()){
                    for(Map.Entry<Integer,String> entry:originalCFG.entrySet()){
                        if(l.getString().equals(entry.getValue())){
                            if(!l.getLeft().getName().equals(startMark)){
                                int num=entry.getKey();
                                ArrayList<String> temp=l.getMark();
                                Map<String,String> tempMap=new HashMap<String,String>();
                                for(String mark:temp){
                                    tempMap.put(mark,"R"+num);
                                }
                                if(action.keySet().contains(statename)){
                                    Map<String,String> newm=action.get(statename);
                                    newm.putAll(tempMap);
                                    action.replace(statename,newm);
                                }
                                else{
                                    action.put(statename,tempMap);
                                }
                                break;
                            }
                        }
                    }

                }
            }
        }
    }

    private void getSquence(){
        Stack<String> stateStack=new Stack<String>();
        Stack<String> markStack=new Stack<String>();
        for(int i=0;i<input.size();i++){
            String s=input.get(i);
            char c[]=s.toCharArray();
            int point=0;
            stateStack.clear();
            markStack.clear();
            stateStack.push("I0");
            markStack.push(startIcon);
            String name="";
            while(point<c.length){
                name=name+c[point];
                point++;
                if(allVTName.contains(name)){
                    String state=stateStack.peek();
                    if(action.keySet().contains(state)&&action.get(state).keySet().contains(name)){
                        String step=action.get(state).get(name);
                        if(step.equals(acc)){
                            System.out.println("ONE SENTENCE COMPLETE!");
                            output.add("ONE SENTENCE COMPLETE!");
                        }
                        else if(step.startsWith("S")){
                            step=step.substring(1);
                            stateStack.push("I"+step);
                            markStack.push(name);
                        }
                        else if(step.startsWith("R")){
                            step=step.substring(1);
                            int num=Integer.parseInt(step);
                            String cfg=originalCFG.get(num);
                            String cfg1[]=cfg.split("->");
                            System.out.println(cfg);
                            output.add(cfg);
                            int lentgh=cfg1[1].length();
                            for(int j=0;j<lentgh;j++){
                                stateStack.pop();
                                markStack.pop();
                            }
                            if(GOTO.keySet().contains(stateStack.peek())&&GOTO.get(stateStack.peek()).keySet().contains(cfg1[0])){
                                String newState=GOTO.get(stateStack.peek()).get(cfg1[0]);
                                stateStack.push(newState);
                                markStack.push(cfg1[0]);
                                point--;
                            }
                            else{
                                System.out.println("ERROR IN LINE "+i+" .");
                                output.add("ERROR IN LINE "+i+" .");
                                return;
                            }
                        }
                    }
                    else{
                        System.out.println("ERROR IN LINE "+i+" .");
                        output.add("ERROR IN LINE "+i+" .");
                        return;
                    }
                    name="";
                }
            }
        }
    }

    private void outputFile(){
        try{
            File out=new File("output");
            FileOutputStream fw=new FileOutputStream(out);
            OutputStreamWriter writer=new OutputStreamWriter(fw,"UTF-8");
            for(int i=0;i<output.size();i++){
                writer.append(output.get(i));
                writer.append("\n");
            }
            writer.flush();
            writer.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
