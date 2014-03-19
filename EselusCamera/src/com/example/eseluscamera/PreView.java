package com.example.eseluscamera;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import jp.co.sharp.android.hardware.CameraEx;
import jp.co.sharp.android.stereo3dlcd.SurfaceController;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public class PreView extends SurfaceView implements SurfaceHolder.Callback,
		Camera.PreviewCallback, Camera.AutoFocusCallback, Runnable {
	private Context context = null;
	private CameraEx myCamera = null;
	private Thread thread = null;
	int previewWidth = 0;
	int previewHeight = 0;
	byte[] data = null;

	public PreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		SurfaceHolder holder = getHolder();

		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		this.thread = new Thread(this);
		this.thread.start();
	}

	protected void doAutoFocus() {
		myCamera.autoFocus(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		int cameras = 0;
		int id = 0;
		cameras = CameraEx.getNumberOfCameras();

		if (cameras > 0) {
			CameraEx.CameraInfo info = new CameraEx.CameraInfo();
			for (int i = 0; i < cameras; i++) {
				CameraEx.getCameraInfo(i, info); /* ��Addon�pAPI�g�p�ӏ��� */
				if (info.facing == CameraEx.CameraInfo.CAMERA_FACING_BACK) {
					if (info.mode == CameraEx.CameraInfo.CAMERA_MODE_SINGLE) {
					} else if (info.mode == CameraEx.CameraInfo.CAMERA_MODE_DOUBLE) {
						id = i;
						break;
					}
				} else if (info.facing == CameraEx.CameraInfo.CAMERA_FACING_FRONT) {
				}
			}
		}

		myCamera = CameraEx.open(id);
		new SurfaceController(this).setStereoView(true);
		try {
			myCamera.setPreviewDisplay(holder);
			myCamera.setPreviewCallback(this);
			myCamera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		myCamera.stopPreview();

		Camera.Parameters parameters = myCamera.getParameters();

		boolean portrait = isPortrait();

		if (portrait) {
			myCamera.setDisplayOrientation(90);
		} else {
			myCamera.setDisplayOrientation(0);
		}

		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		Camera.Size size = sizes.get(0);
		parameters.setPreviewSize(size.width, size.height);
		parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);

		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if (portrait) {
			layoutParams.width = size.height;
			layoutParams.height = size.width;
		} else {
			layoutParams.width = size.width;
			layoutParams.height = size.height;
		}
		setLayoutParams(layoutParams);

		try {
			myCamera.setParameters(parameters);
			myCamera.setPreviewCallback(this);
			myCamera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (myCamera != null) {
			myCamera.stopPreview();
			myCamera.setPreviewCallback(null);
			myCamera.release();
			myCamera = null;
		}
		stop();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}

	protected boolean isPortrait() {
		return (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		System.out.println("changed.");
		previewWidth = camera.getParameters().getPreviewSize().width;
		previewHeight = camera.getParameters().getPreviewSize().height;
		this.data = data;
	}

	public Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
		YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height,
				null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
		byte[] jdata = baos.toByteArray();
		BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
		bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length,
				bitmapFatoryOptions);
		return bmp;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success) {
			myCamera.cancelAutoFocus();
		}
	}

	@Override
	public void run() {
		ServerSocket ssocket = null;
		try {
			ssocket = new ServerSocket(4445);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (!Thread.currentThread().isInterrupted()) {
			Socket socket = null;
			DataOutputStream os = null;
			try {
				socket = ssocket.accept();
				System.err.println("connected.");
				os = new DataOutputStream(socket.getOutputStream());
				while (true) {
					if (this.data != null) {
						byte[] data = this.data;
						Bitmap bmp = getBitmapImageFromYUV(data,
								this.previewWidth, this.previewHeight);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						bmp.compress(Bitmap.CompressFormat.JPEG, 90, bos);
						byte[] jpeg = bos.toByteArray();
						// OutputStream os = socket.getOutputStream();
						// os.write("HTTP/1.0 302 found\n".getBytes());
						// os.write("Content-Type: image/jpeg;\n\n".getBytes());
						os.writeInt(previewWidth);
						os.writeInt(previewHeight);
						MainActivity activity = (MainActivity) this.context;
						os.writeShort(activity.left_x);
						os.writeShort(activity.left_y);
						os.writeShort(activity.left_zoom);
						os.writeShort(activity.right_x);
						os.writeShort(activity.right_y);
						os.writeShort(activity.right_zoom);
						os.writeInt(jpeg.length);
						os.write(jpeg);
						// os.write(data);
						os.flush();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	void stop() {
		if (this.thread != null) {
			this.thread.interrupt();
			this.thread = null;
		}
	}
}
