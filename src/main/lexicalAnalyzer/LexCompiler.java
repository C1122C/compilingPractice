package main;

/*本程序支持的正则表达符号包括：*、+、？、（）、|、{}。*/
import dataStructure.Node;
import dataStructure.DFA;
import NodeType;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Queue;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class LexCompiler{

    private Map posToNode;
    private Set atom;
    private Map re;
    private Map REcode;
    private Map KWcode;
    private Map OPcode;
    private int codeType;
    private Map REToTree;
    private Map REToDFA;
    private ArrayList<Character> part3;
    private ArrayList<Character> codeCopy;
    private Set alphabet;
    private State startPoint;
    private Map map1;
    private Map map2;
    private Map map3;
    private Queue queue;
    private int finalID;
    private Map stateToRE;

    public LexCompiler(){
        atom=new HashSet<char>();
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
        queue=new Queue();
        finalID=0;
        stateToRE=new HashMap<String,Set<String>>();
        codeCopy=new ArrayList<Character>();
    }

    public void getLex(File file){
        readGrammer(file);
        for(String s:KWcode.keySet()){
            re.put(s," "+s+" ");
        }
        for(String s:OPcode.keySet()){
            re.put(s,s);
        }
        int i=0;
        for(Entry<String,String> entry : re.entrySet){
            DFA dfa=makeDFA(entry.key(),entry.value(),i);
            i++;
            REToDFA.put(entry.key(),dfa);
        }
        DFAmerge();
        String s=writeFile();
        return s;
    }

    private void DFAmerge(){
        for(Entry entry:REToDFA){
            String rere=entry.key();
            Map toadd=entry.value().UsableDtran;
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
        for(Entry entry:map3){
            String originalS=entry.key();
            String r=entry.value();
            if(map2.keySet().contains(originalS)){
                String newSt=map2.get(originalS);
                if(!stateToRE.keySet().contains(newSt)){
                    Set ss=new Set<String>();
                    for(Entry en:map2){
                        if(en.value.equals(newSt)){
                            ss.add(map3.get(en.key()));
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
        for(Entry entry:map2){
            if(entry.value().equals(newState)){
                originalS.add(entry.key());
            }
        }
        for(String s:originalS){
            Map temp=map1.get(s);
            for(Entry entry:temp){
                path.add(entry.key());
                destination.add(entry.value());
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
        for(Entry entry:map1){
            Map temp=entry.value();
            for(Entry en:temp){
                if(originalS.contains(en.value())){
                    temp.replace(en.key(),en.value(),newState);
                    map1.replace(entry.key(),entry.value(),temp);
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
            String s="";
            while(s=reader.readLine()){
                writer.write(s);
            }
            for(Entry entry:map1){
                String name=entry.key();
                Map router=entry.value();
                writer.write("\n");
                writer.write("public String "+name+"(){\n");
                writer.write("char path=chars.get(forword);\n");
                writer.write("forword++;\n");
                writer.write("switch(path){\n");
                for(Entry en:router){
                    writer.write("case '"+en.key()+"':return "+en.value()+"();\n");
                }
                writer.write("default:forword=forword-2;\n");
                writer.write("}\n");
                writer.write("begin=lexBegin;\n");
                writer.write("length=forword-lexBegin+1;\n");
                writer.write("lexBegin=forword+1\n");
                writer.write("forword=lexBegin;\n");
                if(stateToRE.containsKey(name)){
                    String reName=stateToRE.get(name);
                    if(KWcode.containsKey(reName)){
                        writer.write(KWcode.get(reName));
                    }
                    else if(REcode.containsKey(reName)){
                        writer.write(REcode.get(reName));
                    }
                    else{
                        writer.write(OPcode.get(reName));
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
            s="";
            while(s=reader.readLine()){
                writer.write(s);
            }
            writer.close();
        }
    }

    private void readGrammar(File file){
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
            while(char c=reader.read()){
                content.add(c);
            }
            reader.close();
        }catch (Exception e){
            System.out.println("not find file");
        }

        while(currentP<content.length()){
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
                                    case 1:RECode.put(reName,reCode);break;
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
                    current=current+2;
                    while(content.get(currentP)==' '||content.get(currentP)=='\n'){
                        currentP++;
                    }
                }
                else if(content.get(spyP)=='%'&&part==2){
                    part=3;
                    copy=true;
                    current=current+2;
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
            for(;currentP<content.length();currentP++){
                part3.add(content.get(currentP));
            }
        }

    }

    private DFA makeDFA(String reName,String input,int pf){
        Node node=makeTree(input);
        REToTree.put(reName,node);
        dfa=new DFA();
        dfa.Dstates=new HashMap<String,Set<Integer>>();
        dfa.Dtran=new HashMap<String,Map<Character,Set<Integer>>>();

        Stack<Node> nstack = new Stack<Node>();
        Node current = root;
        int end=root.end;
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
                posToNode.put(current.getPosition(),current);
                current = current.getRight();
            }
            else{
                break;
            }
        }

        dfa.Dstates.put("notMarked",root.getFirst());
        int i = 0;
        while(dfa.Dstates.containsKey("notMarked")){
            Set temp=dfa.Dstates.get("notMarked");
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
                Set newS = new HashSet<Integer>();
                for(int num:temp){
                    if(posToNode.get(num).getIcon()==c){
                        if(!newS.empty){
                            newS.retainAll(followpos(posToNode.get(num)));
                        }
                        else{
                            newS.addAll(followpos(posToNode.get(num)));
                        }
                    }
                }
                if(!dfa.Dstates.containsValue(newS)){
                    dfa.Dstates.replace("notMarked",newS);
                }
                else{
                    dfa.Dstates.remove("notMarked");
                }

                if(dfa.Dtran.contaisKey(name)){
                    dfa.Dstates.get(name).put(c,newS);
                }
                else{
                    Map add = new HashMap<Character,Set<Integer>>();
                    add.put(c,newS);
                    dfa.Dstates.put(name,add);
                }
            }

        }

        return dfa;
    }

    private Node makeTree(String in){
        alphabet=new HashSet<Character>();
        Node root;
        Stack stack=new Stack();
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
                    node.setType(NodeType.OPREAND);
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
               if(current.getRight==null&&current.getLeft==null){
                   current.setPosition(id);
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
        Stack stack=new Stack();
        stack.push('#');
        Map priority=new HashMap<Character,Integer>();
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
        if(n.isLeaf()){
            if(n.getIcon()==null){
                n.setNullable(true);
            }
            n.setNullable(false);
        }
        if(n.getType()==NodeType.OR){
            n.setNullable(nullable(n.getLeft())||nullable(n.getRight()));
        }
        if(n.getType()==NodeType.CAT){
            n.setNullable(nullable(n.getLeft()&&nullable(n.getRight())));
        }
        if(n.getType()==NodeType.STAR){
            n.setNullable(true);
        }
        n.setNullable(true);
    }

    private void firstpos(Node n){
        Set<Integer> result=new HashSet();
        if(n.isLeaf()){
            if(n.getIcon()==null){
                n.setFirst(null);
                return;
            }
            result.add(n.getPos());
        }
        if(n.getType()==NodeType.OR){
            result=firstpos(n.getLeft());
            result.retainAll(n.getRight());
        }
        if(n.getType()==NodeType.CAT){
            if nullable(n.getLeft()){
                result=firstpos(n.getLeft());
                result.retainAll(n.getRight());
            }
            else{
                result=firstpos(n.getLeft());
            }
        }
        if(n.getType()==NodeType.STAR){
            result=firstpos(n.getLeft());
        }
        n.setFirst(result);
    }

    private void lastpos(Node n){
        Set<Integer> result=new HashSet();
        if(n.isLeaf()){
            if(n.getIcon()==null){
                n.setLast(null);
                return;
            }
            result.add(n.getPos());
        }
        if(n.getType()==NodeType.OR){
            result=lastpos(n.left());
            result.retainAll(n.right());
        }
        if(n.getType()==NodeType.CAT){
            if nullable(n.right()){
                result=lastpos(n.left());
                result.retainAll(n.right());
            }
            else{
                result=lastpos(n.right());
            }
        }
        if(n.getType()==NodeType.STAR){
            result=lastpos(n.left());
        }
        n.setLast(result);
    }

    private void followpos(Node n){
        if(n.getType()==NodeType.CAT){
            Set toAdd = firstpos(n.getRight());
            Set host = lastpos(n.getLeft());
            for(int i:host){
                posToNode.get(i).setFollow(posToNode.get(i).getFollow.retainAll(toAdd));
            }
            return;
        }
        if(n.getType()==NodeType.STAR){
            Set toAdd=firstpos(n);
            Set host=lastpos(n);
            for(int i:host){
                posToNode.get(i).setFollow(posToNode.get(i).getFollow.retainAll(toAdd));
            }
        }
    }
}