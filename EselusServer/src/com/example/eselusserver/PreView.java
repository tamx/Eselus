package com.example.eselusserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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

		// setRotationX(getWidth());
		// setRotationY(getHeight());
		// setRotation(180);
		// // setTranslationX(getWidth() / 2);
		// // setTranslationY(getHeight() / 2);

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
			Thread server = null;
			ssocket = new ServerSocket(4444);
			while (!Thread.currentThread().isInterrupted()) {
				try {
					final Socket socket = ssocket.accept();
					System.err.println("connected.");
					if (server != null && server.isAlive()) {
						server.interrupt();
					}
					server = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								DataInputStream is = new DataInputStream(socket
										.getInputStream());
								while (!Thread.currentThread().isInterrupted()) {
									int width = is.readInt();
									int height = is.readInt();
									int left_x = is.readShort();
									int left_y = is.readShort();
									int left_z = is.readShort();
									int right_x = is.readShort();
									int right_y = is.readShort();
									int right_z = is.readShort();
									int length = is.readInt();
									byte[] jpeg = new byte[length];
									is.readFully(jpeg);
									Bitmap bmp = BitmapFactory.decodeByteArray(
											jpeg, 0, jpeg.length);
									SurfaceHolder holder = getHolder();
									Canvas canvas = null;
									try {
										canvas = holder.lockCanvas();
										if (canvas != null && bmp != null) {
											canvas.drawARGB(0xff, 0, 0, 0);
											{
												Bitmap bitmap = Bitmap
														.createBitmap(bmp, 0,
																0, width / 2,
																height);
												Matrix matrix = new Matrix();
												matrix.postRotate(180,
														width / 2 / 2,
														height / 2);
												matrix.postScale(getWidth()
														/ width,
														(getHeight() - left_z)
																/ height);
												matrix.postTranslate(getWidth()
														/ 2 - left_x, -left_y);
												canvas.drawBitmap(bitmap,
														matrix, null);
											}
											{
												Bitmap bitmap = Bitmap
														.createBitmap(bmp,
																width / 2, 0,
																width / 2,
																height);
												Matrix matrix = new Matrix();
												// matrix.preTranslate(
												// getWidth() / 2 / 2,
												// getHeight() / 2);
												matrix.postRotate(180,
														width / 2 / 2,
														height / 2);
												matrix.postScale(getWidth()
														/ width,
														(getHeight() - right_z)
																/ height);
												matrix.postTranslate(-right_x,
														-right_y);
												canvas.drawBitmap(bitmap,
														matrix, null);
											}
										}
									} finally {
										if (canvas != null) {
											holder.unlockCanvasAndPost(canvas);
										}
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							} finally {
								if (socket != null) {
									try {
										socket.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}
					});
					server.start();
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
