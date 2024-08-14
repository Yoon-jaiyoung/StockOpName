package com.parkland.stockopname;

import java.util.ArrayList;

class Thread_Stockno_BTM extends Thread {
    ArrayList<String> result;
    String fac;

    public Thread_Stockno_BTM(String fac) {
        this.fac = fac;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Stockno_BTM stockno = new DB_Stockno_BTM();
            result = stockno.getStockno(fac);
            notify();
        }
    }
}