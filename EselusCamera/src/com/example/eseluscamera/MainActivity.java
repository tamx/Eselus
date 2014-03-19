package com.example.eseluscamera;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.remoteoculus.R;

public class MainActivity extends Activity {
	private int mode = 0;
	protected int left_x = 0;
	protected int left_y = 0;
	protected int left_zoom = 0;
	protected int right_x = 0;
	protected int right_y = 0;
	protected int right_zoom = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		((Button) findViewById(R.id.left))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mode < 0) {
							left_x--;
						} else if (mode > 0) {
							right_x--;
						}
						update();
					}
				});
		((Button) findViewById(R.id.down))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mode < 0) {
							left_y++;
						} else if (mode > 0) {
							right_y++;
						}
						update();
					}
				});
		((Button) findViewById(R.id.up))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mode < 0) {
							left_y--;
						} else if (mode > 0) {
							right_y--;
						}
						update();
					}
				});
		((Button) findViewById(R.id.right))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mode < 0) {
							left_x++;
						} else if (mode > 0) {
							right_x++;
						}
						update();
					}
				});
		((Button) findViewById(R.id.out))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mode < 0) {
							left_zoom++;
						} else if (mode > 0) {
							right_zoom++;
						}
						update();
					}
				});
		((Button) findViewById(R.id.in))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mode < 0) {
							left_zoom--;
						} else if (mode > 0) {
							right_zoom--;
						}
						update();
					}
				});

		{
			SharedPreferences pref = getSharedPreferences("offset",
					Activity.MODE_PRIVATE);
			this.left_x = pref.getInt("left_x", 0);
			this.left_y = pref.getInt("left_y", 0);
			this.left_zoom = pref.getInt("left_z", 0);
			this.right_x = pref.getInt("right_x", 0);
			this.right_y = pref.getInt("right_y", 0);
			this.right_zoom = pref.getInt("right_z", 0);
		}
		update();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		{
			MenuItem item = menu.add("左調整");
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if (mode == -1) {
						mode = 0;
					} else {
						mode = -1;
					}
					update();
					return true;
				}
			});
		}
		{
			MenuItem item = menu.add("右調整");
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if (mode == 1) {
						mode = 0;
					} else {
						mode = 1;
					}
					update();
					return true;
				}
			});
		}
		return super.onCreateOptionsMenu(menu);
	}

	private void update() {
		View ctrl = findViewById(R.id.ctrl);
		if (this.mode != 0) {
			ctrl.setVisibility(View.VISIBLE);
		} else {
			ctrl.setVisibility(View.GONE);
		}
		{
			SharedPreferences pref = getSharedPreferences("offset",
					Activity.MODE_PRIVATE);
			Editor edit = pref.edit();
			edit.putInt("left_x", left_x);
			edit.putInt("left_y", left_y);
			edit.putInt("left_z", left_zoom);
			edit.putInt("right_x", right_x);
			edit.putInt("right_y", right_y);
			edit.putInt("right_z", right_zoom);
			edit.commit();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		PreView preview = (PreView) findViewById(R.id.preview);
		preview.stop();
	}
}
