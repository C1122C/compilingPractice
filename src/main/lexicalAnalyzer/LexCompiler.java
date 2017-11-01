package main.lexicalAnalyzer;
/**
 * lex功能实现类，读取文法生成词法分析器程序
 * 本程序支持的正则表达符号包括：*、+、？、（）、|、{}和自定义转义符$。
 */
import main.dataStructure.Node;
import main.dataStructure.DFA;
import main.lexicalAnalyzer.NodeType;
import main.dataStructure.DFA;
import main.dataStructure.Node;

import java.io.*;
import java.util.*;

public class LexCompiler{

    /*由节点位置映射到节点*/
    private Map<Integer,Node> posToNode;
    /*本程序支持的正则表达式符号集*/
    private Set<Character> atom;
    /*正则表达式名称-文法*/
    private Map<String,String> re;
    /*正则表达式-代码*/
    private Map<String,String> REcode;
    /*关键字-代码*/
    private Map<String,String> KWcode;
    /*操作符-代码*/
    private Map<String,String> OPcode;
    /*代码类型*/
    private int codeType;
    /*由正则表达式名称到对应树的映射*/
    private Map<String,Node> REToTree;
    /*由正则表达式名称到DFA的映射*/
    private Map<String,DFA> REToDFA;
    /*由文件编写者自己定义的代码*/
    private ArrayList<Character> part3;
    /*文件中需要直接拷贝的部分*/
    private ArrayList<Character> codeCopy;
    /*字母表*/
    private Set<Character> alphabet;
    /*正则表达式-状态转换*/
    private Map<String,Map<Character,String>> map1;
    /*新状态-原状态*/
    private Map<String,Set<String>> map2;
    /*原状态-正则名称*/
    private Map<String,String> map3;
    /*状态合并工具*/
    private LinkedList<String> queue;
    /*节点编号计数器*/
    private int finalID;
    /*状态-正则映射*/
    private Map<String,Set<String>> stateToRE;
    /*记录正则定义顺序*/
    private ArrayList<String> order;
    /*暂存文件内容*/
    private ArrayList<Character> chars;
    /*读头位置*/
    private int forward;
    /*行号*/
    private int line;
    private ArrayList<String> output;


    public LexCompiler(){
        atom=new HashSet<Character>();
        char add[]={'*','.','|','+','?'};
        for(char c:add){
            atom.add(c);
        }
        re=new HashMap<String,String>();
        REcode=new HashMap<String,String>();
        KWcode=new HashMap<String,String>();
        OPcode=new HashMap<String,String>();
        REToTree=new HashMap<String,Node>();
        REToDFA=new HashMap<String,DFA>();
        codeType=2;
        part3=new ArrayList<Character>();
        map1=new HashMap<String,Map<Character,String>>();
        map2=new HashMap<String,Set<String>>();
        map3=new HashMap<String,String>();
        queue=new LinkedList<String>();
        finalID=0;
        stateToRE=new HashMap<String,Set<String>>();
        codeCopy=new ArrayList<Character>();
        order=new ArrayList<String>();
        output=new ArrayList<String>();
    }

    /**
     * 词法分析程序生成
     * @param file 文法文件名
     */
    public void getLex(String file){
        //读取文法规则
        readGrammar(file);
        for(String s:KWcode.keySet()){
            order.add(s);
            re.put(s,s);
        }
        for(String s:OPcode.keySet()){
            order.add(s);
            re.put(s,s);
        }
        //遍历文法，逐个生成DFA
        int i=0;
        for(String s:order){
            DFA dfa=makeDFA(s,re.get(s),i);
            i++;
            REToDFA.put(s,dfa);
        }
        //状态合并
        //DFAmerge();
        //writeFile();
        getTokens("test");
        outpuFile();
    }

    /**
     * DFA合成
     */
    public void DFAmerge(){
        for(Map.Entry<String,DFA> entry:REToDFA.entrySet()){
            String rere=entry.getKey();
            System.out.println("GET RE: "+rere);
            DFA adfa=entry.getValue();
            Map<String,Map<Character,String>> toadd=new HashMap<String, Map<Character, String>>();
            for(Map.Entry<String,Map<Character,String>> en:adfa.UsableDtran.entrySet()){
                String rname=en.getKey();
                Map<Character,String> newPath=new HashMap<Character, String>();
                for(Map.Entry<Character,String> e:en.getValue().entrySet()){
                    newPath.put(e.getKey(),e.getValue());
                }
                map1.put(rname,newPath);
                toadd.put(rname,newPath);
            }

            for(String originalS:toadd.keySet()){
                if(originalS.contains("T")){
                    map3.put(originalS,rere);
                }
            }
        }

        int count=0;
        Set toadd=new HashSet<String>();
        for(String s:map1.keySet()){
            if(s.contains("-0-N")||s.contains("-0-T")){
                count++;
                toadd.add(s);
            }
        }
        map2.put("I"+finalID,toadd);

        if(count>1){
            queue.add("I"+finalID);
            finalID++;
            while(!queue.isEmpty()){
                stateMerge();
            }
        }
        /*for(Map.Entry<String,String> entry:map3.entrySet()){
            String originalS=entry.getKey();
            String r=entry.getValue();
            if(map2.keySet().contains(originalS)){
                String newSt=map2.get(originalS);
                Set<String> ss=new HashSet<String>();
                if(!stateToRE.keySet().contains(newSt)){
                    for(Map.Entry<String,String> en:map2.entrySet()){
                        if(en.getValue().equals(newSt)){
                            ss.add(map3.get(en.getKey()));
                        }
                    }
                }
                stateToRE.put(newSt,ss);
            }
        }
        //test
        for(Map.Entry<String,Map<Character,String>> entry:map1.entrySet()){
            System.out.println("FOR state "+entry.getKey()+" : ");
            for(Map.Entry<Character,String> en:entry.getValue().entrySet()){
                System.out.println("with "+en.getKey()+" : "+en.getValue());
            }
        }*/
    }

    /**
     * 状态合成
     */
    private void stateMerge(){
        Map newMap=new HashMap<Character,String>();
        String newState=queue.remove();
        System.out.println("ID: "+newState);
        ArrayList<String> originalS=new ArrayList<String>();
        ArrayList<Character> path=new ArrayList<Character>();
        ArrayList<String> destination=new ArrayList<String>();
        Iterator<Map.Entry<String,Set<String>>> it=map2.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,Set<String>> entry=it.next();
            if(entry.getKey().equals(newState)){
                originalS.addAll(entry.getValue());
                //System.out.println("s: "+entry.getKey()+" id: "+newState);
                it.remove();
            }
        }

        for(String s:originalS){
            System.out.println("original is "+s);
            //System.out.println(map1.containsKey(s)+" "+map2.get(s));
            for(Map.Entry<Character,String> entry:map1.get(s).entrySet()){
                path.add(entry.getKey());
                destination.add(entry.getValue());
                System.out.println("WITH "+entry.getKey()+" TO "+entry.getValue());
            }
        }
        for(int i=0;i<path.size();i++){
            int count=0;
            ArrayList<String> newOriginal=new ArrayList<String>();
            newOriginal.add(destination.get(i));
            char check=path.get(i);
            for(int j=i+1;j<path.size();j++){
                if(path.get(j)==check){
                    count++;
                    newOriginal.add(destination.get(j));
                    path.remove(j);
                    destination.remove(j);
                }
            }
            String name="";
            if(count>0){
                name="I"+finalID;
                finalID++;
                queue.add(name);
                for(int k=0;k<destination.size();k++){
                    if(newOriginal.contains(destination.get(k))){
                        destination.set(k,name);
                    }
                }
                Iterator<Map.Entry<Character,String>> iterator=newMap.entrySet().iterator();
                ArrayList<Character> ids=new ArrayList<Character>();
                while(iterator.hasNext()){
                    Map.Entry<Character,String> entry=iterator.next();
                    if(newOriginal.contains(entry.getValue())){
                        char id=entry.getKey();
                        ids.add(id);
                        iterator.remove();
                    }
                }
                for(char sid:ids){
                    newMap.put(sid,name);
                }
                Set<String> add=new HashSet<String>();
                if(map2.keySet().contains(name)){
                    map2.get(name).addAll(newOriginal);
                }
                else{
                    for(String s:newOriginal){
                        add.add(s);
                    }
                    map2.put(name,add);
                }

                newMap.put(check,name);
            }
            else{
                newMap.put(check,destination.get(i));
            }
        }
        for(String s:originalS){
            map1.remove(s);
        }
        map1.put(newState,newMap);
        Iterator<Map.Entry<String,Set<String>>> iterator=map2.entrySet().iterator();
        ArrayList<String> key=new ArrayList<String>();
        ArrayList<Set<String>> value=new ArrayList<Set<String>>();
        while(iterator.hasNext()){
            Map.Entry<String,Set<String>> entry=iterator.next();
            Set<String> ids=new HashSet<String>();
            boolean replace=false;
            for(String str:entry.getValue()){
                if(originalS.contains(str)){
                    replace=true;
                    break;
                }
            }
            if(replace){
                for(String str:entry.getValue()){
                    if(!originalS.contains(str)){
                        ids.add(str);
                    }
                }
                iterator.remove();
                key.add(entry.getKey());
                value.add(ids);
            }

        }
        for(int i=0;i<key.size();i++){
            map2.put(key.get(i),value.get(i));
        }
        for(Map.Entry<String,Map<Character,String>> entry:map1.entrySet()){
            Map<Character,String> temp=new HashMap<Character,String>();
            for(Map.Entry<Character,String> e:entry.getValue().entrySet()){
                temp.put(e.getKey(),e.getValue());
            }
            for(Map.Entry<Character,String> en:temp.entrySet()){
                if(originalS.contains(en.getValue())){
                    map1.get(entry.getKey()).replace(en.getKey(),newState);
                }
            }
        }
    }

    /**
     * 写文件
     */
    private void writeFile(){
        try{
            FileReader fr=new FileReader("E:\\IdeaProjects\\compilingPractice\\src\\output\\codeScript1.txt");
            BufferedReader reader=new BufferedReader(fr);
            File out=new File("E:\\IdeaProjects\\compilingPractice\\src\\output\\out.java");
            FileOutputStream fw=new FileOutputStream(out);
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(fw,"UTF-8"));
            //写入直接拷贝部分
            for(char c:codeCopy){
                writer.write(c);
            }
            //写入类定义和读文件方法
            String s=reader.readLine();
            while(s!=null){
                writer.write(s);
                s=reader.readLine();
            }
            //每个状态对应一个函数
            for(Map.Entry<String,Map<Character,String>> entry:map1.entrySet()){
                String name=entry.getKey();
                Map<Character,String> router=entry.getValue();
                writer.write("\n");
                writer.write("public String "+name+"(){\n");
                writer.write("char path=chars.get(forword);\n");
                writer.write("out=out+path;\n");
                writer.write("forword++;\n");
                if(stateToRE.containsKey(name)){
                    Set<String> reName=stateToRE.get(name);
                    for(String rn:reName){
                        writer.write("re.add("+rn+");");
                        writer.write("index.add(forward);");
                    }
                }
                writer.write("switch(path){\n");
                //可能跳转到的下一状态
                for(Map.Entry<Character,String> en:router.entrySet()){
                    writer.write("case '"+en.getKey()+"':return "+en.getValue()+"();\n");
                }
                writer.write("default:forword=forword-2;\n");
                writer.write("}\n");
                writer.write("begin=lexBegin;\n");
                writer.write("length=forword-lexBegin+1;\n");
                writer.write("lexBegin=forword+1\n");
                writer.write("forword=lexBegin;\n");
                //如果符合出口条件，写入文件对应代码
                writer.write("if(!isEmpty(re){\n");
                writer.write("forward=index.get(index.size()-1);\n");
                writer.write("index.remove(index.size()-1);\n");
                writer.write("String rename=re.get(re.size()-1);\n");
                writer.write("re.remove(re.size()-1);\n");
                writer.write("return reNameCode(rename);\n");
                writer.write("else{\n");
                writer.write("return \"Wrong code\";\n");
                writer.write("}\n");
                if(stateToRE.containsKey(name)){
                    Set<String> reName=stateToRE.get(name);
                    for(String rn:reName){
                        writer.write("re.add("+rn+");");
                        if(KWcode.containsKey(rn)){

                        }
                        else if(REcode.containsKey(rn)){
                            writer.write(REcode.get(rn));
                        }
                        else{
                            writer.write(OPcode.get(rn));
                        }
                    }
                }
                writer.write("}\n");
            }
            writer.write("public String reNameCode(String re){\n");
            for(Map.Entry<String,String> entry:KWcode.entrySet()){
                writer.write("if(re.equals("+entry.getKey()+"){");
                writer.write(entry.getValue());
                writer.write("}\n");
            }
            for(Map.Entry<String,String> entry:REcode.entrySet()){
                writer.write("if(re.equals("+entry.getKey()+"){");
                writer.write(entry.getValue());
                writer.write("}\n");
            }
            for(Map.Entry<String,String> entry:OPcode.entrySet()){
                writer.write("if(re.equals("+entry.getKey()+"){");
                writer.write(entry.getValue());
                writer.write("}\n");
            }
            //写入自定义函数
            for(char part:part3){
                writer.write(part);
            }
            //结束类
            writer.write("}");
            //写入工具类
            fr=new FileReader("E:\\IdeaProjects\\compilingPractice\\src\\output\\codeScript2.txt");
            reader=new BufferedReader(fr);
            s=reader.readLine();
            while(s!=null){
                writer.write(s);
            }
            writer.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 文法文件解析
     * @param file 文件名称
     */
    private String readGrammar(String file){
        ArrayList<Character> content=new ArrayList<Character>();
        //是否正在读取复制部分
        boolean copy=false;
        //是否正在读取正则表达式部分
        boolean RE=false;
        //是否正在读取正则表达式转换部分
        boolean trans=false;
        //是否正在读取定义或转换部分的名称
        boolean inName=false;
        //标记当前读取文件的位置
        int part=1;
        //读头位置
        int currentP=0;
        //探针位置
        int spyP=0;
        //正则表达式名称
        String reName="";
        //正则表达式代码
        String reCode="";
        //正则表达式描述
        String reDes="";
        //将文件内容一次性读入content中
        try{
            FileInputStream fr=new FileInputStream(file);
            BufferedReader reader=new BufferedReader(new InputStreamReader(fr,"UTF-8"));
            int in=reader.read();
            while(in!=-1){
                char c=(char)(in);
                content.add(c);
                in=reader.read();
            }
            reader.close();
        }catch (Exception e){
            System.out.println("not find file");
        }


        //扫描文件内容
        while(currentP<content.size()){
            if(part==3){
                break;
            }
            char c=content.get(currentP);
            //正在读取直接拷贝的代码
            if(copy){
                //判断是否读到了结束标记
                if((currentP==content.size()-2)&&((c!='%')||(content.get(currentP+1)!='}'))){
                    return "Fail:Can not match a %} mark to end the grammar file.";
                }
                if(c=='%'&&part==1){
                    spyP=currentP+1;
                    if(content.get(spyP)=='}'){
                        currentP=currentP+2;
                        copy=false;
                    }
                }
                else{
                    codeCopy.add(c);
                    currentP=currentP+1;
                }
            }
            //正在读取正则定义部分的代码
            else if(RE){
                //判断第一部分是否结束
                if(c=='%'){
                    spyP=currentP+1;
                    if(content.get(spyP)=='%'){
                        part=2;
                        RE=false;
                        currentP=currentP+2;
                        while(content.get(currentP)==' '||content.get(currentP)=='\n'||content.get(currentP)=='\r'){
                            currentP++;
                        }
                        inName=true;
                        reName="";
                        continue;
                    }
                }

                //正在读取正则定义的名称
                if(inName){
                    //判断是否读取完毕
                    if(c==' '){
                        while(content.get(currentP)==' '){
                            currentP++;
                        }
                        inName=false;
                    }
                    else{
                        reName=reName+c;
                        currentP++;
                    }
                }
                else{
                    //判断正则定义部分是否读完
                    if(c=='\n'||c=='\r'){
                        while(content.get(currentP)==' '||content.get(currentP)=='\n'||content.get(currentP)=='\r'){
                            currentP++;
                        }
                        inName=true;
                        if(reName.length()!=0){
                            re.put(reName,reDes);
                            order.add(reName);
                            reName="";
                            reDes="";
                        }
                    }
                    else{
                        reDes=reDes+c;
                        currentP++;
                    }
                }
            }
            //正在读取转化部分
            else if(part==2){
                //判断转化部分是否结束
                if(c=='%'){
                    spyP=currentP+1;
                    if(content.get(spyP)=='%'){
                        part=3;
                        currentP=currentP+2;
                        while(content.get(currentP)==' '||content.get(currentP)=='\n'||content.get(currentP)=='\r'){
                            currentP++;
                        }
                        continue;
                    }
                }
                //正在读取正则名称
                if(inName){
                    //遇到一个正则
                    if(c=='$'){
                        reName=reName+c;
                        reName=reName+content.get(currentP+1);
                        currentP+=2;
                    }
                    else if(c=='{'){
                        codeType=1;
                        currentP++;
                    }
                    //名称读取结束
                    else if((c==' '&&codeType==2)||(c=='}'&&codeType==1)||(c=='"'&&codeType==3)){
                        currentP++;
                        while(content.get(currentP)==' '){
                            currentP++;
                        }
                        inName=false;
                    }
                    else{
                        reName=reName+c;
                        currentP++;
                    }
                }
                else{
                    if(reName.startsWith("\"")){
                        codeType=3;
                        reName=reName.substring(1,reName.length()-1);
                    }
                    if(trans){
                        if(c=='$'){
                            reCode=reCode+c;
                            reCode=reCode+content.get(currentP+1);
                            currentP+=2;
                        }
                        else if(c=='}'){
                            if(reName.length()!=0){
                                switch(codeType){
                                    case 1:REcode.put(reName,reCode);break;
                                    case 2:KWcode.put(reName,reCode);break;
                                    case 3:OPcode.put(reName,reCode);break;
                                }
                            }
                            reName="";
                            reCode="";
                            codeType=2;
                            trans=false;
                            currentP++;
                            while(content.get(currentP)==' '||content.get(currentP)=='\n'||content.get(currentP)=='\r'){
                                currentP++;
                            }
                            inName=true;
                        }
                        else{
                            reCode=reCode+c;
                            currentP++;
                        }
                    }
                    else{
                        if(c=='{'){
                            trans=true;
                            currentP++;
                        }
                    }
                }
            }
            //开始复制部分
            else if(c=='%'){
                spyP=currentP+1;
                if(content.get(spyP)=='{'&&part==1){
                    copy=true;
                    currentP=currentP+2;
                }
                else if(content.get(spyP)=='%'&&part==1){
                    part=2;
                    currentP=currentP+2;
                    while(content.get(currentP)==' '||content.get(currentP)=='\n'||content.get(currentP)=='\r'){
                        currentP++;
                        inName=true;
                    }
                }
                else if(content.get(spyP)=='%'&&part==2){
                    part=3;
                    copy=true;
                    currentP=currentP+2;
                    while(content.get(currentP)==' '||content.get(currentP)=='\n'||content.get(currentP)=='\r'){
                        currentP++;
                    }
                }
            }
            //进入正则定义
            else if (c == '/') {
                if(content.get(currentP+1)=='*'&&content.get(currentP+2)=='R'&&
                        content.get(currentP+3)=='E'&&content.get(currentP+4)=='*'&&
                        content.get(currentP+5)=='/'){
                    RE=true;
                    currentP=currentP+6;
                    while(content.get(currentP)==' '||content.get(currentP)=='\n'||content.get(currentP)=='\r'){
                        currentP++;
                    }
                    inName=true;
                }
            }
            else{
                currentP++;
            }

        }

        //拷贝第三部分的代码
        if(part==3){
            for(;currentP<content.size();currentP++){
                part3.add(content.get(currentP));
            }
        }

        return "Success!";

    }

    /**
     * 生成DFA
     * @param reName 正则表达式名称
     * @param input 正则表达式描述
     * @param pf DFA编号
     * @return DFA实体类
     */
    private DFA makeDFA(String reName,String input,int pf){
        //首先生成语法树
        Node node=makeTree(input);
        //System.out.println(node.getIcon());
        REToTree.put(reName,node);
        DFA dfa=new DFA();
        dfa.Dstates=new HashMap<String,Set<Integer>>();
        dfa.Dtran=new HashMap<String,Map<Character,Set<Integer>>>();

        Stack<Node> nstack = new Stack<Node>();
        Node p = node;
        Node current=new Node();
        //得到终态的位置
        int end=node.end;
        posToNode=new HashMap<Integer,Node>();
        boolean out=false;
        //节点属性计算
        for(;;){
            if(out){
                break;
            }
            while(p != null){
                current=p;
                current.tag=0;
                nstack.push(current);
                p = p.getLeft();
            }
            current = nstack.pop();
            p=current;
            while(current.tag==1){
                current.setNullable(nullable(current));
                current.setFirst(firstpos(current));
                //System.out.println("ICON:"+current.getIcon()+" POS:"+current.getPostion()+" NULL:"+current.isNull+" LEAF:"+current.isLeaf()+" SIZE:"+current.getFirst().size());
                current.setLast(lastpos(current));
                //System.out.println("ICON:"+current.getIcon()+" POS:"+current.getPostion()+" NULL:"+current.isNull+" LEAF:"+current.isLeaf()+" SIZE:"+current.getFirst().size());
                followpos(current);
                //System.out.println("ICON:"+current.getIcon()+" POS:"+current.getPostion()+" NULL:"+current.isNull+" LEAF:"+current.isLeaf()+" SIZE:"+current.getFirst().size());
                posToNode.put(current.getPostion(),current);
                //System.out.println("ICON:"+current.getIcon()+" POS:"+current.getPostion()+" NULL:"+current.isNull+" LEAF:"+current.isLeaf()+" SIZE:"+current.getFirst().size());
                if(!nstack.empty()){
                    current = nstack.pop();
                    p = current;
                }
                else{
                    out=true;
                    break;
                }
            }
            current.tag=1;
            nstack.push(current);
            p=p.getRight();
        }


        Set<Integer> tt=node.getFollow();
        tt.add(node.end);
        node.setFollow(tt);
        current=node;
        nstack.clear();
        for(;;){
            while(current!=null){
                if(current.getType()==NodeType.CAT){
                    tt=current.getRight().getFollow();
                    tt.addAll(current.getFollow());
                    current.getRight().setFollow(tt);
                    if(current.getRight().isNullable()){
                        tt=current.getLeft().getFollow();
                        tt.addAll(current.getFollow());
                        current.getLeft().setFollow(tt);
                    }
                }
                else if(current.getType()==NodeType.STAR){
                    tt=current.getLeft().getFollow();
                    tt.addAll(current.getFollow());
                    current.getLeft().setFollow(tt);
                }
                else if(current.getType()==NodeType.OR){
                    tt=current.getRight().getFollow();
                    tt.addAll(current.getFollow());
                    current.getRight().setFollow(tt);
                    tt=current.getLeft().getFollow();
                    tt.addAll(current.getFollow());
                    current.getLeft().setFollow(tt);
                }
                nstack.push(current);
                current=current.getLeft();
            }
            if(!nstack.empty()){
                current=nstack.pop();
                current=current.getRight();
            }
            else{
                break;
            }
        }
        LinkedList<String> link=new LinkedList<String>();
        String name="";
        if(node.getFirst().contains(end)){
            name=pf+"-"+0+"-T";
        }
        else{
            name=pf+"-"+0+"-N";
        }
        link.add(name);
        dfa.Dstates.put(name,node.getFirst());
        int i = 1;
        while(!link.isEmpty()){
            //System.out.println("the "+i+" times");
            String deal=link.remove();
            Set<Integer> temp=dfa.Dstates.get(deal);
            //System.out.println("oldSIZE: "+temp.size());
            //状态编号
            for(char c:alphabet){
                Set<Integer> newS = new HashSet<Integer>();
                for(int num:temp){
                    if(posToNode.get(num).getIcon()==c){
                        newS.addAll((posToNode.get(num)).getFollow());
                    }
                }
                if((!dfa.Dstates.containsValue(newS))&&(newS.size()>0)){
                    name=pf+"-";
                    if(newS.contains(end)){
                        name=name+i+"-T";
                    }
                    else{
                        name=name+i+"-N";
                    }
                    dfa.Dstates.put(name,newS);
                    i++;
                    link.add(name);
                }
                else{
                    //System.out.println("SKIP! "+dfa.Dstates.size());
                }

                if(dfa.Dtran.containsKey(deal)){
                    Map<Character,Set<Integer>> t=dfa.Dtran.get(deal);
                    t.put(c,newS);
                    dfa.Dtran.replace(deal,t);
                }
                else{
                    Map<Character,Set<Integer>> add = new HashMap<Character,Set<Integer>>();
                    add.put(c,newS);
                    dfa.Dtran.put(deal,add);
                }
            }

        }

        dfa.trans();
        return dfa;
    }

    /**
     * 根据正则表达式生成语法树
     * @param in 正则表达式
     * @return 根节点
     */
    private Node makeTree(String in){
        alphabet=new HashSet<Character>();
        Node root=new Node();
        Stack<Node> stack=new Stack<Node>();
        //预处理
        String input=preTransform(in);
        //System.out.println(input);
        for(int i=0;i<input.toCharArray().length;i++){
            char c=input.toCharArray()[i];
            if(c=='#'){
                root=new Node();
                root.setIcon('.');
                root.setType(NodeType.CAT);
                Node left=stack.pop();
                root.setLeft(left);
                Node right=new Node();
                right.setIcon(c);
                right.isEnd=true;
                root.setRight(right);
                break;
            }
            Node node=new Node();
            if(c=='$'){
                node.setIcon(input.toCharArray()[i+1]);
                i++;
            }
            else{
                node.setIcon(c);
            }
            if(!atom.contains(c)){
                node.setType(NodeType.OPERAND);
                node.isLeaf=true;
                stack.push(node);
                if(c=='$'){
                    alphabet.add(input.toCharArray()[i]);
                }
                else{
                    alphabet.add(c);
                }
            }
            else{
                if(c=='*'){
                    node.setType(NodeType.STAR);
                    Node left=stack.pop();
                    node.setLeft(left);
                    stack.push(node);
                }
                else if(c=='+'){
                    node.setType(NodeType.STAR);
                    Node left=stack.pop();
                    node.setLeft(left);
                    Node newNode=new Node();
                    newNode.setType(NodeType.CAT);
                    newNode.setLeft(node);
                    newNode.setRight(left);
                    stack.push(newNode);
                }
                else if(c=='?'){
                    node.setType(NodeType.OR);
                    Node left=stack.pop();
                    node.setLeft(left);
                    Node newNode=new Node();
                    newNode.isNull=true;
                    node.setRight(newNode);
                    stack.push(node);
                }
                else{
                    if(c=='|'){
                        node.setType(NodeType.OR);
                    }
                    else{
                        node.setType(NodeType.CAT);
                    }
                    Node right=stack.pop();
                    Node left=stack.pop();
                    node.setLeft(left);
                    node.setRight(right);
                    stack.push(node);
                }
            }
        }

        //编号
        Node current=root;
        Stack<Node> nstack = new Stack<Node>();
        int id=1;
        for(;;){
            while(current != null){
                nstack.push(current);
                current = current.getLeft();
            }
            if(!nstack.empty()){
                current = nstack.pop();
               if(current.getRight()==null&&current.getLeft()==null){
                   current.setPostion(id);
                   //System.out.println(current.getIcon()+" : "+current.getPostion());
                   id++;
               }
                current = current.getRight();
            }
            else{
                break;
            }
        }
        root.end=id-1;
        //System.out.println("This end : "+root.end);
        return root;
    }

    /**
     * 正则表达式预处理
     * @param in 正则表达式
     * @return 转换后的只含有.*\{}？的后缀正则表达
     */
    private String preTransform(String in){
        String input=in;
        //System.out.println("INTO PRE : "+in);
        while((input.contains("{"))&&(!input.contains("${"))){
            String old=input;
            input="";
            char c[] =old.toCharArray();
            for(int i=0;i<c.length;i++){
                if(c[i]=='$'){
                    input=input+c[i];
                    input=input+c[i+1];
                    i++;
                }
                else if(c[i]=='{'){
                    String source="";
                    i++;
                    while(c[i]!='}'||c[i-1]=='$'){
                        source=source+c[i];
                        i++;
                    }
                    source=re.get(source);
                    input=input+"("+source+")";
                }
                else{
                    input=input+c[i];
                }
            }
        }

        char ca[]=input.toCharArray();
        String withPoint="";
        //第一步：加点
        for(int i=0;i<ca.length-1;i++){
            if(!isOperator(ca[i])){
                if(ca[i]=='$'){
                    withPoint=withPoint+ca[i];
                    if(i<ca.length-2){
                        withPoint=withPoint+ca[i+1];
                        i++;
                        if(ca[i+1]=='('||isOperand(ca[i+1])){
                            if(!(ca[i+1]==')')){
                                withPoint=withPoint+".";
                            }
                        }
                    }
                    continue;
                }
                else if(ca[i]=='('){
                    withPoint=withPoint+ca[i];
                    continue;
                }

                else if(ca[i]==')'){
                    withPoint=withPoint+ca[i];
                    if(!isOperator((ca[i+1]))&&ca[i+1]!=')'){
                        withPoint=withPoint+".";
                    }
                    continue;
                }
                if(ca[i+1]=='('){
                    withPoint=withPoint+".";
                }
                else if(ca[i+1]==')'){
                    withPoint=withPoint+ca[i];
                }
                else if(isOperand(ca[i+1])){
                    withPoint=withPoint+ca[i]+".";
                }
                else{
                    withPoint=withPoint+ca[i];
                }
            }
            else if(ca[i]=='*'||ca[i]=='+'||ca[i]=='?'){
                if(isOperand(ca[i+1])){
                    withPoint=withPoint+ca[i];
                    withPoint=withPoint+".";
                }
            }
            else{
                withPoint=withPoint+ca[i];
            }
        }

        withPoint=withPoint+ca[ca.length-1];
        withPoint=withPoint+"#";
        String result="";
        Stack<Character> stack=new Stack<Character>();
        stack.push('#');
        //定义优先级
        Map<Character,Integer> priority=new HashMap<Character,Integer>();
        priority.put('#',0);
        priority.put('*',5);
        priority.put('+',5);
        priority.put('?',5);
        priority.put('|',2);
        priority.put('.',3);
        priority.put('(',1);
        char cp[]=withPoint.toCharArray();
        char top;
        for(int i=0;i<cp.length;i++){
            //System.out.println("string: "+result+" i: "+i);
            if(cp[i]=='#'){
                while(!stack.empty()){
                    top=stack.pop();
                    result=result+top;
                }
                break;
            }
            else if(cp[i]=='$'){
                result=result+cp[i];
                i=i+1;
                result=result+cp[i];

            }
            else{
                if(isOperator(cp[i])){
                    if(cp[i]=='*'||cp[i]=='+'||cp[i]=='?'){
                        result=result+cp[i];
                        continue;
                    }
                    top=stack.pop();
                    try{
                        while(priority.get(cp[i])<=priority.get(top)){
                            result=result+top;
                            top=stack.pop();
                        }
                    }catch (NullPointerException e){
                        System.out.println("now "+cp[i]+" "+top);
                    }
                    stack.push(top);
                    stack.push(cp[i]);
                }
                else if(cp[i]=='('){
                    stack.push(cp[i]);
                }
                else if(cp[i]==')'){
                    top=stack.pop();
                    while(top!='('){
                        result=result+top;
                        top=stack.pop();
                    }
                }
                else{
                    result=result+cp[i];
                }
            }
        }
        return result;
    }

    /**
     * 判断一个字符是否是操作数
     * @param c 待判断字符
     * @return
     */
    private boolean isOperand(char c){
        switch (c){
            case '*':
            case '+':
            case '?':
            case '|':return false;
            default:return true;
        }
    }

    /**
     * 判断一个字符是否是操作符
     * @param c 待判断字符
     * @return
     */
    private boolean isOperator(char c){
        switch (c){
            case '*':
            case '+':
            case '?':
            case '|':
            case '.':return true;
            default:return false;
        }
    }

    /**
     * 计算节点属性的方法
     * @param n 输入节点
     */
    private boolean nullable(Node n){
        if(n.isLeaf()){
            if(n.isNull){
                return true;
            }
            return false;
        }
        if(n.getType()==NodeType.OR){
            return n.getLeft().isNullable()||n.getRight().isNullable();
        }
        if(n.getType()==NodeType.CAT){
            return n.getLeft().isNullable()&&n.getRight().isNullable();
        }
        if(n.getType()==NodeType.STAR){
            return true;
        }
        return true;
    }

    private Set<Integer> firstpos(Node n){
        Set<Integer> result=new HashSet();
        if(n.isLeaf()){
            if(n.isNull){
                return result;
            }
            result.add(n.getPostion());
            return result;
        }
        else if(n.getType()==NodeType.OR){
            result=n.getLeft().getFirst();
            result.addAll(n.getRight().getFirst());
        }
        else if(n.getType()==NodeType.CAT){
            if(n.getLeft().isNullable()){
                result=n.getLeft().getFirst();
                result.addAll(n.getRight().getFirst());
            }
            else{
                result=n.getLeft().getFirst();
            }
        }
        else if(n.getType()==NodeType.STAR){
            result=n.getLeft().getFirst();
        }
        return result;
    }

    private Set<Integer> lastpos(Node n){
        Set<Integer> result=new HashSet();
        if(n.isLeaf()){
            if(n.isNull){
                return result;
            }
            result.add(n.getPostion());
        }
        else if(n.getType()==NodeType.OR){
            result=n.getLeft().getLast();
            result.addAll(n.getRight().getLast());
        }
        else if(n.getType()==NodeType.CAT){
            if(n.getRight().isNullable()){
                result=n.getLeft().getLast();
                result.addAll(n.getRight().getLast());
            }
            else{
                result=n.getRight().getLast();
            }
        }
        else if(n.getType()==NodeType.STAR){
            result=n.getLeft().getLast();
        }
        return result;
    }

    private void followpos(Node n){
        Set<Integer> result=new HashSet();
        if((n.getType()==NodeType.CAT)||(n.getType()==NodeType.STAR)){
            //System.out.println(n.getIcon()+" "+ n.getType());
            Set<Integer> toAdd = new HashSet<Integer>();
            Set<Integer> host = new HashSet<Integer>();
            if(n.getType()==NodeType.CAT){
                toAdd = n.getRight().getFirst();
                host = n.getLeft().getLast();
            }
            else if(n.getType()==NodeType.STAR){
                toAdd=n.getFirst();
                host=n.getLast();
            }
            for(int i:host){
                Node no=posToNode.get(i);
                Set<Integer> sss=no.getFollow();
                sss.addAll(toAdd);
                no.setFollow(sss);
                posToNode.replace(i,no);
            }
        }
    }

    /**
     * 输出输入文件的全部token
     * @param name 输入文件名
     */
    private void getTokens(String name){
        chars=new ArrayList<Character>();
        forward=0;
        line=1;
        try{
            FileInputStream fr=new FileInputStream(name);
            BufferedReader reader=new BufferedReader(new InputStreamReader(fr,"UTF-8"));
            int in=reader.read();
            while(in!=-1){
                char c=(char)(in);
                chars.add(c);
                in=reader.read();
            }
            reader.close();
        }catch (Exception e){
            System.out.println("not find file");
        }

        //System.out.println(chars.size());
        boolean wrong=false;
        while(forward<chars.size()&&(!wrong)){
            wrong=getNextToken();
        }

    }

    /**
     * 读取下一个token
     * @return
     */
    private boolean getNextToken(){
        ArrayList<String> reget=new ArrayList<String>();
        ArrayList<Integer> index=new ArrayList<Integer>();
        ArrayList<String> currentState=new ArrayList<>();
        ArrayList<String> currentRE=new ArrayList<String>();
        ArrayList<Boolean> endLine=new ArrayList<Boolean>();
        boolean canNotGo=false;
        boolean reachEnd=false;
        while(chars.get(forward)=='\n'||chars.get(forward)=='\r'){
            forward++;
            line++;
            //System.out.println("ADD ONCE!");
            if(forward==chars.size()){
                return true;
            }
        }
        while(chars.get(forward)==' '){
            forward++;
            if(forward==chars.size()){
                return true;
            }
        }
        //System.out.println("MOVE TO: "+forward);

        for(Map.Entry<String,DFA> entry:REToDFA.entrySet()){
            for(Map.Entry<String,Map<Character,String>> en:entry.getValue().UsableDtran.entrySet()){
                if(en.getKey().contains("-0-N")||en.getKey().contains("-0-T")){
                    //System.out.println("GET RE: "+ entry.getKey()+" GET STATE: "+en.getKey());
                    currentState.add(en.getKey());
                    currentRE.add(entry.getKey());
                }
            }
        }
        for(int j=0;j<currentState.size();j++){
            //System.out.println("IN RE: "+currentRE.get(j)+" IN STATE: "+currentState.get(j));
            int head=forward;
            canNotGo=false;
            boolean end=false;
            while(!canNotGo){
                if(currentState.get(j).contains("T")){
                    reget.add(currentRE.get(j));
                    index.add(head);
                    endLine.add(end);
                    if(head==chars.size()){
                        end=true;
                        break;
                    }
                    //System.out.println("ALREADY ADD: "+currentRE.get(j)+" "+head);

                }
                char now=chars.get(head);
                head++;

                if (now == '\n'||now=='\r') {
                    break;
                }
                if(now==' '){
                    break;
                }
                if(REToDFA.get(currentRE.get(j)).UsableDtran.get(currentState.get(j)).keySet().contains(now)){
                    //System.out.println("CAN MOVE ON");
                    currentState.set(j,REToDFA.get(currentRE.get(j)).UsableDtran.get(currentState.get(j)).get(now));
                }
                else{
                    //System.out.println("OUT");
                    canNotGo=true;
                }
            }

        }
        if(reget.size()==0){
            System.out.println("GOT AN ERROR IN LINE "+(1+line/2));
            return true;
        }
        int longest=0;
        for(int i=0;i<reget.size();i++){
            if(index.get(i)>index.get(longest)){
                longest=i;
            }
        }
        int l=index.get(longest);
        Set<String> result=new HashSet<String>();
        for(int i=0;i<reget.size();i++){
            if(index.get(i)==l){
                result.add(reget.get(i));
            }
        }
        String out="";
        boolean ok=false;
        for(String sre:result){
            if(KWcode.keySet().contains(sre)){
                out=KWcode.get(sre);
                ok=true;
            }
        }
        if(!ok){
            for(String sre:result){
                if(REcode.keySet().contains(sre)){
                    out=REcode.get(sre);
                    ok=true;
                }
            }
        }
        if(!ok){
            for(String sre:result){
                if(OPcode.keySet().contains(sre)){
                    out=OPcode.get(sre);
                    ok=true;
                }
            }
        }

        if(!out.contains(">")){
            int i=forward;
            while(i<index.get(longest)){
                out=out+chars.get(i);
                i++;
            }
            out=out+">";
        }
        forward=index.get(longest);
        System.out.println(out);
        output.add(out);
        if(index.get(longest)==chars.size()){
            return true;
        }
        return false;
    }

    /**
     * 输出Tokens到文件
     */
    private void outpuFile(){
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