package com.example.pathfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import databases.Field;
import databases.SQLController;
import dialogs.GridSizeDialog;
import dialogs.SelectExport;
import dialogs.SelectExport.ExportListener;

public class FileActivity extends Activity implements OnClickListener, ExportListener {
	
	// Компоненты UI (public)
	EditText et;
	ListView lv;
	TextView fileStatus;
	Button importFile, exportFile, deleteFile;
	
	// Для работы с файловой системой и базами данных
	public static final String EXTENSION = ".pfn";
	private File pathDir;
	private File file;
	private String fileName;
	private Intent intent;
	private SQLController controller;
	private StringBuilder dataString;
	private String mapName = "";
	private int gSize, pColor, tColor, pathColor, id;
	
	private int wrkMode = 1; // 0 - Импорт, 1 - Экспорт, 2 - Удаление
	private int ttlMode[] = {R.string.import_file, R.string.export_file, R.string.delete_file};
	
	// Другое
	private int selected_id = -1;
	private Field field;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sd_layout);
		
		et = (EditText) findViewById(R.id.sdFileName);
		fileStatus = (TextView) findViewById(R.id.fileStatus);
		lv = (ListView) findViewById(R.id.sdListView);
		importFile = (Button) findViewById(R.id.import_file);
		exportFile = (Button) findViewById(R.id.export_file);
		deleteFile = (Button) findViewById(R.id.delete_file);
		
		importFile.setOnClickListener(this);
		exportFile.setOnClickListener(this);
		deleteFile.setOnClickListener(this);
		
		if(!isSDCardAvailable()) {
			Toast.makeText(getApplicationContext(), "SD-карта не доступна!", Toast.LENGTH_LONG).show();
			finish();
		}
		
		fileStatus.setText(R.string.map_not_selected);
		intent = getIntent();
		wrkMode = intent.getIntExtra("mode", 0);
		setTitle(ttlMode[wrkMode] + " & " + ttlMode[(wrkMode + 1) % ttlMode.length]);
		toggleField(false);
		
		pathDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/Pathfinder/");
		if(!pathDir.exists())
			pathDir.mkdirs();
		
		// Сгенерировать список файлов в директории
		generateFiles(pathDir, lv);
		
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				selected_id = (int) id;
				et.setText(lv.getItemAtPosition(position).toString());
				fileName = lv.getItemAtPosition(position).toString();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.file_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.explorer:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			    Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
			         + "/Android/data/Pathfinder/");
			    startActivity(Intent.createChooser(intent, "Open folder"));
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void toggleField(boolean toggle) {
		et.setEnabled(toggle);
		et.setAlpha(!toggle ? 0.5f : 1f);
	}
	
	public void generateFiles(File path, ListView lv) {
		File[] files = path.listFiles();
		if(files == null)
			return;
		String[] fileNames = new String[files.length];
		for(int i = 0; i < files.length; i++) {
			fileNames[i] = files[i].getName();
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, fileNames);
		lv.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.import_file:
			file = new File(pathDir + "/" + fileName);
			if(file == null || selected_id == -1) {
				Toast.makeText(getApplicationContext(), "Необходимо выбрать файл!", 
						Toast.LENGTH_SHORT).show();
				return;
			}
			readFile(file);
			Toast.makeText(getApplicationContext(), "Файл с именем " + fileName.substring(0, fileName.length() 
					- EXTENSION.length()) + " был сохранён в базу данных!", Toast.LENGTH_SHORT).show();
			break;
		case R.id.export_file:
			if(selected_id == -1) {
				SelectExport seDialog = new SelectExport();
			    seDialog.show(getFragmentManager(), "export");
			} else {
				fileName = et.getText().toString();
				if(fileName.length() == 0) {
					Toast.makeText(getApplicationContext(), "Введите имя файла!", Toast.LENGTH_SHORT).show();
					return;
				}
				fileName = et.getText().toString().endsWith(".txt") || et.getText().toString().endsWith(".pfn")
						? fileName : fileName + EXTENSION;
				writeFile(fileName, field);
			}
			break;
		case R.id.delete_file:
			fileName = et.getText().toString();
			file = new File(pathDir + "/" + fileName);
			if(selected_id != -1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(FileActivity.this);
				builder.setMessage("Вы действительно хотите удалить файл? (" + fileName + ")");
				builder.setCancelable(false);
				builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (file.exists()) {
							file.delete();
							generateFiles(pathDir, lv);
							Toast.makeText(getApplicationContext(), "Файл с именем " + fileName + " был удалён!", 
									Toast.LENGTH_SHORT).show();
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
				builder.create();
				builder.show();
			} else
				Toast.makeText(getApplicationContext(), "Необходимо выбрать файл!", 
						Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	public void writeFile(String fileName, Field field) {
		if (isExternalStorageWritable()) {
			file = new File(pathDir + "/" + fileName);
			try {
				controller = new SQLController(getApplicationContext());
				controller.open();
				String data = controller.makeData(field);
				if(!file.exists())
					file.createNewFile();
				controller.close();
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				fileOutputStream.write(data.getBytes());
				fileOutputStream.close();
				generateFiles(pathDir, lv);
				Toast.makeText(getApplicationContext(), "Файл с именем " + fileName + " был сохранён!", 
						Toast.LENGTH_SHORT).show();
			} catch (java.io.IOException e) {
				e.printStackTrace();
				Toast.makeText(this, "Ошибка записи файла", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, "Внешняя память недоступна", Toast.LENGTH_LONG).show();
		}
	}
	
	public void readFile(File file) {
		if (isExternalStorageReadable()) {
			StringBuilder stringBuilder = new StringBuilder();
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				if (fileInputStream != null) {
					InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String newLine = null;
					while ((newLine = bufferedReader.readLine()) != null) {
						stringBuilder.append(newLine);
					}
					fileInputStream.close();
				}
				String mName = getNameFromFile(file);
				controller = new SQLController(getApplicationContext());
				controller.open();
				boolean flag = controller.nameExists(mName);
				controller.insertConfig(stringBuilder.toString(), flag);
				controller.close();
				generateFiles(pathDir, lv);
			} catch (java.io.IOException e) {
				e.printStackTrace();
				Toast.makeText(this, "Ошибка чтения файла", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, "Внешняя память недоступна", Toast.LENGTH_LONG).show();
		}
	}
	
	public String getNameFromFile(File file) {
		dataString = new StringBuilder();
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(file);
			if (fileInputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String newLine = null;
				while ((newLine = bufferedReader.readLine()) != null) {
					dataString.append(newLine);
				}
				fileInputStream.close();
			}
			String[] sa = dataString.toString().split("[|]");
			String ar[];
			for (int i = 0; i < sa.length; i++) {
				if (sa[i].startsWith("#DATA:"))
					continue;
				if (sa[i].startsWith("GRID:")) {
					int pos = sa[i].indexOf(":") + 1;
					ar = sa[i].substring(pos).split("[;]");

					return ar[0];
				}
			}
		} catch (java.io.IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "Ошибка чтения файла", Toast.LENGTH_LONG).show();
		}
		
		return dataString.toString();
	}
	
	@Override
	public void exportMapSelected(long id, Field field) {
		this.field = field;
		selected_id = (int) id;
		toggleField(true);
		
		fileStatus.setText(R.string.current_map + ": " + field.mapName);
	}
	
	public boolean isSDCardAvailable() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	public boolean isExternalStorageWritable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return true;
		}
		return false;
	}

	public boolean isExternalStorageReadable() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
		 || Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())) {
			return true;
		}
		return false;
	}

}
