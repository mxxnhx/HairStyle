package com.example.badasaza.gohaesungsamodel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Badasaza on 2015-11-23.
 */
public class ItemModel implements Serializable {
    public List<String> imgFiles;
    public String dateTime;
    public boolean inApp;

    public ItemModel(List<String> imgFiles, String dateTime, boolean inApp){
        this.imgFiles = imgFiles;
        this.dateTime = dateTime;
        this.inApp = inApp;
    }
}