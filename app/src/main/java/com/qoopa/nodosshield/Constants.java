package com.qoopa.nodosshield;

/**
 * Created by diego.saavedra on 30/03/2016.
 */
public class Constants {

    public interface ACTION {
        public static String MAIN_ACTION = "com.qoopa.nodosshield.LockService.action.main";
        public static String STARTFOREGROUND_ACTION = "com.qoopa.nodosshield.LockService.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.qoopa.nodosshield.LockService.action.stopforeground";
        public static String PLAYSERVICE = "com.qoopa.nodosshield.LockService.action.playservice";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 1;
    }
}
