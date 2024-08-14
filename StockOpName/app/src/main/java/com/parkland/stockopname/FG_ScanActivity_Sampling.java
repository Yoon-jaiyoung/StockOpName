package com.parkland.stockopname;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FG_ScanActivity_Sampling extends Activity {
    String intent_factory;
    String intent_id;
    String intent_location;
    String intent_order;
    String intent_grade;
    String intent_stockno;
    EditText etBarcode;
    String nowDate;
    String BarcodeString;
    ScrollView sv_table;
    TableLayout tb;


    public ArrayList<String> getDbarray() {
        return dbarray;
    }

    public void setDbarray(ArrayList<String> dbarray) {
        this.dbarray = dbarray;
    }

    ArrayList<String> dbarray;

    public ArrayList<String> getBarcodeArray() {
        return BarcodeArray;
    }

    public void setBarcodeArray(ArrayList<String> barcodeArray) {
        BarcodeArray = barcodeArray;
    }

    ArrayList<String> BarcodeArray;
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    int count;
    String fac_carton_digit;
    Vibrator vib;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_activity_scan_sampling);
        setCount(0);

        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf_date = new SimpleDateFormat("MMdd");
        nowDate = sdf_date.format(date);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        intent_factory = intent.getStringExtra("intent_factory");
        intent_id = intent.getStringExtra("intent_id");
        intent_location = intent.getStringExtra("intent_location");
        intent_order = intent.getStringExtra("intent_order");
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

        BarcodeArray = new ArrayList<String>();
        Thread_Read_by_po b = new Thread_Read_by_po(intent_order,intent_factory, intent_stockno);
        b.start();

        synchronized (b) {
            try {
                System.out.println("Waiting for b to complete...");
                b.wait(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setDbarray(b.result);
            appendRow(getDbarray());
            for(int i=0; i<getDbarray().size();i++)
            {
                BarcodeArray.add(getDbarray().get(i).split(",")[0]);
            }

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
                    else if(!getBarcodeArray().contains(BarcodeString)){
                        Toast.makeText(getApplicationContext(), "Carton is not in the Order number :  " + intent_id, Toast.LENGTH_LONG).show();
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

                        Thread_Read_by_po b = new Thread_Read_by_po(intent_order,intent_factory, intent_stockno);
                        b.start();

                        synchronized (b) {
                            try {
                                System.out.println("Waiting for b to complete...");
                                b.wait(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            setDbarray(b.result);
                            appendRow(getDbarray());

                        }

                    }
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        etBarcode.setText("");
                        etBarcode.requestFocus();
                        sv_table.smoothScrollTo(0,(int)((float)(tb.getBottom()-tb.getTop()-200)*((float)Integer.parseInt(BarcodeString.substring(BarcodeString.length()-3))/getDbarray().size())));

                        return true;

                }
                return false;
            }
        });

    }





    public void appendRow(ArrayList<String> array){
        tb.removeAllViewsInLayout();
        for (int i=0; i<array.size();i++) {
            //System.out.println(key + " is " + hashmap.get(key));
            TableRow row = new TableRow(this);
            row.setLayoutParams(new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));

            TextView tv1 = new TextView(this);
            tv1.setText(String.valueOf(i+1));
            tv1.setTextColor(Color.parseColor("#111111"));
            tv1.setBackgroundResource(R.drawable.border);
            tv1.setGravity(Gravity.CENTER);
            tv1.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            TextView tv2 = new TextView(this);
            tv2.setText(array.get(i).split(",")[0]);
            tv2.setTextColor(Color.parseColor("#111111"));
            tv2.setBackgroundResource(R.drawable.border);
            tv2.setGravity(Gravity.CENTER);
            tv2.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            TextView tv3 = new TextView(this);
            if(array.get(i).split(",").length < 2) tv3.setText("");
            else tv3.setText(array.get(i).split(",")[1]);
            tv3.setTextColor(Color.parseColor("#111111"));
            tv3.setBackgroundResource(R.drawable.border);
            tv3.setGravity(Gravity.CENTER);
            tv3.setShadowLayer(1.0f, 1, 1, Color.parseColor("#AFFFFFFF"));

            row.addView(tv1, new LayoutParams(0, LayoutParams.WRAP_CONTENT,1));
            row.addView(tv2, new LayoutParams(0, LayoutParams.WRAP_CONTENT,5));
            row.addView(tv3, new LayoutParams(0, LayoutParams.WRAP_CONTENT,2));
            tb.addView(row,new TableLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        etBarcode.requestFocus();

        MediaPlayer player;
        player = MediaPlayer.create(FG_ScanActivity_Sampling.this, R.raw.complete);
        player.start();
    }
}



