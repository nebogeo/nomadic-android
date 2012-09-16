// Copyright (C) 2011 Dave Griffiths
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

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.app.AlertDialog;
import android.widget.EditText;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;
import java.io.IOException;
import java.io.FileNotFoundException;
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

public class Nomadic extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLView = new FluxusGLSurfaceView(this);
        setContentView(mGLView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
        {        
            mGLView.doCode();            
        }
        return true;
    }

    private FluxusGLSurfaceView mGLView;

    static {
        System.loadLibrary("nomadic");
    }
}

class FlxImage
{
    byte [] mData;
    int mWidth;
    int mHeight;
}

class FluxusGLSurfaceView extends GLSurfaceView {
    public FluxusGLSurfaceView(Context context) {
        super(context);
        mRenderer = new NomadicRenderer(context);
        mAct = context;
        setRenderer(mRenderer);
        mCode=mRenderer.readRawTextFile(mAct,"startup.scm");
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
        input.setText(mCode);
        alert.setPositiveButton("eval", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mCode = input.getText().toString();
                mRenderer.eval("(pre-process-run '("+mCode+"))");
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
        
        mRenderer.eval(code);

        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {        

        }
        return true;
    }

    NomadicRenderer mRenderer;
    Context mAct;
    String mCode;

    private static native void nativePause();
}

class NomadicRenderer implements GLSurfaceView.Renderer {
    public NomadicRenderer(Context ctx)
    {
        mAct=ctx;
    }

    static class Lock extends Object {}
    static public Lock mLock = new Lock();
    

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        nativeInit();
        loadTexture("test.png");
        eval(readRawTextFile(mAct, "init.scm"));
        eval(readRawTextFile(mAct, "boot.scm"));        
        eval("(pre-process-run '("+readRawTextFile(mAct, "startup.scm")+"))");        
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
        //gl.glViewport(0, 0, w, h);
        nativeResize(w, h);
    }

    public void onDrawFrame(GL10 gl) {
        synchronized (mLock) 
        {
            nativeRender();
        }
    }

    public void eval(String code) {
        synchronized (mLock) 
        {
            nativeEval(code);
        }
    }

    public void loadTexture(String texname) {
        FlxImage tex=readRawImage(mAct,texname);
        if (tex!=null)
        {
            synchronized (mLock) 
            {
                nativeLoadTexture(texname,tex.mData,tex.mWidth,tex.mHeight);
            }
        }
    }

    Context mAct;

    private static native void nativeInit();
    private static native void nativeResize(int w, int h);
    private static native void nativeRender();
    private static native void nativeDone();
    private static native void nativeEval(String code);
    private static native void nativeLoadTexture(String texname, byte[] arr, int w, int h);

    public FlxImage readRawImage(Context ctx, String fn)
    {
        InputStream fis = null;
        try
        {
            fis = ctx.getAssets().open(fn);
            Bitmap bmp = BitmapFactory.decodeStream(fis);
            ByteBuffer bb = ByteBuffer.allocate(bmp.getWidth()*bmp.getHeight()*4);
            bmp.copyPixelsToBuffer(bb);
            FlxImage ret=new FlxImage();
            ret.mWidth = bmp.getWidth(); 
            ret.mHeight = bmp.getHeight(); 
            ret.mData = bb.array();
            return ret;
        }
        catch(FileNotFoundException e)
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(mAct);
            alert.setTitle(e.toString());
            alert.show();
        }
        catch(IOException ioe)
        {
            AlertDialog.Builder alert = new AlertDialog.Builder(mAct);
            alert.setTitle(ioe.toString());
            alert.show();
        }
        return null;
    }

    public static String readRawTextFile(Context ctx, String fn)
    {
        BufferedReader inRd=null;
        try
        { 
            StringBuffer inLine = new StringBuffer();
            inRd = 
                new BufferedReader(new InputStreamReader
                                   (ctx.getAssets().open(fn))); 
            
            String text;
            while ((text = inRd.readLine()) != null) {
                inLine.append(text);
                inLine.append("\n");
            }
     
            return inLine.toString();
        }
        catch (IOException e)
        {
            return "";
        }
        finally
        {
            try { inRd.close(); } 
            catch (IOException e) { return ""; }
        } 
    }

}
