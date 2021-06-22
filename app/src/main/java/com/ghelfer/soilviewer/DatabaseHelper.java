package com.ghelfer.soilviewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO_DADOS = "agroa.db";
    private static int VERSAO = 1;


    public DatabaseHelper(Context context){
        super(context, NOME_BANCO_DADOS, null, VERSAO);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS soil");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE soil (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT," +
                "latitude TEXT," +
                "longitude TEXT," +
                "dt TEXT" +
                ");");

    }
}
