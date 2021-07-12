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
import android.widget.SeekBar;
import android.widget.TextView;

public class SpeedChanger extends DialogFragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
	
	// Для работы диалога
	public static final int DEFAULT_SPEED = 45;
	private int speed;
	
	// Компоненты UI
	SeekBar sb;
	TextView currSpeed;
	Button reset;
	
	public SpeedChanger(int speed) {
		this.speed = speed;
	}
	
	public interface SpeedListener {
		public void onSpeedChange(int value);
	}
	
	private SpeedListener listener;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle(R.string.animation_speed);
        View v = i.inflate(R.layout.speed_dialog, null);
        
        sb = (SeekBar) v.findViewById(R.id.speedSb);
		sb.setOnSeekBarChangeListener(this);
        currSpeed = (TextView) v.findViewById(R.id.currSpeed);
        reset = (Button) v.findViewById(R.id.defaultOption);
        currSpeed.setText(String.valueOf(speed));
        sb.setProgress(speed - 10);
        reset.setOnClickListener(this);
        
		db.setView(v);
		db.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	listener.onSpeedChange(speed);
				getDialog().dismiss();
		    }
		});
		db.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				getDialog().dismiss();
		    }
		});
        
        return db.create();
    }
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.defaultOption:
			this.speed = DEFAULT_SPEED;
			currSpeed.setText(String.valueOf(speed));
			sb.setProgress(speed - 10);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		this.speed = progress + 10;
		currSpeed.setText(String.valueOf(speed));
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Проверить, что activity реализовала interface 
        try {
            // Получаем ссылку на NoticeDialogListener для доставки событий в activity 
            listener = (SpeedListener) activity;
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
