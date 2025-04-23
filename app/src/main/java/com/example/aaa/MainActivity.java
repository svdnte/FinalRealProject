package com.example.aaa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;
import okio.GzipSource;
import okio.GzipSource;
import okio.Okio;

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
    Button deleteBtn;
    ViewSwitcher switcher;

    TextView sumText, lenText, selectedText;

    private boolean selectedMenuWorking = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        mContext = this;
        dbConnector = new DBUsers(this);

        switcher = findViewById(R.id.viewSwitcher);
        deleteBtn = findViewById(R.id.button_delete_view_switcher);
        searchView = findViewById(R.id.search_view);
        listView = findViewById(R.id.list_view);
        customAdapter = new CustomAdapter(mContext, dbConnector.selectAll());
        listView.setAdapter(customAdapter);
        spinner = findViewById(R.id.spinner);
        switchCompat = findViewById(R.id.switch1);
        switchCompat.setChecked(true);
        updateList();
        selectedText = findViewById(R.id.selectedCountView);
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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> customAdapter.set_direction(isChecked));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedMenuWorking) {
                customAdapter.setSelected(id);
                if (customAdapter.getSelectedCount() != 0) {
                    switcher.setDisplayedChild(1);
                    selectedText.setText("Выбрано элементов: " + customAdapter.getSelectedCount());
                    selectedMenuWorking = true;
                } else {
                    switcher.setDisplayedChild(0);
                    selectedMenuWorking = false;
                }
                customAdapter.notifyDataSetChanged();
            } else {
                Intent intent1 = new Intent(mContext, PersonActivity.class);
                intent1.putExtra("USER", customAdapter.getItem(position));
                startActivity(intent1);
            }
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            customAdapter.setSelected(id);
            if (customAdapter.getSelectedCount() != 0) {
                switcher.setDisplayedChild(1);
                selectedText.setText("Выбрано элементов: " + customAdapter.getSelectedCount());
                selectedMenuWorking = true;
            } else {
                switcher.setDisplayedChild(0);
                selectedMenuWorking = false;
            }
            customAdapter.notifyDataSetChanged();


            return true;
        });

        deleteBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(mContext).setTitle("Подтверждение удаления").
                    setMessage("Вы уверены, что хотите удалить данные пользователя?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        dbConnector.deleteSome(customAdapter.getSelectedItems().toArray());
                        customAdapter.clearSelected();
                        switcher.setDisplayedChild(0);
                        selectedMenuWorking = false;
                        customAdapter.setArrayMyData(dbConnector.selectAll());
                        customAdapter.notifyDataSetChanged();
                        customAdapter.notifyDataSetInvalidated();
                        dataChanged();
                        Toast.makeText(mContext, "Успешно удалено", Toast.LENGTH_SHORT).show();
                        ;
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        updateList();
        sumText.setText(String.valueOf(customAdapter.getSum()));
        lenText.setText(String.valueOf(customAdapter.getLength()));
        super.onResume();
    }

    private void updateList() {
        customAdapter.setArrayMyData(dbConnector.selectAll());
        customAdapter.notifyDataSetChanged();
    }

    private void updateList(String request) {
        customAdapter.setArrayMyData(dbConnector.select(request));
        customAdapter.notifyDataSetChanged();
    }

    private void dataChanged() {
        sumText.setText(String.valueOf(customAdapter.getSum()));
        lenText.setText(String.valueOf(customAdapter.getLength()));
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
        } else if (id == R.id.action_update) {
            NetworkHelperClass apiClient = NetworkHelperClass.getInstance(mContext);
            apiClient.get("/get_all", new NetworkHelperClass.JsonApiCallback() {
                public void onSuccess(JSONObject response) {
                    try {
                        dbConnector.saveProductsFromJson(response.getJSONObject("tables"));
                        customAdapter.setArrayMyData(dbConnector.selectAll());
                        customAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(mContext, "Произошла ошибка!" + "\nСвяжитесь с @svdnte", Toast.LENGTH_LONG).show();
                        });
                        this.onFailure(new Throwable("failed save json to sql in main activity"));
                    }
                }

                @Override
                public void onError(int statusCode, String message) {
                    // Обработка ошибки от сервера
                    Log.e("NETWORK_HELPER", "Error: " + statusCode + " - " + message);
                }

                @Override
                public void onFailure(Throwable t) {
                    // Обработка ошибки сети/соединения
                    Log.e("NETWORK_HELPER", "Failure: ", t);
                }
            });
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static class NetworkHelperClass {
        private static OkHttpClient client;
        private static final int CONNECT_TIMEOUT = 15;
        private static final int READ_TIMEOUT = 30;
        private static final int WRITE_TIMEOUT = 15;
        private final String baseUrl = "https://finalprojectbackend-oi8b.onrender.com";
        private static NetworkHelperClass instance;

        public NetworkHelperClass(Context context) {
            // Настройка OkHttp клиента
            client = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .addNetworkInterceptor(new GzipInterceptor()) // Для ответов
                    .build();
        }

        public static synchronized NetworkHelperClass getInstance(Context context) {
            if (instance == null) {
                instance = new NetworkHelperClass(context);
            }
            return instance;
        }

        public void get(String endpoint, JsonApiCallback callback) {
            Request.Builder builder = new Request.Builder()
                    .url(baseUrl + endpoint)
                    .addHeader("Accept-Encoding", "gzip")
                    .get();

            Request request = builder.build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onFailure(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseBody = response.body() != null ? response.body().string() : "";
                            Log.w("WWWW", responseBody);
                            Headers headers = response.headers();
                            for (String name : headers.names()) {
                                Log.d("HEADER", name + ": " + headers.get(name));
                            }
                            JSONObject json = new JSONObject(responseBody);
                            callback.onSuccess(json);
                        } catch (Exception e) {
                            callback.onFailure(e);
                        }
                    } else {
                        callback.onError(response.code(), response.message());
                    }
                }


            });
        }

        private void onError(int code, String message) {
        }

        void onFailure(Exception e) {
        }

        public void onSuccess(JSONObject json) {
        }



        public interface JsonApiCallback {
            void onSuccess(JSONObject response);

            void onError(int statusCode, String message);

            void onFailure(Throwable t);
        }
    }
}