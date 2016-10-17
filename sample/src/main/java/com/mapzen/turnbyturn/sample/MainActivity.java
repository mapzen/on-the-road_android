package com.mapzen.turnbyturn.sample;

import com.mapzen.model.ValhallaLocation;
import com.mapzen.valhalla.Instruction;
import com.mapzen.valhalla.Route;
import com.mapzen.valhalla.RouteCallback;
import com.mapzen.valhalla.Router;
import com.mapzen.valhalla.ValhallaRouter;

import org.jetbrains.annotations.NotNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static com.mapzen.valhalla.Router.*;
import retrofit.RestAdapter;

public class MainActivity extends AppCompatActivity {

  Spinner languageSpinner;
  Spinner costingSpinner;
  EditText startLatText;
  EditText startLngText;
  EditText endLatText;
  EditText endLngText;
  ListView listView;

  Router router = new ValhallaRouter();
  Route route;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    configureRouter();
    configureLanguageSpinner();
    configureCostingSpinner();
    configureTextViews();
    configureRouteBtn();
    configureListView();
  }

  private void configureRouter() {
    router.setHttpHandler(new SampleHttpHandler(RestAdapter.LogLevel.FULL));
    router.setCallback(new RouteCallback() {
      @Override public void success(@NotNull Route route) {
        MainActivity.this.route = route;
        BaseAdapter adapter = (BaseAdapter) listView.getAdapter();
        adapter.notifyDataSetChanged();
      }

      @Override public void failure(int statusCode) {
      }
    });
  }

  private void configureLanguageSpinner() {
    languageSpinner = (Spinner) findViewById(R.id.language);
    ArrayAdapter<CharSequence> spinnerAdapter =
        ArrayAdapter.createFromResource(this, R.array.languages_array,
            android.R.layout.simple_spinner_item);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    languageSpinner.setAdapter(spinnerAdapter);
  }

  private void configureCostingSpinner() {
    costingSpinner = (Spinner) findViewById(R.id.costing_type);
    ArrayAdapter<CharSequence> spinnerAdapter =
        ArrayAdapter.createFromResource(this, R.array.costing_types_array,
            android.R.layout.simple_spinner_item);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    costingSpinner.setAdapter(spinnerAdapter);
  }

  private void configureTextViews() {
    startLatText = (EditText) findViewById(R.id.start_lat);
    startLngText = (EditText) findViewById(R.id.start_lng);
    endLatText = (EditText) findViewById(R.id.end_lat);
    endLngText = (EditText) findViewById(R.id.end_lng);
  }

  private void configureRouteBtn() {
    Button routeBtn = (Button) findViewById(R.id.route_btn);
    routeBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        generateRoute();
      }
    });
  }

  private void configureListView() {
    listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(new BaseAdapter() {
      @Override public int getCount() {
        if (route == null) {
          return 0;
        }
        return route.getRouteInstructions().size();
      }

      @Override public Object getItem(int position) {
        return null;
      }

      @Override public long getItemId(int position) {
        return position;
      }

      @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
          convertView = View.inflate(MainActivity.this, R.layout.route_item, null);
          holder = new ViewHolder(convertView);
          convertView.setTag(holder);
        } else {
          holder = (ViewHolder) convertView.getTag();
        }


        Instruction instruction = route.getRouteInstructions().get(position);

        holder.title.setText(" • " + instruction.getVerbalPreTransitionInstruction());
        holder.subtitle.setText(" • " + instruction.getHumanTurnInstruction());
        if (!instruction.getVerbalPostTransitionInstruction().isEmpty()) {
          holder.tertiaryTitle.setText(" • " + instruction.getVerbalPostTransitionInstruction());
        } else {
          holder.tertiaryTitle.setText(null);
        }

        ValhallaLocation point = route.getGeometry().get(position);
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        builder.append(point.getLatitude());
        builder.append(", ");
        builder.append(point.getLongitude());
        builder.append(")");
        holder.details.setText(builder.toString());

        return convertView;
      }

      class ViewHolder {

        TextView title;
        TextView subtitle;
        TextView tertiaryTitle;
        TextView details;

        ViewHolder(View view) {
          title = (TextView) view.findViewById(R.id.title);
          subtitle = (TextView) view.findViewById(R.id.subtitle);
          tertiaryTitle = (TextView) view.findViewById(R.id.tertiary_title);
          details = (TextView) view.findViewById(R.id.details);
        }
      }
    });
  }

  private void generateRoute() {
    String startLat = startLatText.getText().toString();
    String startLng = startLngText.getText().toString();
    String endLat = endLatText.getText().toString();
    String endLng = endLngText.getText().toString();
    if (startLat.isEmpty() || startLng.isEmpty() || endLat.isEmpty() || endLng.isEmpty()) {
      Toast.makeText(this, R.string.missing_vals, Toast.LENGTH_SHORT).show();
    } else {
      router.clearLocations();
      router.setLocation(new double[]{Double.valueOf(startLat), Double.valueOf(startLng)});
      router.setLocation(new double[]{Double.valueOf(endLat), Double.valueOf(endLng)});
      if (costingSpinner.getSelectedItem().equals("auto")) {
        router.setDriving();
      } else if (costingSpinner.getSelectedItem().equals("bicycle")) {
        router.setBiking();
      } else if (costingSpinner.getSelectedItem().equals("pedestrian")) {
        router.setWalking();
      } else {
        router.setMultimodal();
      }
      Language language = Language.valueOf((String) languageSpinner.getSelectedItem());
      router.setLanguage(language);
      router.fetch();
    }
  }
}
