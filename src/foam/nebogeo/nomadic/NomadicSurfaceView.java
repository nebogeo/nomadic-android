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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.File;
import java.io.Writer;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import android.widget.EditText;
import android.app.AlertDialog;

import android.util.Log;

import java.io.IOException;
import java.io.FileNotFoundException;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.content.DialogInterface;

class NomadicSurfaceView extends GLSurfaceView {

    NomadicRenderer mRenderer;
    Context mAct;
    String mFilename;

    public NomadicSurfaceView(Context context) {
        super(context);
        mAct = context;
        mFilename="";
        String code=mRenderer.readRawTextFile(mAct,"startup.scm");
        mRenderer = new NomadicRenderer(context,code);
        setRenderer(mRenderer);
    }

    public String readLog()
    {
        //create file object
        File file = new File("/sdcard/nomadic-log.txt");
        int ch;
        
        StringBuffer strContent = new StringBuffer("");
        FileInputStream fin = null;
        try
        {
            fin = new FileInputStream(file);
            while( (ch = fin.read()) != -1)
                strContent.append((char)ch);
            fin.close();
        }
        catch(FileNotFoundException e)
        {
        }
        catch(IOException ioe)
        {
        }
        return strContent.toString();
    }

    public void showLog()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(mAct);
        alert.setTitle("nomadic");
        alert.setMessage("s p a t");
        final EditText input = new EditText(mAct);
        alert.setView(input);
        input.setText(readLog());
        alert.setPositiveButton("done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        
        alert.setNegativeButton("out", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }
    

    public void doCode()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(mAct);
        alert.setTitle("nomadic");
        alert.setMessage("f e e d   m e");
        final EditText input = new EditText(mAct);
        alert.setView(input);
        input.setText(mRenderer.getCode());
        alert.setPositiveButton("eval", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mRenderer.setCurrent(input.getText().toString());
                mRenderer.evalCurrent();
            }
        });
        
        alert.setNegativeButton("sip", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showLog();
            }
        });
        alert.show();

     }

    public boolean onTouchEvent(MotionEvent event) {
        final int pointerCount = event.getPointerCount();
        String code="(input-touches (list ";
        for (int p = 0; p < pointerCount; p++) {
            code+="(list "+event.getPointerId(p)+" "+event.getX(p)+" "+event.getY(p)+") ";
        }
        code+="))";

        mRenderer.evalCode(code);

        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {        

        }
        return true;
    }

    public void loadCodeExternal(String filename)
    {
        Log.i("nomadic","loading "+filename);
        mFilename=filename;
        mRenderer.loadExternal(filename);
    }

    public void saveCodeExternal()
    {
        if (mFilename!="") {
            Log.i("nomadic","saving "+mFilename);
            mRenderer.saveExternal(mFilename);
        }
    }

    private static native void nativePause();
}
