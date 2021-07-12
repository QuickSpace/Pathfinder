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
import android.widget.ListView;
import android.widget.TextView;

public class AlgorithmSelector extends DialogFragment {
	
	private int algorithm = 0;
	ListView lv;
	TextView tv;
	String[] algorithmsArray = {"Алгоритм A*", "Best-first search", "Алгоритм Дейкстры"};

	public AlgorithmSelector() {
		
	}
	
	public interface AlgorithmListener {
		public void algorithmSelected(int algorithm);
	}
	
	private AlgorithmListener listener;
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Выбор алгоритма");
        View v = i.inflate(R.layout.alg_picker, null);
        
        lv = (ListView) v.findViewById(R.id.listView1);
        tv = (TextView) v.findViewById(R.id.textView1);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, algorithmsArray);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
				algorithm = position;
				tv.setText("Выбрано: " + algorithmsArray[position]);
			}
		});
        
        db.setView(v);
		db.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		    	listener.algorithmSelected(algorithm);
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
            listener = (AlgorithmListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString()
                    + " реализуйте NoticeDialogListener");
        }
    }

}
