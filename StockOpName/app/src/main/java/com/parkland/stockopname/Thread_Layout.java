package com.parkland.stockopname;

import java.util.ArrayList;

class Thread_Layout extends Thread {
    ArrayList<String> result;
    String fac;

    public Thread_Layout(String fac) {
        this.fac = fac;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Layout layout = new DB_Layout();
            result = layout.getLayout(fac);
            notify();
        }
    }
}