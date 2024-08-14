package com.parkland.stockopname;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Alaeddin on 5/21/2017.
 */

public class ConnectionHelper {

    String ip,db,DBUserNameStr,DBPasswordStr;


    @SuppressLint("NewApi")
    public Connection connectionclasss(String fac)
    {

        // Declaring Server ip, username, database name and password
        if (fac.equals("PWJ"))
            ip = "192.168.40.176";
        else if(fac.equals("PWI"))
            ip = "192.168.3.176";
        else if(fac.equals("PWN"))
            ip = "192.168.3.176";
        else if(fac.equals("PWA"))
            ip = "192.168.70.176";
        else if(fac.equals("PNP"))
            ip = "10.20.3.176";
        else ip = "192.168.40.176";
        db = "PWIFMS";
        DBUserNameStr = "sa";
        DBPasswordStr = "!Q@W#E";
        // Declaring Server ip, username, database name and password


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String ConnectionURL = null;
        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + ip +";databaseName="+ db + ";user=" + DBUserNameStr+ ";password=" + DBPasswordStr + ";";
            connection = DriverManager.getConnection(ConnectionURL);
        }
        catch (SQLException se)
        {
            Log.e("error here 1 : ", se.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("error here 2 : ", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("error here 3 : ", e.getMessage());
        }
        return connection;
    }
}