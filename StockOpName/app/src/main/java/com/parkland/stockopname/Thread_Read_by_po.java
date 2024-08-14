package com.parkland.stockopname;

import java.util.ArrayList;
import java.util.HashMap;

class Thread_Read_by_po extends Thread {
    ArrayList<String> result;
    String o_no;
    String fac;
    String stockno;

    public Thread_Read_by_po(String o_no, String fac, String stockno) {
        this.o_no = o_no;
        this.fac = fac;
        this.stockno = stockno;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Read_by_po check = new DB_Read_by_po();
            result = check.getCarton(o_no,fac,stockno);
            notify();
        }
    }
}