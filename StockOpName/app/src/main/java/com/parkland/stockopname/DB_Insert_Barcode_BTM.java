package com.parkland.stockopname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DB_Insert_Barcode_BTM {

    Connection connect;
    String ConnectionResult = "";

    public String getFac_cd() {
        return fac_cd;
    }

    public void setFac_cd(String fac_cd) {
        this.fac_cd = fac_cd;
    }

    String fac_cd;
    public void insertBarcode(String stockno, String barcode, String fac, String id, String layout){

        PreparedStatement cstmt =null;
        ResultSet rs = null;



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


                cstmt = connect.prepareStatement("INSERT INTO BTM_STOCK_OPNAME_BARCODE_PWJ (STOCK_NO, ART_BARCODE, UKSIZE, QTY, ST_DATE, FAC_CD, ST_CD, ISSUE_TIME, ISSUE_USER, MODIFY_TIME, MODIFY_USER) \n" +
                        "SELECT ?, ART_BARCODE, UKSIZE, QTY, FORMAT(getdate(), 'yyyyMMdd'), ?, (select ST_CD from MST_STORAGE where FAC_CD = ? and ST_TYPE = '1' and ST_CLASS = '306' and USE_YN = 'Y' AND BUILD_CD = ? ), GETDATE(), ?, GETDATE(), ? \n" +
                        "from MES_ART_BARCODE where FAC_CD = ? AND ART_BARCODE = ?");

                cstmt.setString(1, stockno);
                cstmt.setString(2, getFac_cd());
                cstmt.setString(3, getFac_cd());
                cstmt.setString(4, layout);
                cstmt.setString(5, id);
                cstmt.setString(6, id);
                cstmt.setString(7, getFac_cd());
                cstmt.setString(8, barcode);
                System.out.println(cstmt);

                rs = cstmt.executeQuery();


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

    }
}
