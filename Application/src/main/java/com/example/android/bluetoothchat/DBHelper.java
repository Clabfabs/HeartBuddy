package com.example.android.bluetoothchat;

/**
 * Created by isler on 20-Apr-16.
 */

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "HealthKit.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_PHONE = "phone";

    private static DBHelper instance;

    // Singleton pattern
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public static DBHelper getDBHelper(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
            return instance;
        } else {
            return instance;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + CONTACTS_TABLE_NAME + "(" +
                CONTACTS_COLUMN_ID + " INTEGER PRIMARY KEY," +
                CONTACTS_COLUMN_NAME + " TEXT," +
                CONTACTS_COLUMN_PHONE + " TEXT)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    public void reset() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_NAME, name);
        contentValues.put(CONTACTS_COLUMN_PHONE, phone);
        db.insert(CONTACTS_TABLE_NAME, null, contentValues);
        return true;
    }

    public int numberOfContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
    }

    public boolean updateContact(Integer id, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_NAME, name);
        contentValues.put(CONTACTS_COLUMN_PHONE, phone);
        db.update(CONTACTS_TABLE_NAME, contentValues, CONTACTS_COLUMN_ID + "= ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteContact(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CONTACTS_TABLE_NAME,
                CONTACTS_COLUMN_ID + "= ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<String> getAllContacts() {
        ArrayList<String> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + CONTACTS_TABLE_NAME, null);
        res.moveToFirst();

        // TODO: Only takes names, we want phone numbers as well
        /*while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            res.moveToNext();
        }*/
        //To add the contact details: name, phone numbers

        try {
            if (res.moveToFirst()) {
                do {
                    array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));

                    array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)));

                } while (res.moveToNext());
            }
        } finally {

            db.close();






        }

        return array_list;


    }
/* update of detail information.(Need to be verified well) */
  /*  public List< String > getAllContacts() {
        List< String> array_List = new ArrayList< String>();
        String refQuery = "Select * From " + CONTACTS_TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery(refQuery, null);
        try {
            if (res.moveToFirst()) {
                do {
                    String column_info = new String();

                    column_info.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));

                    column_info.setCol1(res.getString(1));
                    column_info.setCol2(res.getInt(2));

                    array_List.add(column_info);
                } while (res.moveToNext());
            }
        } finally {

            db.close();
        }

        Collections.sort(column_infoList);
        return column_infoList;
    }*/


}


