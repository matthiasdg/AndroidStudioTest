//package be.iminds.mix.streamstore;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.widget.Toast;
//
//import com.dsi.ant.plugins.AntPluginMsgDefines;
//import com.dsi.ant.plugins.AntPluginPcc;
//import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
//
///**
//* Created by matthias on 14/06/13.
//*/
//public class HeartRateTracker {
//    AntPlusHeartRatePcc hrPcc = null;
//
//    public HeartRateTracker(final Activity activity, final Context ctx){
//        if(hrPcc != null)
//        {
//            hrPcc.releaseAccess();
//            hrPcc = null;
//        }
//        AntPlusHeartRatePcc.requestAccess(activity, ctx,
//                new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>(){
//                    @Override
//                    public void onResultReceived(AntPlusHeartRatePcc result, int resultCode,
//                                                 int initialDeviceStateCode)
//                    {
//                        switch(resultCode)
//                        {
//                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatSUCCESS:
//                                hrPcc = result;
//                                Toast.makeText(ctx, result.getDeviceName() + ": " + AntPlusHeartRatePcc.statusCodeToPrintableString(initialDeviceStateCode), Toast.LENGTH_LONG).show();
//                                subscribeToEvents();
//                                break;
//                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatCHANNELNOTAVAILABLE:
//                                Toast.makeText(ctx, "Channel Not Available", Toast.LENGTH_SHORT).show();
////                                tv_status.setText("Error. Do Menu->Reset.");
//                                break;
//                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatOTHERFAILURE:
//                                Toast.makeText(ctx, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
////                                tv_status.setText("Error. Do Menu->Reset.");
//                                break;
//                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatDEPENDENCYNOTINSTALLED:
////                                tv_status.setText("Error. Do Menu->Reset.");
//                                AlertDialog.Builder adlgBldr = new AlertDialog.Builder(ctx);
//                                adlgBldr.setTitle("Missing Dependency");
//                                adlgBldr.setMessage("The required application\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n is not installed. Do you want to launch the Play Store to search for it?");
//                                adlgBldr.setCancelable(true);
////                                adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
////                                {
////                                    @Override
////                                    public void onClick(DialogInterface dialog, int which)
////                                    {
////                                        Intent startStore = null;
////                                        startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
////                                        startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////
////                                        Activity_HeartRateSampler.this.startActivity(startStore);
////                                    }
////                                });
////                                adlgBldr.setNegativeButton("Cancel", new OnClickListener()
////                                {
////                                    @Override
////                                    public void onClick(DialogInterface dialog, int which)
////                                    {
////                                        dialog.dismiss();
////                                    }
////                                });
//
//                                final AlertDialog waitDialog = adlgBldr.create();
//                                waitDialog.show();
//                                break;
//                            case AntPluginMsgDefines.MSG_REQACC_RESULT_whatUSERCANCELLED:
//                                Toast.makeText(ctx, "Canceled", Toast.LENGTH_SHORT).show();
//                                break;
//                            default:
//                                Toast.makeText(ctx, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
////                                tv_status.setText("Error. Do Menu->Reset.");
//                                break;
//                        }
//                    }
//                    private void subscribeToEvents()
//                    {
//                        hrPcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver()
//                        {
//                            @Override
//                            public void onNewHeartRateData(final int currentMessageCount,
//                                                           final int computedHeartRate, final long heartBeatCounter)
//                            {
//                                activity.runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
////                                      run  tv_msgsRcvdCount.setText(String.valueOf(currentMessageCount));
//                                        Toast.makeText(ctx, "messagecount: " + String.valueOf(currentMessageCount) + " comp heartrate " + String.valueOf(computedHeartRate) + " heartbeatcounter " + String.valueOf(heartBeatCounter), Toast.LENGTH_SHORT).show();
////                                        tv_computedHeartRate.setText(String.valueOf(computedHeartRate));
////                                        tv_heartBeatCounter.setText(String.valueOf(heartBeatCounter));
//                                    }
//                                });
//                            }
//                        });
//                    }
//
//                }, new AntPluginPcc.IDeviceStateChangeReceiver(){
//                    @Override
//                    public void onDeviceStateChange(final int newDeviceState)
//                    {
//                       activity.runOnUiThread(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                Toast.makeText(ctx, hrPcc.getDeviceName() + ": " + AntPlusHeartRatePcc.statusCodeToPrintableString(newDeviceState), Toast.LENGTH_LONG).show();
////                                tv_status.setText(hrPcc.getDeviceName() + ": " + AntPlusHeartRatePcc.statusCodeToPrintableString(newDeviceState));
////                                if(newDeviceState == AntPluginMsgDefines.DeviceStateCodes.DEAD)
////
//                            }
//                        });
//
//
//                    }
//                });
//    }
//}
