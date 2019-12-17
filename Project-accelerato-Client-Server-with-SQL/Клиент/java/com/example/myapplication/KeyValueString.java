package com.example.myapplication;

import java.util.ArrayList;

public class KeyValueString {
    class KeyValue{
        String key;
        String value;

        KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    ArrayList<KeyValue> keyValues = new ArrayList<>();

    synchronized void add(String key, String value){
        if (!key.equals("empty"))
            keyValues.add(new KeyValue(key,value));
    }

    synchronized String get(String key){
        String value = null;
        for (KeyValue o : keyValues){
            if (o.key.equals(key)){
                value = o.value;
                keyValues.remove(o);
                break;
            }
        }
        return value;
    }
}
