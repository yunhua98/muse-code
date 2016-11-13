package com.yzhao.musecode;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.choosemuse.libmuse.Accelerometer;
import com.choosemuse.libmuse.AnnotationData;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.LibmuseVersion;
import com.choosemuse.libmuse.MessageType;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConfiguration;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseFileFactory;
import com.choosemuse.libmuse.MuseFileReader;
import com.choosemuse.libmuse.MuseFileWriter;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;
import com.choosemuse.libmuse.Result;
import com.choosemuse.libmuse.ResultLevel;
import com.choosemuse.libmuse.LogManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.speech.tts.TextToSpeech;
import java.util.Locale;

import static com.choosemuse.libmuse.MuseDataPacketType.ACCELEROMETER;

public class MainActivity extends Activity implements View.OnClickListener {

    // used for logging
    private final String TAG = "MuseCode";

    // used to detect muse headbands
    private MuseManagerAndroid manager;

    // our muse object
    private Muse muse;

    // notified when connection state of headband changes
    private ConnectionListener connectionListener;

    // This is how we get our data
    private MuseDataListener dataListener;


    private final double[] accelBuffer = new double[3];
    private boolean accelStale;

    // these hold the state of our EKG data


    /**
     * these hold the state of our EKG data. the last state variables are used to ensure we don't record
     * multiple blinks or jaw clenches at a time
     */
    private boolean blink = false;
    private boolean lastBlink = false;
    private boolean jawClench = false;
    private boolean lastJawClench = false;

    // for updating UI
    private final Handler handler = new Handler();


    private int EMGcounter = 0;
    private boolean EMGflag = false;

    // for the list of Muses
    private ArrayAdapter<String> spinnerAdapter;

    // check whether data transmission is enabled
    private boolean dataTransmission = true;

    // left tilt threshold
    private final float LEFT_THRESHOLD = -0.25f;

    // nod threshold
    private final float FRONT_THRESHOLD = 0.35f;

    // running queue of data from accelerometer to determine nods for next character function
    private AccelerometerData nodQ = new AccelerometerData(FRONT_THRESHOLD);

    // running queue of data from accelerometer to determine left tilts for backspace function
    private AccelerometerData backspaceQ = new AccelerometerData(LEFT_THRESHOLD);

    // list of current characters to be displayed
    private StringBuilder translation = new StringBuilder();

    // this holds the sequence of signals that we receive
    private SignalQueue sigQ = new SignalQueue();



    // adapter for the translateTextView
    //private translationAdapter tAdapter;
    // counter views
    private TextView blinkView, jawView;

    // test vars
    private int blinkCount = 0;
    private int jawCount = 0;
    private int nodCount = 0;
    private int tiltCount = 0;

    // morse dictionary converter
    private MorseDictionary dict = new MorseDictionary();

    // TextView of the initial Morse code
    private TextView morseTextView;
    // list of morseCode strings
    private ArrayList<String> morseSequences;

    // used to hold EMGData and determine if an event occured

    EMGData jawData = new EMGData();
    EMGData blinkData = new EMGData();

    // used to count artifact cycles
    private int packetCounter = 0;

    // TextView of the text translated from Morse
    private TextView translateTextView;

    private final Runnable tickUi = new Runnable() {
        @Override
        public void run() {

            //updateEMG();
            updateHead();

            //updateTranslation();
            handler.postDelayed(tickUi, 1000 / 60);
        }
    };

    // this is for TextToSpeech
    TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);

        // listener for connection state changes
        WeakReference<MainActivity> weakActivity = new WeakReference<>(this);
        connectionListener = new ConnectionListener(weakActivity);


        dataListener = new DataListener(weakActivity);
        // Register a listener to receive notifications of what Muse headbands
        // we can connect to.
        manager.setMuseListener(new MuseL(weakActivity));

        ensurePermissions();

        initUI();

        handler.post(tickUi);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect) {
            manager.stopListening();

            List<Muse> availableMuses = manager.getMuses();
            Spinner musesSpinner = (Spinner) findViewById(R.id.muses_spinner);

            if (availableMuses.size() < 1 || musesSpinner.getAdapter().getCount() < 1) {
                Log.w(TAG, "There is nothing to connect to");
            } else {

                // Cache the Muse that the user has selected.
                muse = availableMuses.get(musesSpinner.getSelectedItemPosition());
                // Unregister all prior listeners and register our data listener to
                // receive the MuseDataPacketTypes we are interested in.  If you do
                // not register a listener for a particular data type, you will not
                // receive data packets of that type.
                muse.unregisterAllListeners();
                muse.registerConnectionListener(connectionListener);
                muse.registerDataListener(dataListener, MuseDataPacketType.ARTIFACTS);
                muse.registerDataListener(dataListener, MuseDataPacketType.ACCELEROMETER);


                // Initiate a connection to the headband and stream the data asynchronously.
                muse.runAsynchronously();
            }
        }

        else if(v.getId() == R.id.refresh) {
            manager.stopListening();
            manager.startListening();
        }
        else if (v.getId() == R.id.disconnect) {

            // The user has pressed the "Disconnect" button.
            // Disconnect from the selected Muse.
            if (muse != null) {
                muse.disconnect(false);
            }

        } else if (v.getId() == R.id.pause) {

            // The user has pressed the "Pause/Resume" button to either pause or
            // resume data transmission.  Toggle the state and pause or resume the
            // transmission on the headband.
            if (muse != null) {
                dataTransmission = !dataTransmission;
                muse.enableDataTransmission(dataTransmission);
            }
        }
    }

    /**
     * You will receive a callback to this method each time a headband is discovered.
     * In this example, we update the spinner with the MAC address of the headband.
     */
    public void museListChanged() {
        final List<Muse> list = manager.getMuses();
        spinnerAdapter.clear();
        for (Muse m : list) {
            spinnerAdapter.add(m.getName() + " - " + m.getMacAddress());
        }

    }

    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
        // valuesSize returns the number of data values contained in the packet.
        final long n = p.valuesSize();
        // check if it's accelerometer data
        if (p.packetType() == ACCELEROMETER) {
            assert (accelBuffer.length >= n);
            getAccelValues(p);
            accelStale = true;
        }
        updateHead();
    }

    // get the blink and jawClench data here
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
        //lastBlink = blink;
        if(packetCounter == 0) { // packetCounter is reset after 5 ignored packets
            blink = p.getBlink();
            //lastJawClench = jawClench;
            jawClench = p.getJawClench();
            jawData.add(jawClench);
            blinkData.add(blink);
            updateEMG();
        }
        else { // if we just got an action reading
            packetCounter = (packetCounter+1)%5; // wait for 5 packets before scanning again
        }
    }

    // update the displayed EMG values
    public void updateEMG() {
        TextView jawView = (TextView) findViewById(R.id.jaw);
        if(jawData.actionOccured(1)) {
            jawData.data.clear();
            jawCount++;
            blinkData.data.clear();
            packetCounter = 1; // set packetCounter to 1 to start ignoring packets
        }
        jawView.setText(String.format("jaw: %d\n", jawCount));
        TextView blinkView = (TextView) findViewById(R.id.blink);
        if(blinkData.actionOccured(1)) {
            blinkData.data.clear();
            blinkCount++;
            jawData.data.clear();
            packetCounter = 1; // set packetCounter to 1 to start ignoring packets
        }
        blinkView.setText(String.format("blink: %d\n", blinkCount));
    }

    // update the displayed Head Action values

    public void updateHead() {
        TextView nodText = (TextView) findViewById(R.id.nod);
        if(nodQ.isTilt()) {
            t1.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
        }
        nodCount += (nodQ.isTilt()? 1 : 0);
        nodText.setText(String.format("head nod: %d\n", nodCount));
        TextView tiltText = (TextView) findViewById(R.id.tilt);
        tiltCount += (backspaceQ.isTilt()? 1 : 0);
        tiltText.setText(String.format("head tilt: %d\n", tiltCount));

    }


    public void updateTranslation() { // uses sigQ, nodQ, and backspaceQ to update text
        if (nodQ.isTilt()) {
            if (sigQ.size() == 0) translation.append(" ");
            else {
                char letter = dict.translate(sigQ);
                if (letter != (char) Character.MIN_VALUE) translation.append(letter);
            }
            sigQ.clear(); // nodding clears current signal regardless of successful translation
        }
        else if (backspaceQ.isTilt()) {
            translation.deleteCharAt(translation.length() - 1);
            sigQ.clear();
        }
        translateTextView.setText(translation.toString());
    }

    private void getAccelValues(MuseDataPacket p) {
        nodQ.add((float)(p.getAccelerometerValue(Accelerometer.X)));
        backspaceQ.add((float)(p.getAccelerometerValue(Accelerometer.Y)));
        //accelBuffer[2] = p.getAccelerometerValue(Accelerometer.Z);

    }

    /**
     * You will receive a callback to this method each time there is a change to the
     * connection state of one of the headbands.
     *
     * @param p    A packet containing the current and prior connection states
     * @param muse The headband whose state changed.
     */
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();

        final String status = p.getPreviousConnectionState() + " -> " + current;
        // Update the UI with the change in connection state.
        handler.post(new Runnable() {
            @Override
            public void run() {

                final TextView statusText = (TextView) findViewById(R.id.con_status);
                statusText.setText(status);

                final MuseVersion museVersion = muse.getMuseVersion();
                //final TextView museVersionText = (TextView) findViewById(R.id.version);
                // If we haven't yet connected to the headband, the version information
                // will be null.  You have to connect to the headband before either the
                // MuseVersion or MuseConfiguration information is known.
                if (museVersion != null) {
                    final String version = museVersion.getFirmwareType() + " - "
                            + museVersion.getFirmwareVersion() + " - "
                            + museVersion.getProtocolVersion();
                    //museVersionText.setText(version);
                } /*else {
                    museVersionText.setText(R.string.undefined);
                }
                */
            }
        });

        if (current == ConnectionState.DISCONNECTED) {
            // We have disconnected from the headband, so set our cached copy to null.
            this.muse = null;
        }
    }



    /**
     * Classes go here;
     */

    class MuseL extends MuseListener {
        final WeakReference<MainActivity> activityRef;

        MuseL(final WeakReference<MainActivity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void museListChanged() {
            activityRef.get().museListChanged();
        }
    }

    class ConnectionListener extends MuseConnectionListener {
        final WeakReference<MainActivity> activityRef;

        ConnectionListener(final WeakReference<MainActivity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
            activityRef.get().receiveMuseConnectionPacket(p, muse);
        }
    }

    // datalistener class goes here:

    class DataListener extends MuseDataListener {
        final WeakReference<MainActivity> activityRef;


        DataListener(final WeakReference<MainActivity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            activityRef.get().receiveMuseDataPacket(p, muse);
        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
            activityRef.get().receiveMuseArtifactPacket(p, muse);
        }
    }

    // we initialize the UI here
    private void initUI() {
        setContentView(R.layout.activity_main);
        Button refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = (Button) findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);
        Button pauseButton = (Button) findViewById(R.id.pause);
        pauseButton.setOnClickListener(this);

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        Spinner musesSpinner = (Spinner) findViewById(R.id.muses_spinner);
        musesSpinner.setAdapter(spinnerAdapter);

        //morseTextView = (TextView) findViewById(R.id.morseCode);
        //mAdapter = new MorseAdapter(sigQ, nodQ, backspaceQ);
        //morseTextView.setAdapter(mAdapter);

        translateTextView = (TextView) findViewById(R.id.translation);
        morseTextView = (TextView) findViewById(R.id.morse_code);
        morseSequences = new ArrayList<>();
    }

    /**
     * The ACCESS_COARSE_LOCATION permission is required to use the
     * Bluetooth Low Energy library and must be requested at runtime for Android 6.0+
     * On an Android 6.0 device, the following code will display 2 dialogs,
     * one to provide context and the second to request the permission.
     * On an Android device running an earlier version, nothing is displayed
     * as the permission is granted from the manifest.
     *
     * If the permission is not granted, then Muse 2016 (MU-02) headbands will
     * not be discovered and a SecurityException will be thrown.
     */
    private void ensurePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have the ACCESS_COARSE_LOCATION permission so create the dialogs asking
            // the user to grant us the permission.

            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    0);
                        }
                    };

            // This is the context dialog which explains to the user the reason we are requesting
            // this permission.  When the user presses the positive (I Understand) button, the
            // standard Android permission dialog will be displayed (as defined in the button
            // listener above).
            AlertDialog introDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_description)
                    .setPositiveButton(R.string.permission_dialog_understand, buttonListener)
                    .create();
            introDialog.show();
        }
    }
}

