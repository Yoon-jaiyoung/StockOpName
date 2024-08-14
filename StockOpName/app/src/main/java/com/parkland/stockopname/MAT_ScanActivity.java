package com.parkland.stockopname;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
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
import java.util.Objects;

public class MAT_ScanActivity extends Activity {
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
    RecyclerView recyclerView_mat;
    RecyclerViewAdapter_MAT recyclerViewAdapter_mat;
    ArrayList<String> rowsArrayList = new ArrayList<>();
    boolean isLoading = false;
    ArrayList<String> dbarray;

    ProgressDialog customProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mat_activity_scan);
        setCount(0);

        recyclerView_mat = findViewById(R.id.recyclerView);

        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf_date = new SimpleDateFormat("MMdd");
        nowDate = sdf_date.format(date);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf_date2 = new SimpleDateFormat("yyyy-MM-dd");
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

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        //로딩창을 투명하게
        Objects.requireNonNull(customProgressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        etBarcode = findViewById(R.id.etBarcode);
        try {
            customProgressDialog.show();
            readTxt();
            customProgressDialog.dismiss();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        etBarcode.setOnKeyListener((v, keyCode, event) -> {
            //Enter key Action
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                DB_BarcodeString = String.valueOf(etBarcode.getText());
                if(DB_BarcodeString.length()==0){
                    Toast.makeText(getApplicationContext(),"INPUT SOMETHING",Toast.LENGTH_LONG).show();
                }
                else if(getCount()>999){
                    Toast.makeText(getApplicationContext(),"TIDAK BISA LEBIH DARI 1000, BIKIN FILE BARU",Toast.LENGTH_LONG).show();
                }
                else {
                    setCount(getCount() + 1);

                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    assert connManager != null;
                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (mWifi.isConnected()) {
                        Thread_Upload b = new Thread_Upload(intent_factory, DB_BarcodeString, 0, "","");
                        b.start();

                        synchronized (b) {
                            try {
                                System.out.println("Waiting for b to complete...");
                                customProgressDialog.show();
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
                    }else {
                        long now1 = System.currentTimeMillis();
                        Date date1 = new Date(now1);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String getTime = sdf.format(date1);

                        DB_stock = "-1.00";
                        setTimeSave(getTime);
                        DB_mat = "-1.00";
                        DB_spot = "0";
                        DB_trolley = "0";
                        DB_in = "-1.00";
                        DB_out = "-1.00";
                        setUploadCheck("X");

                    }
                    final Handler handler = new Handler();  //Optional. Define as a variable in your activity.

                    Runnable r = () -> {
                        try {
                            customProgressDialog.dismiss();
                            @SuppressLint("DefaultLocale") String cnt = String.format("%03d", getCount());
                            saveInTxt(intent_factory, intent_id, intent_location, DB_BarcodeString, cnt, DB_stock, DB_stock, getUploadCheck(), getTimeSave(), DB_mat, DB_spot, DB_trolley, DB_in, DB_out);
                            //readTxt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // your code here

                        //If you want to update the UI, queue the code on the UI thread
                        handler.post(() -> {
                            try {
                                customProgressDialog.show();
                                readTxt();
                                customProgressDialog.dismiss();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //Code to update the UI
                        });


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
                assert imm != null;
                imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                etBarcode.setText("");
                etBarcode.requestFocus();
                return true;
            }
            return false;
        });

    }

    @SuppressLint("DefaultLocale")
    public void saveInTxt (String fac, String ID, String loc, String barcode, String scount, String stock, String actual_stock, String check, String time, String mat, String spot, String trolley, String in, String out) throws IOException {
        File saveFile = new File(getFilesDir().getPath() + "/mat-stockopname"); // 저장 경로

        stock = String.format("%.2f",Double.parseDouble(stock.replace(',','.')));
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
            buf.append(scount).append("/"); //NO count 0
            buf.append(fac).append("/"); //사업부 1
            buf.append(ID).append("/");//사번 2
            buf.append(loc).append("/");
            //위치 3
            buf.append(barcode).append("/"); // 바코드 4
            buf.append(time).append("/"); // 날짜 쓰기 5

            buf.append(stock.replace(',', '.')).append("/"); // 6
            buf.append(actual_stock.replace(',', '.')).append("/");//7
            buf.append(check).append("/"); //8
            buf.append(mat).append("/"); //9
            buf.append(spot).append("/"); //10
            buf.append(trolley).append("/"); //11
            buf.append(in.replace(',', '.')).append("/"); //12
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
        String line; // 한줄씩 읽기
        File saveFile = new File(getFilesDir().getPath() + "/mat-stockopname"); // 저장 경로
        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf_date = new SimpleDateFormat("MMdd");
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
            ArrayList<String> data = new ArrayList<>();

            ArrayList<String> data_fac = new ArrayList<>();
            ArrayList<String> data_id = new ArrayList<>();
            ArrayList<String> data_loc = new ArrayList<>();
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
                //Scan 시각을 앞으로 배치
                for (int i = 0; i < data.size(); i++) {
                    String[] txt_data = data.get(i).split("/");
                    String[] txt_data_time = txt_data[5].split("\\s+");
                    data.set(i, txt_data_time[1]+"/"+data.get(i));
                }
                //Scan 시각 대로 배치
                Collections.sort(data, Collections.reverseOrder());
                //데이터 각 리스트에 저장
                for (int i = 0; i < data.size(); i++){
                    String[] txt_data = data.get(i).split("/");
                    data_count.add(txt_data[1]);
                    data_fac.add(txt_data[2]);
                    data_id.add(txt_data[3]);
                    data_loc.add(txt_data[4]);
                    data_barcode.add(txt_data[5]);

                    data_time.add(txt_data[0]);//

                    setCount(Math.max(getCount(), Integer.valueOf(txt_data[1])));
                    data_stock.add(txt_data[7]);
                    data_actualstock.add(txt_data[8]);
                    data_uploadcheck.add(txt_data[9]);
                    data_mat.add(txt_data[10]);
                    data_spot.add(txt_data[11]);
                    data_trolley.add(txt_data[12]);
                    data_in.add(txt_data[13]);
                    data_out.add(txt_data[14]);
                }
                for (int i = 0; i < data.size(); i++) {
                    String[] txt_data = data.get(i).split("/");
                    data.set(i, txt_data[1]+"/"+txt_data[2]+"/"+txt_data[3]+"/"+txt_data[4]+"/"+txt_data[5]+"/"+txt_data[6]+"/"+txt_data[7]+"/"+txt_data[8]+"/"+txt_data[9]+"/"+txt_data[10]+"/"+txt_data[11]+"/"+txt_data[12]+"/"+txt_data[13]+"/"+txt_data[14]);
                }
            }

            dbarray = data;
            if(dbarray.isEmpty()){

            }else appendRow();
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
                    final EditText edittext = new EditText(MAT_ScanActivity.this);
                    edittext.setFilters(new MAT_InputFilter[]{ new MAT_InputFilter("0", "500")});
                    edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    AlertDialog.Builder oDialog = new AlertDialog.Builder(MAT_ScanActivity.this,
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

                                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                                    long now = System.currentTimeMillis();
                                    Date date = new Date(now);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    String getTime = sdf.format(date);

                                    if (mWifi.isConnected()) {
                                        Thread_Upload b = new Thread_Upload(intent_factory, data_barcode.get(finalI), Double.parseDouble(edittext.getText().toString()), todayDate + " " + data_time.get(finalI),getTime);
                                        b.start();

                                        synchronized (b) {
                                            try {
                                                System.out.println("Waiting for b inside to complete...");
                                                b.wait(3000);
                                                customProgressDialog.show();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                                b.result = "0";
                                                setUploadCheck("X");
                                            }

                                            if (b.result.split(",")[0].equals("0")) {
                                                System.out.println(b.result + " ---------------------------------------------");
                                                setUploadCheck("X");
                                            } else {
                                                System.out.println("-------" + b.result.split(",")[1] + "-------");
                                                DB_stock = b.result.split(",")[1];
                                                setUploadCheck("O");
                                                //setTimeSave(b.result.split(",")[2]);
                                                setTimeSave(getTime);
                                            }
                                        }
                                    }
                                    else {
                                        setTimeSave(getTime);
                                        DB_stock = edittext.getText().toString();
                                        setUploadCheck("X");
                                    }
                                    Runnable r = new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try {
                                                customProgressDialog.dismiss();
                                                deleteLineTxt(data_count.get(finalI));
                                                saveInTxt(intent_factory,intent_id,intent_location,data_barcode.get(finalI),data_count.get(finalI),DB_stock,edittext.getText().toString()
                                                        ,getUploadCheck(),getTimeSave()//todayDate+" "+data_time.get(finalI) //update시 udpate시각으로 업데이트
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
                                                        customProgressDialog.show();
                                                        readTxt();
                                                        customProgressDialog.dismiss();
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

                            .setNegativeButton("[ Cancel ]", (dialog, which) -> {
                                //ScanActivity.finish();
                                dialog.dismiss();
                            })
                            //.setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.


                            .show();



                }
            });
        }

        etBarcode.requestFocus();
    }
    public void appendRow(){
        rowsArrayList.clear();
        populateData();
        initAdapter();
        initScrollListener();

        MediaPlayer player;
        player = MediaPlayer.create(MAT_ScanActivity.this, R.raw.complete);
        player.start();
    }

    private void populateData() {
        int i = 0;
        if(dbarray.isEmpty()){
            rowsArrayList.add(" , ");
        }else {
            while (i < dbarray.size() && i < 30) {
                String[] txt_data = dbarray.get(i).split("/");
                rowsArrayList.add(txt_data[0]+","+txt_data[4]+","+txt_data[5].split("\\s+")[1]+","+txt_data[7]+"/"+txt_data[6]+","+txt_data[8]);
                i++;
            }
        }
    }

    private void initAdapter() {

        recyclerViewAdapter_mat = new RecyclerViewAdapter_MAT(getApplicationContext(), rowsArrayList, (v, position) -> {
            Log.d(TAG, "clicked position:" + position);
            if(position>=0)
            {
                final EditText edittext = new EditText(MAT_ScanActivity.this);
                edittext.setFilters(new MAT_InputFilter[]{ new MAT_InputFilter("0", "500")});
                final int pos = position;
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                final AlertDialog.Builder oDialog = new AlertDialog.Builder(MAT_ScanActivity.this,
                        android.R.style.Theme_DeviceDefault_Light_Dialog);
                oDialog.setMessage("Mat : "     + dbarray.get(pos).split("/")[9] + "\n"
                        + "Spot : "         + dbarray.get(pos).split("/")[10] + "\n"
                        + "Trolley : "      + dbarray.get(pos).split("/")[11] + "\n"
                        + "IN : "           + dbarray.get(pos).split("/")[12] + "\t\t\t\t"
                        + "OUT : "          + dbarray.get(pos).split("/")[13] + "\n"
                        + "ACT : "       + dbarray.get(pos).split("/")[7]+ "\t\t\t\t"
                        + "STOCK : "        + dbarray.get(pos).split("/")[6]
                )
                        .setTitle(dbarray.get(position).split("/")[4])
                        .setView(edittext)
                        .setPositiveButton("[ Change Actual Stock ]", (dialog, which) -> {
                            if(edittext.getText().toString().matches("")){
                                Toast.makeText(getApplicationContext(),"INPUT ACTUAL STOCK",Toast.LENGTH_SHORT).show();
                            }else{
                                final Handler handler = new Handler();  //Optional. Define as a variable in your activity.

                                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                assert connManager != null;
                                NetworkInfo mWifi = Objects.requireNonNull(connManager).getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                                long now = System.currentTimeMillis();
                                Date date = new Date(now);
                                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String getTime = sdf.format(date);

                                if (mWifi.isConnected()) {
                                    Thread_Upload b = new Thread_Upload(intent_factory, dbarray.get(pos).split("/")[4], Double.parseDouble(edittext.getText().toString()), todayDate + " " + dbarray.get(pos).split("/")[5].split("\\s+")[1],getTime);
                                    b.start();

                                    synchronized (b) {
                                        try {
                                            System.out.println("Waiting for b to complete...");
                                            customProgressDialog.show();
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
                                            System.out.println("-------" + b.result.split(",")[1] + "-------");
                                            DB_stock = b.result.split(",")[1];
                                            setUploadCheck("O");
                                            setTimeSave(b.result.split(",")[2]);
                                        }
                                    }
                                }
                                else {
                                    setTimeSave(getTime);
                                    DB_stock = edittext.getText().toString();
                                    setUploadCheck("X");
                                }
                                Runnable r = () -> {
                                    try {
                                        customProgressDialog.dismiss();
                                        deleteLineTxt(dbarray.get(pos).split("/")[0]);
                                        saveInTxt(intent_factory,intent_id,intent_location,dbarray.get(pos).split("/")[4],dbarray.get(pos).split("/")[0],DB_stock,edittext.getText().toString()
                                                ,getUploadCheck(),getTimeSave()//todayDate+" "+dbarray.get(pos).split("/")[5].split("\\s+")[1]  //update시 update시각으로 업데이트
                                                ,dbarray.get(pos).split("/")[9],dbarray.get(pos).split("/")[10],dbarray.get(pos).split("/")[11],dbarray.get(pos).split("/")[12],dbarray.get(pos).split("/")[13]);
                                        //readTxt();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    // your code here

                                    //If you want to update the UI, queue the code on the UI thread
                                    handler.post(() -> {
                                        try {
                                            customProgressDialog.show();
                                            readTxt();
                                            customProgressDialog.dismiss();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        //Code to update the UI
                                    });


                                };

                                Thread t = new Thread(r);
                                t.start();
                            }
                        })

                        .setNegativeButton("[ Cancel ]", (dialog, which) -> {
                            //ScanActivity.finish();
                            dialog.dismiss();
                        })
                        //.setCancelable(false) // 백버튼으로 팝업창이 닫히지 않도록 한다.


                        .show();
            }
        });
        recyclerView_mat.setAdapter(recyclerViewAdapter_mat);
    }


    private void initScrollListener() {
        recyclerView_mat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == rowsArrayList.size() - 1) {
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMore() {
        rowsArrayList.add(null);
        recyclerViewAdapter_mat.notifyItemInserted(rowsArrayList.size() - 1);


        Handler handler = new Handler();

        handler.postDelayed(() -> {
            rowsArrayList.remove(rowsArrayList.size() - 1);
            int scrollPosition = rowsArrayList.size();
            recyclerViewAdapter_mat.notifyItemRemoved(scrollPosition);
            int currentSize = scrollPosition;
            int nextLimit = currentSize + 10;

            while ((currentSize < dbarray.size()) && (currentSize - 1 < nextLimit)) {
                String[] txt_data = dbarray.get(currentSize).split("/");
                rowsArrayList.add(txt_data[0]+","+txt_data[4]+","+txt_data[5].split("\\s+")[1]+","+txt_data[7]+"/"+txt_data[6]+","+txt_data[8]);
                currentSize++;





            }

            recyclerViewAdapter_mat.notifyDataSetChanged();
            isLoading = false;
        }, 2000);
    }
}



