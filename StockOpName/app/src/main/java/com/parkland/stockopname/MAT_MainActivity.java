package com.parkland.stockopname;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MAT_MainActivity extends AppCompatActivity {
    private EditText etFactory;
    private EditText etID;
    private EditText etLocation;
    private Spinner spinFactory;
    private LinearLayout main_layout_content;
    private AppBarLayout appBarLayout;
    private long backKeyPressedTime = 0;
    private String android_id;
    int serverResponseCode = 0;
    ProgressDialog pDialog = null;
    String upLoadServerUri = "http://192.168.40.175:9000/uploadtoserver.php";

    /**********  File Path *************/
    String uploadFilePath = "";
    String uploadFileName = ""; //전송하고자하는 파일 이름
    String[] fileNameArray;
    int standardSize_X, standardSize_Y;
    float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getResources().getString(R.string.app_name) + " V"+ BuildConfig.VERSION_NAME;
        setTitle(title);
        setContentView(R.layout.mat_activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        CoordinatorLayout mainLayout_main = (CoordinatorLayout) findViewById(R.id.main_layout);
        mainLayout_main.startAnimation(animFadeIn);

        android_id = Secure.getString(MAT_MainActivity.this.getContentResolver(), Secure.ANDROID_ID);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        main_layout_content = (LinearLayout) findViewById(R.id.content_linear);

        etID = (EditText) findViewById(R.id.txtID);
        etLocation = (EditText) findViewById(R.id.txtLocation);

        etID.setOnFocusChangeListener(etFocusListener);
        etLocation.setOnFocusChangeListener(etFocusListener);

        etID.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etLocation.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        spinFactory = (Spinner) findViewById(R.id.fac_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.mat_simple_spinner_item, getResources().getStringArray(R.array.fac_array));
        adapter.setDropDownViewResource(R.layout.mat_simple_spinner_dropdown_item);
        spinFactory.setAdapter(adapter);

        uploadFilePath = getFilesDir().getPath() + "/mat-stockopname/";


        final Button btnStart = (Button) findViewById(R.id.btnSend);

        etID.setOnKeyListener(new View.OnKeyListener() {
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
                }else if(etLocation.length()==0){
                    Toast.makeText(getApplicationContext(),"Please Fill Location", Toast.LENGTH_SHORT).show();
                }else if (etID.length()==0){
                    Toast.makeText(getApplicationContext(),"Please Fill ID", Toast.LENGTH_SHORT).show();
                }else if(etLocation.getText().toString().contains("/") || etLocation.getText().toString().contains("_") || etLocation.getText().toString().contains(",")){
                    Toast.makeText(getApplicationContext(),"Location cannot contain '/' or '_' or ','", Toast.LENGTH_SHORT).show();
                }else if(etID.getText().toString().contains("/") || etID.getText().toString().contains("_") || etID.getText().toString().contains(",")){
                    Toast.makeText(getApplicationContext(),"ID cannot contain '/' or '_' or ','", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(v.getContext(), MAT_ScanActivity.class);
                    intent.putExtra("intent_factory", spinFactory.getSelectedItem().toString());
                    intent.putExtra("intent_id", etID.getText().toString());
                    intent.putExtra("intent_location", etLocation.getText().toString());
                    startActivity(intent);
                }
            }
        });

        Button btnStat = (Button) findViewById(R.id.btnStat);
        btnStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(MAT_MainActivity.this, R.style.MyDialogTheme);
                String path = uploadFilePath; // 저장 경로

                final View bannerView = View.inflate(MAT_MainActivity.this, R.layout.mat_dialog_title, null);
                TextView titleView = bannerView.findViewById(R.id.tvDialogTitle);
                titleView.setText(path);
                builder.setCustomTitle(bannerView);
                File saveFile = new File(path); // 저장 경로
                if (!saveFile.exists()) { // 폴더 없을 경우
                    saveFile.mkdir(); // 폴더 생성
                }
                // add a list
                ///List<String> fileNameList = FileList(path);
                File[] fileArray =saveFile.listFiles();
                Arrays.sort(fileArray, new Comparator<File>(){
                    public int compare(File f1, File f2)
                    {
                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    } });
                fileNameArray = new String[fileArray.length];
                for (int i = 0; i < fileArray.length; i++) {
                    fileNameArray[i] = fileArray[i].getName();
                }
                ///final String[] fileNameArray = fileNameList.toArray(new String[fileNameList.size()]);
                /*
                String[] files = (String[])Arrays.asList(dir.listFiles(filefilter))
                  .stream().map(x->x.getName())
                  .collect(Collectors.toList())
                  .toArray();
                  */



                builder.setItems(fileNameArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String intent_filename = fileNameArray[which];
                        Intent intent = new Intent(MAT_MainActivity.this, MAT_StatActivity.class);
                        intent.putExtra("intent_filename", intent_filename);
                        startActivity(intent);
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(MAT_MainActivity.this, R.style.MyDialogTheme);
                String path = uploadFilePath; // 저장 경로

                //List<String> fileNameList = FileList(path);
                //final String[] fileNameArray = fileNameList.toArray(new String[fileNameList.size()]);


                final View bannerView = View.inflate(MAT_MainActivity.this, R.layout.mat_dialog_title, null);

                TextView titleView = bannerView.findViewById(R.id.tvDialogTitle);
                titleView.setText(path);
                builder.setCustomTitle(bannerView);
                File saveFile = new File(path); // 저장 경로
                if (!saveFile.exists()) { // 폴더 없을 경우
                    saveFile.mkdir(); // 폴더 생성
                }
                File[] fileArray =saveFile.listFiles();
                Arrays.sort(fileArray, new Comparator<File>(){
                    public int compare(File f1, File f2)
                    {
                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    } });
                fileNameArray = new String[fileArray.length];
                for (int i = 0; i < fileArray.length; i++) {
                    fileNameArray[i] = fileArray[i].getName();
                }
                builder.setPositiveButton("UPLOAD ALL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MAT_MainActivity.this, R.style.MyDialogTheme);
                        final View bannerView = View.inflate(getApplicationContext(), R.layout.mat_dialog_title, null);
                        TextView titleView = bannerView.findViewById(R.id.tvDialogTitle);
                        titleView.setText(fileNameArray.length+" Files will uploaded via "+upLoadServerUri);
                        builder.setCustomTitle(bannerView);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pDialog = ProgressDialog.show(MAT_MainActivity.this, "", "Uploading file...", true);
                                new Thread(new Runnable() {
                                    public void run() {

                                        if (fileNameArray!=null && fileNameArray.length > 0) {
                                            for (int j = 0; j < fileNameArray.length; j++) {
                                                uploadFileName = fileNameArray[j];
                                                uploadFile(uploadFilePath + "" + fileNameArray[j]);
                                            }
                                        }else {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    pDialog.dismiss();
                                                    Toast.makeText(getApplicationContext(),"NO FILE TO UPLOAD",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }

                                    }
                                }).start();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                return;
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();



                    }
                });

                // add a list


                builder.setItems(fileNameArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {

                        pDialog = ProgressDialog.show(MAT_MainActivity.this, "", "Uploading file...", true);
                        new Thread(new Runnable() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        //messageText.setText("uploading started.....");
                                    }
                                });
                                    uploadFile(uploadFilePath + "" + fileNameArray[which]);
                            }
                        }).start();
                    }
                });
                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



        //권한
        //권한ID를 가져옵니다
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // 권한이 열려있는지 확인
        if (permission == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED) {
            // 마쉬멜로우 이상버전부터 권한을 물어본다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 권한 체크 (READ_PHONE_STATE의 requestCode를 1000으로 세팅
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1000); } return;
        }

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

    private List<String> FileList(String path) {
        File directory = new File(path);
        File[] files = directory.listFiles();

        List<String> fileNameList = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            fileNameList.add(files[i].getName());
        }
        return fileNameList;
    }


    @SuppressLint("LongLogTag")
    public int uploadFile(String sourceFileUri) {
        final String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        if (!sourceFile.isFile()) {
            pDialog.dismiss();
            Log.e("uploadFile", "Source File not exist :"
                    + uploadFilePath + "" + uploadFileName);

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MAT_MainActivity.this, "Source File not exist :" + uploadFilePath + "" + uploadFileName, Toast.LENGTH_SHORT).show();
                }

            });
            return 0;
        } else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri+"?model="+android_id);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                //

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();
                Log.i("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MAT_MainActivity.this, "Uploaded Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch (MalformedURLException ex) {
                pDialog.dismiss();
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(MAT_MainActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                pDialog.dismiss();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        //messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(MAT_MainActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : "
                        + e.getMessage(), e);
            }
            pDialog.dismiss();
            return serverResponseCode;
        } // End else block
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        // READ_PHONE_STATE의 권한 체크 결과를 불러온다
        if(requestCode == 1000) {
            boolean check_result = true; // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) { check_result = false; break; } }
            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if(check_result == true) { } else { finish(); } }
    }

}
