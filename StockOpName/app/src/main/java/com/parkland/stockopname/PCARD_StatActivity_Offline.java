package com.parkland.stockopname;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

public class PCARD_StatActivity_Offline extends Activity {
    String intent_filename;
    String[] txt_data;
    File saveFile;
    File txtFile;


    //
    private final int WRITE_REQUEST_CODE = 43;
    String mOutputDir;
    private ParcelFileDescriptor pfd;
    File src;
    String fileName;
    FileChannel inChannel;
    FileChannel outChannel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pcard_activity_stat_offline);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();

        intent_filename = intent.getStringExtra("intent_filename");
        txt_data = intent_filename.split("_");

        saveFile = new File(getFilesDir().getPath() + "/pcard-stockopname"); // 저장 경로
        txtFile = new File(saveFile.getAbsolutePath()+"/"+intent_filename);

        // Capture the layout's TextView and set the string as its text
        TextView tvFac = findViewById(R.id.tvFac);
        TextView tvLoc = findViewById(R.id.tvLoc);
        TextView tvID = findViewById(R.id.tvID);
        TextView tvDate = findViewById(R.id.tvDate);
        tvFac.setText(txt_data[0]);
        tvLoc.setText(txt_data[1]);
        tvID.setText(txt_data[2]);
        //tvDate.setText(txt_data[3].split(".")[0]);
        tvDate.setText(txt_data[3].substring(0,4));
        try {
            readTxt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*  DELETE BUTTON
        Button btnDelete = (Button)findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StatActivity.this, R.style.MyDialogTheme);

                final View bannerView = View.inflate(StatActivity.this, R.layout.dialog_title, null);

                TextView titleView = bannerView.findViewById(R.id.tvDialogTitle);
                titleView.setText("ARE YOU SURE TO DELETE "+intent_filename+"?");

                builder.setCustomTitle(bannerView);

                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Boolean deleted = txtFile.delete();
                        //new File(saveFile.getAbsolutePath()+"/PWI_TESY_TEST_1119.txt").delete();
                        if(deleted) {
                            Toast.makeText(getApplicationContext(),"Deleted file Succeed",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(StatActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else Toast.makeText(getApplicationContext(),"Deleted file Failed",Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        */

    }

    public void readTxt() throws IOException {
        String line = null; // 한줄씩 읽기
        File saveFile = new File(getFilesDir().getPath() + "/pcard-stockopname"); // 저장 경로

        txtFile = new File(saveFile.getAbsolutePath()+"/"+intent_filename);
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
            int count=0;
            while((line=buf.readLine())!=null){
                String[] txt_data = line.split("/");
                data_fac.add(txt_data[0]);
                data_id.add(txt_data[1]);
                data_loc.add(txt_data[2]);
                data_barcode.add(txt_data[3]);
                String[] txt_data_time = txt_data[4].split("\\s+");
                data_time.add(txt_data_time[1]);
                data_count.add(txt_data[5]);
            }
            Collections.reverse(data_fac);
            Collections.reverse(data_id);
            Collections.reverse(data_loc);
            Collections.reverse(data_barcode);
            Collections.reverse(data_time);
            Collections.reverse(data_count);

            appendRow(data_count,data_barcode,data_time);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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
    }




    public void onClickCopy(View view){
        saf();
    }
    public void saf(){
        fileName=intent_filename;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain" );
        intent.putExtra(Intent.EXTRA_TITLE,fileName);
        startActivityForResult(intent,WRITE_REQUEST_CODE);
    }
    FileOutputStream outStream;
    @Override
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                writeFile(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else super.onActivityResult(requestCode,resultCode,data);
    }
    public void writeFile(Intent data) throws IOException {
        Uri uri  = data.getData();
        mOutputDir= getFilesDir().getPath();
        FileInputStream inStream = new FileInputStream(txtFile);
        try{
            pfd=this.getContentResolver().openFileDescriptor(uri,"w");
            outStream = new FileOutputStream(pfd.getFileDescriptor());
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            inChannel = inStream.getChannel();
            outChannel = outStream.getChannel();
        }catch (Exception e){
            e.printStackTrace();
        }
        try{
            inChannel.transferTo(0,inChannel.size(),outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inChannel.close();
            outChannel.close();
            if(outStream!=null){
                try{
                    outStream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            pfd.close();
        }
    }
}



