package ia2.datagather.geophoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button proceedBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        proceedBtn=findViewById(R.id.logInButton);
        proceedBtn.setOnClickListener(this);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE},1);

        proceedBtn.setEnabled(checkCameraHardware(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logInButton:
                goToCamera();
                break;
        }
    }

    private void goToCamera() {
        Intent i=new Intent(this,CameraActivity.class);
        startActivity(i);
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
}