package com.parkland.stockopname;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


public class splash_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activity);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button btn_fg = (Button) findViewById(R.id.btn_fg);
        Button btn_mat = (Button) findViewById(R.id.btn_mat);
        Button btn_btm = (Button) findViewById(R.id.btn_btm);
        Button btn_pcard = (Button) findViewById(R.id.btn_pcard);
        btn_fg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity.this, splash_activity_fg.class);
                startActivity(intent);
                finish();
            }
        });
        btn_mat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity.this,MAT_MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_btm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity.this,BTM_MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btn_pcard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity.this,splash_activity_pcard.class);
                startActivity(intent);
                finish();
            }
        });

    }
}