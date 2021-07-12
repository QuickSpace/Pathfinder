package dialogs;

import com.example.pathfinder.MainActivity;
import com.example.pathfinder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import databases.DBHelper;
import databases.Field;
import databases.SQLController;

public class SelectExport extends DialogFragment implements OnClickListener {
	
	SQLController controller;
	SimpleCursorAdapter adapter;
	Cursor cursor;
	ListView lv;
	Field field;
	Button exit, select;
	long selected_id = -1; // Для случая, если карта не будет выбрана
	
	public interface ExportListener {
		public void exportMapSelected(long id, Field field);
	}
	
	ExportListener listener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        View v = i.inflate(R.layout.export_file_dialog, null);
        db.setTitle("Выбор карты");
        db.setView(v);
		db.setCancelable(false);
		
		exit = (Button) v.findViewById(R.id.exit);
		select = (Button) v.findViewById(R.id.select);
		lv = (ListView) v.findViewById(R.id.exportList);
		
		exit.setOnClickListener(this);
		select.setOnClickListener(this);
        
        controller = new SQLController(getActivity());
        controller.open();
        
        controller = new SQLController(getActivity());
        controller.open();
        Cursor cursor = controller.readMaps();
     	String[] from = new String[] { DBHelper.ID, DBHelper.MAP_NAME };
     	int[] to = new int[] { R.id.rowid, R.id.mapname };
     	adapter = new SimpleCursorAdapter(getActivity(), R.layout.map_list, cursor, from, to, 1);
     	lv.setAdapter(adapter);
     	adapter.notifyDataSetChanged();
     	controller.close();
     		
     	lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		@Override
    		public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
    			selected_id = (int) id;
    			Cursor cur = (Cursor) lv.getItemAtPosition(position);
    			field = SQLController.cursorToField(cur);
    		}
    	});
        
		controller.close();
		return db.create();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.select:
				if(selected_id != -1 && field != null) {
					listener.exportMapSelected(selected_id, field);
				} else {
					Toast.makeText(getActivity().getApplicationContext(), R.string.select_map, Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.exit:
				getDialog().dismiss();
				break;
		}
	}
	
	public void refresh() {
		Cursor cursor = controller.readMaps();
		adapter.changeCursor(cursor);
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Проверить, что activity реализовала interface и получить на него ссылку
        try {
            listener = (ExportListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString() + " реализуйте ExportListener");
        }
	}

}
