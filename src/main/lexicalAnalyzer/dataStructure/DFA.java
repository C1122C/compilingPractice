package main;

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
        Dtran-=new HashMap<String,Map<Character,Set<Integer>>>();
        UsableDtran=new HashMap<String,Map<Character,String>>();
    }

    public void trans(){
        String name="";
        for(Entry entry:Dtran.entrySet()){
            Map map1=entry.value();
            Map newMap=new HashMap<Character,String>();
            for(Entry en:map1.entrySet()){
                Set set=en.value();
                String name="";
                for(Entry s:Dstates.entrySet()){
                    if(s.value().equals(set)){
                        name=s.key();
                        newMap.put(en.key(),name);
                        break;
                    }
                }
            }
            UsableDtran.put(entry.key(),newMap);
        }
    }
}
