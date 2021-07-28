package com.example.dhvani;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FindActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationV;
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    short[] audioData;
    EditText iden_ans;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    Complex[] fftTempArray; //fft array
    Complex[] fftArray;
    int[] bufferData;
    int bytesRecorded;
    double mPeakPos;

    final int mNumberOfFFTPoints = 8192;
    double[] absNormalizedSignal = new double[mNumberOfFFTPoints / 2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_find);
        bottomNavigationV = findViewById(R.id.bottomNavigationView);
        //  bottomNavigationV.setSelectedItemId(R.id.home);
        bottomNavigationV.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {


            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                   /* case R.id.home:
                        Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        break;*/
                    case R.id.home:
                        // Toast.makeText(MainActivity.this, "Record", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(R.anim.leftanimation, R.anim.rightanimation);
                        break;
                    case R.id.record:
                        // Toast.makeText(MainActivity.this, "Record", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), RecordActivity.class));
                        overridePendingTransition(R.anim.leftanimation, R.anim.rightanimation);
                        break;
                    case R.id.find:
                        //Toast.makeText(MainActivity.this, "Find", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FindActivity.class));
                        overridePendingTransition(R.anim.leftanimation, R.anim.rightanimation);
                        break;

                }
                return true;
            }
        });

        setButtonHandlers();
        display();
        enableButtons(false);

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING)*3;

        audioData = new short [bufferSize]; // Array PCM

        requestRecordAudioPermission();
        requestRecordFilePermission();
    }


    private void setButtonHandlers() {
        ((Button)findViewById(R.id.spk_button)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.st_spk_button)).setOnClickListener(btnClick);
    }

    private void enableButton(int id,boolean isEnable){
        ((Button)findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording){
        enableButton(R.id.spk_button,!isRecording);
        enableButton(R.id.st_spk_button,isRecording);
    }
    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
//Creates a new File instance from a parent pathname string and a child pathname string.
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        if (!file.exists()){
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    public double[] calculateFFT(byte[] signal){
//        final int mNumberOfFFTPoints = 1024;
        double mMaxFFTSample;
        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints/2];
        for (int i=0; i < mNumberOfFFTPoints; i++){
            temp = (double)((signal[2*i]&0xFF) | (signal[2*i+1] << 8))/32768.0F;
            complexSignal[i] = new Complex(temp,0.0);
        }
        y = FFT.fft(complexSignal);

        mMaxFFTSample = 0.0;
        mPeakPos = 0;
        for(int i=0;i<(mNumberOfFFTPoints/2);i++){
            absSignal[i]=Math.sqrt(Math.pow(y[i].re(),2)+Math.pow(y[i].im(),2));
            if(absSignal[i] > mMaxFFTSample){
                mMaxFFTSample = absSignal[i];
                mPeakPos = i;
            }
        }
        return absSignal;
    }
  private String getTempFilename(){
        String filepath=Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);
        if (!file.exists()){
            file.mkdirs();
        }
        File tempFile=new File(filepath,AUDIO_RECORDER_TEMP_FILE);
        if (tempFile.exists())
            tempFile.delete();
        return (file.getAbsolutePath()+"/"+AUDIO_RECORDER_TEMP_FILE);
    }

    private void startRecording(){

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING,bufferSize);
        Log.d("See: ","?");
        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("see: ", "AudioRecord could not be initialized. Exiting!");
            return;
        }
        recorder.startRecording();
        Log.d("see: ","????");
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"Audio Recorder Thread");
        recordingThread.start();
    }

    private void writeAudioDataToFile(){
        byte data[] = new byte[bufferSize];
//        final int mNumberOfFFTPoints = 1024;
//        double[] absNormalizedSignal = new double[mNumberOfFFTPoints/2];
        String filename = getTempFilename();
        FileOutputStream os = null;
        Log.d("See: ","in writeAudioDataToFile : "+filename);
        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            Log.d("See: ", "Error");
            e.printStackTrace();
        }

        Log.d("see: ","isRecording vale : "+isRecording);

        int read=0;
        int jj=0;
        if(null != os){
            while(isRecording){
//
                read = recorder.read(data, 0, bufferSize);
                if (read > 0){
                    if (jj==5) {
                        jj=0;
                        absNormalizedSignal = calculateFFT(data);

                    }
                    jj++;
                }
                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        display();

    }
    private void stopRecording(){

        if(null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
        copyWaveFile(getTempFilename(),getFilename());
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename){

    }

 private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.spk_button:{
                    Log.d("See: ","Init");
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.st_spk_button:{
                    Log.d("See: ","Fin reg");
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

private void requestRecordAudioPermission() {
    //check API version, do nothing if API version < 23!
    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
        }
    }
}

    private void requestRecordFilePermission() {
        //check API version, do nothing if API version < 23!
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    Log.d("See: ", "Granted!");

                } else {


                    Log.d("See: ", "Denied!");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    void display()
    {
        Arrays.sort(absNormalizedSignal);
        String identity;
        if(absNormalizedSignal[0]>=160)
        {
            identity="FEMALE";
        }
        else
        {
            identity="MALE";
        }

      /*  The average man’s speaking voice, for example, typically has a
       fundamental frequency between 85 Hz and 155 Hz. A woman’s speech range
        is about 165 Hz to 255 Hz, and a child’s voice typically ranges from
        250 Hz to 300 Hz
       */
       iden_ans.setText(identity);

    }

}