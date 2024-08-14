package com.parkland.stockopname;

import java.util.HashMap;

class Thread_Insert_Carton extends Thread {
    String stockno, carton,  fac,  id,  grade, layout;

    public Thread_Insert_Carton(String stockno, String carton, String fac, String id, String grade, String layout) {
        this.stockno = stockno;
        this.carton = carton;
        this.fac = fac;
        this.id = id;
        this.grade = grade;
        this.layout = layout;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Insert_Carton check = new DB_Insert_Carton();
            check.insertCarton(stockno, carton,  fac,  id,  grade, layout);
            notify();
        }
    }
}