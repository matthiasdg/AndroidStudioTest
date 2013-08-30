package be.iminds.mix.streamstore;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetworkState {

    Context context;
    String currentConnection = "";

    public NetworkState(Context context) {
        this.context = context;
    }

    public void haveNetworkConnection() {

        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni!=null){
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;

            if(haveConnectedWifi)
                currentConnection="WIFI";
            if(haveConnectedMobile){
                TelephonyManager telManager;
                telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                int cType = telManager.getNetworkType();
                switch (cType) {
                    case 1: currentConnection = "GPRS"; break;
                    case 2: currentConnection = "EDGE"; break;
                    case 3: currentConnection = "UMTS"; break;
                    case 8: currentConnection = "HSDPA"; break;
                    case 9: currentConnection = "HSUPA"; break;
                    case 10:currentConnection = "HSPA"; break;
                    default:currentConnection = "unknown"; break;
                }
            }
        }
    }

    public String toString(){
        this.haveNetworkConnection();
        return "\"connection\": \"" + currentConnection + "\"";
    }
}