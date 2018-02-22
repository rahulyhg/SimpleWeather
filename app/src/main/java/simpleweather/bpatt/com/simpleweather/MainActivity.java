package simpleweather.bpatt.com.simpleweather;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity implements View.OnClickListener {

    public static final String WX_URL = "http://forecast.weather.gov/MapClick.php?lat=35.913501&lon=-79.051664&FcstType=json";
    public static final int WEATHER_NOTIFICATION_ID = 99;
    public static final String NOTIFICATION_CHANNEL = "notify_weather_update";

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    private void setup() {

        button = findViewById(R.id.button);
        button.setOnClickListener(this);

        GetDataTask tsk = new GetDataTask();
        tsk.execute(new String[]{});
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button:
                //notifyUser();
                break;
        }
    }


    protected   void notifyUser(String temp, String weather) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL);
        Intent intent = new Intent(this.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(temp + " degrees and " + weather);
        bigText.setBigContentTitle("Current Conditions");
        bigText.setSummaryText("Weather from NOAA");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Your Title");
        mBuilder.setContentText("Your text");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }
        
        mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
    }

    public void processWeatherData(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            JSONObject current = obj.getJSONObject("currentobservation");
            String temp = current.getString("Temp");
            String weather = current.getString("Weather");

            notifyUser(temp, weather);
        } catch(JSONException e) {
            e.printStackTrace();
        }

    }

    class GetDataTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                // create a url
                URL url = new URL(WX_URL);
                // create a HttpUrlConnection for server communication and open the connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // connect to the endpoint
                connection.connect();
                // create a stream to load the bytes into
                InputStream in = connection.getInputStream();

                // BufferedReader
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                response = sb.toString();

            } catch(Exception e) {
                e.printStackTrace();
            }


            return response;
        }

        protected void onProgressUpdate(Integer... params) {

        }

        protected void onPostExecute(String result) {
            processWeatherData(result);
        }

    }
}
