package main.dataStructure;

import java.util.ArrayList;

public class LR1Item {
    private V left;
    private ArrayList<V> right;
    private int pointPos;
    private String mark;

    public LR1Item(V l, ArrayList<V> r, int p, String m){
        left=l;
        right=r;
        pointPos=p;
        mark=m;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
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
        result=result+",";
        result=result+mark;
        return result;
    }
}
