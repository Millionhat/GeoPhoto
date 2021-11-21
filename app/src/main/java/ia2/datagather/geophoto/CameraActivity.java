package ia2.datagather.geophoto;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, LocationListener {
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Button recordButton;
    Boolean isActive;
    Camera.PictureCallback jpegCallback;
    LocationManager lm;
    Location ubicacion;
    View vista;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        recordButton = findViewById(R.id.btnCameraInit);
        recordButton.setOnClickListener(this);
        recordButton.setEnabled(false);
        isActive = false;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);


        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        File pictureFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/GeoPhoto/");

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Log.d("PhotoHandler", "Can't create directory to save image.");
            Toast.makeText(this, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
        }

        try {
            String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/GeoPhoto/";

            File root = new File(rootPath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File f =new File(rootPath + "/location.txt");
            if (f.exists()) {
                f.delete();
                Log.e(">>>>","EXISTE");
            }else{
                f.createNewFile();

                FileOutputStream out = new FileOutputStream(f);

                out.flush();
                out.close();
            }
        } catch (Exception e) {
            Log.e(">>>>", ""+e.getMessage());
        }

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        jpegCallback = new Camera.PictureCallback() {
            @SuppressLint("WrongConstant")
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                FileOutputStream textStream = null;
                long strDate1 = System.currentTimeMillis();
                Date date = new Date(strDate1);
                String formattedDate = "";

                SimpleDateFormat df1 = new SimpleDateFormat("EEEddMMMyyyyHHmmss");
                //SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
                //date = df2.format(format.parse("yourdate");
                formattedDate = df1.format(date);
                try {
                    fillRegister(formattedDate);
                    //outStream = new FileOutputStream(String.format(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/%d.jpg", formattedDate));
                    outStream = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/GeoPhoto/" + formattedDate + ".jpg");
                    outStream.write(data);
                    outStream.close();
                    Log.e(">>>>", "onPictureTaken - wrote bytes: " + data.length);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                Toast.makeText(getApplicationContext(), "Picture Saved", 500).show();
                refreshCamera();
            }
        };
    }

    public void captureImage(View v) throws IOException {
        //take the picture
        camera.takePicture(null, null, jpegCallback);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            // open the camera
            camera = Camera.open();
        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();

        // modify parameter
        List<Camera.Size> sizes = param.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        param.setPreviewSize(selected.width, selected.height);
        camera.setParameters(param);
        try {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            //camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCameraInit:
                isActive = !isActive;
                if (isActive) {
                    vista=v;

                    new Thread(new Runnable() {
                        public void run() {
                            while(isActive)
                            {
                                try {
                                    captureImage(v);
                                    Thread.sleep(4000);
                                } catch (InterruptedException | IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                    runOnUiThread(
                            () -> {
                                recordButton.setText("Parar");
                                recordButton.setBackgroundColor(Color.parseColor("#4f000b"));
                            }
                    );
                } else {
                    //lm.removeUpdates(this);
                    runOnUiThread(
                            () -> {
                                recordButton.setText("Grabar");
                                recordButton.setBackgroundColor(Color.parseColor("#504E4E"));
                            }
                    );
                }


                break;
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(@NonNull Location location) {
        ubicacion = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(ubicacion!=null){
            runOnUiThread(
                    () -> {
                        recordButton.setEnabled(true);
                    }
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void fillRegister(String formattedDate) throws IOException {
        File f =new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/GeoPhoto/" + "location.txt");
        String str = formattedDate+ ";"+ubicacion.getLongitude()+";"+ubicacion.getLatitude();
        Log.e(">>>>","dato: "+str);
        BufferedWriter writer = new BufferedWriter(new FileWriter(f, true));
        writer.append('\n');
        writer.append(str);
        writer.close();
    }
}