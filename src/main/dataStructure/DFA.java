package main.dataStructure;


import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class  DFA{
    public Map<String,Set<Integer>> Dstates;
    public Map<String,Map<Character,Set<Integer>>> Dtran;
    public Map<String,Map<Character,String>> UsableDtran;

    public DFA(){
        Dstates=new HashMap<String,Set<Integer>>();
        Dtran=new HashMap<String,Map<Character,Set<Integer>>>();
        UsableDtran=new HashMap<String,Map<Character,String>>();
    }

    public void trans(){
        String name="";
        for(Map.Entry<String,Map<Character,Set<Integer>>> entry:Dtran.entrySet()){
            Map<Character,Set<Integer>> map1=entry.getValue();
            Map<Character,String> newMap=new HashMap<Character,String>();
            for(Map.Entry<Character,Set<Integer>> en:map1.entrySet()){
                Set<Integer> set=en.getValue();
                String name1="";
                for(Map.Entry<String,Set<Integer>> s:Dstates.entrySet()){
                    if(s.getValue().equals(set)){
                        name1=s.getKey();
                        newMap.put(en.getKey(),name1);
                        break;
                    }
                }
            }
            UsableDtran.put(entry.getKey(),newMap);
        }
    }
}
