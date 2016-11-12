package com.yzhao.musecode;


import android.app.Activity;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.choosemuse.libmuse.MuseDataPacketType.ACCELEROMETER;

public class MainActivity extends Activity implements View.OnClickListener{

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
    // use MuseArtifactPacket.getBlink()
    // use MuseArtifactPacket.getJawClench()
    private boolean blink;
    private boolean jawClench;

    // for updating UI
    private final Handler handler = new Handler();

    // for the list of Muses
    private ArrayAdapter<String> spinnerAdapter;

    // check whether data transmission is enabled
    private boolean dataTransmission = true;

    // left tilt threshold
    private final float LEFT_THRESHOLD = -0.5f;

    // nod threshold
    private final float FRONT_THRESHOLD = 0.3f;

    // running queue of data from accelerometer to determine nods for next character function
    private AccelerometerData nodQ = new AccelerometerData(FRONT_THRESHOLD);

    // running queue of data from accelerometer to determine left tilts for backspace function
    private AccelerometerData backspaceQ = new AccelerometerData(LEFT_THRESHOLD);

    // list of current characters to be displayed
    private ArrayList<Character> charList = new ArrayList<>();


    private final Runnable tickUi = new Runnable() {
        @Override
        public void run() {
            updateEKG();
            handler.postDelayed(tickUi, 1000 / 60);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);

        // listener for connection state changes
        WeakReference<MainActivity> weakActivity = new WeakReference<->(this);
        connectionListener = new ConnectionListener(weakActivity);


        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.connect) {
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
                muse.registerDataListener(dataListener, MuseDataPacketType.EEG);
                muse.registerDataListener(dataListener, MuseDataPacketType.ALPHA_RELATIVE);
                muse.registerDataListener(dataListener, MuseDataPacketType.ACCELEROMETER);
                muse.registerDataListener(dataListener, MuseDataPacketType.BATTERY);
                muse.registerDataListener(dataListener, MuseDataPacketType.DRL_REF);
                muse.registerDataListener(dataListener, MuseDataPacketType.QUANTIZATION);

                // Initiate a connection to the headband and stream the data asynchronously.
                muse.runAsynchronously();
            }
        }
    }
    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
        // valuesSize returns the number of data values contained in the packet.
        final long n = p.valuesSize();
        // check if it's accelerometer data
        if(p.packetType() == ACCELEROMETER) {
                assert(accelBuffer.length >= n);
                getAccelValues(p);
                accelStale = true;
        }
    }

    // get the blink and jawClench data here
    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
        blink = p.getBlink();
        jawClench = p.getJawClench();
    }

    // update the displayed EKG values
    public void updateEKG() {
        TextView blink = (TextView)findViewById(R.id.blink);
        blink.setText(String.format("blink:%d\n", blink));
    }

    private void getAccelValues(MuseDataPacket p) {
        accelBuffer[0] = p.getAccelerometerValue(Accelerometer.X);
        accelBuffer[1] = p.getAccelerometerValue(Accelerometer.Y);
        accelBuffer[2] = p.getAccelerometerValue(Accelerometer.Z);
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

    public void
}
