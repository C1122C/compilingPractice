package main.dataStructure;

import java.util.ArrayList;

public class LR1Item {
    private V left;
    private ArrayList<V> right;
    private int pointPos;
    private ArrayList<String>  mark;

    public LR1Item(V l,int p){
        mark=new ArrayList<String>();
        right=new ArrayList<V>();
        left=l;
        pointPos=p;
    }

    public void addRight(V v){
        right.add(v);
    }

    public void addMark(String v){
        mark.add(v);
    }

    public ArrayList<String> getMark() {
        return mark;
    }

    public void setMark(ArrayList<String> mark) {
        this.mark = mark;
    }

    public V getLeft() {
        return left;
    }

    public void setLeft(V left) {
        this.left = left;
    }

    public ArrayList<V> getRight() {
        return right;
    }

    public void setRight(ArrayList<V> right) {
        this.right = right;
    }

    public int getPointPos() {
        return pointPos;
    }

    public void setPointPos(int pointPos) {
        this.pointPos = pointPos;
    }

    public String toString(){
        String result=left.getName();
        result=result+"->";
        for(int i=0;i<right.size();i++){
            if(i==pointPos){
                result=result+".";
            }
            result=result+right.get(i).getName();
        }
        if(pointPos==right.size()){
            result=result+".";
        }
        result=result+",";
        for(int i=0;i<mark.size();i++){
            if(i>0){
                result=result+"|";
            }
            result=result+mark.get(i);
        }
        return result;
    }

    public String getString(){
        String result=left.getName();
        result=result+"->";
        for(int i=0;i<right.size();i++){
            result=result+right.get(i).getName();
        }
        return result;
    }

    public boolean equals(LR1Item lr1Item){
        if(this.toString().equals(lr1Item.toString())){
            return true;
        }
        return false;
    }
}
