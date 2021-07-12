package dialogs;

import com.example.pathfinder.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import databases.SQLController;

public class GridSizeDialog extends DialogFragment implements SeekBar.OnSeekBarChangeListener {
	
	SQLController controller;
	SeekBar sb1;
	TextView gridSize;
	EditText name;
	Button create;
	
	public static final int DEFAULT_GRID_SIZE = 16;
	private int currentGridSize;
	
	public interface GridSizeListener {
		public void changeGridSize(DialogFragment dialog, int gridSizeValue);
	}
	
	public GridSizeDialog() {

	}
	
	GridSizeListener listener;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Изменение размера");
        View v = i.inflate(R.layout.gsize_dialog, null);
        
        sb1 = (SeekBar) v.findViewById(R.id.seekBar1);
		sb1.setOnSeekBarChangeListener(this);
        
        gridSize = (TextView) v.findViewById(R.id.gridSize);
        gridSize.setText(String.valueOf(DEFAULT_GRID_SIZE));
        currentGridSize = DEFAULT_GRID_SIZE;
        
		db.setView(v);
		db.setPositiveButton("Применить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	listener.changeGridSize(GridSizeDialog.this, currentGridSize);
				getDialog().dismiss();
		    }
		});
		db.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				getDialog().dismiss();
		    }
		});
        
        return db.create();
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		currentGridSize = progress + 16;
		gridSize.setText(String.valueOf(currentGridSize));
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Проверить, что activity реализовала interface 
        try {
            // Получаем ссылку на NoticeDialogListener для доставки событий в activity 
            listener = (GridSizeListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString()
                    + " реализуйте NoticeDialogListener");
        }
    }

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	
}

/* ------------------------------------ (Больше не используется)

		create = (Button) v.findViewById(R.id.create);
		create.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.startActivity(GridSizeDialog.this, getGridSize());
				getDialog().dismiss();
			}
		});

---------------------------------------*/ 
