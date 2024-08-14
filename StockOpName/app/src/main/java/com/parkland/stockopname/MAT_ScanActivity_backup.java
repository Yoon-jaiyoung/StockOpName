package com.parkland.stockopname;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MAT_ScanActivity_backup extends Activity {
    String intent_factory;
    String intent_id;
    String intent_location;
    EditText etBarcode;
    String nowDate;
    String todayDate;
    String DB_BarcodeString,DB_mat,DB_spot,DB_trolley,DB_in,DB_out,DB_stock;

    public String getTimeSave() {
        return timeSave;
    }

    public void setTimeSave(String timeSave) {
        this.timeSave = timeSave;
    }

    String timeSave;

    public String getUploadCheck() {
        return uploadCheck;
    }

    public void setUploadCheck(String uploadCheck) {
        this.uploadCheck = uploadCheck;
    }

    String uploadCheck;
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mat_activity_scan);
        setCount(0);

        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf_date = new SimpleDateFormat("MMdd");
        nowDate = sdf_date.format(date);
        SimpleDateFormat sdf_date2 = new SimpleDateFormat("yyyy-MM-dd");
        todayDate = sdf_date2.format(date);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        intent_factory = intent.getStringExtra("intent_factory");
        intent_id = intent.getStringExtra("intent_id");
        intent_location = intent.getStringExtra("intent_location");
        // Capture the layout's TextView and set the string as its text
        TextView tvFac = findViewById(R.id.tvFac);
        TextView tvLoc = findViewById(R.id.tvLoc);
        TextView tvID = findViewById(R.id.tvID);
        TextView tvDate = findViewById(R.id.tvDate);
        tvFac.setText(intent_factory);
        tvLoc.setText(intent_location);
        tvID.setText(intent_id);
        tvDate.setText(nowDate);
        etBarcode = findViewById(R.id.etBarcode);
        try {
            readTxt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        etBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(getCount()>999){
                        Toast.makeText(getApplicationContext(),"TIDAK BISA LEBIH DARI 1000, BIKIN FILE BARU",Toast.LENGTH_LONG).show();
                    }
                    else {
                        setCount(getCount() + 1);
                        DB_BarcodeString = String.valueOf(etBarcode.getText());
                        final Handler handler = new Handler();  //Optional. Define as a variable in your activity.

                        Thread_Upload b = new Thread_Upload(intent_factory, DB_BarcodeString, 0, "", "");
                        b.start();

                        synchronized (b) {
                            try {
                                System.out.println("Waiting for b to complete...");
                                b.wait(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                b.result = "0";
                                setUploadCheck("X");
                            }

                            if (b.result.split(",")[0].equals("0")) {
                                System.out.println(b.result + " ---------------------------------------------");
                                DB_stock = b.result.split(",")[1];
                                setTimeSave(b.result.split(",")[2]);
                                DB_mat = b.result.split(",")[3];
                                DB_spot = b.result.split(",")[4];
                                DB_trolley = b.result.split(",")[5];
                                DB_in = b.result.split(",")[6];
                                DB_out = b.result.split(",")[7];
                                setUploadCheck("X");
                            } else {
                                System.out.println("-------" + b.result.split(",")[1] + "-------");
                                DB_stock = b.result.split(",")[1];
                                setTimeSave(b.result.split(",")[2]);
                                DB_mat = b.result.split(",")[3];
                                DB_spot = b.result.split(",")[4];
                                DB_trolley = b.result.split(",")[5];
                                DB_in = b.result.split(",")[6];
                                DB_out = b.result.split(",")[7];
                                setUploadCheck("O");

                            }
                        }


                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String cnt = String.format("%03d", getCount());
                                    saveInTxt(intent_factory, intent_id, intent_location, DB_BarcodeString, cnt, DB_stock, DB_stock, getUploadCheck(), getTimeSave(), DB_mat, DB_spot, DB_trolley, DB_in, DB_out);
                                    //readTxt();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // your code here

                                handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                                {
                                    public void run() {
                                        try {
                                            readTxt();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        //Code to update the UI
                                    }
                                });


                            }
                        };

                        Thread t = new Thread(r);
                        t.start();
                    /*
                    try {
                        readTxt();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    */
                    }
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    etBarcode.setText("");
                    etBarcode.requestFocus();
                    return true;
                }
                return false;
            }
        });

    }

    public void saveInTxt (String fac,String ID, String loc,String barcode,String scount,String stock,String actual_stock,String check,String time,String mat, String spot, String trolley, String in, String out) throws IOException {
        File saveFile = new File(getFilesDir().getPath() + "/mat-stockopname"); // 저장 경로

        stock = String.format("%.2f",Double.parseDouble(stock.replace(',','.')));
        actual_stock = String.format("%.2f", Double.parseDouble(actual_stock.replace(',','.')));
        in = String.format("%.2f", Double.parseDouble(in.replace(',','.')));
        out = String.format("%.2f", Double.parseDouble(out.replace(',','.')));

        File txtFile = new File(saveFile.getAbsolutePath()+"/"+intent_factory+"_"+intent_location+"_"+intent_id+"_"+nowDate+".txt");
// 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }

        if(!txtFile.exists()){
            txtFile.createNewFile();
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(txtFile, true));
            buf.append(scount+"/"); //NO count 0
            buf.append(fac+"/"); //사업부 1
            buf.append(ID+"/"); //사번 2
            buf.append(loc+"/"); //위치 3
            buf.append(barcode+"/"); // 바코드 4
            buf.append(time+"/"); // 날짜 쓰기 5

            buf.append(stock.replace(',','.')+"/"); // 6
            buf.append(actual_stock.replace(',','.')+"/");  //7
            buf.append(check+"/"); //8
            buf.append(mat+"/"); //9
            buf.append(spot+"/"); //10
            buf.append(trolley+"/"); //11
            buf.append(in.replace(',','.')+"/"); //12
            buf.append(out.replace(',','.')); //13
            buf.newLine(); // 개행
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readTxt() throws IOException {
        String line = null; // 한줄씩 읽기
        File saveFile = new File(getFilesDir().getPath() + "/mat-stockopname"); // 저장 경로
        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf_date = new SimpleDateFormat("MMdd");
        String nowDate = sdf_date.format(date);
        File txtFile = new File(saveFile.getAbsolutePath()+"/"+intent_factory+"_"+intent_location+"_"+intent_id+"_"+nowDate+".txt");
        // 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        if(!txtFile.exists()){
            txtFile.createNewFile();
        }
        try {


            BufferedReader buf = new BufferedReader(new FileReader(txtFile));
            ArrayList<String> data = new ArrayList<String>();

            ArrayList<String> data_fac = new ArrayList<String>();
            ArrayList<String> data_id = new ArrayList<String>();
            ArrayList<String> data_loc = new ArrayList<String>();
            ArrayList<String> data_barcode = new ArrayList<String>();
            ArrayList<String> data_time = new ArrayList<String>();
            ArrayList<String> data_count = new ArrayList<String>();
            ArrayList<String> data_stock = new ArrayList<String>();
            ArrayList<String> data_actualstock = new ArrayList<String>();
            ArrayList<String> data_uploadcheck = new ArrayList<String>();
            ArrayList<String> data_mat = new ArrayList<String>();
            ArrayList<String> data_spot = new ArrayList<String>();
            ArrayList<String> data_trolley = new ArrayList<String>();
            ArrayList<String> data_in = new ArrayList<String>();
            ArrayList<String> data_out = new ArrayList<String>();

            while((line=buf.readLine())!=null){
                data.add(line);
                /*
                String[] txt_data = line.split(",");
                data_fac.add(txt_data[0]);
                data_id.add(txt_data[1]);
                data_loc.add(txt_data[2]);
                data_barcode.add(txt_data[3]);
                String[] txt_data_time = txt_data[4].split("\\s+");
                data_time.add(txt_data_time[1]);
                data_count.add(txt_data[5]);
                setCount(Math.max(getCount(),Integer.valueOf(txt_data[5])));
                data_stock.add(txt_data[6]);
                data_actualstock.add(txt_data[7]);
                data_uploadcheck.add(txt_data[8]);

                 */
            }
            if(data.isEmpty()){
            }
            else {
                Collections.sort(data, Collections.reverseOrder());
                for (int i = 0; i < data.size(); i++) {
                    String[] txt_data = data.get(i).split("/");
                    data_count.add(txt_data[0]);
                    data_fac.add(txt_data[1]);
                    data_id.add(txt_data[2]);
                    data_loc.add(txt_data[3]);
                    data_barcode.add(txt_data[4]);
                    String[] txt_data_time = txt_data[5].split("\\s+");
                    data_time.add(txt_data_time[1]);

                    setCount(Math.max(getCount(), Integer.valueOf(txt_data[0])));
                    data_stock.add(txt_data[6]);
                    data_actualstock.add(txt_data[7]);
                    data_uploadcheck.add(txt_data[8]);
                    data_mat.add(txt_data[9]);
                    data_spot.add(txt_data[10]);
                    data_trolley.add(txt_data[11]);
                    data_in.add(txt_data[12]);
                    data_out.add(txt_data[13]);
                }
            }

            /*
            Collections.reverse(data_fac);
            Collections.reverse(data_id);
            Collections.reverse(data_loc);
            Collections.reverse(data_barcode);
            Collections.reverse(data_time);
            Collections.reverse(data_count);
            Collections.reverse(data_stock);
            Collections.reverse(data_actualstock);

             */
            appendRow(data_count,data_barcode,data_time,data_stock,data_actualstock,data_uploadcheck,data_mat,data_spot,data_trolley,data_in,data_out);//작업 필요
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteLineTxt(String number) throws IOException {
        try {
            String msg;


            //결과 출력파일
            File saveFile = new File(getFilesDir().getPath() + "/mat-stockopname"); // 저장 경로
            File txtFile = new File(saveFile.getAbsolutePath()+"/"+intent_factory+"_"+intent_location+"_"+intent_id+"_"+nowDate+".txt");

            //소스 파일읽기
            BufferedReader br = new BufferedReader(new FileReader(txtFile));

            ArrayList<String> data = new ArrayList<String>();
            //파일생성
            if(!saveFile.exists()){ // 폴더 없을 경우
                saveFile.mkdir(); // 폴더 생성
            }
            if(!txtFile.exists()){
                txtFile.createNewFile();
            }

            //한줄씩 읽는다
            while((msg=br.readLine())!=null) {

                data.add(msg);
                //특정 문자가 포함된 열은 건너 뛰고, 없는 열만 새 파일에 쓰자
                //b.result.split(",")[0].equals("0")
                //if(!msg.split(",")[5].equals(number)) {
                //bw.write(msg);
                //}
                //한줄내려쓰기
                //bw.newLine();
            }
            br.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(txtFile, false));

            for(int i=0;i<data.size();i++) {
                if (data.get(i).split("/")[0].equals(number)){

                }else {
                    try {
                        bw.write(data.get(i));
                        bw.newLine();
                    }catch (IOException e){

                    }
                }


            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;

    }

    public void appendRow(final ArrayList<String> data_count, final ArrayList<String> data_barcode, final ArrayList<String> data_time
            , final ArrayList<String> data_stock, final ArrayList<String> data_actualstock, final ArrayList<String> data_uploadcheck
            , final ArrayList<String> data_mat, final ArrayList<String> data_spot, final ArrayList<String> data_trolley
            , final ArrayList<String> data_in, final ArrayList<String> data_out){
        TableLayout tb = findViewById(R.id.scan_table);
        tb.removeAllViewsInLayout();
        //TableRow row = (TableRow) findViewById(R.id.Body_Row);

        // TextView count = (TextView)row.findViewById(R.id.Body_Count);
        // count.setText("1");
        //  TextView barcode = (TextView)row.findViewById(R.id.Body_Barcode);
        //  arcode.setText(strarray[3]);
        // TextView time = (TextView)row.findViewById(R.id.Body_Time);
        // time.setText(strarray[4]);
        TextView[] tv = new TextView[data_count.size()];

        for(int i=0;i<data_count.size();i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));

            TextView tv1 = new TextView(this);
            tv1.setText(data_count.get(i));
            tv1.setTextColor(Color.parseColor("#111111"));
            tv1.setBackgroundResource(R.drawable.border);
            tv1.setGravity(Gravity.CENTER);
            tv1.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            TextView tv2 = new TextView(this);
            tv2.setText(data_barcode.get(i));
            tv2.setTextColor(Color.parseColor("#111111"));
            tv2.setBackgroundResource(R.drawable.border);
            tv2.setGravity(Gravity.CENTER);
            tv2.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            TextView tv3 = new TextView(this);
            tv3.setText(data_time.get(i));
            tv3.setTextColor(Color.parseColor("#111111"));
            tv3.setBackgroundResource(R.drawable.border);
            tv3.setGravity(Gravity.CENTER);
            tv3.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            TextView tv4 = new TextView(this);
            String actual =  data_actualstock.get(i)+" / "+data_stock.get(i);
            tv4.setText(actual);
            tv4.setTextColor(Color.parseColor("#111111"));
            tv4.setBackgroundResource(R.drawable.border);
            tv4.setGravity(Gravity.CENTER);
            tv4.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            TextView tv5 = new TextView(this);
            tv5.setText(data_uploadcheck.get(i));
            tv5.setTextColor(Color.parseColor("#111111"));
            tv5.setBackgroundResource(R.drawable.border);
            tv5.setGravity(Gravity.CENTER);
            tv5.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            row.addView(tv1, new LayoutParams(0, 50,1));
            row.addView(tv2, new LayoutParams(0, 50,2));
            row.addView(tv3, new LayoutParams(0, 50,2));
            row.addView(tv4, new LayoutParams(0, 50,3));
            row.addView(tv5, new LayoutParams(0, 50,1));
            tb.addView(row,new TableLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            final int finalI = i;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    //int p = v.getId();
                    Toast.makeText(getApplicationContext(),data_count.get(finalI)+","+data_barcode.get(finalI)+","+data_time.get(finalI), Toast.LENGTH_SHORT).show();
                    CustomDialog customDialog = new CustomDialog(ScanActivity.this);
                    String changedActualStock = customDialog.callFunction("mat","spot","trol","in","out",data_stock.get(finalI),data_actualstock.get(finalI));
                    if(!changedActualStock.equals("-1")) {
                        try {

                            deleteLineTxt(data_count.get(finalI));
                            saveInTxt(intent_factory, intent_id, intent_location, BarcodeString, Integer.toString(getCount()), stock, changedActualStock, getUploadCheck());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                     */
                    final EditText edittext = new EditText(MAT_ScanActivity_backup.this);
                    edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(MAT_ScanActivity_backup.this,
                            android.R.style.Theme_DeviceDefault_Light_Dialog);
                    oDialog.setMessage("Mat : "     + data_mat.get(finalI) + "\n"
                            + "Spot : "         + data_spot.get(finalI) + "\n"
                            + "Trolley : "      + data_trolley.get(finalI) + "\n"
                            + "IN : "           + data_in.get(finalI) + "\t\t\t\t"
                            + "OUT : "          + data_out.get(finalI) + "\n"
                            + "ACT : "       + data_actualstock.get(finalI)+ "\t\t\t\t"
                            + "STOCK : "        + data_stock.get(finalI)
                    )
                            .setTitle(data_barcode.get(finalI))
                            .setView(edittext)
                            .setPositiveButton("[ Change Actual Stock ]", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    if(edittext.getText().toString().matches("")){
                                        Toast.makeText(getApplicationContext(),"INPUT ACTUAL STOCK",Toast.LENGTH_SHORT).show();
                                    }else{
                                    final Handler handler = new Handler();  //Optional. Define as a variable in your activity.
                                    Thread_Upload b = new Thread_Upload(intent_factory,data_barcode.get(finalI),Double.parseDouble(edittext.getText().toString()),todayDate+" "+data_time.get(finalI) ,"");//TIMUPDATE 없애야함
                                    b.start();

                                    synchronized (b) {
                                        try {
                                            System.out.println("Waiting for b to complete...");
                                            b.wait(3000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                            b.result = "0";
                                            setUploadCheck("X");
                                        }

                                        if (b.result.split(",")[0].equals("0")) {
                                            System.out.println(b.result + " ---------------------------------------------");
                                            setUploadCheck("X");
                                        } else {
                                            System.out.println("-------"+b.result.split(",")[1]+"-------");
                                            DB_stock = b.result.split(",")[1];
                                            setUploadCheck("O");
                                            setTimeSave(b.result.split(",")[2]);
                                        }
                                    }
                                    Runnable r = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try {
                                                deleteLineTxt(data_count.get(finalI));
                                                saveInTxt(intent_factory,intent_id,intent_location,data_barcode.get(finalI),data_count.get(finalI),DB_stock,edittext.getText().toString()
                                                        ,getUploadCheck(),todayDate+" "+data_time.get(finalI)
                                                        ,data_mat.get(finalI),data_spot.get(finalI),data_trolley.get(finalI),data_in.get(finalI),data_out.get(finalI));
                                                //readTxt();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            // your code here

                                            handler.post(new Runnable()  //If you want to update the UI, queue the code on the UI thread
                                            {
                                                public void run()
                                                {
                                                    try {
                                                        readTxt();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    //Code to update the UI
                                                }
                                            });


                                        }
                                    };

                                    Thread t = new Thread(r);
                                    t.start();
                                }
                                }
                            })

                            .setNegativeButton("[ Cancel ]", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    //ScanActivity.finish();

                                }
                            })
                            //.setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.


                            .show();



                }
            });
        }

        etBarcode.requestFocus();
    }
}



