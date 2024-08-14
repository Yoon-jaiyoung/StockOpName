package com.parkland.stockopname;

class Thread_Insert_Barcode_BTM extends Thread {
    String stockno, carton,  fac,  id,  grade, layout;

    public Thread_Insert_Barcode_BTM(String stockno, String carton, String fac, String id, String layout) {
        this.stockno = stockno;
        this.carton = carton;
        this.fac = fac;
        this.id = id;
        this.layout = layout;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Insert_Barcode_BTM check = new DB_Insert_Barcode_BTM();
            check.insertBarcode(stockno, carton,  fac,  id, layout);
            notify();
        }
    }
}