package com.parkland.stockopname;

class Thread_Upload extends Thread {
    String result;
    String label_id;
    double act_qty;
    String time;
    String fac;
    String timeUpdate;

    public Thread_Upload(String fac, String label_id, double act_qty, String time, String timeUpdate) {
        this.fac = fac;
        this.label_id = label_id;
        this.act_qty = act_qty;
        this.time = time;
        this.timeUpdate = timeUpdate;
    }

    @Override
    public void run() {
        synchronized (this) {
            DB_Upload upload = new DB_Upload();
            result = upload.uploadServer(fac,label_id,act_qty,time,timeUpdate);
            notify();
        }
    }
}