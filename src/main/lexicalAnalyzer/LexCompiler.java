package main.lexicalAnalyzer;

/*本程序支持的正则表达符号包括：*、+、？、（）、|、{}。*/
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
import java.util.Queue;
import java.util.LinkedList;

public class LexCompiler{

    private Map<Integer,Node> posToNode;
    private Set<Character> atom;
    private Map<String,String> re;
    private Map<String,String> REcode;
    private Map<String,String> KWcode;
    private Map<String,String> OPcode;
    private int codeType;
    private Map<String,Node> REToTree;
    private Map<String,DFA> REToDFA;
    private ArrayList<Character> part3;
    private ArrayList<Character> codeCopy;
    private Set<Character> alphabet;
    private Map<String,Map<Character,String>> map1;
    private Map<String,String> map2;
    private Map<String,String> map3;
    private LinkedList<String> queue;
    private int finalID;
    private Map<String,Set<String>> stateToRE;

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
    }

    public void getLex(String file){
        readGrammar(file);
        for(String s:KWcode.keySet()){
            re.put(s," "+s+" ");
        }
        for(String s:OPcode.keySet()){
            re.put(s,s);
        }
        int i=0;
        for(Map.Entry<String,String> entry : re.entrySet()){
            DFA dfa=makeDFA(entry.getKey(),entry.getKey(),i);
            i++;
            REToDFA.put(entry.getKey(),dfa);
        }
        DFAmerge();
        writeFile();
    }

    private void DFAmerge(){
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
                for(String s:originalS){
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

    private void writeFile(){
        try{
            FileReader fr=new FileReader("E:\\IdeaProjects\\compilingPractice\\src\\output\\codeScript1.txt");
            BufferedReader reader=new BufferedReader(fr);
            File out=new File("E:\\IdeaProjects\\compilingPractice\\src\\output\\out.java");
            FileOutputStream fw=new FileOutputStream(out);
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(fw,"UTF-8"));
            for(char c:codeCopy){
                writer.write(c);
            }
            String s=reader.readLine();
            while(s!=null){
                writer.write(s);
                s=reader.readLine();
            }
            for(Map.Entry<String,Map<Character,String>> entry:map1.entrySet()){
                String name=entry.getKey();
                Map<Character,String> router=entry.getValue();
                writer.write("\n");
                writer.write("public String "+name+"(){\n");
                writer.write("char path=chars.get(forword);\n");
                writer.write("forword++;\n");
                writer.write("switch(path){\n");
                for(Map.Entry<Character,String> en:router.entrySet()){
                    writer.write("case '"+en.getKey()+"':return "+en.getValue()+"();\n");
                }
                writer.write("default:forword=forword-2;\n");
                writer.write("}\n");
                writer.write("begin=lexBegin;\n");
                writer.write("length=forword-lexBegin+1;\n");
                writer.write("lexBegin=forword+1\n");
                writer.write("forword=lexBegin;\n");
                if(stateToRE.containsKey(name)){
                    Set<String> reName=stateToRE.get(name);
                    for(String rn:reName){
                        if(KWcode.containsKey(rn)){
                            writer.write(KWcode.get(rn));
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
            for(char part:part3){
                writer.write(part);
            }
            writer.write("}");
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

    private void readGrammar(String file){
        ArrayList<Character> content=new ArrayList<Character>();
        boolean copy=false;
        boolean RE=false;
        boolean trans=false;
        boolean inName=false;
        int part=1;
        int currentP=0;
        int spyP=0;
        String reName="";
        String reCode="";
        String reDes="";
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

        while(currentP<content.size()){
            if(part==3){
                break;
            }
            char c=content.get(currentP);
            if(copy){
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
            else if(RE){
                if(c=='%'){
                    spyP=currentP+1;
                    if(content.get(spyP)=='%'){
                        part=2;
                        RE=false;
                        currentP=currentP+2;
                        while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                            currentP++;
                        }
                        inName=true;
                        reName="";
                        continue;
                    }
                }

                if(inName){
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
                    if(c=='\n'){
                        while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                            currentP++;
                        }
                        inName=true;
                        if(reName.length()!=0){
                            re.put(reName,reDes);
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
            else if(part==2){
                if(c=='%'){
                    spyP=currentP+1;
                    if(content.get(spyP)=='%'){
                        part=3;
                        currentP=currentP+2;
                        while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                            currentP++;
                        }
                        continue;
                    }
                }
                if(inName){
                    if(c=='{'){
                        codeType=1;
                        currentP++;
                    }
                    else if(c=='"'){
                        codeType=3;
                        currentP++;
                    }
                    else if(c==' '||c=='}'||c=='"'){
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
                            trans=false;
                            while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                                currentP++;
                            }
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
            else if(c=='%'){
                spyP=currentP+1;
                if(content.get(spyP)=='{'&&part==1){
                    copy=true;
                    currentP=currentP+2;
                }
                else if(content.get(spyP)=='%'&&part==1){
                    part=2;
                    currentP=currentP+2;
                    while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                        currentP++;
                    }
                }
                else if(content.get(spyP)=='%'&&part==2){
                    part=3;
                    copy=true;
                    currentP=currentP+2;
                    while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                        currentP++;
                    }
                }
            }
            else if (c == '/') {
                if(content.get(currentP+1)=='*'&&content.get(currentP+2)=='R'&&
                        content.get(currentP+3)=='E'&&content.get(currentP+4)=='*'&&
                        content.get(currentP+5)=='/'){
                    RE=true;
                    currentP=currentP+6;
                    while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                        currentP++;
                    }
                }
            }

        }

        if(part==3){
            for(;currentP<content.size();currentP++){
                part3.add(content.get(currentP));
            }
        }

    }

    private DFA makeDFA(String reName,String input,int pf){
        Node node=makeTree(input);
        REToTree.put(reName,node);
        DFA dfa=new DFA();
        dfa.Dstates=new HashMap<String,Set<Integer>>();
        dfa.Dtran=new HashMap<String,Map<Character,Set<Integer>>>();

        Stack<Node> nstack = new Stack<Node>();
        Node current = node;
        int end=node.end;
        posToNode=new HashMap<Integer,Node>();
        for(;;){
            while(current != null){
                nstack.push(current);
                current = current.getLeft();
            }
            if(!nstack.empty()){
                current = nstack.pop();
                nullable(current);
                firstpos(current);
                lastpos(current);
                followpos(current);
                posToNode.put(current.getPostion(),current);
                current = current.getRight();
            }
            else{
                break;
            }
        }

        dfa.Dstates.put("notMarked",node.getFirst());
        int i = 0;
        while(dfa.Dstates.containsKey("notMarked")){
            Set<Integer> temp=dfa.Dstates.get("notMarked");
            String name="";
            if(i!=0){
                name=pf+"-";
            }
            if(temp.contains(end)){
                name=name+i+"-T";
            }
            else{
                name=name+i+"-N";
            }
            dfa.Dstates.put(name,temp);
            i++;
            for(char c:alphabet){
                Set<Integer> newS = new HashSet<Integer>();
                for(int num:temp){
                    if(posToNode.get(num).getIcon()==c){
                        if(!newS.isEmpty()){
                            newS.retainAll((posToNode.get(num)).getFollow());
                        }
                        else{
                            newS.addAll((posToNode.get(num)).getFollow());
                        }
                    }
                }
                if(!dfa.Dstates.containsValue(newS)){
                    dfa.Dstates.replace("notMarked",newS);
                }
                else{
                    dfa.Dstates.remove("notMarked");
                }

                if(dfa.Dtran.containsKey(name)){
                    Map<Character,Set<Integer>> t=dfa.Dtran.get(name);
                    t.put(c,newS);
                    dfa.Dtran.replace(name,t);
                }
                else{
                    Map<Character,Set<Integer>> add = new HashMap<Character,Set<Integer>>();
                    add.put(c,newS);
                    dfa.Dtran.put(name,add);
                }
            }

        }

        return dfa;
    }

    private Node makeTree(String in){
        alphabet=new HashSet<Character>();
        Node root=new Node();
        Stack<Node> stack=new Stack<Node>();
        String input=preTransform(in);
        boolean inName=false;
        String name="";
        for(char c:input.toCharArray()){
            if(c=='#'){
                root=stack.pop();
                break;
            }
            Node node=new Node();
            node.setIcon(c);
            alphabet.add(c);
            if(!atom.contains(c)){
                if(inName){
                   if(c=='}'){
                       node=REToTree.get(name);
                       stack.push(node);
                       inName=false;
                       name="";
                   }
                   else{
                       name=name+c;
                   }
                }
                else if(c=='{'){
                    inName=true;
                }
                else{
                    node.setType(NodeType.OPERAND);
                    stack.push(node);
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
                   id++;
               }
                current = current.getRight();
            }
            else{
                break;
            }
        }
        root.end=id-1;
        return root;
    }

    private String preTransform(String in){
        String input="";
        char ca[]=in.toCharArray();
        for(int i=0;i<ca.length-1;i++){
            if(isOperand(ca[i])){
                if(ca[i]=='{'){
                    while(ca[i]!='}'){
                        input=input+ca[i];
                        i++;
                    }
                }
                if(isOperand(ca[i+1])){
                    input=input+ca[i]+".";
                }
                else{
                    input=input+ca[i];
                }
            }
            else{
                input=input+ca[i];
            }
        }
        input=input+"#";
        String result="";
        Stack<Character> stack=new Stack<Character>();
        stack.push('#');
        Map<Character,Integer> priority=new HashMap<Character,Integer>();
        priority.put('#',0);
        priority.put('*',5);
        priority.put('+',5);
        priority.put('?',5);
        priority.put('|',1);
        priority.put('.',2);
        char cp[]=input.toCharArray();
        char top;
        for(int i=0;i<cp.length;i++){
            if(cp[i]=='#'){
                while(!stack.empty()){
                    top=stack.pop();
                    result=result+top;
                }
                break;
            }
            if(cp[i]=='{'){
                while(cp[i]!='}'){
                    result=result+cp[i];
                    i++;
                }
                result=result+cp[i];
            }
            else{
                if(isOperator(cp[i])){
                    top=stack.pop();
                    while(priority.get(cp[i])<=priority.get(top)){
                        result=result+top;
                        top=stack.pop();
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

    private boolean isOperand(char c){
        switch (c){
            case '*':
            case '+':
            case '?':
            case '|':
            case ')':return false;
            default:return true;
        }
    }

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

    private void nullable(Node n){
        nullable(n.getLeft());
        nullable(n.getRight());
        if(n.isLeaf()){
            if(n.isNull){
                n.setNullable(true);
            }
            n.setNullable(false);
        }
        if(n.getType()==NodeType.OR){
            n.setNullable(n.getLeft().isNullable()||n.getRight().isNullable());
        }
        if(n.getType()==NodeType.CAT){
            n.setNullable(n.getLeft().isNullable()&&n.getRight().isNullable());
        }
        if(n.getType()==NodeType.STAR){
            n.setNullable(true);
        }
        n.setNullable(true);
    }

    private void firstpos(Node n){
        firstpos(n.getLeft());
        firstpos(n.getRight());
        Set<Integer> result=new HashSet();
        if(n.isLeaf()){
            if(n.isNull){
                n.setFirst(null);
                return;
            }
            result.add(n.getPostion());
        }
        if(n.getType()==NodeType.OR){
            result=n.getLeft().getFirst();
            result.retainAll(n.getRight().getFirst());
        }
        if(n.getType()==NodeType.CAT){
            if(n.getLeft().isNullable()){
                result=n.getLeft().getFirst();
                result.retainAll(n.getRight().getFirst());
            }
            else{
                result=n.getLeft().getFirst();
            }
        }
        if(n.getType()==NodeType.STAR){
            result=n.getLeft().getFirst();
        }
        n.setFirst(result);
    }

    private void lastpos(Node n){
        Set<Integer> result=new HashSet();
        lastpos(n.getLeft());
        lastpos(n.getRight());
        if(n.isLeaf()){
            if(n.isNull){
                n.setLast(null);
                return;
            }
            result.add(n.getPostion());
        }
        if(n.getType()==NodeType.OR){
            result=n.getLeft().getLast();
            result.retainAll(n.getRight().getLast());
        }
        if(n.getType()==NodeType.CAT){
            if(n.getRight().isNullable()){
                result=n.getLeft().getLast();
                result.retainAll(n.getRight().getLast());
            }
            else{
                result=n.getRight().getLast();
            }
        }
        if(n.getType()==NodeType.STAR){
            result=n.getLeft().getLast();
        }
        n.setLast(result);
    }

    private void followpos(Node n){
        firstpos(n.getLeft());
        firstpos(n.getRight());
        lastpos(n.getLeft());
        lastpos(n.getRight());
        if(n.getType()==NodeType.CAT){
            Set<Integer> toAdd = n.getRight().getFirst();
            Set<Integer> host = n.getLeft().getLast();
            for(int i:host){
                Node no=posToNode.get(i);
                Set<Integer> sss=no.getFollow();
                sss.retainAll(toAdd);
                no.setFollow(sss);
                posToNode.replace(i,no);
            }
            return;
        }
        if(n.getType()==NodeType.STAR){
            Set<Integer> toAdd=n.getFirst();
            Set<Integer> host=n.getLast();
            for(int i:host){
                Node no=posToNode.get(i);
                Set<Integer> sss=no.getFollow();
                sss.retainAll(toAdd);
                no.setFollow(sss);
                posToNode.replace(i,no);
            }
        }
    }
}