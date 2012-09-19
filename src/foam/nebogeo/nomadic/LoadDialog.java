package foam.nebogeo.nomadic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Intent;

public class LoadDialog extends ListActivity {
    
    private List<String> item = null;
    private List<String> path = null;
    private String root="/";
    private TextView myPath;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_dialog);
        myPath = (TextView)findViewById(R.id.path);
        getDir(root);
    }
    
    private void getDir(String dirPath)
    {
        myPath.setText("Location: " + dirPath);
        
        item = new ArrayList<String>();
        path = new ArrayList<String>();
     
        File f = new File(dirPath);
        File[] files = f.listFiles();
        
        if(!dirPath.equals(root))
        {            
            item.add(root);
            path.add(root);
            item.add("../");
            path.add(f.getParent());   
        }
        
        for(int i=0; i < files.length; i++)
        {
            File file = files[i];
            path.add(file.getPath());
            if(file.isDirectory())
                item.add(file.getName() + "/");
            else
                item.add(file.getName());
        }
        
        ArrayAdapter<String> fileList =
            new ArrayAdapter<String>(this, R.layout.row, item);
        setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        File file = new File(path.get(position));
        
        if (file.isDirectory())
        {
            if(file.canRead())
                getDir(path.get(position));
            else
            {
                new AlertDialog.Builder(this)
//    .setIcon(R.drawable.icon)
                    .setTitle("[" + file.getName() + "] folder can't be read!")
                    .setPositiveButton("OK", 
                                       new DialogInterface.OnClickListener() {
                                           
                                           @Override
                                           public void onClick(DialogInterface dialog, int which) {
                                               // TODO Auto-generated method stub
                                           }
                                       }).show();
            }
        }
        else
        {
            Intent result = new Intent();
            result.putExtra("filename", file.getPath());
            setResult(RESULT_OK, result);
            finish();
        }
    }
}


/*
  
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import foam.nebogeo.nomadic.R; 

public class LoadDialog extends Activity {
	private static final String TAG = "LoadDialog";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_dialog);
		ArrayList<String> filenames = new ArrayList<String>();
		final ListView filelist = (ListView)findViewById(R.id.filelist);
		Intent intent = getIntent();

        File f = new File("/"); 
        String[] files = f.list();

		//List<File> list = new ArrayList<File>(); //= IoUtils.find(new File(intent.getStringExtra("directory")), ".*\\." + intent.getStringExtra("extension") + "$");
		for (String n: files) {
			String fn = f.getName();
			int i = fn.lastIndexOf('.');
			if (i > 0 && i < fn.length() - 1) {
				filenames.add(fn.substring(0, i));
			}
		}
		if (filenames.size() > 0) {
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoadDialog.this, android.R.layout.simple_list_item_1, filenames);
			filelist.setAdapter(adapter);
		}
		
		Button cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				setResult(RESULT_CANCELED, result);
				finish();
			}
		});
		
		filelist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
				TextView item = (TextView) v;
				String name = item.getText().toString();
				Intent result = new Intent();
				result.putExtra("filename", name);
				setResult(RESULT_OK, result);
				finish();
			}
		});
	}
}
*/