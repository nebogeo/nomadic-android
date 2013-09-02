// Copyright (C) 2012 Dave Griffiths
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package foam.nebogeo.nomadic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import android.text.SpannableString;
import android.content.res.AssetManager;
import android.text.Html;
import android.widget.TextView;
import android.content.Intent;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import foam.nebogeo.nomadic.LoadDialog;
import foam.nebogeo.nomadic.NomadicSurfaceView;
import foam.nebogeo.nomadic.NomadicRenderer;

public class Nomadic extends Activity implements SensorEventListener {

	private MenuItem menuabout = null;
	private MenuItem menuexit = null;
	private MenuItem menuload = null;
	private MenuItem menuloadtex = null;
	private MenuItem menuedit = null;
	private MenuItem menusave = null;
    private int DIALOG_LOAD = 1;
    private int DIALOG_TEXLOAD = 2;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new NomadicSurfaceView(this);
        setContentView(mGLView);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //get the accelerometer sensor
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mGLView.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
//        {
//            mGLView.doCode();
//        }
        return super.onKeyDown(keyCode,event);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	// menu launch yeah
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menuabout = menu.add(0, Menu.FIRST + menu.size(), 0, "About");
		menuabout.setIcon(android.R.drawable.ic_menu_info_details);
		menuload = menu.add(0, Menu.FIRST + menu.size(), 0, "Load");
		menuload.setIcon(android.R.drawable.ic_menu_info_details);
		//menuloadtex = menu.add(0, Menu.FIRST + menu.size(), 0, "Load Texture");
		//menuloadtex.setIcon(android.R.drawable.ic_menu_info_details);
		menuexit = menu.add(0, Menu.FIRST + menu.size(), 0, "Exit");
		menuexit.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menuedit = menu.add(0, Menu.FIRST + menu.size(), 0, "Edit");
		menuedit.setIcon(android.R.drawable.ic_menu_edit);
		menusave = menu.add(0, Menu.FIRST + menu.size(), 0, "Save");
		menusave.setIcon(android.R.drawable.ic_menu_save);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == menuabout) {
			// load in the about dialog contents from assets/about.html
			StringBuffer sb = new StringBuffer();
			try {
				AssetManager assets = getAssets();
				InputStreamReader reader = new InputStreamReader(assets.open("about.html"), "UTF-8");
				BufferedReader br = new BufferedReader(reader);
				String line = br.readLine();
				while(line != null) {
					sb.append(line + "\n");
					line = br.readLine();
				}
			} catch (IOException e) {
				sb.append("Copyright Dave Griffiths, 2012");
			}

			// convert the string to HTML for the about dialog
			final SpannableString s = new SpannableString(Html.fromHtml(sb.toString()));
			Linkify.addLinks(s, Linkify.ALL);

			AlertDialog ab = new AlertDialog.Builder(this)
			.setTitle("About")
			.setIcon(android.R.drawable.ic_dialog_info)
			.setMessage(s)
			.setPositiveButton("ok", null)
			.create();
			ab.show();
			// make the links clickable
			((TextView)ab.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
		} else if (item == menuload) {
			Intent it = new Intent(this, LoadDialog.class);
			startActivityForResult(it, DIALOG_LOAD);
		} else if (item == menuloadtex) {
			Intent it = new Intent(this, LoadDialog.class);
			startActivityForResult(it, DIALOG_TEXLOAD);
		} else if (item == menuexit) {
			finish();
		} else if (item == menuedit) {
            mGLView.doCode();
		} else if (item == menusave) {
            mGLView.saveCodeExternal();
		} else {
			// pass the menu selection through to the MenuBang manager
//			MenuBang.hit(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
            if (requestCode == DIALOG_LOAD) {
                mGLView.loadCodeExternal(data.getStringExtra("filename"));
            }
		}
	}

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event)
    {
        // Many sensors return 3 values, one for each axis.
        int x = (int)(event.values[0]*10);
        int y = (int)(event.values[1]*10);
        int z = (int)(event.values[2]*10);

        mGLView.updateSensor(x,y,z);
    }



    private NomadicSurfaceView mGLView;

    static {
        System.loadLibrary("nomadic");
    }
}
