package com.example.aaa;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.appcompat.widget.SwitchCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.aaa.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    SearchView searchView;
    ListView listView;
    DBUsers dbConnector;
    Context mContext;
    CustomAdapter customAdapter;
    CustomSpinnerAdapter customSpinnerAdapter;
    Spinner spinner;
    SwitchCompat switchCompat;

    TextView sumText, lenText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        mContext = this;
        dbConnector = new DBUsers(this);

        searchView = findViewById(R.id.search_view);
        listView = findViewById(R.id.list_view);
        customAdapter = new CustomAdapter(mContext, dbConnector.selectAll());
        listView.setAdapter(customAdapter);
        spinner = findViewById(R.id.spinner);
        switchCompat = findViewById(R.id.switch1);
        switchCompat.setChecked(true);
        updateList();
        sumText = findViewById(R.id.sumTextView);
        lenText = findViewById(R.id.lenTextView);

        sumText.setText(String.valueOf(customAdapter.getSum()));
        lenText.setText(String.valueOf(customAdapter.getLength()));



        Intent intent = new Intent(this, AddActivity.class);

        binding.fab.setOnClickListener(view -> {
            startActivity(intent);
            updateList();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateList(query);
                sumText.setText(String.valueOf(customAdapter.getSum()));
                lenText.setText(String.valueOf(customAdapter.getLength()));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateList(newText);
                sumText.setText(String.valueOf(customAdapter.getSum()));
                lenText.setText(String.valueOf(customAdapter.getLength()));
                return true;
            }
        });

        String[] parameters = {"По дате", "По сумме", "По алфавиту"};
        customSpinnerAdapter = new CustomSpinnerAdapter(mContext, parameters);
        spinner.setAdapter(customSpinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                customAdapter.setSort(item, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                customAdapter.set_direction(isChecked);
            }
        });

    }

    @Override
    protected void onResume() {
        updateList();
        super.onResume();
    }

    private void updateList () {
        customAdapter.setArrayMyData(dbConnector.selectAll());
        customAdapter.notifyDataSetChanged();
    }

    private void updateList (String request) {
        customAdapter.setArrayMyData(dbConnector.select(request));
        customAdapter.notifyDataSetChanged();
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}