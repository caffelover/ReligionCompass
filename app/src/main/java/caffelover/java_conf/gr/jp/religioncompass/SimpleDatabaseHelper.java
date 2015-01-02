package caffelover.java_conf.gr.jp.religioncompass;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by caffelover on 14/12/30.
 */
public class SimpleDatabaseHelper extends SQLiteOpenHelper{
    private static final String TAG = SimpleDatabaseHelper.class.getSimpleName();
    static final private String DBNAME = "religioncompass.sqlite";
    static final private int VERSION = 7;

    //コンストラクタ
    public SimpleDatabaseHelper(Context context){
        super(context,DBNAME,null,VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //聖地テーブル作成
        db.execSQL("Create Table HOLY_LAND(" +
                "DEF_FLG INTEGER NOT NULL" +
                ", HOLY_ID INTEGER NOT NULL" +
                ", HOLY_NAME_JP TEXT" +
                ", HOLY_NAME_ENG TEXT" +
                ", LATITUDE REAL" +
                ", LONGITUDE REAL" +
                ", SHOW_FLG INTEGER" +
                ", PRIMARY KEY(DEF_FLG,HOLY_ID))");
        //聖地テーブルデータ準備
        int def_flg = 1; //初期値フラグは1固定
        String[] holy_name_jp = {"エルサレム","メッカ","ブッダガヤ"};
        int[] holy_id = {1,2,3};
        String[] holy_name_eng = {"Jerusalem","Mecca","BuddhGaya"};
        double[] latitude = {31.768889,21.416667,24.697778};
        double[] longitude = {35.216111,39.816667,84.991944};
        int[] show_flg = {0,1,0};

        //トランザクション開始(聖地テーブル)
        db.beginTransaction();

        try{
            SQLiteStatement sql = db.compileStatement("insert into HOLY_LAND(" +
                    "DEF_FLG" +
                    ",HOLY_ID" +
                    ",HOLY_NAME_JP" +
                    ",HOLY_NAME_ENG" +
                    ",LATITUDE" +
                    ",LONGITUDE" +
                    ",SHOW_FLG) VALUES(?,?,?,?,?,?,?)");
            for (int i=0;i<holy_name_jp.length;i++){
                sql.bindLong(1,def_flg);
                sql.bindLong(2,holy_id[i]);
                sql.bindString(3,holy_name_jp[i]);
                sql.bindString(4,holy_name_eng[i]);
                sql.bindDouble(5,latitude[i]);
                sql.bindDouble(6,longitude[i]);
                sql.bindLong(7,show_flg[i]);
                sql.executeInsert();
            }

            //トランザクションを成功
            db.setTransactionSuccessful();
        }finally{
            //トランザクションを終了
            db.endTransaction();
        }

        //宗教テーブル作成
        db.execSQL("Create Table LAND_RELIGION(" +
                "DEF_FLG INTEGER NOT NULL" +
                ", HOLY_ID INTEGER NOT NULL" +
                ", HOLY_RELIGION_NO INTEGER NOT NULL" +
                ", RELIGION_NAME_JP TEXT" +
                ", RELIGION_NAME_ENG TEXT" +
                ", PERSUASION_JP TEXT" +
                ", PERSUASION_ENG TEXT" +
                ", PRIMARY KEY(DEF_FLG,HOLY_ID,HOLY_RELIGION_NO))");

        //宗教テーブルデータ準備
        int def_flg_rel = 1; //初期値フラグは1固定
        int[] holy_id_rel = {1,1,1,2,3,4,5};
        int[] holy_religion_no = {1,2,3,1,1,1,1};
        String[] religion_name_jp_rel = {"キリスト教","ユダヤ教","イスラム","イスラム","仏教","仏教","仏教"};
        String[] religion_name_eng_rel = {"Christian religion","Judaism","Islam","Islam","Buddhism","Buddhism","Buddhism"};
        String[] persuasion_jp = {"","","","","","浄土真宗","浄土真宗"};
        String[] persuasion_eng = {"","","","","","The True Pure Land School","The True Pure Land School"};

        //トランザクション開始(聖地テーブル)
        db.beginTransaction();

        try{
            SQLiteStatement sql_rel = db.compileStatement("insert into LAND_RELIGION(" +
                    "DEF_FLG" +
                    ",HOLY_ID" +
                    ",HOLY_RELIGION_NO" +
                    ",RELIGION_NAME_JP" +
                    ",RELIGION_NAME_ENG" +
                    ",PERSUASION_JP" +
                    ",PERSUASION_ENG) VALUES(?,?,?,?,?,?,?)");
            for (int i=0;i<holy_religion_no.length;i++){
                sql_rel.bindLong(1,def_flg_rel);
                sql_rel.bindLong(2,holy_id_rel[i]);
                sql_rel.bindLong(3,holy_religion_no[i]);
                sql_rel.bindString(4,religion_name_jp_rel[i]);
                sql_rel.bindString(5,religion_name_eng_rel[i]);
                sql_rel.bindString(6,persuasion_jp[i]);
                sql_rel.bindString(7,persuasion_eng[i]);
                sql_rel.executeInsert();
            }

            //トランザクションを成功
            db.setTransactionSuccessful();
        }finally{
            //トランザクションを終了
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_v, int new_v){
        db.execSQL("drop table if exists HOLY_LAND");
        db.execSQL("drop table if exists LAND_RELIGION");
        onCreate(db);
    }

}
