/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hr.adinfo.client;

import hr.adinfo.utils.LoadingDialog;
import hr.adinfo.utils.Values;
import hr.adinfo.utils.communication.ServerQueryTask;
import hr.adinfo.utils.communication.ServerResponse;
import hr.adinfo.utils.database.DatabaseQuery;
import hr.adinfo.utils.database.DatabaseQueryResponse;
import hr.adinfo.utils.database.DatabaseQueryResult;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;

/**
 *
 * @author Ismedin
 */
public class Tecaj {
    
    public static int ID;
    public static int PRICE_CONVERTED;
    public static int APP_CONVERTED;
   
    public static void Init(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){

                    if(getDataFromDB()){
                        try {
                            Thread.sleep(1000000000);
                    } catch (InterruptedException ex) {}
                    }
                    try {
                            Thread.sleep(1000);
                    } catch (InterruptedException ex) {}
                }
            }
        }).start();
    }
    public static boolean setTecaj(){
        SimpleDateFormat formater = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        Date date = new Date();
        String strDate = "01.01.2023 0:0:0.001";
        Date d1 = null;
        try{
            d1 = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")).parse(strDate);
        }catch(Exception e){e.printStackTrace();}
        if(date.compareTo(d1) >= 0){
            setArticlePrice();
            setTRPrice();
            setServicesPrice(); 
            updateTecaj(); 
            return true;
        }
        return false;
    }
    
    public static void updateTecaj(){
        final JDialog loadingDialog = new LoadingDialog(null, true); 
        String q = "UPDATE TECAJ SET PRICE_CONVERTED = ?, APP_CONVERTED = ? WHERE ID >= 0";
        DatabaseQuery dbQryTecaj = new DatabaseQuery(q);
        dbQryTecaj.AddParam(1, 1);
        dbQryTecaj.AddParam(2, 1);
        ServerQueryTask dbTaskTecaj = new ServerQueryTask(loadingDialog, dbQryTecaj, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        dbTaskTecaj.execute();
        if(!dbTaskTecaj.isDone()){
            dbTaskTecaj.cancel(true);
        } else{
            System.out.println("Konverzija cijena je završena");
        }
    }
    
    private static boolean getDataFromDB(){
        final JDialog loadingDialog = new LoadingDialog(null, true);	
        DatabaseQuery databaseQuery = new DatabaseQuery("SELECT APP_CONVERTED FROM TECAJ WHERE 1 = 1");
        ServerQueryTask databaseQueryTask = new ServerQueryTask(loadingDialog, databaseQuery, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());

        databaseQueryTask.execute();
        loadingDialog.setVisible(false);
        if(!databaseQueryTask.isDone()){
                databaseQueryTask.cancel(true);
        } else{
            try{
                ServerResponse serverResponse = databaseQueryTask.get();
                DatabaseQueryResult databaseQueryResult = null;
                if(serverResponse != null && serverResponse.errorCode == Values.RESPONSE_ERROR_CODE_SUCCESS){
                    databaseQueryResult = ((DatabaseQueryResponse) serverResponse).databaseQueryResult;
                }
                if(databaseQueryResult != null){
                    while (databaseQueryResult.next()){                        
                        if( databaseQueryResult.getInt(0) == -1)
                        {
                            if(setTecaj()){
                                return true;
                            }
                        }

                    }
                }
            }catch (InterruptedException | ExecutionException ex) {
                ClientAppLogger.GetInstance().ShowErrorLog(ex);
            }
        }
        return false;
    }
    
    //konverzija cijena u EUR 
    //Artikli
    
    
    
    private static boolean setArticlePrice(){
        final JDialog loadingDialog = new LoadingDialog(null, true);
        String q = "UPDATE ARTICLES SET PRICE = (PRICE/7.53450), EVENT_PRICE = (EVENT_PRICE/7.53450) WHERE ID > 0";
        DatabaseQuery dbQryTecaj = new DatabaseQuery(q);
        ServerQueryTask dbTaskTecaj = new ServerQueryTask(loadingDialog, dbQryTecaj, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        dbTaskTecaj.execute();
        if(!dbTaskTecaj.isDone()){
            dbTaskTecaj.cancel(true);
        } else{
            System.out.println("Konverzija cijena je završena");
            return true;
        }
        return false;
    }
    
    private static boolean setTRPrice(){
        final JDialog loadingDialog = new LoadingDialog(null, true);
        String q = "UPDATE TRADING_GOODS SET PRICE = (PRICE/7.53450), EVENT_PRICE = (EVENT_PRICE/7.53450) WHERE ID > 0";
        DatabaseQuery dbQryTecaj = new DatabaseQuery(q);
        ServerQueryTask dbTaskTecaj = new ServerQueryTask(loadingDialog, dbQryTecaj, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        dbTaskTecaj.execute();
        if(!dbTaskTecaj.isDone()){
            dbTaskTecaj.cancel(true);
        } else{
            System.out.println("Konverzija cijena je završena");
            return true;
        }
        return false;
    }
    
    private static boolean setServicesPrice(){
        final JDialog loadingDialog = new LoadingDialog(null, true);
        String q = "UPDATE SERVICES SET PRICE = (PRICE/7.53450), EVENT_PRICE = (EVENT_PRICE/7.53450) WHERE ID > 0";
        DatabaseQuery dbQryTecaj = new DatabaseQuery(q);
        ServerQueryTask dbTaskTecaj = new ServerQueryTask(loadingDialog, dbQryTecaj, ClientAppLocalServerClient.GetInstance(), ClientAppLogger.GetInstance());
        dbTaskTecaj.execute();
        if(!dbTaskTecaj.isDone()){
            dbTaskTecaj.cancel(true);
        } else{
            System.out.println("Konverzija usluga je završena");
            return true;
        }
        return false;
    }
}
