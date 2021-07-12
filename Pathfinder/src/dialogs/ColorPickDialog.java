package dialogs;

import java.util.Random;

import com.example.pathfinder.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import databases.SQLController;

public class ColorPickDialog extends DialogFragment {

	LinearLayout ln1, ln2, ln3;
	TextView tv1, tv2, tv3;
	private int currentPColor = -1, currentTColor = -1, currentPathColor = -1;
	SQLController controller;
	Button generateRandom;
	String[] pColorNames = new String[] {"Красный", "Синий", "Зелёный", "Желтый", "Фиолетовый", "Аква"};
	int[] pColors = new int[] {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.DKGRAY, Color.BLACK, Color.LTGRAY};	
	String[] tColorNames = new String[] {"Красный", "Синий", "Зелёный", "Желтый", "Фиолетовый", "Аква"};
    int[] tColors = new int[] {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.DKGRAY, Color.BLACK, Color.LTGRAY};
    int[] pathColors = new int[] {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.DKGRAY, Color.BLACK, Color.LTGRAY};
	int arrColor[] = { Color.WHITE, Color.RED, Color.GREEN, Color.BLUE,
			Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.LTGRAY,
			Color.GRAY, Color.DKGRAY, Color.BLACK };
	int colors = arrColor.length;
	
	public interface ColorPickListener {
		public void onPickedColor(int pColor, int tColor, int pathColor);
	}
	
	ColorPickListener listener;
	
	public ColorPickDialog() {
		
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity());
        LayoutInflater i = getActivity().getLayoutInflater();
        
        db.setTitle("Выбор цвета");
        View v = i.inflate(R.layout.color_picker, null);
        
        ln1 = (LinearLayout) v.findViewById(R.id.linearHscroll1);
        ln2 = (LinearLayout) v.findViewById(R.id.linearHscroll2);
        ln3 = (LinearLayout) v.findViewById(R.id.pathColorPick);
        tv1 = (TextView) v.findViewById(R.id.textView1);
        tv2 = (TextView) v.findViewById(R.id.textView2);
        tv3 = (TextView) v.findViewById(R.id.textView3);
        
        generateRandom = (Button) v.findViewById(R.id.button1);
        
        generateView(pColors, tv1, ln1, v, 1);
        generateView(tColors, tv2, ln2, v, 2);
        generateView(pathColors, tv3, ln3, v, 3);
        
        generateRandom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ln1.removeAllViews();
				ln2.removeAllViews();
				ln3.removeAllViews();
				pColors = generateRandomColors(pColors.length);
				generateView(pColors, tv1, ln1, v, 1);
		        tColors = generateRandomColors(tColors.length);
		        generateView(tColors, tv2, ln2, v, 2);
		        pathColors = generateRandomColors(pathColors.length);
		        generateView(pathColors, tv3, ln3, v, 3);
			}
        });
        
		db.setView(v);
		db.setPositiveButton("Применить", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
				controller = new SQLController(getActivity());
		    	listener.onPickedColor(currentPColor, currentTColor, currentPathColor);
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
	
	public void generateView(final int[] colorsArray, final TextView tv, LinearLayout ln, View v, final int mode) {
		for (int j = 0; j <= colorsArray.length - 1; j++) {
			ImageView b = new ImageView(v.getContext());
			b.setPadding(0, 0, 20, 0);
			b.setImageResource(R.drawable.oval);

			b.setId(j);
			b.setColorFilter(colorsArray[j]);

			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String colorHex = formatHex(Integer.toHexString(Color.red(colorsArray[v.getId()])))
							+ formatHex(Integer.toHexString(Color.green(colorsArray[v.getId()])))
							+ formatHex(Integer.toHexString(Color.blue(colorsArray[v.getId()])));
					tv.setText("Выбранный цвет (Hex): #" + colorHex.toUpperCase());
					switch(mode) {
					case 1:
						currentPColor = colorsArray[v.getId()];
						break;
					case 2:
						currentTColor = colorsArray[v.getId()];
						break;
					case 3:
						currentPathColor = colorsArray[v.getId()];
						break;
					}
				}
			});
			ln.addView(b);
		}
	}
	
	public String formatHex(String hexCode) {
		if(hexCode.length() <= 1)
			return "0" + hexCode;
		return hexCode;
	}
	
	public int[] generateRandomColors(int count) {
		Random rand = new Random();
		int[] colors = new int[count];
		for(int i = 0; i <= count - 1; i++) {
			colors[i] = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
		}
		
		return colors;
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Проверить, что activity реализовала interface 
        try {
            listener = (ColorPickListener) activity;
        } catch (ClassCastException e) {
            // activity не реализовала интерфейс 
            throw new ClassCastException(activity.toString()
                    + " реализуйте NoticeDialogListener");
        }
	}

}
