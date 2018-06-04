package io.luxurytech.gala;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class HomeActivity extends AppCompatActivity implements SurfaceHolder.Callback, AdapterView.OnItemSelectedListener{

    /** Camera preview components */
    final int CAMERA_REQUEST_CODE = 1;
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    /** UI Components */
    Button settingsButton;
    Button goButton;
    Spinner selectEchelonSpinner;
    TextView cloutTextView;

    /** Shared Prefs */
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        // Setup camera preview
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();

        // If permission was not granted, ask for it
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        // Setup UI
        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUIInvisible();
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
            }
        });

        goButton = (Button) findViewById(R.id.goButton);

        cloutTextView = (TextView) findViewById(R.id.cloutTextView);
        cloutTextView.setText(getEmojiByUnicode(Constants.PURPLE_HEART_UNICODE) + sharedPref.getInt(getString(R.string.userClout), 1));

        selectEchelonSpinner = (Spinner) findViewById(R.id.selectEchelonSpinner);
        ArrayAdapter<CharSequence> selectEchelonSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.select_echelon_choices, android.R.layout.simple_spinner_item);
        selectEchelonSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectEchelonSpinner.setAdapter(selectEchelonSpinnerAdapter);
    }



    /** SurfaceHolder.Callback */
    @Override
    public void surfaceCreated(SurfaceHolder sh) {
        // Start the camera
        try {
            releaseCameraAndPreview();
            camera = Camera.open();
        } catch (Exception e){
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
            return;
        }

        Camera.Parameters parameters;
        parameters = camera.getParameters();

        // Set camera settings
        camera.setDisplayOrientation(90);
        parameters.setPreviewFrameRate(30);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);

        // Set to screen
        try {
            camera.setPreviewDisplay(sh);
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.startPreview();
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//        camera.stopPreview();
  //      camera.release();
    //    camera = null;
    }

    /** Handle if permissions needed to be asked for */
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Try callback again
                    surfaceHolder.addCallback(this);
                    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                } else {
                    // Tell user it needs permission
                    Toast.makeText(this, "Please provide camera permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /** OnItemSelectedListener */
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        //parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {}

    private void setUIInvisible() {
        goButton.setVisibility(View.INVISIBLE);
        settingsButton.setVisibility(View.INVISIBLE);
        cloutTextView.setVisibility(View.INVISIBLE);
        selectEchelonSpinner.setVisibility(View.INVISIBLE);

    }

    private void releaseCameraAndPreview() {
        //myCameraPreview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

}
