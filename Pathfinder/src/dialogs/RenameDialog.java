package dialogs;

import com.example.pathfinder.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import databases.Field;
import databases.SQLController;

public class RenameDialog extends DialogFragment {
	
	private SQLController controller;
	private Field field;
	private EditText et;
	private MapDialog md;

	public RenameDialog(Field field, MapDialog md, SQLController controller) {
		this.field = field;
		this.md = md;
		this.controller = controller;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Переименование поля");
        View v = i.inflate(R.layout.rename_dialog, null);
        
        et = (EditText) v.findViewById(R.id.newMapNameEt);
        
		db.setView(v);
		db.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	if(et.getText().toString().length() == 0) {
		    		Toast.makeText(getActivity().getApplicationContext(), "Введите название!", Toast.LENGTH_LONG).show();
		    		return;
		    	}
		    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Вы действительно хотите переименовать карту? (" + field.mapName + ")");
				builder.setCancelable(false);
				builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						controller.updateMap(field.id, et.getText().toString(), field.gsize, field.pcolor, field.tcolor, field.lcolor);
						md.refresh();
					}
				});
				builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getDialog().dismiss();
					}
				});
				
				builder.create().show();
		    }
		});
		db.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				getDialog().dismiss();
		    }
		});
		db.setCancelable(false);
        
        return db.create();
    }

}
