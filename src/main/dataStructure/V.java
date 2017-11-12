package main.dataStructure;

import main.syntaxAnalyzer.VType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class V{

    private String name;
    private VType type;
    private Set<String> first;
    private Set<String> follow;
    private Set<Production> pros;
    private Set<String> prosInString;
    private boolean isEpsilon;
    private boolean haveEpsilon;

    public V(String name, VType type) {
        this.name = name;
        this.type = type;
        isEpsilon=false;
        haveEpsilon=false;
        first=new HashSet<String>();
        follow=new HashSet<String>();
        pros=new HashSet<Production>();
        prosInString=new HashSet<String>();
    }

    public boolean isHaveEpsilon() {
        return haveEpsilon;
    }

    public void setHaveEpsilon(boolean haveEpsilon) {
        this.haveEpsilon = haveEpsilon;
    }


    public Set<Production> getPros() {
        return pros;
    }

    public void setPros(Set pros) {
        this.pros = pros;
    }

    public boolean isEpsilon() {
        return isEpsilon;
    }

    public void setEpsilon(boolean epsilon) {
        isEpsilon = epsilon;
    }


    public void setFirst(Set first) {
        this.first = first;
    }

    public void setFollow(Set follow) {
        this.follow = follow;
    }

    public Set<String> getFirst() {
        return first;
    }

    public Set<String> getFollow() {
        return follow;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(VType type) {
        this.type = type;
    }


    public String getName() {
        return name;
    }


    public VType getType() {
        return type;
    }

    public Set<String> getProsInString() {
        if(prosInString.size()<=0){
            for(Production p:pros){
                ArrayList<V> list=p.getList();
                for(V v:list){
                    prosInString.add(v.getName());
                }
            }
        }
        return prosInString;
    }


}
