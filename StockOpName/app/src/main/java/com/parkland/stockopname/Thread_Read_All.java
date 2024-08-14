package com.parkland.stockopname;

import java.util.ArrayList;
import java.util.HashMap;

class Thread_Read_All extends Thread {
    ArrayList<String> result;
    String fac;
    String st_cd;
    String id;
    String grade;
    String stockno;

    public Thread_Read_All(String fac, String st_cd, String id, String grade, String stockno) {
        this.fac = fac;
        this.st_cd = st_cd;
        this.id = id;
        this.grade = grade;
        this.stockno = stockno;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Read_All read = new DB_Read_All();
            result = read.readAll(fac,st_cd,id,grade,stockno);
            notify();
        }
    }
}