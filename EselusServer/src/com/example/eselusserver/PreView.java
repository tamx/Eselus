package com.example.eselusserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PreView extends SurfaceView implements SurfaceHolder.Callback,
		Runnable {
	private Context context = null;
	private Thread thread = null;
	int previewWidth = 0;
	int previewHeight = 0;
	byte[] data = null;

	public PreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		SurfaceHolder holder = getHolder();

		holder.addCallback(this);
		// holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		this.thread = new Thread(this);
		this.thread.start();
	}

	protected void doAutoFocus() {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}

	@Override
	public void run() {
		ServerSocket ssocket = null;
		try {
			ssocket = new ServerSocket(4444);
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Socket socket = ssocket.accept();
					System.err.println("connected.");
					DataInputStream is = new DataInputStream(
							socket.getInputStream());
					while (true) {
						// BufferedReader reader = new BufferedReader(
						// new InputStreamReader(is));
						// reader.readLine();
						// reader.readLine();
						// reader.readLine();
						int width = is.readInt();
						int height = is.readInt();
						int length = is.readInt();
						byte[] jpeg = new byte[length];
						is.readFully(jpeg);
						Bitmap bmp = BitmapFactory.decodeByteArray(jpeg, 0,
								jpeg.length);
						SurfaceHolder holder = getHolder();
						Canvas canvas = null;
						try {
							canvas = holder.lockCanvas();
							if (canvas != null && bmp != null) {
								canvas.drawBitmap(bmp, 0, 0, null);
								holder.unlockCanvasAndPost(canvas);
							}
						} finally {
							if (canvas != null) {
								holder.unlockCanvasAndPost(canvas);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (ssocket != null) {
				try {
					ssocket.close();
				} catch (IOException e) {
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
