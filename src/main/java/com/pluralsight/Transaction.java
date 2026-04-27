package com.pluralsight;

public class Transaction {
    private String date;
    private String time;
    private String description;
    private String vendor;
    private double price;

    public Transaction(String date, String time, String description, String vendor, double price) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.price = price;
    }

    public void setDate(String date) {this.date = date;}
    public void setDescription(String description) {this.description = description;}
    public void setPrice(double price) {this.price = price;}
    public void setTime(String time) {this.time = time;}
    public void setVendor(String vendor) {this.vendor = vendor;}

    public String getDate() {return date;}
    public String getDescription() {return description;}
    public double getPrice() {return price;}
    public String getTime() {return time;}
    public String getVendor() {return vendor;}
}
