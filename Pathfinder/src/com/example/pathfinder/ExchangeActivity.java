package com.example.pathfinder;

import com.example.bluetoothspplibrary.BluetoothSPP;
import com.example.bluetoothspplibrary.BluetoothState;
import com.example.bluetoothspplibrary.DeviceList;
import com.example.bluetoothspplibrary.BluetoothSPP.BluetoothConnectionListener;
import com.example.bluetoothspplibrary.BluetoothSPP.OnDataReceivedListener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import databases.DBHelper;
import databases.Field;
import databases.SQLController;

public class ExchangeActivity extends Activity implements OnClickListener {
	BluetoothSPP bt;	
	SQLController controller;
	
	TextView textStatus, textRead;
	EditText etMessage;
	Button btnConnect, loadBtn, sendBtn, clearBtn, saveBtn;

	ListView listView;
	SimpleCursorAdapter adapter;
	String currMapName = "";
	String receivedData = "";
	
	Cursor cursor;
	Field field;
	long selected_id = -1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exchange_activity);
		
		btnConnect = (Button) findViewById(R.id.btnConnect);
		loadBtn = (Button) findViewById(R.id.loadMapBtn);
		sendBtn = (Button) findViewById(R.id.btnSendMap);
		clearBtn = (Button) findViewById(R.id.btnClear);
		saveBtn = (Button) findViewById(R.id.btnSaveMap);
		
		textRead = (TextView) findViewById(R.id.textRead);
		textStatus = (TextView) findViewById(R.id.textStatus);
		etMessage = (EditText) findViewById(R.id.etMessage);
		listView = (ListView) findViewById(R.id.mapsListView);
		etMessage.setInputType(InputType.TYPE_NULL); // - это скрывает насовсем!
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		btnConnect.setOnClickListener(this);
		loadBtn.setOnClickListener(this);
		sendBtn.setOnClickListener(this);
		clearBtn.setOnClickListener(this);
		saveBtn.setOnClickListener(this);

		controller = new SQLController(getApplicationContext());
		cursor = controller.readMaps();
 		String[] from = new String[] { DBHelper.MAP_NAME };
 		int[] to = new int[] { R.id.mapNameTv };
 		adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.map_name, cursor, from, to, 1);
		adapter.notifyDataSetChanged();
 		listView.setAdapter(adapter);
		
		bt = new BluetoothSPP(this); 		
		if (!bt.isBluetoothAvailable()) {
			Toast.makeText(getApplicationContext(), R.string.bt_not_available, Toast.LENGTH_LONG).show();
			finish();
		}
		
		// Получение и вывод данных в поле сообщения 
		bt.setOnDataReceivedListener(new OnDataReceivedListener() {
			public void onDataReceived(byte[] data, String message) {
				textRead.setText("");
				if (message.startsWith("#DATA:")) {
					receivedData = message; 
					String[] lines = message.split("[|]");
					for (int i = 0; i < lines.length; i++) {
						textRead.append(lines[i] + "\n");
					}
				}
			}
		});

		// Слушатель подключения к bluetooth
		bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
			public void onDeviceDisconnected() {
				textStatus.setText(R.string.status_not_connected);// "Status : Not connected"
				btnConnect.setText(R.string.connect);
			}

			public void onDeviceConnectionFailed() {
				textStatus.setText(R.string.status_conn_failed);// "Status : Connection failed"
				btnConnect.setText(R.string.connect);
			}

			public void onDeviceConnected(String name, String address) {
				String sss = getResources().getString(R.string.status_connected_to, name);
				textStatus.setText(sss);// "Status : Connected to "
				btnConnect.setText(R.string.action_disconnect);
			}
		});

		// обработка выбора в списке
		OnItemClickListener itemClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				currMapName = ((TextView) view).getText().toString();
				etMessage.setText(currMapName);
				selected_id = id;
				Cursor cur = (Cursor) listView.getItemAtPosition(position);
				field = SQLController.cursorToField(cur);
			}
		};

		listView.setOnItemClickListener(itemClickListener);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnConnect:
				if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
					bt.disconnect();
					btnConnect.setText(R.string.connect);
				} else {
					bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
					Intent intent = new Intent(getApplicationContext(), DeviceList.class);
					startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
					btnConnect.setText(R.string.action_disconnect);
				}
				break;
			case R.id.btnMapLoad:
				adapter.changeCursor(controller.readMaps());
				break;
			case R.id.btnSaveMap:
				controller.insertConfig(receivedData, false);
				break;
			case R.id.btnSendMap:
				if(selected_id == -1)
					return;
				if (etMessage.getText().length() != 0) {
					// если ввели новое имя карты - делаем текущим
					field.mapName = currMapName = etMessage.getText().toString();
				}
				// читаем данные из БД
				String data = controller.makeData(field);
				bt.send(data, true);
				break;
			case R.id.btnClear:
				etMessage.setText("");
				break;
			default:
				break;
		}
	}
	
	public void onStart() {
		super.onStart();
		if (!bt.isBluetoothEnabled()) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
		} else {
			if (!bt.isServiceAvailable()) {
				bt.setupService();
				bt.startService(BluetoothState.DEVICE_ANDROID);
			}
		}
	}
	
	public void onDestroy() {
		super.onDestroy();
		bt.stopService();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			if (resultCode == Activity.RESULT_OK)
				bt.connect(data);

		} else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				bt.setupService();
				bt.startService(BluetoothState.DEVICE_ANDROID);
			} else {
				Toast.makeText(getApplicationContext(), R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

}
