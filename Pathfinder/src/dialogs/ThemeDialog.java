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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ThemeDialog extends DialogFragment {
	
	Spinner spin;
	TextView tv;
	String[] themesArray = {"Космическая", "Классическая"};
	private int theme = 0;

	public ThemeDialog() {
		
	}
	
	public interface ThemeListener {
		public void onThemeSelected(int theme);
	}
	
	private ThemeListener listener;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Выбор темы");
        View v = i.inflate(R.layout.theme_selector, null);
        
        spin = (Spinner) v.findViewById(R.id.spinner1);
        tv = (TextView) v.findViewById(R.id.textView1);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), 
        		android.R.layout.simple_spinner_dropdown_item, themesArray);
        spin.setAdapter(adapter);
        
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	theme = position;
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            	theme = 0;
            }
        };
        spin.setOnItemSelectedListener(itemSelectedListener);
        
        db.setView(v);
		db.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	listener.onThemeSelected(theme);
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
	
	@SuppressWarnings("deprecation")
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Проверить, что activity реализовала interface 
        try {
            // Получаем ссылку на NoticeDialogListener для доставки событий в activity 
            listener = (ThemeListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString()
                    + " реализуйте NoticeDialogListener");
        }
    }

}
