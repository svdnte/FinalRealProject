package com.example.aaa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;


// Активность создания пользователя
public class AddActivity extends AppCompatActivity {
    Button backButton, saveButton;
    EditText surnameEdit, nameEdit, otchEdit, sumEdit, infoEdit;
    RadioButton nalRadio, cardRadio;
    CheckBox anonCheckBox;

    int method = -1;

    Context mContext;

    DBUsers dbConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbConnector = new DBUsers(this);
        mContext = this;

        saveButton = findViewById(R.id.save_button);
        surnameEdit = findViewById(R.id.surname_edit);
        nameEdit = findViewById(R.id.name_edit);
        otchEdit = findViewById(R.id.otch_edit);
        sumEdit = findViewById(R.id.sum_edit);
        infoEdit = findViewById(R.id.info_edit);
        nalRadio = findViewById(R.id.nal_radio);
        cardRadio = findViewById(R.id.card_radio);
        backButton = findViewById(R.id.back);
        anonCheckBox = findViewById(R.id.anon_check_box);

        backButton.setOnClickListener(v -> {
            if (anonCheckBox.isChecked() || !surnameEdit.getText().toString().isEmpty() ||
            !nameEdit.getText().toString().isEmpty() || !otchEdit.getText().toString().isEmpty() ||
            !sumEdit.getText().toString().isEmpty() || !infoEdit.getText().toString().isEmpty()) {
                new AlertDialog.Builder(mContext).setTitle("Подтверждение выхода").
                        setMessage("Данные не сохранены. Вы уверены, что хотите выйти?")
                        .setPositiveButton("Остаться", null)
                        .setNegativeButton("Выйти", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            } else {
                finish();
            }
        });
        anonCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                surnameEdit.setEnabled(false);
                nameEdit.setEnabled(false);
                otchEdit.setEnabled(false);
            } else {
                surnameEdit.setEnabled(true);
                nameEdit.setEnabled(true);
                otchEdit.setEnabled(true);
            }
        });

        nalRadio.setOnClickListener(v -> method = 0);
        cardRadio.setOnClickListener(v -> method = 1);

        saveButton.setOnClickListener(v -> {
            if ((!anonCheckBox.isChecked() && (surnameEdit.getText().toString().isEmpty() || nameEdit.getText().toString().isEmpty())) || sumEdit.getText().toString().isEmpty()
                    || method == -1) {
                Toast.makeText(mContext, "Не все данные введены!", Toast.LENGTH_LONG).show();
            } else {
                int anon = anonCheckBox.isChecked() ? 1 : 0;
                String name, surname, otch;
                if (anon == 1) {
                    name = "Аноним";
                    surname = "";
                    otch = "";
                } else {
                    name = nameEdit.getText().toString();
                    surname = surnameEdit.getText().toString();
                    otch = otchEdit.getText().toString();
                }
                long date = new Date().getTime();
                dbConnector.insert(anon, surname, name,
                        otch, date, Integer.parseInt(sumEdit.getText().toString()),
                        infoEdit.getText().toString(), method);
                Toast.makeText(mContext, "Успешно добавлено!", Toast.LENGTH_LONG).show();
                clearAll();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            void pattern(){
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
            @Override
            public void handleOnBackPressed() {
                if (anonCheckBox.isChecked() || !surnameEdit.getText().toString().isEmpty() ||
                        !nameEdit.getText().toString().isEmpty() || !otchEdit.getText().toString().isEmpty() ||
                        !sumEdit.getText().toString().isEmpty() || !infoEdit.getText().toString().isEmpty()) {
                    new AlertDialog.Builder(mContext).setTitle("Подтверждение выхода").
                            setMessage("Данные не сохранены. Вы уверены, что хотите выйти?")
                            .setPositiveButton("Остаться", null)
                            .setNegativeButton("Выйти", (dialog, which) -> pattern())
                            .setCancelable(false)
                            .show();
                } else {
                    pattern();
                }
            }
        });
    }

    // Очистить все поля
    private void clearAll(){
        nameEdit.setText("");
        surnameEdit.setText("");
        otchEdit.setText("");
        anonCheckBox.setChecked(false);
        nalRadio.setChecked(false);
        cardRadio.setChecked(false);
        infoEdit.setText("");
        sumEdit.setText("");

    }
}