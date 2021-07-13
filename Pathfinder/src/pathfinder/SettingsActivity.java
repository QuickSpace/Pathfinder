package pathfinder;

import java.util.Locale;
import java.util.Random;

import com.example.pathfinder.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	
	// Компоненты UI
	private Button saveAndExit;
	private TextView gridStatus;
	private LinearLayout ln1, ln2, ln3;
	private TextView tv1, tv2, tv3, themeStatus, gSizeTv, selSize;
	private Button generateRandom;
	private Spinner themeSpin;
	private CheckBox devModeCb, langCb;
	private SeekBar gSizeSb;
	private Locale locale;
	private boolean useEnglish = false;
	
	// Другое
	private Intent intent;
	private int gridSize = 16;
	private int themeCode = 0;
	private boolean devMode = false;
	private String[] themesArray = {"Космическая", "Классическая"};
	
	// Для цвета
	private int currentPColor = -1, currentTColor = -1, currentPathColor = -1;
	int[] pColors = new int[] {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.DKGRAY, Color.BLACK, Color.LTGRAY};	
    int[] tColors = pColors.clone();
    int[] pathColors = pColors.clone();
	int arrColor[] = { Color.WHITE, Color.RED, Color.GREEN, Color.BLUE,
			Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.LTGRAY,
			Color.GRAY, Color.DKGRAY, Color.BLACK };
	int colors = arrColor.length;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config_activity);
		
		intent = getIntent();
		gridSize = intent.getIntExtra("gridSize", 16);
		devMode = intent.getBooleanExtra("dev_mode", false);
		useEnglish = intent.getBooleanExtra("useEngLang", false);
		saveAndExit = (Button) findViewById(R.id.saveAndExit);
		saveAndExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("gSize", gridSize);
				intent.putExtra("tcode", themeCode);
				intent.putExtra("pcolor", currentPColor);
				intent.putExtra("tcolor", currentTColor);
				intent.putExtra("path_color", currentPathColor);
				intent.putExtra("dev_mode", devMode);
				intent.putExtra("useEng", useEnglish);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
		
		generateRandom = (Button) findViewById(R.id.randomColor);
		ln1 = (LinearLayout) findViewById(R.id.linearHscroll1);
        ln2 = (LinearLayout) findViewById(R.id.linearHscroll2);
        ln3 = (LinearLayout) findViewById(R.id.pathColorPick);
        tv1 = (TextView) findViewById(R.id.playerColorTv);
        tv2 = (TextView) findViewById(R.id.targetColorTv);
        tv3 = (TextView) findViewById(R.id.pathColorTv);
        themeStatus = (TextView) findViewById(R.id.themeStatus);
        gSizeTv = (TextView) findViewById(R.id.gridSizeStatus);
        selSize = (TextView) findViewById(R.id.selectedGSize);
        devModeCb = (CheckBox) findViewById(R.id.devModeCb);
        devModeCb.setChecked(devMode);
        langCb = (CheckBox) findViewById(R.id.langCb);
        langCb.setChecked(!useEnglish);
        setLocale(!useEnglish ? "ru" : "en");
        
        devModeCb.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				devMode = devModeCb.isChecked();
			}
		});
        
        langCb.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(langCb.isChecked()) {
					setLocale("ru");
					useEnglish = false;
				} else {
					setLocale("en");
					useEnglish = true;
				}
			}
		});
        
        generateView(pColors, tv1, ln1, 1);
        generateView(tColors, tv2, ln2, 2);
        generateView(pathColors, tv3, ln3, 3);
        
        generateRandom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ln1.removeAllViews();
				ln2.removeAllViews();
				ln3.removeAllViews();
				pColors = generateRandomColors(pColors.length);
				generateView(pColors, tv1, ln1, 1);
		        tColors = generateRandomColors(tColors.length);
		        generateView(tColors, tv2, ln2, 2);
		        pathColors = generateRandomColors(pathColors.length);
		        generateView(pathColors, tv3, ln3, 3);
			}
        });
		
		gridStatus = (TextView) findViewById(R.id.selectedGSize);
		gridStatus.setText("Выбранный размер: " + gridSize);
		
		gSizeSb = (SeekBar) findViewById(R.id.gridSizeSb);
		gSizeSb.setOnSeekBarChangeListener(this);
		gSizeSb.setProgress(gridSize - 16);
		
		themeSpin = (Spinner) findViewById(R.id.themeSpin);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, themesArray);
        themeSpin.setAdapter(adapter);
        themeSpin.setSelection(themeCode);
        
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	// HEX-код фиолетового цвета
    			int purpleColor = Color.rgb(247, 2, 141);
            	
            	((TextView) parent.getChildAt(0)).setTextColor(purpleColor);
                ((TextView) parent.getChildAt(0)).setTextSize(19);
                ((TextView) parent.getChildAt(0)).setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            	
            	themeCode = position;
            }
 
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            	themeCode = 0;
            }
        };
        themeSpin.setOnItemSelectedListener(itemSelectedListener);
	}
	
	public void setLocale(String lang) {
		locale = new Locale(lang);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = locale;
		res.updateConfiguration(conf, dm);
		getBaseContext().getResources().updateConfiguration(conf,
				getBaseContext().getResources().getDisplayMetrics());
		updateTexts();
	}
	
	public void updateTexts() {
		themeStatus.setText(R.string.theme_selector);
		gSizeTv.setText(R.string.grid_size);
		devModeCb.setText(R.string.developer_mode);
		langCb.setText(R.string.russian);
		saveAndExit.setText(R.string.save_and_exit);
		generateRandom.setText(R.string.random_color);
	}
	
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent();
		intent.putExtra("gSize", gridSize);
		intent.putExtra("tcode", themeCode);
		intent.putExtra("pcolor", currentPColor);
		intent.putExtra("tcolor", currentTColor);
		intent.putExtra("path_color", currentPathColor);
		intent.putExtra("dev_mode", devMode);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	public String formatHex(String hexCode) {
		if(hexCode.length() <= 1)
			return "0" + hexCode;
		return hexCode;
	}
	
	public int[] generateRandomColors(int count) {
		Random rand = new Random();
		int[] colors = new int[count];
		for(int i = 0; i < count; i++) {
			colors[i] = Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
		}
		
		return colors;
	}
	
	public void generateView(final int[] colorsArray, final TextView tv, LinearLayout ln, final int mode) {
		for (int j = 0; j < colorsArray.length; j++) {
			ImageView b = new ImageView(getApplicationContext());
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

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		gridStatus.setText("Выбранный размер: " + (gridSize = progress + 16));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}

}
