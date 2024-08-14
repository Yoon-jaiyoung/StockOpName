package com.parkland.stockopname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class DB_Read_by_po {
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

    public ArrayList<String> getCarton(String o_no, String fac, String stockno){

        PreparedStatement cstmt =null;
        ResultSet rs = null;

        HashMap<String, String> hashMap = new HashMap<>();
        ArrayList<String> array = new ArrayList<>();

        try {
            ConnectionHelperPWIERP conStr = new ConnectionHelperPWIERP();
            connect = conStr.connectionclasss(fac);// Connect to database

            if (connect == null) {
                ConnectionResult = "Check Your Internet Access!";
            } else {
                cstmt = connect.prepareStatement("SELECT CM.CARTON_BARCODE , FORMAT(ISSUE_TIME,'hh:mm:ss') as ISSUE_TIME FROM SAL_CARTON_MST CM WITH (NOLOCK) " +
                        "LEFT OUTER JOIN FG_STOCK_OPNAME_CARTON_PWJ STO WITH (NOLOCK) ON STO.CARTON_BARCODE = CM.CARTON_BARCODE AND STOCK_NO = ? " +
                        "WHERE PWI_NO = (SELECT PWI_NO FROM SAL_ORDER_MST WITH (NOLOCK) WHERE O_NO= ?) AND USE_YN = 'Y' ORDER BY CARTON_NO");
                cstmt.setString(1, stockno);
                cstmt.setString(2, o_no);



                rs = cstmt.executeQuery();

                while (rs.next()) {
                    if(rs.getString("ISSUE_TIME") != null)
                        array.add(rs.getString("CARTON_BARCODE")+","+rs.getString("ISSUE_TIME"));
                    else array.add(rs.getString("CARTON_BARCODE")+","+"");
                }
            }
        }catch (Exception ex) {
            //Logger.getLogger(MAT_MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    //Logger.getLogger(MAT_MainActivity.class.getName()).log(Level.WARNING, null, ex);
                }
            }
            if (cstmt != null) {
                try {
                    cstmt.close();
                } catch (SQLException ex) {
                    //Logger.getLogger(MAT_MainActivity.class.getName()).log(Level.WARNING, null, ex);
                }
            }
        }
            return array;
    }
}
