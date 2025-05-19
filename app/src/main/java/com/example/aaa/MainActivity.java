package com.example.aaa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private DatabaseAutoBackupManager dbm;
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

    private static final int REQUEST_CODE_SAVE_DB = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dbm = new DatabaseAutoBackupManager(this, "database.db");
        new Thread(() -> dbm.executeAutoBackup()).start();


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


        // Настройка листенеров и адаптеров

        binding.fab.setOnClickListener(view -> {
            startActivity(intent);
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
                        ;})
                    .setCancelable(false)
                    .show();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            void pattern(){
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
            @Override
            public void handleOnBackPressed() {
                if (selectedMenuWorking) {
                    customAdapter.clearSelected();
                    selectedMenuWorking = false;
                    switcher.setDisplayedChild(0);
                    customAdapter.notifyDataSetChanged();

                } else {
                    pattern();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        updateList();
        sumText.setText(String.valueOf(customAdapter.getSum()));
        lenText.setText(String.valueOf(customAdapter.getLength()));
        super.onResume();
    }

    // Обновляем list view свежими данными
    private void updateList() {
        customAdapter.setArrayMyData(dbConnector.selectAll());
        customAdapter.notifyDataSetChanged();
    }

    private void updateList(String request) {
        customAdapter.setArrayMyData(dbConnector.select(request));
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void dataChanged(){
        sumText.setText(String.valueOf(customAdapter.getSum()));
        lenText.setText(String.valueOf(customAdapter.getLength()));
    }

    // Меню с тремя точками
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_save_db){
            downloadDbFile();
        } else if (id == R.id.action_export_db){
            exportDb();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private File getDatabaseFile() {
        return getDatabasePath("database.db");
    }

    private void downloadDbFile() {
        // Получаем файл базы данных
        File dbFile = getDatabaseFile();

        if (dbFile.exists()) {
            // Создаем Intent для создания документа
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/x-sqlite3"); // MIME type для .db файлов
            intent.putExtra(Intent.EXTRA_TITLE, "backup " + new SimpleDateFormat("HH:mm dd-MM", Locale.getDefault())
                    .format(new Date()) + ".db");

            // Запускаем активность для выбора места сохранения
            startActivityForResult(intent, REQUEST_CODE_SAVE_DB);
        } else {
            Toast.makeText(this, "Файл базы данных не найден", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SAVE_DB && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri destinationUri = data.getData();
                copyDatabaseToDestination(destinationUri);
                shareDbFile(destinationUri);
            }
        }
    }
    private void copyDatabaseToDestination(Uri destinationUri) {
        File dbFile = getDatabaseFile();

        try (InputStream in = Files.newInputStream(dbFile.toPath());
             OutputStream out = getContentResolver().openOutputStream(destinationUri)) {

            if (out != null) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                Toast.makeText(this, "База данных успешно сохранена", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при экспорте базы данных", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareDbFile(Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/x-sqlite3");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Экспорт базы данных");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Файл базы данных");

        startActivity(Intent.createChooser(shareIntent, "Поделиться базой данных"));
    }

    private void exportDb(){
        Uri uri = FileProvider.getUriForFile(
                mContext,
                "com.example.aaa.provider",
                dbm.getLatestBackup()
        );
        shareDbFile(uri);
    }
}