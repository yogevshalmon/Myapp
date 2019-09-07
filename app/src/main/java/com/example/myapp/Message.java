package com.example.myapp;

public class Message {
    public String message;
    public String date;

    @Override
    public String toString() {
        return message;
    }

    public Message(){
        message="";
        date="";
    }

    public Message(String mes, String date){
        this.message=mes;
        this.date=date;
    }
}
