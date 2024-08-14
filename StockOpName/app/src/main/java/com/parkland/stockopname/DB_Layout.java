package com.parkland.stockopname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class DB_Layout {
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

    public String getFac_cd() {
        return fac_cd;
    }

    public void setFac_cd(String fac_cd) {
        this.fac_cd = fac_cd;
    }

    String fac_cd;
    public ArrayList<String> getLayout(String fac){

        PreparedStatement cstmt =null;
        ResultSet rs = null;

        ArrayList<String> layout_list = new ArrayList<String>();;

        try {
            ConnectionHelperPWIERP conStr = new ConnectionHelperPWIERP();
            connect = conStr.connectionclasss(fac);// Connect to database

            if (connect == null) {
                ConnectionResult = "Check Your Internet Access!";
            } else {
                switch(fac){
                    case "PWI" : setFac_cd("01");
                        break;
                    case "PWN" : setFac_cd("02");
                        break;
                    case "PWJ" : setFac_cd("04");
                        break;
                    case "PWA" : setFac_cd("07");
                        break;
                    case "PNP" : setFac_cd("51");
                        break;
                    default:
                }
                cstmt = connect.prepareStatement("SELECT COM_DESC from mst_comm_det where com_cd = 'FMS_WH_LAYOUT' and USE_YN='Y' AND REF_01= ? ORDER BY SORT_SEQ DESC");
                cstmt.setString(1, getFac_cd());   //SCAN_DATE


                rs = cstmt.executeQuery();

                while (rs.next()) {
                    layout_list.add(rs.getString("COM_DESC"));
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
            return layout_list;
    }
}
