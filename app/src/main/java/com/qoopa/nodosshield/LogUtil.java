package com.qoopa.nodosshield;
// Listo Try - Log

import android.os.Environment;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class LogUtil {

    static boolean debugON = true;

    LogUtil() {

    }

    //Imprime Mensajes de Debug
    public static void printDebug(String tag, String msg) { // Registro - LoginActivity
        try {
            if (debugON) {
                Log.d(tag, msg);
                InfoLog("D1: " + Calendar.getInstance().getTime() + " " + tag + " " + msg);
            }
        } catch (Exception e) {
            printError("Error", "LogUtil - printDebug");
            printFullError("Error", "LogUtil - printDebug", e);
        }
    }

    //Imprime Mensajes de Error
    public static void printError(String tag, String msg) { //CaptureImage - BootStart - LockService - LockActivity
        try {
            if (debugON) {
                if (tag.equals(null)) tag = "";
                if (msg==(null)) msg = "";
                Log.e(tag, msg);
                ErrorLog("E1: " + Calendar.getInstance().getTime() + " " + tag + " " + msg);
            }
        } catch (Exception e) {
            printError("Error","LogUtil - PrintError");
            printFullError("Error","LogUtil - PrintError",e);
        }
    }

    //Imprime Mensajes de Error y la traza e
    public static void printFullError(String tag, String msg, Throwable e) {
        try {
            if (debugON) {
                Log.e(tag, msg, e);
                ErrorLog("E2: " + Calendar.getInstance().getTime() + " " + tag + " " + msg + " " + e);
            }
        } catch (Exception er) {
            printError("Error","LogUtil - PrintFullError");
            printFullError("Error","LogUtil - PrintFullError",er);
        }
    }

    public static void printFullErrorInit(String tag, String msg, Throwable e) { // Database
        try {
            if (debugON) {
                Log.e(tag, msg, e);
                ErrorLog("E3: " + Calendar.getInstance().getTime() + " " + tag + " " + msg + " " + Log.getStackTraceString(e));
            }
        } catch (Exception er) {
            printError("Error","LogUtil - PrintFullErrorInit");
            printFullError("Error", "LogUtil - PrintFullErrorInit", er);
        }
    }

    public static void InfoLog(String text) {

        File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "InfoLogs.txt");

        if (!logFile.exists()) {
            try { //Try01
                logFile.createNewFile();
            } catch (IOException e) {
                printError("Error", "LogUtil - InfoLog - IO Try01");
                printFullError("Error", "LogUtil - InfoLog - IO Try01", e);
            } catch (Exception ex) {
                printError("Error", "LogUtil - InfoLog - Ex Try01");
                printFullError("Error", "LogUtil - InfoLog - Ex Try01", ex);
            }
        }
        try { //Try02
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            printError("Error", "LogUtil - InfoLog - IO Try02");
            printFullError("Error", "LogUtil - InfoLog - IO Try02", e);
        } catch (Exception ex) {
            printError("Error", "LogUtil - InfoLog - Ex Try02");
            printFullError("Error", "LogUtil - InfoLog - Ex Try02", ex);
        }
    }

    public static void ErrorLog(String text) {

        File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ErrLogs.txt");

        if (!logFile.exists()) {
            try { //Try01
                logFile.createNewFile();
            } catch (IOException e) {
                printError("Error", "LogUtil - ErrorLog - IO Try01");
                printFullError("Error", "LogUtil - ErrorLog - IO Try01", e);
            } catch (Exception ex) {
                printError("Error", "LogUtil - ErrorLog - Ex Try01");
                printFullError("Error", "LogUtil - ErrorLog - Ex Try01", ex);
            }
        }
        try { //Try02
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            printError("Error", "LogUtil - ErrorLog - IO Try02");
            printFullError("Error", "LogUtil - ErrorLog - IO Try02", e);
        } catch (Exception ex) {
            printError("Error", "LogUtil - ErrorLog - Ex Try02");
            printFullError("Error", "LogUtil - ErrorLog - Ex Try02", ex);
        }
    }

    public static void LogEnvioDatos(String Url, String Parametros) { // CaptureImage, LockActivity, LockService, LoginActivity

        String text;
        if (Url.equals("Respuesta")) {
            text = Calendar.getInstance().getTime() + " Respuesta => Parametros: "+Parametros;
        } else if (Url.equals("SocketR")) {
            text = Calendar.getInstance().getTime() + " Respuesta Socket => "+Parametros;
        } else if (Url.equals("Socket")) {
            text = Calendar.getInstance().getTime() + " Socket.emit => "+Parametros;
        } else {
            text = Calendar.getInstance().getTime() + " Url: "+Url+"  Parametros: "+Parametros;
        }

        File logFileData = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LogEnvioDatos.txt");

        if (!logFileData.exists()) {
            try { //Try01
                logFileData.createNewFile();
            } catch (IOException e) {
                printError("Error", "LogUtil - LogEnvioDatos - IO Try01");
                printFullError("Error", "LogUtil - LogEnvioDatos - IO Try01", e);
            } catch (Exception ex) {
                printError("Error", "LogUtil - LogEnvioDatos - Ex Try01");
                printFullError("Error", "LogUtil - LogEnvioDatos - Ex Try01", ex);
            }
        }
        try { //Try02
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFileData, true));
            buf.newLine();
            buf.append(text);
            buf.newLine();
            buf.close();
            //Log.e("***Seguimiento", "DataLog Exitoso");
        } catch (IOException e) {
            printError("Error", "LogUtil - LogEnvioDatos - IO Try02");
            printFullError("Error", "LogUtil - LogEnvioDatos - IO Try02", e);
        } catch (Exception ex) {
            printError("Error", "LogUtil - LogEnvioDatos - Ex Try02");
            printFullError("Error", "LogUtil - LogEnvioDatos - Ex Try02", ex);
        }
    }

    public static void LogEnvClaseDatos(String Url, String Parametros) { // JSONParser
        String text;
        if (Url.equals("Respuesta")) {
            text = Calendar.getInstance().getTime() + " Respuesta => Parametros: "+Parametros;
        } else if (Url.equals("SocketR")) {
            text = Calendar.getInstance().getTime() + " Respuesta Socket => "+Parametros;
        } else if (Url.equals("Socket")) {
            text = Calendar.getInstance().getTime() + " Socket.emit => "+Parametros;
        } else {
            text = Calendar.getInstance().getTime() + " Url: "+Url+"  Parametros: "+Parametros;
        }

        File logFileData = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LogEnvClaseDatos.txt");

        if (!logFileData.exists()) {
            try { //Try01
                logFileData.createNewFile();
            } catch (IOException e) {
                printError("Error", "LogUtil - LogEnvClaseDatos - IO Try01");
                printFullError("Error", "LogUtil - LogEnvClaseDatos - IO Try01", e);
            } catch (Exception ex) {
                printError("Error", "LogUtil - LogEnvClaseDatos - Ex Try01");
                printFullError("Error", "LogUtil - LogEnvClaseDatos - Ex Try01", ex);
            }
        }
        try { //Try02
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFileData, true));
            buf.newLine();
            buf.append(text);
            buf.newLine();
            buf.close();
            //Log.e("***Seguimiento", "DataLog Exitoso");
        } catch (IOException e) {
            printError("Error", "LogUtil - LogEnvClaseDatos - IO Try02");
            printFullError("Error", "LogUtil - LogEnvClaseDatos - IO Try02", e);
        } catch (Exception ex) {
            printError("Error", "LogUtil - LogEnvClaseDatos - Ex Try02");
            printFullError("Error", "LogUtil - LogEnvClaseDatos - Ex Try02", ex);
        }
    }
}
