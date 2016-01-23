package com.example.ejwessel.eden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by ejwessel on 11/14/15.
 */
public class UserPresentReceiver extends BroadcastReceiver {

  private final static String TAG = "UserPresentReceiver";

  @Override
  public void onReceive(Context context, Intent intent)
  {

    Log.i(TAG, "onReceive");
    Intent i = new Intent();
    i.setClassName("com.example.ejwessel.eden", "com.example.ejwessel.eden.CameraActivity");
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(i);

    //remove this later
//    Toast.makeText(context, "User has unlocked screen", Toast.LENGTH_LONG).show();
  }
}
