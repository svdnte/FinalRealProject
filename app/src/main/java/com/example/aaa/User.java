package com.example.aaa;

public class User {
    private long id, date;
    private int anon, sum, meth;
    private String surname, name, otch, info;

    User(long id, int anon,String surname, String name, String otch, long date, int sum, String info, int meth) {
        this.id = id;
        this.anon = anon;
        this.surname = surname;
        this.name = name;
        this.otch = otch;
        this.date = date;
        this.sum = sum;
        this.info = info;
        this.meth = meth;
    }

    public long getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public int getAnon() {
        return anon;
    }

    public int getSum() {
        return sum;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public String getOtch() {
        return otch;
    }

    public String getInfo() {
        return info;
    }

    public int getMeth() {
        return meth;
    }
}
