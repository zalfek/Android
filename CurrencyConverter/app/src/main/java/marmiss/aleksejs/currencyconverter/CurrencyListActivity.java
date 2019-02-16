package marmiss.aleksejs.currencyconverter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class CurrencyListActivity extends AppCompatActivity {
    private CurrenciesAdapter ad;
    private ExchangeRateDatabase list = new ExchangeRateDatabase();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list);
        ad = new CurrenciesAdapter(Arrays.asList(list.getMembers()));
        ListView listView = findViewById(R.id.my_list_view);
        listView.setAdapter(ad);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currency = ad.getItem(position);
                ExchangeRateDatabase data = new ExchangeRateDatabase();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + data.getCapital(currency)));
                startActivity(intent);
            }
        });
    }
}
