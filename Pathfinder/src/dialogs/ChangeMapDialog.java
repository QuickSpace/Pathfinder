package dialogs;

import com.example.pathfinder.MainActivity;
import com.example.pathfinder.R;
import android.annotation.SuppressLint;
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

public class ChangeMapDialog extends DialogFragment implements OnClickListener {
	
	// 0 - выбор карты, 1 - сохранение, 2 - удаление/переименование
	SQLController controller;
	SimpleCursorAdapter adapter;
	ListView lv;
	EditText et;
	Button btnExec, btnExit, btnInfo;
	LinearLayout ln;
	ImageView player, target, path;
	private Field field;
	private int dialogType = 0;
	private int selected_id = -1;
	private boolean infoShown = false;

	public ChangeMapDialog(int dialogType) {
		this.dialogType = dialogType;
	}
	
	public interface ChangeMapListener {
		public void openMap(Field field);
		public Field saveMap(String mapName);
	}
	
	ChangeMapListener listener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        View v = i.inflate(R.layout.change_map, null);
        switch(dialogType) {
        	case 0:
        		db.setTitle("Загрузка карты");
        		break;
        	case 1:
        		db.setTitle("Сохранение карты");
        		break;
        	case 2:
        		db.setTitle("Изменение карт");
        		break;
        }
        db.setView(v);
		db.setCancelable(false);
		
		btnExec = (Button) v.findViewById(R.id.execute);
		btnExit = (Button) v.findViewById(R.id.exit);
		btnInfo = (Button) v.findViewById(R.id.info);
		et = (EditText) v.findViewById(R.id.mapNameInput);
		ln = (LinearLayout) v.findViewById(R.id.imageViewLayout);
		player = (ImageView) v.findViewById(R.id.playerStatus);
		target = (ImageView) v.findViewById(R.id.targetStatus);
		path = (ImageView) v.findViewById(R.id.pathStatus);
		
		btnExec.setOnClickListener(this);
		btnExit.setOnClickListener(this);
		btnInfo.setOnClickListener(this);
		
		player.setVisibility(ImageView.GONE);
		target.setVisibility(ImageView.GONE);
		path.setVisibility(ImageView.GONE);
		ln.setVisibility(LinearLayout.GONE);
		
		if(dialogType == 0 || dialogType == 2)
			et.setVisibility(EditText.GONE);
        
        lv = (ListView) v.findViewById(R.id.mapsList);
        
        // Для случая, если карта не будет выбрана
        selected_id = -1;
        
        if(dialogType == 2)
        	btnInfo.setText(R.string.rename);
        
        if(dialogType == 1) {
        	btnInfo.setAlpha(0.5f);
        	btnInfo.setClickable(false);
        }
        
        controller = new SQLController(getActivity());
        controller.open();
        
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
    			if(infoShown && dialogType != 2)
    				infoShown = false;
    			selected_id = (int) id;
    			Cursor cur = (Cursor) lv.getItemAtPosition(position);
    			field = SQLController.cursorToField(cur);
    			if(dialogType == 1)
    				et.setText(field.mapName);
    		}
    	});
        
		controller.close();
		return db.create();
	}
	
	public void onExec() {
		switch(dialogType) {
			case 0:
				if(selected_id == -1) {
					Toast.makeText(getActivity(), "Выберите карту!", Toast.LENGTH_SHORT).show();
					break;
				}
				listener.openMap(field);
				break;
			case 1:
				if(et.getText().toString().length() == 0) {
		    		Toast.makeText(getActivity().getApplicationContext(), "Введите название!", Toast.LENGTH_SHORT).show();
		    		break;
				}
		    	listener.saveMap(et.getText().toString());
		    	getDialog().dismiss();
				break;
			case 2:
				if(selected_id == -1) {
					Toast.makeText(getActivity(), "Выберите карту!", Toast.LENGTH_SHORT).show();
					break;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Вы действительно хотите удалить карту? (" + field.mapName + ")");
				builder.setCancelable(false);
				builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (field.id > 1) { // запись default не удаляем !
							controller.deleteMap(field.id);
							refresh();
							((TextView) ((MainActivity) getActivity()).getDisplayState()).setText("Карта удалена!");
							selected_id = -1;
						}	
					}
				});
				builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				builder.create().show();
				break;
			case 3: // Выбрано переименование
				if(et.getText().toString().length() == 0) {
					Toast.makeText(getActivity(), "Введите новое имя!", Toast.LENGTH_SHORT).show();
					break;
				}
				
				builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Вы действительно хотите переименовать карту? (" + field.mapName + ")");
				builder.setCancelable(false);
				builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						controller.updateMap(field.id, et.getText().toString(), field.gsize, field.pcolor, field.tcolor, field.lcolor);
						refresh();
						et.setText("");
					}
				});
				builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				
				builder.create().show();
				break;
		}
	}
	
	public void onInfo() {
		if((dialogType == 0 || dialogType == 1) && field != null) {
			if(!infoShown) {
				player.setVisibility(ImageView.VISIBLE);
				target.setVisibility(ImageView.VISIBLE);
				path.setVisibility(ImageView.VISIBLE);
				ln.setVisibility(ImageView.VISIBLE);
				
				int pColor = field.pcolor;
				int tColor = field.tcolor;
				int pathColor = field.lcolor;
				
				player.setColorFilter(pColor);
				target.setColorFilter(tColor);
				path.setColorFilter(pathColor);
				infoShown = true;
			} else {
				player.setVisibility(ImageView.GONE);
				target.setVisibility(ImageView.GONE);
				path.setVisibility(ImageView.GONE);
				ln.setVisibility(LinearLayout.GONE);
				infoShown = false;
			}
		} else if(dialogType == 2 || dialogType == 3) {
			if(!infoShown) {
				et.setVisibility(EditText.VISIBLE);
				et.setHint(R.string.enter_new_map_name);
				btnExec.setText(R.string.save);
				dialogType = 3;
				infoShown = true;
			} else {
				et.setVisibility(EditText.GONE);
				btnExec.setText(R.string.execute);
				dialogType = 2;
				infoShown = false;
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.execute:
				onExec();
				if(dialogType == 0)
					getDialog().dismiss();
				break;
			case R.id.info:
				if(selected_id == -1) {
					Toast.makeText(getActivity(), "Выберите карту!", Toast.LENGTH_SHORT).show();
					break;
				}
				onInfo();
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
        // Проверить, что activity реализовала interface 
        try {
            listener = (ChangeMapListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString()
                    + " реализуйте ChangeMapListener");
        }
	}

}
