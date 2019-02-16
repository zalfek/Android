package marmiss.aleksejs.currencyconverter;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;


public class MainActivity extends AppCompatActivity  {

   private ShareActionProvider shareActionProvider;
   private ExchangeRateDatabase list = new ExchangeRateDatabase();
   private CurrenciesAdapter adapter = new CurrenciesAdapter(Arrays.asList(list.getMembers()));
   private ExchangeRateUpdateRunnable runnable;
   private Spinner Spinner1;
   private Spinner Spinner2;
   private static final int JOB_ID = 101;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner1 = (Spinner)findViewById(R.id.spinner1);
        Spinner2 = (Spinner)findViewById(R.id.spinner2);
        Spinner1.setAdapter(adapter);
        Spinner2.setAdapter(adapter);
        android.support.v7.widget.Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           registerService();
       }
       LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
               new IntentFilter("Currencies were updated"));
   }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
            Toast toast = Toast.makeText(MainActivity.this, "Currencies update finished!", Toast.LENGTH_SHORT);
            toast.show();
            Log.d("receiver", "Got message: ");
        }
    };
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.my_menu, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);
        setShareText(null);
        return true;
    }
    private void setShareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareActionProvider.setShareIntent(shareIntent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case R.id.my_entry:
                GoToAnotherActivity(item.getActionView());
                return true;
            case R.id.refresh_rates:
                runnable = new ExchangeRateUpdateRunnable(list,this);
                new Thread(runnable).start();
                return true;
        default:return super.onOptionsItemSelected(item);
        }

    }
    public void onClick (View view) {

      Spinner Spinner1 = (Spinner)findViewById(R.id.spinner1);
      Spinner Spinner2 = (Spinner)findViewById(R.id.spinner2);
      TextView textView = (TextView)findViewById(R.id.textView5);
      EditText editText = (EditText)findViewById(R.id.editText);
        double exch;
      if(editText.getText().toString().matches("")) {
            exch = 0.0;
      }else{
           exch = list.convert(Double.parseDouble(editText.getText().toString()), Spinner1.getSelectedItem().toString(), Spinner2.getSelectedItem().toString());
      }
      DecimalFormat df = new DecimalFormat("#.##");
      String value = "" + df.format(exch);
      textView.setText(value);
        double money;
        if(editText.getText().toString().matches("")) {
            money = 0.0;
        }else{
            money =Double.parseDouble(editText.getText().toString());
        }
      setShareText("Currency Converter says: " + money + " " + Spinner1.getSelectedItem().toString() + " are " + value + " " + Spinner2.getSelectedItem().toString());

    }
    public void GoToAnotherActivity(View v) {
    Intent myIntent = new Intent(MainActivity.this, CurrencyListActivity.class);
    startActivity(myIntent);
}
    @Override
    protected void onPause(){
       super.onPause();
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        SharedPreferences ShPref = getSharedPreferences("Updated Currencies",Context.MODE_PRIVATE);
        SharedPreferences.Editor editorShared = ShPref.edit();
        String value = ((EditText)findViewById(R.id.editText)).getText().toString();
        int position1 = Spinner1.getSelectedItemPosition();
        int position2 = Spinner2.getSelectedItemPosition();
        editor.putString("Convert amount", value);
        editor.putInt("Convert From", position1);
        editor.putInt("Convert to", position2);
        for (int i = 0; i < list.getCurrencies().length; i++) {
            editorShared.putString(list.getCurrencies()[i], Double.toString(list.getExchangeRate(list.getCurrencies()[i])));
        }
        editor.apply();
        editorShared.apply();
    }
    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences ShPref = getSharedPreferences("Updated Currencies",Context.MODE_PRIVATE);
        String value = pref.getString("Convert amount", "");
        int position1 = pref.getInt("Convert From", 0);
        int position2 = pref.getInt("Convert to", 0);
        ((EditText)findViewById(R.id.editText)).setText(value);
        Spinner1.setSelection(position1);
        Spinner2.setSelection(position2);
        for (int i = 0; i < list.getCurrencies().length; i++) {
            double ShareExchangeRate = Double.parseDouble(Objects.requireNonNull(ShPref.getString(list.getCurrencies()[i],
                    Double.toString(list.getExchangeRate(list.getCurrencies()[i])))));
            list.setExchangeRate(list.getCurrencies()[i], ShareExchangeRate);
        }

    }
    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
    public void registerService(){
        ComponentName serviceName = new ComponentName(this, UpdateJobService.class);
        JobInfo jobInfo;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(JOB_ID, serviceName).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setRequiresDeviceIdle(false).setRequiresCharging(false).setPersisted(true).setPeriodic(86400000).build();
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if(scheduler.getPendingJob(JOB_ID) == null) {
                scheduler.schedule(jobInfo);
            }
        }
    }
}