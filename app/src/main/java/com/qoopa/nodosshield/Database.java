package com.qoopa.nodosshield;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian.agreda on 29/08/2015.
 */
public class Database extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "nodosShield";
    public static final int DATABASE_VERSION = 1;
    Context context;
    Database bdAyuda;
    SQLiteDatabase db;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try { //try01
            db.execSQL("CREATE TABLE IF NOT EXISTS localizacion (" +
                    "idLocalizacion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "latitud TEXT NULL, " +
                    "longitud TEXT NULL, " +
                    "fecha TEXT NULL" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS politicas (" +
                    "idPolitica INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "latitud TEXT NULL, " +
                    "longitud TEXT NULL, " +
                    "estado TEXT NULL," +
                    "radio TEXT NULL" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS sim (" +
                    "idSim INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "simnum TEXT" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS off(" +
                    "idOff INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "time TEXT" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS estado(" +
                    "idEstado INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "codigo TEXT," +
                    "estado TEXT" +
                    ");");

            db.execSQL("CREATE TABLE IF NOT EXISTS queue (" +
                    "idqueue INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombreQueue TEXT unique," +
                    "valorQueue TEXT" +
                    ");");

        } catch (Exception e) {
            LogUtil.printError("Error", "Error creando tablas.");
            LogUtil.printError("Error", "Database - onCreate - try01");
            LogUtil.printFullErrorInit("Error", "Database - onCreate - try01", e);
        }

        try { //try02
            //db.execSQL("INSERT INTO sim(idSim,  simnum) VALUES(1,'89577321111487405225')");
            db.execSQL("INSERT into queue (nombreQueue, valorQueue) values('PriorityQueueBool', 'false')");
            db.execSQL("INSERT into queue (nombreQueue, valorQueue) values('PQ1', 'false')");
            db.execSQL("INSERT into queue (nombreQueue, valorQueue) values('PQ2', 'false')");
            db.execSQL("INSERT into queue (nombreQueue, valorQueue) values('PQ3', 'false')");

//            INSERT into queue (nombreQueue, valorQueue) values("PriorityQueueBool", "false")
//            INSERT into queue (nombreQueue, valorQueue) values ("PQ1","false");
//            INSERT into queue (nombreQueue, valorQueue) values ("PQ2","false");
//            INSERT into queue (nombreQueue, valorQueue) values ("PQ3","false");

//            update queue set valorQueue="true" where nombreQueue="PriorityQueueBool";
//            update queue set valorQueue="false" where nombreQueue="PQ1";
//            update queue set valorQueue="true" where nombreQueue="PQ2";
//            update queue set valorQueue="false" where nombreQueue="PQ3";

            Log.e("Seguimiento", "Inicializando Queue Exitoso");
            //consultarQueue();
        } catch (Exception e) {
            LogUtil.printDebug("Error", "Error inicializando Queue");
            LogUtil.printError("Error", "Database - onCreate - try02");
            LogUtil.printFullErrorInit("Error", "Database - onCreate - try02", e);
        }

//        try {
//            consultarQueue();
//        } catch (Exception e) {
//            LogUtil.printDebug("Error", "Error leyendo Queue");
//            LogUtil.printError("Error", "Database - onCreate - try03");
//            LogUtil.printFullErrorInit("Error", "Database - onCreate - try03", e);
//        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS localizacion");
            onCreate(db);
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - onUpgrade");
            LogUtil.printFullErrorInit("Error", "Database - onUpgrade", e);
        }
    }

    public void abrir() {
        try {
            bdAyuda = new Database(context);
            db = bdAyuda.getWritableDatabase();
            LogUtil.printDebug("DataBase", "OPENED");
        } catch (Exception e) {
            db.close();
            LogUtil.printError("Error", "Database - abrir");
            LogUtil.printFullErrorInit("Error", "Database - abrir", e);
        }
    }

    public void cerrar() {
        try {
            db.close();
            LogUtil.printDebug("DataBase", "CLOSED");
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - cerrar");
            LogUtil.printFullErrorInit("Error", "Database - cerrar", e);
        }
    }

    //Metodo para registrar nueva localizacion.
    public long registrarLocalizacion(String latitud, String longitud, String fecha) {
        ContentValues valores = new ContentValues();
        try {
            valores.put("latitud", latitud);
            valores.put("longitud", longitud);
            valores.put("fecha", fecha);
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - registrarLocalizacion");
            LogUtil.printFullErrorInit("Error", "Database - registrarLocalizacion", e);
        }
        return db.insert("localizacion", null, valores);
    }

    //Metodo para registrar el estado
    public long registrarEstado(String codigo, String estado) {
        ContentValues valores = new ContentValues();
        try {
            valores.put("codigo", codigo);
            valores.put("estado", estado);
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - registrarEstado");
            LogUtil.printFullErrorInit("Error", "Database - registrarEstado", e);
        }
        return db.insert("estado", null, valores);
    }

    //Metodo para modificar los valores de la PriorityQueue
    public boolean actualizarQueue (String variable, String valor) {
        ContentValues valores = new ContentValues();
        try {
            //valores.put("nombreQueue", variable);
            valores.put("valorQueue", valor);
            //db.execSQL("update queue set valorQueue=" + valor + " where nombreQueue=" + variable + "");
            String dat = "where nombreQueue="+variable;
            String query = "update queue set valorQueue='" + valor + "' where nombreQueue='" + variable + "'";
            LogUtil.printError("Error", "query123: " + query);
            //db.update("queue",valores,"2="+variable,null);
            db.execSQL(query);
            //db.update()
            //db.update("queue",valores,dat,null);
            LogUtil.printError("Error", "No hay Error durante actualizarQueue");
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - actualizarQueue");
            LogUtil.printFullErrorInit("Error", "Database - actualizarQueue", e);
        }
        return true;//db.insert("queue", null, valores);
    }

    public String[][] consultarQueue() throws Exception {
        Cursor c = null;
        String[][] datos = null;
        int i = 0;
        try { //try01
            c = db.rawQuery("SELECT * FROM queue", null);
            datos = new String[c.getCount()][3];
            i = 0;
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - consultarQueue - try01");
            LogUtil.printFullErrorInit("Error", "Database - consultarQueue - try01", e);
        }

        try { //try02
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                Log.e("cursor", c.getString(c.getColumnIndex("idqueue")) + " - " + c.getString(c.getColumnIndex("nombreQueue")) + " - " + c.getString(c.getColumnIndex("valorQueue")));
                String nombre = c.getString(c.getColumnIndex("nombreQueue"));
                String valor = c.getString(c.getColumnIndex("valorQueue"));
                LockService.ActValuesPQ(nombre,valor);
                datos[i][0] = c.getString(c.getColumnIndex("idqueue"));
                datos[i][1] = c.getString(c.getColumnIndex("nombreQueue"));
                datos[i][2] = c.getString(c.getColumnIndex("valorQueue"));
                i++;
            }
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
            Log.e("queue", "**************************************************");
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - consultarQueue - try02");
            LogUtil.printFullErrorInit("Error", "Database - consultarQueue - try02", e);
        }
        return datos;
    }

    public void borrarEstado() {
        try {
            db.execSQL("DELETE FROM estado");
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - borrarEstado");
            LogUtil.printFullErrorInit("Error", "Database - borrarEstado", e);
        }
    }

    public void borrarLocalizacion() {
        try {
            db.execSQL("DELETE FROM localizacion");
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - borrarEstado");
            LogUtil.printFullErrorInit("Error", "Database - borrarEstado", e);
        }
    }

    //Metodo para consultar todas las localizaciones en una matriz.
    public String[][] consultarLocalizacion() throws Exception {
        Cursor c = null;
        String[][] datos = null;
        int i = 0;
        try { //try01
            c = db.rawQuery("SELECT * FROM localizacion", null);
            datos = new String[c.getCount()][5];
            i = 0;
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - consultarLocalizacion - try01");
            LogUtil.printFullErrorInit("Error", "Database - consultarLocalizacion - try01", e);
        }

        try { //try02
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                Log.e("cursor", i + "");
                datos[i][0] = c.getString(c.getColumnIndex("idLocalizacion"));
                datos[i][1] = c.getString(c.getColumnIndex("latitud"));
                datos[i][2] = c.getString(c.getColumnIndex("longitud"));
                datos[i][3] = c.getString(c.getColumnIndex("fecha"));
                i++;
            }
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - consultarLocalizacion - try02");
            LogUtil.printFullErrorInit("Error", "Database - consultarLocalizacion - try02", e);
        }
        return datos;
    }

    public List<String> consultarSim() throws Exception {
        Cursor c = null;
        List<String> datos = null;

        try { //try01
            c = db.rawQuery("SELECT * FROM sim", null);
            datos = new ArrayList<>();
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - consultarSim - try01");
            LogUtil.printFullErrorInit("Error", "Database - consultarSim - try01", e);
        }

        try { //try02
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                datos.add(c.getString(c.getColumnIndex("simnum")));
            }
        } catch (Exception e) {
            LogUtil.printError("Error", "Database - consultarSim - try02");
            LogUtil.printFullErrorInit("Error", "Database - consultarSim - try02", e);
        }
        return datos;
    }

    //****************************** Pendientes Por Try-Log Porque no se Usan ******************************
    public Database(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
        this.context = context;
    }

    //Metodo para registrar la politica.
    public long registrarPolitica(String latitud, String longitud, String estado, String radio) {
        ContentValues valores = new ContentValues();
        valores.put("latitud", latitud);
        valores.put("longitud", longitud);
        valores.put("estado", estado);
        valores.put("radio", radio);
        return db.insert("politicas", null, valores);
    }

    // Metodo para registrar la sim.
    public long registrarSim(String idSim, String simnum) {

        ContentValues valores = new ContentValues();
        valores.put("simnum", simnum);
        return db.insert("sim", null, valores);
    }

    // Metodo para registrar politica offline
    public long registrarOffline(String idOff, String time) {

        ContentValues valores = new ContentValues();
        valores.put("time", time);
        return db.insert("off", null, valores);
    }

    //Metodo para borrar las localizaciones despues de enviarlas
    public void borrarPolitica() throws Exception {
        db.rawQuery("DELETE FROM politicas;", null);

    }

    public String[][] consultarPoliticas() throws Exception {

        Cursor c = db.rawQuery("SELECT * FROM politicas", null);
        String[][] datos = new String[c.getCount()][5];
        int i = 0;
        try {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

                datos[i][0] = c.getString(c.getColumnIndex("idPolitica"));
                datos[i][1] = c.getString(c.getColumnIndex("latitud"));
                datos[i][2] = c.getString(c.getColumnIndex("longitud"));
                datos[i][3] = c.getString(c.getColumnIndex("estado"));
                datos[i][4] = c.getString(c.getColumnIndex("radio"));
                i++;
            }
        } catch (Exception e) {
            LogUtil.printFullErrorInit("DB", " EXCEPTION ON CONSULT POLITIC", e);
        }
        return datos;
    }

    public String[] consultarEstado() {
        Cursor c = db.rawQuery("SELECT * FROM estado", null);

        try {
            if (c.isFirst()) {
                String[] consulta = new String[2];
                consulta[0] = c.getString(c.getColumnIndex("estado"));
                consulta[1] = c.getString(c.getColumnIndex("codigo"));

                return consulta;
            }
        } catch (Exception e) {
            LogUtil.printFullErrorInit("DB", " EXCEPTION ON CONSULT STATE", e);
        }
        return null;
    }

    public String consultarOffline() throws Exception {

        Cursor c = db.rawQuery("SELECT * FROM off", null);
        String datos = null;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            datos = c.getString(c.getColumnIndex("time"));
            Log.e("off", c.getString(c.getColumnIndex("time")) + "," + c.getString(c.getColumnIndex("idOff")));
        }

        return datos;
    }

}
