package com.fletchto99.pebble;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.Random;

/*
 * A simple example using the Pebble Sports API
 * No watchapp is required as the sports API
 * since it uses a hidden built in sports app on the Pebble
 *
 */
public class PebbleSportsExample extends Activity {

    //Used to generate random time values for the purpose of demonstration
    private final Random rand = new Random();


    //Some states/vars used
    private int sportsState = Constants.SPORTS_STATE_INIT;
    private boolean useMetric = false;
    private boolean isPaceLabel = true;

    //A handler to receive data from the Pebble sports app.
    private PebbleKit.PebbleDataReceiver sportsDataHandler = null;


    //Displays the main window of the application
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sports);
    }

    //Called when the android application is no-longer in the foreground and enters a "paused" state
    @Override
    protected void onPause() {
        super.onPause();

        // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
        if (sportsDataHandler != null) {
            unregisterReceiver(sportsDataHandler);
            sportsDataHandler = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //used to update the UI Asynchronously
        final Handler handler = new Handler();

        /*
         * To receive data back from the sports watch-app, Android
         * applications must register a "DataReceiver" to operate on the
         * dictionaries received from the watch.
         *
         * In this example, we're registering a receiver to listen for
         * changes in the activity state sent from the watch, allowing
         * us the pause/resume the activity when the user presses a
         * button in the watch-app.
         */
        sportsDataHandler = new PebbleKit.PebbleDataReceiver(Constants.SPORTS_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                sportsState = data.getUnsignedIntegerAsLong(Constants.SPORTS_STATE_KEY).intValue();

                PebbleKit.sendAckToPebble(context, transactionId);

                //Post to handler to ensure the state has been updated, but is not blocking
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleStatechange();
                    }
                });
            }
        };

        //Register the received handler, for when an appmessage comes in.
        PebbleKit.registerReceivedDataHandler(this, sportsDataHandler);
    }

    public void handleStatechange() {
        TextView statusText = (TextView) findViewById(R.id.status);

        //For some reason this is backwards... Their example was like that too.
        if (sportsState == Constants.SPORTS_STATE_PAUSED) {
            statusText.setText("Running");
        } else {
            statusText.setText("Paused");
        }
    }

    // Send a broadcast to launch the specified application on the connected Pebble
    public void startWatchApp(View view) {
        PebbleKit.startAppOnPebble(getApplicationContext(), Constants.SPORTS_UUID);
    }

    // Send a broadcast to close the specified application on the connected Pebble
    public void stopWatchApp(View view) {
        PebbleKit.closeAppOnPebble(getApplicationContext(), Constants.SPORTS_UUID);
    }

    /*
     * A custom icon and name can be applied to the sports-app to
     * provide some support for "branding" your Pebble-enabled sports
     * application on the watch.
     *
     * It is recommended that applications customize the sports
     * application before launching it. Only one application may
     * customize the sports application at a time on a first-come,
     * first-serve basis.
     */
    public void customizeWatchApp(View view) {
        final String customAppName = getString(R.string.pebble_app_name);
        final Bitmap customIcon = BitmapFactory.decodeResource(getResources(), R.drawable.watch);

        PebbleKit.customizeWatchApp(
                getApplicationContext(), Constants.PebbleAppType.SPORTS, customAppName, customIcon);
    }

    /* Push (distance, time, pace) data to be displayed on Pebble's Sports app.
     *
     * To simplify formatting, values are transmitted to the watch as strings.
     */
    public void updateWatchApp(View view) {

        //Build some random data to send to the watch app
        String time = String.format("%02d:%02d", rand.nextInt(60), rand.nextInt(60));
        String distance = String.format("%02.02f", 32 * rand.nextDouble());
        String addl_data = String.format("%02d:%02d", rand.nextInt(10), rand.nextInt(60));

        //Build the dictionary of data
        PebbleDictionary data = new PebbleDictionary();
        data.addString(Constants.SPORTS_TIME_KEY, time);
        data.addString(Constants.SPORTS_DISTANCE_KEY, distance);
        data.addString(Constants.SPORTS_DATA_KEY, addl_data);

        //determine if we should show pace or speed
        data.addUint8(Constants.SPORTS_LABEL_KEY, (byte) (isPaceLabel ? Constants.SPORTS_DATA_SPEED : Constants.SPORTS_DATA_PACE));

        //Send the dictionary to the pebble sports app
        PebbleKit.sendDataToPebble(getApplicationContext(), Constants.SPORTS_UUID, data);
        isPaceLabel = !isPaceLabel;
    }

    /*
     * The units in the sports app can be toggled between Metric (1) and Imperial (0)
     *
     * by sending the following message to Pebble once the sports app is running.
     */
    public void changeUnitsOnWatch(View view) {
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(Constants.SPORTS_UNITS_KEY,
                (byte) (useMetric ? Constants.SPORTS_UNITS_METRIC : Constants.SPORTS_UNITS_IMPERIAL));

        //Send the dictionary to the Pebble sports app
        PebbleKit.sendDataToPebble(getApplicationContext(), Constants.SPORTS_UUID, data);
        useMetric = !useMetric;
    }
}
