package com.parkland.stockopname;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DB_Upload {
    /*
    @iFlag				VARCHAR(30),
    @P_FAC_CD			VARCHAR(4),
    @P_LAYOUT_CD		VARCHAR(10),
    @P_UCC_BARCODE		VARCHAR(40) = NULL,
    @P_OLD_RACK_CD		VARCHAR(20) = NULL,
    @P_OLD_LAYER_SEQ	INT			= NULL,
    @P_NEW_RACK_CD		VARCHAR(20) = NULL,
    @P_NEW_LAYER_SEQ	INT			= NULL,
    @P_SPOT_NM			VARCHAR(20) = NULL,
    @P_FG_OUT_TYPE		VARCHAR(2)	= NULL,
    @P_USER_IP  		VARCHAR(20) = NULL,
    @P_USER_CD			VARCHAR(50)
        */
    /*	     @iFlag				= @iFlag
	   , @FAC_CD			= @P_FAC_CD
	   , @LAYOUT_CD			= @P_LAYOUT_CD
	   , @CARTON_BARCODE	= @V_CARTON_BARCODE
	   , @NEW_RACK_CD		= @P_NEW_RACK_CD
	   , @NEW_LAYER_SEQ		= @P_NEW_LAYER_SEQ
	   , @SPOT_NM			= @P_SPOT_NM
	   , @FG_OUT_TYPE		= @P_FG_OUT_TYPE
	   , @USER_CD			= @P_USER_CD;       */
    Connection connect;
    String ConnectionResult = "";

    public String uploadServer(String fac, String label_id, double act_qty, String time, String timeUpdate){

        int reply=0;
        //CallableStatement cstmt = null;
        PreparedStatement cstmt =null;
        ResultSet rs = null;
        //Boolean ok=false;
        String answer;

        String mat = null;
        String spot = null;
        String trolley = null;
        String in = null;
        String out = null;
        String stock =null;

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String getTime = sdf.format(date);
        if(time.equals("0") || time.equals("") || time.isEmpty())
            time = getTime;
        try {
            ConnectionHelper conStr = new ConnectionHelper();
            connect = conStr.connectionclasss(fac);// Connect to database
            answer ="";
            stock="";
            if (connect == null) {
                ConnectionResult = "Check Your Internet Access!";
                Log.e("ERROR CONNECTION : ", ConnectionResult);
                return reply+","+stock+","+getTime+","+mat+","+spot+","+trolley+","+in+","+out;
            } else {
                cstmt = connect.prepareStatement("SP_RMS_LBL_SCAN_HIST  ?,?,?,?");
                cstmt.setString(1,time);   //SCAN_DATE
                cstmt.setString(2, label_id);               //LABEL_ID
                cstmt.setDouble(3, act_qty);                 //ACTUAL_QTY
                cstmt.setString(4, timeUpdate);   //SCAN_DATE Update

                rs = cstmt.executeQuery();

                while (rs.next()) {
                   answer = rs.getString("ERR_FLAG");
                   mat = rs.getString("MAT_CD");
                   spot = rs.getString("SPOT_CD");
                   trolley = rs.getString("TROLLEY_CD");
                   in = rs.getString("IN_QTY");
                   out = rs.getString("OUT_QTY");
                   stock = rs.getString("STOCK_QTY");
                }
                if(answer.equals("S")){
                    reply=1;
                }else reply=0;

            }
        }catch (Exception ex) {
            Log.e("ERROR CONNECTION1 : ", ConnectionResult);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Log.e("ERROR CONNECTION2 : ", ConnectionResult);
                }
            }
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException ex) {
                    Log.e("ERROR CONNECTION3 : ", ConnectionResult);
                }
            }
        }
            return reply+","+stock+","+getTime+","+mat+","+spot+","+trolley+","+in+","+out;
    }

        //EXEC PWIERP.dbo.SP_FMS_SCAN_SAVE FixScanByCarton,'04','','2021020505490070','','','000319','1','','','',''
        // "EXEC PWIERP.dbo.SP_FMS_SCAN_SAVE FixScanByCarton,'04','"+layout_cd+"',' "+carno+"','','','"+rackno+"','"+layno+"','','','','"+userid+"'";



}
