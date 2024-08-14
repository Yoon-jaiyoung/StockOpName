package com.parkland.stockopname;

import java.util.ArrayList;

class Thread_Stockno extends Thread {
    ArrayList<String> result;
    String fac;

    public Thread_Stockno(String fac) {
        this.fac = fac;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Stockno stockno = new DB_Stockno();
            result = stockno.getStockno(fac);
            notify();
        }
    }
}