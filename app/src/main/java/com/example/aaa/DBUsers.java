package com.example.aaa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class DBUsers {
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "users";

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE = "Date";
    private static final String COLUMN_NAME = "Name";
    private static final String COLUMN_SURNAME = "Surname";
    private static final String COLUMN_OTCH = "Otch";
    private static final String COLUMN_SUM = "Sum";
    private static final String COLUMN_INFO = "Info";
    private static final String COLUMN_ANON = "Anon";
    private static final String COLUMN_METHOD = "Meth";
    private static final String COLUMN_ABSOLUT_ID = "Abs_id";

    private static final int NUM_COLUMN_ID = 0;
    private static final int NUM_COLUMN_DATE = 5;
    private static final int NUM_COLUMN_NAME = 3;
    private static final int NUM_COLUMN_SURNAME = 2;
    private static final int NUM_COLUMN_OTCH = 4;
    private static final int NUM_COLUMN_SUM = 6;
    private static final int NUM_COLUMN_INFO = 7;
    private static final int NUM_COLUMN_ANON = 1;
    private static final int NUM_COLUMN_METHOD = 8;
    private static final int NUM_COLUMN_ABSOLUT_ID = 9;

    private final SQLiteDatabase database;

    public DBUsers(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        Log.w("DATABASE", database.toString());
    }

    public void insert(int anon, String surname, String name, String otch, long date, int sum, String info, int meth) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ANON, anon);
        cv.put(COLUMN_SURNAME, surname);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_OTCH, otch);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_SUM, sum);
        cv.put(COLUMN_INFO, info);
        cv.put(COLUMN_METHOD, meth);

        database.beginTransaction();
        Cursor cursor = database.query(TABLE_NAME, new String[]{"surname", "name", "otch"},
                "surname = ? AND name = ? AND otch = ?", new String[]{surname, name, otch}, null, null, null);

        if (cursor.getCount() > 0) {
            database.update("users", cv, "surname = ? AND name = ? AND otch = ?", new String[]{surname, name, otch});
        } else {
            database.insert(TABLE_NAME, null, cv);
        }
        cursor.close();
    }

    public Object[] select(String request) {
        String[] args = request.split(" ");
        Set<User> solutions = new HashSet<>();

        for (String arg : args) {
            Cursor mCursor = database.query(TABLE_NAME, null, COLUMN_SURNAME +
                            " LIKE \"" + arg + "%\"" + " OR " + COLUMN_NAME + " LIKE \"" + arg + "%\""
                            + " OR " + COLUMN_OTCH + " LIKE \"" + arg + "%\"", null, null,
                    null, null);

            mCursor.moveToFirst();
            if (!mCursor.isAfterLast()) {
                do {
                    long id = mCursor.getLong(NUM_COLUMN_ID);
                    int anon = mCursor.getInt(NUM_COLUMN_ANON);
                    String surname = mCursor.getString(NUM_COLUMN_SURNAME);
                    String name = mCursor.getString(NUM_COLUMN_NAME);
                    String otch = mCursor.getString(NUM_COLUMN_OTCH);
                    long date = mCursor.getLong(NUM_COLUMN_DATE);
                    int sum = mCursor.getInt(NUM_COLUMN_SUM);
                    String info = mCursor.getString(NUM_COLUMN_INFO);
                    int meth = mCursor.getInt(NUM_COLUMN_METHOD);
                    solutions.add(new User(id, anon, surname, name, otch, date, sum, info, meth));
                } while (mCursor.moveToNext());
            }
            mCursor.close();
        }
        solutions.toArray();
        return solutions.toArray();
    }


    public Object[] selectAll() {
        Cursor mCursor = database.query(TABLE_NAME, null, null, null, null, null, null);

        ArrayList<User> arr = new ArrayList<User>();
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                long id = mCursor.getLong(NUM_COLUMN_ID);
                int anon = mCursor.getInt(NUM_COLUMN_ANON);
                String surname = mCursor.getString(NUM_COLUMN_SURNAME);
                String name = mCursor.getString(NUM_COLUMN_NAME);
                String otch = mCursor.getString(NUM_COLUMN_OTCH);
                long date = mCursor.getLong(NUM_COLUMN_DATE);
                int sum = mCursor.getInt(NUM_COLUMN_SUM);
                String info = mCursor.getString(NUM_COLUMN_INFO);
                int meth = mCursor.getInt(NUM_COLUMN_METHOD);
                arr.add(new User(id, anon, surname, name, otch, date, sum, info, meth));
            } while (mCursor.moveToNext());
        }
        Log.w("INSERT", Arrays.toString(arr.toArray()));
        mCursor.close();
        return arr.toArray();
    }

    public void deleteAll() {
        database.delete(TABLE_NAME, null, null);
    }

    public void delete(long id) {
        database.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void deleteSome(Object[] ids) {
        for (Object i : ids) {
            delete((int) i);
        }
    }

    public void saveProductsFromJson(JSONObject jsonObject) throws JSONException {
        deleteAll();
        JSONArray users = jsonObject.getJSONArray("users");

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);

            int id = user.getInt("id");
            int anon = user.getInt("anon");
            String name = user.getString("name");
            String surname = user.getString("surname");
            String otch = user.getString("otch");
            int date = user.getInt("date");
            int sum = user.getInt("sum");
            String info = user.getString("info");
            int meth = user.getInt("meth");

            this.insert(anon, surname, name, otch, date, sum, info, meth);
        }
    }


    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ANON + " INTEGER, " +
                    COLUMN_SURNAME + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_OTCH + " TEXT, " +
                    COLUMN_DATE + " INTEGER, " +
                    COLUMN_SUM + " INTEGER, " +
                    COLUMN_INFO + " TEXT, " +
                    COLUMN_METHOD + " INTEGER, " +
                    COLUMN_ABSOLUT_ID + " INTEGER ); ";
            db.execSQL(query);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}