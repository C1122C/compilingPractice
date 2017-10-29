package main.dataStructure;

/**
 * 确定的有限自动机实体类
 */

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class  DFA{
    /*状态集*/
    public Map<String,Set<Integer>> Dstates;
    /*状态转换：对应位置集*/
    public Map<String,Map<Character,Set<Integer>>> Dtran;
    /*状态转换：对应状态*/
    public Map<String,Map<Character,String>> UsableDtran;

    public DFA(){
        Dstates=new HashMap<String,Set<Integer>>();
        Dtran=new HashMap<String,Map<Character,Set<Integer>>>();
        UsableDtran=new HashMap<String,Map<Character,String>>();
    }

    /**
     * 自动构建状态转换集
     */
    public void trans(){
        for(Map.Entry<String,Map<Character,Set<Integer>>> entry:Dtran.entrySet()){
            Map<Character,Set<Integer>> map1=entry.getValue();
            Map<Character,String> newMap=new HashMap<Character,String>();
            for(Map.Entry<Character,Set<Integer>> en:map1.entrySet()){
                Set<Integer> set=en.getValue();
                String name="";
                for(Map.Entry<String,Set<Integer>> s:Dstates.entrySet()){
                    if(s.getValue().equals(set)){
                        name=s.getKey();
                        newMap.put(en.getKey(),name);
                        break;
                    }
                }
            }
            UsableDtran.put(entry.getKey(),newMap);
        }
    }
}
