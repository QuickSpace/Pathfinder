package databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_ELEMENTS = "Elements";
	public static final String TABLE_GRIDS = "Maps";
	public static final String ID = "_id";
	public static final String MAP_ID = "map_id";
	public static final String MAP_NAME = "mname";
	public static final String PLAYER_COLOR = "pcolor";
	public static final String TARGET_COLOR = "tcolor";
	public static final String PATH_COLOR = "path_color";
	public static final String CELL_X = "cx";
	public static final String CELL_Y = "cy";
	public static final String CELL_CODE = "ct";
	public static final String GRID_SIZE = "gsize";
	
	public static final String DB_NAME = "pathdata.db";
	public static final int DB_VERSION = 1;
	
	private static final String CREATE_ELEMENTS = "CREATE TABLE " + TABLE_ELEMENTS
			+ " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ CELL_X + " INTEGER NOT NULL, "
			+ CELL_Y + " INTEGER NOT NULL, "
			+ CELL_CODE + " INTEGER NOT NULL, "
			+ MAP_ID + " INTEGER NOT NULL, "
			+ "FOREIGN KEY (" + MAP_ID + ") REFERENCES " + TABLE_GRIDS + " (" + ID + ") ON DELETE CASCADE);";
	
	private static final String CREATE_GRIDS = "CREATE TABLE " + TABLE_GRIDS
			+ " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ MAP_NAME + " TEXT, "
			+ GRID_SIZE + " INTEGER, "
			+ PLAYER_COLOR + " INTEGER, "
			+ TARGET_COLOR + " INTEGER, "
			+ PATH_COLOR + " INTEGER );";

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_ELEMENTS);
		db.execSQL(CREATE_GRIDS);
		
		ContentValues cv = new ContentValues();
		cv.put(DBHelper.MAP_NAME, "default");
		cv.put(DBHelper.GRID_SIZE, 16);
		cv.put(DBHelper.PLAYER_COLOR, Color.GREEN);
		cv.put(DBHelper.TARGET_COLOR, Color.RED);
		cv.put(DBHelper.PATH_COLOR, Color.BLUE);
		db.insert(DBHelper.TABLE_GRIDS, null, cv);
		
		cv.clear();
		cv.put(DBHelper.CELL_X, 0);
		cv.put(DBHelper.CELL_Y, 0);
		cv.put(DBHelper.CELL_CODE, 1);
		cv.put(DBHelper.MAP_ID, 1);
		db.insert(DBHelper.TABLE_ELEMENTS, null, cv);
		
		cv.clear();
		cv.put(DBHelper.CELL_X, 15);
		cv.put(DBHelper.CELL_Y, 15);
		cv.put(DBHelper.CELL_CODE, 2);
		cv.put(DBHelper.MAP_ID, 1);
		db.insert(DBHelper.TABLE_ELEMENTS, null, cv);
		
		cv.clear();
		// false - заполнять клетку с x = 16, true - x = 0;
		boolean state = false;
		for(int y = 1; y < 16; y += 2) {
			for(int x = 0; x < 16; x++) {
				if(x == 15 && state)
					continue;
				else if(x == 0 && !state)
					continue;
				cv.put(DBHelper.CELL_X, x);
				cv.put(DBHelper.CELL_Y, y);
				cv.put(DBHelper.CELL_CODE, 3);
				cv.put(DBHelper.MAP_ID, 1);
				db.insert(DBHelper.TABLE_ELEMENTS, null, cv);
				cv.clear();
			}
			state = !state;
		}
	}
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ELEMENTS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRIDS);
		onCreate(db);
	}

}
