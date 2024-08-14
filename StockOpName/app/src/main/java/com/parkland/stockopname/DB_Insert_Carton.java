package com.parkland.stockopname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class DB_Insert_Carton {

    Connection connect;
    String ConnectionResult = "";

    public String getFac_cd() {
        return fac_cd;
    }

    public void setFac_cd(String fac_cd) {
        this.fac_cd = fac_cd;
    }

    String fac_cd;
    String answer;
    int reply;
    public void insertCarton(String stockno, String carton, String fac, String id, String grade, String layout){

        PreparedStatement cstmt =null;
        ResultSet rs = null;

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String getTime = sdf.format(date);

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

/*
                cstmt = connect.prepareStatement("INSERT INTO FG_STOCK_OPNAME_CARTON (STOCK_NO, CARTON_BARCODE, ST_DATE, FAC_CD, ST_CD, ISSUE_TIME, ISSUE_USER, PWI_NO, FG_GRADE, QTY, CREATE_DATE, CREATE_USER, MODIFY_DATE, MODIFY_USER) " +
                        "SELECT '"+stockno+"', '"+carton+"', FORMAT(getdate(), 'yyyyMMdd'), '"+getFac_cd()+"', REF_04, GETDATE(), '"+id+"', (SELECT PWI_NO FROM SAL_CARTON_DET with(nolock) WHERE CARTON_BARCODE = '"+carton+"'), '"+grade+"', (SELECT SUM(QTY) FROM SAL_CARTON_DET with(nolock) WHERE CARTON_BARCODE = '"+carton+"')" +
                        ", GETDATE(), '"+id+"', GETDATE(), '"+id+"' " +
                        "from mst_comm_det where com_cd = 'FMS_WH_LAYOUT' AND USE_YN = 'Y' AND REF_01 = '"+getFac_cd()+"' AND com_desc = '"+layout+"'");
                System.out.println(cstmt);
*/

                cstmt = connect.prepareStatement("SP_FG_STOCKOPNAME_IMPORT 'ImportCarton', '"+getFac_cd()+"', null, null,    '"+carton+"', null, null, null, null, null, 0,    '"+stockno+"', '"+layout+"', '"+grade+"', '"+getTime+"', '"+id+"', '"+id+"'");
                //cstmt = connect.prepareStatement("SP_FG_STOCKOPNAME_IMPORT 'ImportCarton','04',NULL,NULL,'202212090404000095',NULL,NULL,NULL,NULL,NULL,0,'PWJ-00-T','14402','A','2023-02-22 10:10:00.000','TESTERP','TESTERP'");
/*
                cstmt.setString(1, "ImportCarton");
                cstmt.setString(2, getFac_cd());
                cstmt.setNull(3, Types.NULL);
                cstmt.setNull(4, Types.NULL);

                cstmt.setString(5, carton);
                cstmt.setNull(6, Types.NULL);
                cstmt.setNull(7, Types.NULL);
                cstmt.setNull(8, Types.NULL);
                cstmt.setNull(9, Types.NULL);
                cstmt.setNull(10, Types.NULL);
                cstmt.setInt(11, 0);

                cstmt.setString(12, stockno);
                cstmt.setString(13, layout);
                cstmt.setString(14, grade);
                cstmt.setString(15, getTime);
                cstmt.setString(16, id);
                cstmt.setString(17, id);
*/
                System.out.println(cstmt);
                rs = cstmt.executeQuery();

                while (rs.next()) {
                    answer = rs.getString("RTN_CODE");
                }
                if(answer.equals("S")){
                    reply=1;
                }else reply=0;


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
