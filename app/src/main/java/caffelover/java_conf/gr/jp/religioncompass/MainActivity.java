package caffelover.java_conf.gr.jp.religioncompass;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.*;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;


public class MainActivity extends ActionBarActivity implements LocationListener,SensorEventListener{
    private double dest_latitude = 0.0D;
    private double dest_longitude = 0.0D;
    private float currentDegree = 0.0f;
    private boolean mIsMagSensor;
    private boolean mIsAccSensor;
    private double angle_to_dest = 0.0d;
    private ImageView image;
    private LocationManager mLocationManager;
    private SensorManager mSensorManager;
    private SimpleDatabaseHelper helper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //コンパス画像
        image = (ImageView) findViewById(R.id.ImageViewCompas);

        //センサーマネージャ取得
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        //DBヘルパー準備
        helper = new SimpleDatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        //目的地データの取得
        String sql = "select * from HOLY_LAND a " +
                "inner join " +
                "LAND_RELIGION b " +
                "on a.DEF_FLG = b.DEF_FLG " +
                "and a.HOLY_ID = b.HOLY_ID " +
                "where a.SHOW_FLG = 1;";

        SQLiteCursor c = (SQLiteCursor)db.rawQuery(sql,null);

        String holy_name = "";
        String religion_name = "";

        if(c.moveToFirst()) {
            holy_name = c.getString(c.getColumnIndex("HOLY_NAME_JP")); //目的地名
            dest_latitude = c.getDouble(c.getColumnIndex("LATITUDE")); //緯度
            dest_longitude = c.getDouble(c.getColumnIndex("LONGITUDE")); //経度

            for (int i = 0; i < c.getCount(); i++) {
                religion_name = religion_name + c.getString(c.getColumnIndex("RELIGION_NAME_JP")) + " " ;

                if(c.getString(c.getColumnIndex("PERSUASION_JP")).length() != 0){
                    religion_name = religion_name + "(" + c.getString(c.getColumnIndex("PERSUASION_JP")) + ")" ;
                }
                religion_name = religion_name + " ";
                c.moveToNext();
            }
        }

        //目的地データのセットと表示
        TextView textViewHoly = (TextView)findViewById(R.id.db_holy);
        TextView textViewReligion1 = (TextView)findViewById(R.id.db_religion1);
        TextView textViewDestLa = (TextView)findViewById(R.id.dest_latitude);
        TextView textViewDestLong = (TextView)findViewById(R.id.dest_longitude);
        textViewHoly.setText("目的地: " + holy_name);
        textViewDestLa.setText(String.valueOf("目的緯度: " + dest_latitude));
        textViewDestLong.setText(String.valueOf("目的経度: " + dest_longitude));
        textViewReligion1.setText("宗教: " + religion_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        if (mLocationManager != null) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
//                LocationManager.NETWORK_PROVIDER,
                    0,
                    0,
                    this);
        }

        super.onResume();
        // センサの取得
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        // センサマネージャへリスナーを登録(implements SensorEventListenerにより、thisで登録する)
        for (Sensor sensor : sensors) {
            if( sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                mIsMagSensor = true;
            }

            if( sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                mIsAccSensor = true;
            }
        }
    }

    @Override
    protected void onPause() {
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }

        //センサーマネージャのリスナ登録破棄
        if (mIsMagSensor || mIsAccSensor) {
            mSensorManager.unregisterListener(this);
            mIsMagSensor = false;
            mIsAccSensor = false;
        }

        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView textViewPresentLa = (TextView)findViewById(R.id.present_latitude);
        TextView textViewPresentLong = (TextView)findViewById(R.id.present_longitude);
        TextView textViewAngle = (TextView)findViewById(R.id.angle);
        angle_to_dest = getDirection(location.getLatitude(),location.getLongitude(),dest_latitude, dest_longitude);

        textViewPresentLa.setText("現在緯度: " + location.getLatitude());
        textViewPresentLong.setText("現在経度: " + location.getLongitude());
        textViewAngle.setText("角度: " + angle_to_dest);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    /*    TextView textViewStatus = (TextView)findViewById(R.id.status);
        switch (status) {
            case LocationProvider.AVAILABLE:
                textViewStatus.setText("Status: AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                textViewStatus.setText("Status: OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                textViewStatus.setText("Status: TEMPORARILY_UNAVAILABLE");
                break;
        }*/
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    //センサー処理
    private static final int MATRIX_SIZE = 16;

    /* 回転行列 */
    float[]  inR = new float[MATRIX_SIZE];
    //float[] outR = new float[MATRIX_SIZE];
    float[]    I = new float[MATRIX_SIZE];

    /* センサーの値 */
    float[] orientationValues   = new float[3];
    float[] magneticValues      = new float[3];
    float[] accelerometerValues = new float[3];

    public void onSensorChanged(SensorEvent event){
        // get the angle around the z-axis rotated
        TextView textview_degree = (TextView)findViewById(R.id.view_degree);
        TextView textview_current_degree = (TextView)findViewById(R.id.view_current_degree);
        float degree = 0.0f;

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values.clone();
        }

        if (magneticValues != null && accelerometerValues != null) {

            SensorManager.getRotationMatrix(inR, I, accelerometerValues, magneticValues);

            //Activityの表示が縦固定の場合。横向きになる場合、修正が必要です
            //SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            SensorManager.getOrientation(inR, orientationValues);

            degree = Math.round(Math.toDegrees(orientationValues[0]));

            textview_degree.setText("MAG+ACC: " + degree);
            textview_current_degree.setText("CurrentDegree: " + currentDegree);

            //currentDegree = (float)(degree + angle_to_dest);

            //アニメーション作成
            RotateAnimation ra = new RotateAnimation(
                    currentDegree + (float)angle_to_dest,
                    -degree + (float)angle_to_dest,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(10);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            image.startAnimation(ra);
            //currentDegree = (float)(-degree-angle_to_dest);
            currentDegree = -degree;
        }

    }

    public static double getDirection(double latitude1, double longitude1, double latitude2, double longitude2) {
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double lng1 = Math.toRadians(longitude1);
        double lng2 = Math.toRadians(longitude2);
        double Y = Math.sin(lng2 - lng1) * Math.cos(lat2);
        double X = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);
        double deg = Math.toDegrees(Math.atan2(Y, X));
        double angle = (deg + 360) % 360;
        //return (int) (Math.abs(angle) + (1 / 7200));
        return angle + (1 / 7200);
    }

}
