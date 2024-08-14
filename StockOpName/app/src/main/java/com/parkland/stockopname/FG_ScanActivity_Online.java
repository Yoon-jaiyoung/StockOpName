package com.parkland.stockopname;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class FG_ScanActivity_Online extends Activity {
    String intent_factory;
    String intent_id;
    String intent_location;
    String intent_grade;
    String intent_stockno;
    EditText etBarcode;
    String nowDate;
    String BarcodeString;
    ScrollView sv_table;
    TableLayout tb;
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<String> rowsArrayList = new ArrayList<>();
    boolean isLoading = false;
    ArrayList<String> dbarray;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    int count;
    String fac_carton_digit;
    Vibrator vib;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_activity_scan);
        setCount(0);

        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf_date = new SimpleDateFormat("MMdd");
        nowDate = sdf_date.format(date);

        recyclerView = findViewById(R.id.recyclerView);
        

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        intent_factory = intent.getStringExtra("intent_factory");
        intent_id = intent.getStringExtra("intent_id");
        intent_location = intent.getStringExtra("intent_location");
        intent_grade = intent.getStringExtra("intent_grade");
        intent_stockno = intent.getStringExtra("intent_stockno");
        // Capture the layout's TextView and set the string as its text
        TextView tvFac = findViewById(R.id.tvFac);
        TextView tvLoc = findViewById(R.id.tvLoc);
        TextView tvID = findViewById(R.id.tvID);
        TextView tvDate = findViewById(R.id.tvDate);
        tvFac.setText(intent_factory);
        tvLoc.setText(intent_location);
        tvID.setText(intent_id);
        tvDate.setText(nowDate);

        sv_table = findViewById(R.id.sv_table);
        tb = findViewById(R.id.scan_table);
        //Carton_List = new ArrayList<String>();

        etBarcode = findViewById(R.id.etBarcode);

        Thread_Read_All b = new Thread_Read_All(intent_factory, intent_location, intent_id, intent_grade, intent_stockno);
        b.start();

        synchronized (b) {
            try {
                System.out.println("Waiting for b to complete...");
                b.wait(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dbarray = (b.result);
            if(dbarray.isEmpty()){
            }else appendRow();
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
                    else {
                        Thread_Insert_Carton a = new Thread_Insert_Carton(intent_stockno, BarcodeString, intent_factory, intent_id, intent_grade, intent_location);
                        a.start();
                        synchronized (a) {
                            try {
                                System.out.println("Waiting for a to complete...");
                                a.wait(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }


                        Thread_Read_All b = new Thread_Read_All(intent_factory, intent_location, intent_id, intent_grade, intent_stockno);
                        b.start();

                        synchronized (b) {
                            try {
                                System.out.println("Waiting for b to complete...");
                                b.wait(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            dbarray = (b.result);
                            if(dbarray.isEmpty()){

                            }else appendRow();
                        }
                    }
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        etBarcode.setText("");
                        etBarcode.requestFocus();
                        //sv_table.smoothScrollTo(0,(int)((float)(tb.getBottom()-tb.getTop()-200)*((float)Integer.parseInt(BarcodeString.substring(BarcodeString.length()-3))/hashmap_list.size())));

                        return true;

                }
                return false;
            }
        });

    }





    public void appendRow(){
        rowsArrayList.clear();
        populateData();
        initAdapter();
        initScrollListener();

        MediaPlayer player;
        player = MediaPlayer.create(FG_ScanActivity_Online.this, R.raw.complete);
        player.start();
    }

    private void populateData() {
        int i = 0;
        if(dbarray.isEmpty()){
            rowsArrayList.add(" , ");
        }else {
            while (i < dbarray.size() && i < 30) {
                rowsArrayList.add((dbarray.size()-i)+","+dbarray.get(i));
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
                    rowsArrayList.add((dbarray.size()-currentSize)+","+dbarray.get(currentSize));
                    currentSize++;
                }

                recyclerViewAdapter.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);
    }
}



