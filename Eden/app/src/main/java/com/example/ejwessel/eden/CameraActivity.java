package com.example.ejwessel.eden;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.graphics.Matrix;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.style.BackgroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;

import com.example.ejwessel.eden.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class CameraActivity extends Activity
{
  private final int IMAGE_SIZE = 300;
  private final int CAMERA_WARMUP_DELAY = 400;
  private final static String TAG = "CameraActivity";
  private TextureView mTextureView = null;
  private ImageReader mImageReader;
  private Handler backgroundHandler;

  private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback()
  {
    @Override
    public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber)
    {
      super.onCaptureStarted(session, request, timestamp, frameNumber);
    }

    @Override
    public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult)
    {
      super.onCaptureProgressed(session, request, partialResult);
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)
    {
      super.onCaptureCompleted(session, request, result);
    }

    @Override
    public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure)
    {
      super.onCaptureFailed(session, request, failure);
    }

    @Override
    public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber)
    {
      super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
    }

    @Override
    public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId)
    {
      super.onCaptureSequenceAborted(session, sequenceId);
    }
  };

  private TextureView.SurfaceTextureListener mSurfaceTextureListner = new TextureView.SurfaceTextureListener()
  {

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface)
    {
      // TODO Auto-generated method stub
      //Log.i(TAG, "onSurfaceTextureUpdated()");

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height)
    {
      // TODO Auto-generated method stub
      Log.i(TAG, "onSurfaceTextureSizeChanged()");

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface)
    {
      // TODO Auto-generated method stub
      Log.i(TAG, "onSurfaceTextureDestroyed()");
      return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height)
    {
      // TODO Auto-generated method stub
      Log.i(TAG, "onSurfaceTextureAvailable()");

      CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
      try {
        String cameraId = getCameraID(manager);
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
        manager.openCamera(cameraId, mStateCallback, null);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }

    }
  };

  private String getCameraID(CameraManager cm)
  {
    try {
      String[] ids = cm.getCameraIdList();
      for (int i = 0; i < ids.length; i++) {
        CameraCharacteristics camChar = cm.getCameraCharacteristics(ids[i]);
        if (camChar.get(camChar.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
          return ids[i];
        }
      }
    } catch (Exception e) {

    }
    return null;
  }

  private Size mPreviewSize = null;
  private CameraDevice mCameraDevice = null;
  private CaptureRequest.Builder mPreviewBuilder = null;
  private CameraCaptureSession mPreviewSession = null;
  private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener()
  {
    @Override
    public void onImageAvailable(ImageReader reader)
    {
      Image im = reader.acquireLatestImage();
      ByteBuffer buffer = im.getPlanes()[0].getBuffer();
      byte[] bytes = new byte[buffer.remaining()];

      //byte array to bitmap

      //rotate bitmap

      //bitmap to byte array

      System.out.println("buffer: " + buffer.remaining());
      buffer.get(bytes);
      try {
        makePostRequest(bytes);
        finish();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback()
  {

    @Override
    public void onOpened(CameraDevice camera)
    {
      // TODO Auto-generated method stub
      Log.i(TAG, "onOpened");
      mCameraDevice = camera;

      SurfaceTexture texture = mTextureView.getSurfaceTexture();
      if (texture == null) {
        Log.e(TAG, "texture is null");
        return;
      }

      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      Surface surface = new Surface(texture);
      HandlerThread backgroundThread = new HandlerThread("CameraPreview");
      backgroundThread.start();
      backgroundHandler = new Handler(backgroundThread.getLooper());

      mImageReader = ImageReader.newInstance(IMAGE_SIZE, IMAGE_SIZE, ImageFormat.JPEG, 1);
      mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, backgroundHandler);

      List<Surface> mList = new ArrayList<Surface>();
      mList.add(surface);
      mList.add(mImageReader.getSurface());

      try {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }

      mPreviewBuilder.addTarget(surface);
//      mPreviewBuilder.addTarget(mImageReader.getSurface());
      try {
        mCameraDevice.createCaptureSession(mList, mPreviewStateCallback, null);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onError(CameraDevice camera, int error)
    {
      // TODO Auto-generated method stub
      Log.e(TAG, "onError");

    }

    @Override
    public void onDisconnected(CameraDevice camera)
    {
      // TODO Auto-generated method stub
      Log.e(TAG, "onDisconnected");

    }
  };

  private CameraCaptureSession.StateCallback mPreviewStateCallback = new CameraCaptureSession.StateCallback()
  {

    @Override
    public void onConfigured(CameraCaptureSession session)
    {
      // TODO Auto-generated method stub
      Log.i(TAG, "onConfigured");
      mPreviewSession = session;

      mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270);

      try {
        mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }

      //warm up camera
      backgroundHandler.postDelayed(new Runnable()
      {
        @Override
        public void run()
        {
          try {
            mPreviewSession.stopRepeating();
            mPreviewBuilder.addTarget(mImageReader.getSurface());
            mPreviewSession.capture(mPreviewBuilder.build(), mCaptureCallback, backgroundHandler);
          } catch (CameraAccessException e) {
            e.printStackTrace();
          }
        }
      }, CAMERA_WARMUP_DELAY);
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session)
    {
      // TODO Auto-generated method stub
      Log.e(TAG, "CameraCaptureSession Configure failed");
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    //same as set-up android:screenOrientation="portrait" in <activity>, AndroidManifest.xml
    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setContentView(R.layout.activity_camera);

    mTextureView = (TextureView) findViewById(R.id.cameraView);
    mTextureView.setSurfaceTextureListener(mSurfaceTextureListner);
  }

  @Override
  protected void onPause()
  {
    // TODO Auto-generated method stub
    super.onPause();

    if (mCameraDevice != null) {
      mCameraDevice.close();
      mCameraDevice = null;
    }
  }

  private void makePostRequest(byte[] image)
  {

    HttpClient httpClient = new DefaultHttpClient();
    // replace with your url
    HttpPost httpPost = new HttpPost("https://api.sendgrid.com/api/mail.send.json");

    //Post Data
    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
    nameValuePair.add(new BasicNameValuePair("api_user", "*****"));
    nameValuePair.add(new BasicNameValuePair("api_key", "******"));
    nameValuePair.add(new BasicNameValuePair("to", "try@eden-battlehack.bymail.in"));
    nameValuePair.add(new BasicNameValuePair("from", "******"));
    nameValuePair.add(new BasicNameValuePair("subject", "mood-6")); //1 for now...

    String string64 = Base64.encodeToString(image, Base64.DEFAULT);
    nameValuePair.add(new BasicNameValuePair("text", string64));

    //Encoding POST data
    try {
      httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
    } catch (UnsupportedEncodingException e) {
      // log exception
      e.printStackTrace();
    }

    //making POST request.
    try {
      HttpResponse response = httpClient.execute(httpPost);
      // write response to log
      Log.d("Http Post Response:", response.toString());
    } catch (ClientProtocolException e) {
      // Log exception
      e.printStackTrace();
    } catch (IOException e) {
      // Log exception
      e.printStackTrace();
    }
  }


}
