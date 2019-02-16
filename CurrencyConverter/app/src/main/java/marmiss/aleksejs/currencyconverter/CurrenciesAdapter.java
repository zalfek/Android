package marmiss.aleksejs.currencyconverter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;


public class CurrenciesAdapter extends BaseAdapter {

    private List<ExchangeRate>  dataList;

public CurrenciesAdapter(List<ExchangeRate> list) {

        this.dataList = list;

}

    @Override
    public int getCount() {
    return dataList.size();
     }

    @Override
    public String getItem(int position) {
    return dataList.get(position).getCurrencyName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_item, null, false);
        }

        ImageView image =(ImageView)convertView.findViewById(R.id.list_image_view);
        String imageName = "flag_"+ dataList.get(position).getCurrencyName().toLowerCase();
        image.setImageResource( context.getResources().getIdentifier(imageName, "drawable", "marmiss.aleksejs.currencyconverter"));

        TextView currencies = convertView.findViewById(R.id.list_text_view1);
        currencies.setText(dataList.get(position).getCurrencyName());

        TextView rates = convertView.findViewById(R.id.list_text_view2);
        double buffer = dataList.get(position).getRateForOneEuro();
        rates.setText("" + buffer);


        return convertView;

    }

}
