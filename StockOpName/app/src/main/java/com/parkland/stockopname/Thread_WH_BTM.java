package com.parkland.stockopname;

import java.util.ArrayList;

class Thread_WH_BTM extends Thread {
    ArrayList<String> result;
    String fac;

    public Thread_WH_BTM(String fac) {
        this.fac = fac;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_WH_BTM layout = new DB_WH_BTM();
            result = layout.getLayout(fac);
            notify();
        }
    }
}