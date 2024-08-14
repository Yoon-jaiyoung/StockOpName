package com.parkland.stockopname;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class FG_MainActivity_Sampling extends AppCompatActivity {

    private EditText etID;
    private EditText etOrder;

    private Spinner spinFactory;
    private Spinner spinGrade;
    private Button btnLayout;
    private Button btnStockno;
    private LinearLayout main_layout_content;
    private AppBarLayout appBarLayout;
    private long backKeyPressedTime = 0;


    public ArrayList<String> getLayout_ArrayList() {
        return Layout_ArrayList;
    }

    public void setLayout_ArrayList(ArrayList<String> layout_ArrayList) {
        Layout_ArrayList = layout_ArrayList;
    }

    ArrayList<String> Layout_ArrayList;

    public ArrayList<String> getStockno_ArrayList() {
        return Stockno_ArrayList;
    }

    public void setStockno_ArrayList(ArrayList<String> stockno_ArrayList) {
        Stockno_ArrayList = stockno_ArrayList;
    }

    ArrayList<String> Stockno_ArrayList;

    String[] layout_list;
    String[] stockno_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getResources().getString(R.string.app_name) + " V"+ BuildConfig.VERSION_NAME;
        setTitle(title);
        setContentView(R.layout.fg_activity_main_sampling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        CoordinatorLayout mainLayout_main = (CoordinatorLayout) findViewById(R.id.main_layout);
        mainLayout_main.startAnimation(animFadeIn);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        main_layout_content = (LinearLayout) findViewById(R.id.content_linear);

        etID = (EditText) findViewById(R.id.txtID);
        etOrder = (EditText) findViewById(R.id.txtONO);

        etID.setOnFocusChangeListener(etFocusListener);
        etOrder.setOnFocusChangeListener(etFocusListener);

        etID.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etOrder.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        spinGrade = (Spinner) findViewById(R.id.grade_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.fg_simple_spinner_item, getResources().getStringArray(R.array.grade_array));
        adapter.setDropDownViewResource(R.layout.fg_simple_spinner_dropdown_item);
        spinGrade.setAdapter(adapter);

        btnStockno = (Button) findViewById(R.id.stockno_button);
        btnStockno.setEnabled(false);
        btnLayout = (Button) findViewById(R.id.loc_button);
        btnLayout.setEnabled(false);

        spinFactory = (Spinner) findViewById(R.id.fac_spinner);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>
                (this, R.layout.fg_simple_spinner_item, getResources().getStringArray(R.array.fac_array));
        adapter2.setDropDownViewResource(R.layout.fg_simple_spinner_dropdown_item);
        spinFactory.setAdapter(adapter2);
        spinFactory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if (parentView.getItemAtPosition(position).toString().equals("FAC")) {
                    btnLayout.setText("Select FACTORY");
                    btnLayout.setEnabled(false);
                    btnStockno.setText("Select FACTORY");
                    btnStockno.setEnabled(false);
                } else {
                    Thread_Stockno a = new Thread_Stockno(parentView.getItemAtPosition(position).toString());
                    a.start();

                    synchronized (a) {
                        try {
                            System.out.println("Waiting for a to complete...");
                            a.wait(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        setStockno_ArrayList(a.result);
                        if(getStockno_ArrayList().isEmpty())
                            btnStockno.setText("NULL : CONTACT IT");
                        else
                            btnStockno.setText(getStockno_ArrayList().get(0));
                    }

                    Thread_Layout b = new Thread_Layout(parentView.getItemAtPosition(position).toString());
                    b.start();

                    synchronized (b) {
                        try {
                            System.out.println("Waiting for b to complete...");
                            b.wait(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        setLayout_ArrayList(b.result);
                        btnLayout.setText("SELECT WH");
                        btnLayout.setEnabled(true);
                        btnStockno.setEnabled(true);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                btnLayout.setText("Select WH");
            }

        });
        btnLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(FG_MainActivity_Sampling.this);
                builder.setTitle("Choose Layout");
                // add a radio button list
                layout_list = new String[getLayout_ArrayList().size()];
                int size=0;
                for(String temp : getLayout_ArrayList())
                {
                    layout_list[size++] = temp;
                }

                int checkedItem = 0;
                builder.setSingleChoiceItems(layout_list, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btnLayout.setText(layout_list[which]);
                        dialog.dismiss();
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnStockno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(FG_MainActivity_Sampling.this);
                builder.setTitle("Choose Stock Opname No");
                // add a radio button list
                stockno_list = new String[getStockno_ArrayList().size()];
                int size=0;
                for(String temp : getStockno_ArrayList())
                {
                    stockno_list[size++] = temp;
                }

                int checkedItem = 0;
                builder.setSingleChoiceItems(stockno_list, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btnStockno.setText(stockno_list[which]);
                        dialog.dismiss();
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        final Button btnStart = (Button) findViewById(R.id.btnSend);

        etOrder.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    btnStart.callOnClick();
                }
                return false;
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(spinFactory.getSelectedItem().toString().equals("FAC")) {
                    Toast.makeText(getApplicationContext(), "Please Choose Factory", Toast.LENGTH_SHORT).show();
                }else if(btnLayout.getText().equals("SELECT WH")){
                    Toast.makeText(getApplicationContext(),"Please Select Location",Toast.LENGTH_SHORT).show();
                }else if (etID.length()==0){
                    Toast.makeText(getApplicationContext(),"Please Fill ID",Toast.LENGTH_SHORT).show();
                }else if(etOrder.length()==0){
                    Toast.makeText(getApplicationContext(),"Please Fill Order Number",Toast.LENGTH_SHORT).show();
                }else if(btnStockno.getText().equals("NULL : CONTACT IT")){
                    Toast.makeText(getApplicationContext(),"Please Contact IT",Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent;
                    intent = new Intent(v.getContext(), FG_ScanActivity_Sampling.class);

                    intent.putExtra("intent_factory", spinFactory.getSelectedItem().toString());
                    intent.putExtra("intent_id", etID.getText().toString());
                    intent.putExtra("intent_location", btnLayout.getText().toString());
                    intent.putExtra("intent_order", etOrder.getText().toString());
                    intent.putExtra("intent_grade", spinGrade.getSelectedItem().toString());
                    intent.putExtra("intent_stockno", btnStockno.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            main_layout_content.requestFocus();
            appBarLayout.setExpanded(true);
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }

    }


    View.OnFocusChangeListener etFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            // code to execute when EditText loses focus
            appBarLayout.setExpanded(!b);
        }


    };


}
