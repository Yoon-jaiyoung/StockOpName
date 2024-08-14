package com.parkland.stockopname;

import java.util.ArrayList;

class Thread_Read_All_BTM extends Thread {
    ArrayList<String> result;
    String fac;
    String st_cd;
    String id;
    String stockno;

    public Thread_Read_All_BTM(String fac, String st_cd, String id, String stockno) {
        this.fac = fac;
        this.st_cd = st_cd;
        this.id = id;
        this.stockno = stockno;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Read_All_BTM read = new DB_Read_All_BTM();
            result = read.readAll(fac,st_cd,id,stockno);
            notify();
        }
    }
}