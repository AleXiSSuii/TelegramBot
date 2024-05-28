package com.telegramBot.model.enums;

public enum Source {
    RIA,RBC;

    public static String getStringSource(Source source){
        if(source == RIA){
            return "РИА Новости";
        }else {
            return "РБК Новости";
        }
    }
}
