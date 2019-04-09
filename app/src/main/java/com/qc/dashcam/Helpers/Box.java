package com.qc.dashcam.Helpers;

import java.util.ArrayList;

public class Box {
    // coordinates
    public float top;
    public float left;
    public float bottom;
    public float right;
    // class, and chance the content belongs to the type
    public int type_id;
    public float type_score;
    public String type_name;

    // duplicates into another Box
    void copyTo(Box b) {
        b.top = top;
        b.left = left;
        b.bottom = bottom;
        b.right = right;
        b.type_id = type_id;
        b.type_name = type_name;
        b.type_score = type_score;
    }

    // convenience function
    public static ArrayList<Box> createBoxes(int count) {
        final ArrayList<Box> boxes = new ArrayList<>();
        for (int i = 0; i < count; ++i)
            boxes.add(new Box());
        return boxes;
    }
}
