package com.parkland.stockopname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

public class FG_ScanActivity_Offline extends Activity {
    String intent_factory;
    String intent_id;
    String intent_location;
    EditText etBarcode;
    String nowDate;
    String BarcodeString;
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    int count;
    String fac_carton_digit;
    Vibrator vib;
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<String> rowsArrayList = new ArrayList<>();
    boolean isLoading = false;
    ArrayList<String> dbarray = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_activity_scan);
        setCount(0);

        recyclerView = findViewById(R.id.recyclerView);

        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf_date = new SimpleDateFormat("MMdd");
        nowDate = sdf_date.format(date);

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
        if(intent_factory.equals("PWI")||intent_factory.equals("PWJ")||intent_factory.equals("PWA"))
            fac_carton_digit="1618";
        else if(intent_factory.equals("PWN")||intent_factory.equals("PNP"))
            fac_carton_digit="9";
        else fac_carton_digit = "0";

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        etBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //Enter key Action
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    BarcodeString= String.valueOf(etBarcode.getText());
                    if(fac_carton_digit.equals("1618")&&BarcodeString.length() != 16 && BarcodeString.length() != 18) {
                        Toast.makeText(getApplicationContext(), "Must be 16 or 18 digits.. Length : " + BarcodeString.length(), Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            vib.vibrate(1000);
                        }
                    }
                    else if(fac_carton_digit.equals("9")&&BarcodeString.length() != 9) {
                        Toast.makeText(getApplicationContext(), "Must be 9 digits.. Length : " + BarcodeString.length(), Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            vib.vibrate(1000);
                        }
                    }
                    else if(hasSpecialCharacter(BarcodeString) == true || BarcodeString.matches("\\d+(?:\\.\\d+)?") == false){
                        Toast.makeText(getApplicationContext(), "Contain Unspecific words.. Please Scan Again ", Toast.LENGTH_SHORT).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            vib.vibrate(1000);
                        }
                    }
                    else {
                        setCount(getCount() + 1);
                        final Handler handler = new Handler();  //Optional. Define as a variable in your activity.

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    saveInTxt(intent_factory, intent_id, intent_location, BarcodeString, Integer.toString(getCount()));
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

    public void saveInTxt (String fac,String ID, String loc,String barcode,String scount) throws IOException {
        File saveFile = new File(getFilesDir().getPath() + "/fg-stockopname"); // 저장 경로
        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = sdf.format(date);

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
            buf.append(fac+"/"); //사업부 0
            buf.append(ID+"/"); //사번 1
            buf.append(loc+"/"); //위치 2
            buf.append(barcode+"/"); // 바코드 3
            buf.append(nowTime+"/"); // 날짜 쓰기 4
            buf.append(scount); //NO count 5
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
        File saveFile = new File(getFilesDir().getPath() + "/fg-stockopname"); // 저장 경로
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
            ArrayList<String> data_fac = new ArrayList<String>();
            ArrayList<String> data_id = new ArrayList<String>();
            ArrayList<String> data_loc = new ArrayList<String>();
            ArrayList<String> data_barcode = new ArrayList<String>();
            ArrayList<String> data_time = new ArrayList<String>();
            ArrayList<String> data_count = new ArrayList<String>();
            dbarray.clear();
            while((line=buf.readLine())!=null){
                String[] txt_data = line.split("/");
                data_fac.add(txt_data[0]);
                data_id.add(txt_data[1]);
                data_loc.add(txt_data[2]);
                data_barcode.add(txt_data[3]);
                String[] txt_data_time = txt_data[4].split("\\s+");
                data_time.add(txt_data_time[1]);
                data_count.add(txt_data[5]);
                setCount(Math.max(getCount(),Integer.valueOf(txt_data[5])));
                dbarray.add(txt_data[5] + ',' + txt_data[3] + ',' + txt_data_time[1]);
            }
            Collections.reverse(data_fac);
            Collections.reverse(data_id);
            Collections.reverse(data_loc);
            Collections.reverse(data_barcode);
            Collections.reverse(data_time);
            Collections.reverse(data_count);

            if(dbarray.isEmpty()){

            }else {
                appendRow();
            }
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void appendRow(){
        rowsArrayList.clear();
        populateData();
        initAdapter();
        initScrollListener();

        MediaPlayer player;
        player = MediaPlayer.create(FG_ScanActivity_Offline.this, R.raw.complete);
        player.start();
    }

    private void populateData() {
        int i = 0;
        Collections.reverse(dbarray);
        if(dbarray.isEmpty()){
            rowsArrayList.add(" , ");
        }else {
            while (i < dbarray.size() && i < 30) {
                //rowsArrayList.add((dbarray.size()-i)+","+dbarray.get(i));
                rowsArrayList.add(dbarray.get(i));
                i++;
            }
        }
    }

    private void initAdapter() {

        recyclerViewAdapter = new RecyclerViewAdapter(rowsArrayList);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void loadMore() {
        rowsArrayList.add(null);
        recyclerViewAdapter.notifyItemInserted(rowsArrayList.size() - 1);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rowsArrayList.remove(rowsArrayList.size() - 1);
                int scrollPosition = rowsArrayList.size();
                recyclerViewAdapter.notifyItemRemoved(scrollPosition);
                int currentSize = scrollPosition;
                int nextLimit = currentSize + 10;

                while ((currentSize < dbarray.size()) && (currentSize - 1 < nextLimit)) {
                    rowsArrayList.add(dbarray.get(currentSize));
                    currentSize++;
                }

                recyclerViewAdapter.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);
    }

    /**
     * 특수문자 포함 여부 확인
     * @param string
     * @return
     */
    public static boolean hasSpecialCharacter(String string) {
        if (TextUtils.isEmpty(string)) {
            return false;
        }

        for (int i = 0; i < string.length(); i++) {
            if (!Character.isLetterOrDigit(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }

/*
    public void appendRow(ArrayList<String> data_count,ArrayList<String> data_barcode,ArrayList<String> data_time){
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
            row.setLayoutParams(new TableRow.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));

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

            row.addView(tv1, new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT,1));
            row.addView(tv2, new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT,5));
            row.addView(tv3, new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT,2));
            tb.addView(row,new TableLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        etBarcode.requestFocus();
    }
 */
}



