package dialogs;

import com.example.pathfinder.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.view.View.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import databases.DBHelper;
import databases.Field;
import databases.SQLController;

public class MapDialog extends DialogFragment implements OnClickListener {
	
	SQLController controller;
	SimpleCursorAdapter adapter;
	TextView noMapsDisplay;
	Cursor cursor;
	ListView lv;
	Field field;
	Button btnLoad, btnClose, btnDelete;
	private TextView displayState;
	private long selected_id = -1;
	
	public MapDialog(TextView displayState) {
		this.displayState = displayState;
	}
	
	public MapDialog() {
		
	}
	
	public interface MapListener {
		public void openDevMap(Field field);
	}
	
	MapListener listener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@SuppressLint("InflateParams")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        View v = i.inflate(R.layout.map_dialog, null);
        db.setTitle("Загрузка карты");
        db.setView(v);
		db.setCancelable(false);
        
        noMapsDisplay = (TextView) v.findViewById(R.id.textView1);
        noMapsDisplay.setVisibility(TextView.GONE);
        
        btnLoad = (Button) v.findViewById(R.id.btnMapLoad);
		btnClose = (Button) v.findViewById(R.id.btnMapClose);
		btnDelete = (Button) v.findViewById(R.id.btnMapDel);
		btnLoad.setOnClickListener(this);
		btnClose.setOnClickListener(this);
		btnDelete.setOnClickListener(this);
        
        lv = (ListView) v.findViewById(R.id.listView1);
        
        controller = new SQLController(getActivity());
        controller.open();
        
        // Для случая, если карта не будет выбрана
        selected_id = -1;
        
        if(controller.getProfilesCount() < 2) {
        	noMapsDisplay.setVisibility(TextView.VISIBLE);
        	noMapsDisplay.setText("К сожалению, у вас ещё нет карт. Сохраните текущее поле.");
        	controller.close();
        }
        else {
        	controller = new SQLController(getActivity());
        	controller.open();
        	Cursor cursor = controller.readMaps();
     		String[] from = new String[] { DBHelper.ID, DBHelper.MAP_NAME };
     		int[] to = new int[] { R.id.rowid, R.id.mapname };
     		adapter = new SimpleCursorAdapter(getActivity(), R.layout.map_list, cursor, from, to, 1);
    		adapter.notifyDataSetChanged();
     		lv.setAdapter(adapter);
     		controller.close();
     		
     		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    			@Override
    			public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
    				selected_id = id;
    				Cursor cur = (Cursor) lv.getItemAtPosition(position);
    				field = SQLController.cursorToField(cur);
    			}
    		});
     		
     		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, final long id) {
					Cursor cur = (Cursor) lv.getItemAtPosition(position);
    				field = SQLController.cursorToField(cur);
					FragmentManager fm = getFragmentManager();
					RenameDialog rd = new RenameDialog(field, MapDialog.this, controller);
					rd.show(fm, "rename");
					return true;
				}
			});
        }
        
        controller.close();
        return db.create();
    }
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnMapDel:
			if(selected_id != -1) {
				if (field.id > 1) { // запись default не удаляем !
					controller.deleteMap(field.id);
					refresh();
					displayState.setText("Карта удалена!");
				}
			} else
				Toast.makeText(getActivity(), "Выберите карту!", Toast.LENGTH_SHORT).show();
			break;
		case R.id.btnMapLoad:
			if(selected_id == -1) {
				Toast.makeText(getActivity(), "Выберите карту!", Toast.LENGTH_SHORT).show();
				return;
			}
			listener.openDevMap(field);
			getDialog().dismiss();
			break;
		case R.id.btnMapClose:
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
        // Проверить, что activity реализовала interface 
        try {
            listener = (MapListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString()
                    + " реализуйте NoticeDialogListener");
        }
	}

}
