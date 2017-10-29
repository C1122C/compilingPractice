package main.lexicalAnalyzer;
/**
 * lex功能实现类，读取文法生成词法分析器程序
 * 本程序支持的正则表达符号包括：*、+、？、（）、|、{}。
 */
import main.dataStructure.Node;
import main.dataStructure.DFA;
import main.lexicalAnalyzer.NodeType;
import main.dataStructure.DFA;
import main.dataStructure.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.LinkedList;

public class LexCompiler{

    /*由节点位置映射到节点*/
    private Map<Integer,Node> posToNode;
    /*本程序支持的正则表达式符号集*/
    private Set<Character> atom;
    /*正则表达式名称-文法*/
    public Map<String,String> re;
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
    /*原状态-新状态*/
    private Map<String,String> map2;
    /*原状态-正则名称*/
    private Map<String,String> map3;
    /*状态合并工具*/
    private LinkedList<String> queue;
    private int finalID;
    /*状态-正则映射*/
    private Map<String,Set<String>> stateToRE;
    private ArrayList<String> order;
    int lexBegin=0;
    int forword=0;

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
        map2=new HashMap<String,String>();
        map3=new HashMap<String,String>();
        queue=new LinkedList<String>();
        finalID=0;
        stateToRE=new HashMap<String,Set<String>>();
        codeCopy=new ArrayList<Character>();
        order=new ArrayList<String>();
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
            re.put(s," "+s+" ");
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
        DFAmerge();
        writeFile();
    }

    /**
     * DFA合成
     */
    public void DFAmerge(){
        for(Map.Entry<String,DFA> entry:REToDFA.entrySet()){
            String rere=entry.getKey();
            Map<String,Map<Character,String>> toadd=entry.getValue().UsableDtran;
            map1.putAll(toadd);
            for(String originalS:toadd.keySet()){
                if(originalS.contains("T")){
                    map3.put(originalS,rere);
                }
            }
        }
        int count=0;
        for(String s:map1.keySet()){
            if(s.equals("0-N")||s.equals("0-T")){
                count++;
                map2.put(s,"I"+finalID);
            }
        }
        if(count>1){
            queue.add("I"+finalID);
            finalID++;
            while(!queue.isEmpty()){
                stateMerge();
            }
        }
        for(Map.Entry<String,String> entry:map3.entrySet()){
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
    }

    /**
     * 状态合成
     */
    private void stateMerge(){
        Map newMap=new HashMap<Character,String>();
        String newState=queue.remove();
        ArrayList<String> originalS=new ArrayList<String>();
        ArrayList<Character> path=new ArrayList<Character>();
        ArrayList<String> destination=new ArrayList<String>();
        for(Map.Entry<String,String> entry:map2.entrySet()){
            if(entry.getValue().equals(newState)){
                originalS.add(entry.getKey());
            }
        }
        for(String s:originalS){
            Map<Character,String> temp=map1.get(s);
            for(Map.Entry<Character,String> entry:temp.entrySet()){
                path.add(entry.getKey());
                destination.add(entry.getValue());
            }
            map1.remove(s);
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
            if(count>0){
                String name="I"+finalID;
                finalID++;
                queue.add(name);
                for(String s:newOriginal){
                    map2.put(s,name);
                }
                newMap.put(check,name);
            }
            else{
                newMap.put(check,destination.get(i));
            }
        }
        map1.put(newState,newMap);
        for(Map.Entry<String,Map<Character,String>> entry:map1.entrySet()){
            Map<Character,String> temp=entry.getValue();
            for(Map.Entry<Character,String> en:temp.entrySet()){
                if(originalS.contains(en.getValue())){
                    temp.replace(en.getKey(),en.getValue(),newState);
                    map1.replace(entry.getKey(),entry.getValue(),temp);
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
                    if(c=='\n'){
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
                    if(c=='{'){
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
                        if(c=='}'){
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
        /*test:System.out.println("COPY_PART:");
        for(char ch:codeCopy){
            System.out.print(ch);
        }
        System.out.println("PART3_PART:");
        for(char ch:part3){
            System.out.print(ch);
        }
        System.out.println("RE_PART:");
        for(Map.Entry<String,String> entry:re.entrySet()){
            System.out.println("NAME: "+entry.getKey()+" DES: "+entry.getValue());
        }
        System.out.println("RE_PART:");
        for(Map.Entry<String,String> entry:REcode.entrySet()){
            System.out.println("NAME: "+entry.getKey()+" CODE: "+entry.getValue());
        }
        System.out.println("KW_PART:");
        for(Map.Entry<String,String> entry:KWcode.entrySet()){
            System.out.println("NAME: "+entry.getKey()+" CODE: "+entry.getValue());
        }
        System.out.println("OP_PART:");
        for(Map.Entry<String,String> entry:OPcode.entrySet()){
            System.out.println("NAME: "+entry.getKey()+" CODE: "+entry.getValue());
        }*/
        return "Success!";

    }

    /**
     * 生成DFA
     * @param reName 正则表达式名称
     * @param input 正则表达式描述
     * @param pf DFA编号
     * @return DFA实体类
     */
    public DFA makeDFA(String reName,String input,int pf){
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

        //test
        for(;;){
            while(current!=null){
                nstack.push(current);
                current=current.getLeft();
            }
            if(!nstack.empty()){
                current=nstack.pop();
                //System.out.println("ICON:"+current.getIcon()+" POS:"+current.getPostion()+" NULL:"+current.isNull+" LEAF:"+current.isLeaf()+" SIZE:"+current.getFirst().size());
                current=current.getRight();
            }
            else{
                break;
            }
        }
        //test end
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
            name=name+0+"-T";
        }
        else{
            name=name+0+"-N";
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
                //System.out.println("alpha "+c);
                Set<Integer> newS = new HashSet<Integer>();
                for(int num:temp){
                    if(posToNode.get(num).getIcon()==c){
                        //System.out.println("added");
                        newS.addAll((posToNode.get(num)).getFollow());
                        //System.out.println("newSIZE: "+newS.size());
                    }
                }
                //System.out.println(dfa.Dstates.containsValue(newS));
                if((!dfa.Dstates.containsValue(newS))&&(newS.size()>0)){
                    //System.out.println("new, stateSIZE: "+dfa.Dstates.size());
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
        for(char c:input.toCharArray()){
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
            node.setIcon(c);
            if(!atom.contains(c)){
                node.setType(NodeType.OPERAND);
                node.isLeaf=true;
                node.setIcon(c);
                stack.push(node);
                alphabet.add(c);
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
        while(input.contains("{")){
            String old=input;
            input="";
            char c[] =old.toCharArray();
            for(int i=0;i<c.length;i++){
                if(c[i]=='{'){
                    String source="";
                    i++;
                    while(c[i]!='}'){
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
                if(ca[i]=='('){
                    withPoint=withPoint+ca[i];
                    continue;
                }

                if(ca[i]==')'){
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
        //System.out.println("we get "+input);
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
            if(cp[i]=='#'){
                while(!stack.empty()){
                    top=stack.pop();
                    result=result+top;
                }
                break;
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

    public void getNextToken(String name){
        ArrayList<String> reget=new ArrayList<String>();
        String currentState;
        boolean canNotGo=false;
        currentState="I0";
        boolean canOut=false;
        ArrayList<Character> chars=new ArrayList<Character>();
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
        while(!canNotGo){
            Map router=(HashMap<Character,String>)map1.get(currentState);
            char path=(Character)chars.get(forword);
            if(stateToRE.containsKey(currentState)){
                canOut=true;
                reget.addAll(stateToRE.get(currentState));
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
        String gainedRE=reget.get(reget.size()-1);
        lexBegin=forword+1;
        forword=lexBegin;
        System.out.println(gainedRE);
    }
}