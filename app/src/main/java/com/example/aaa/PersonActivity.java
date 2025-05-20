package com.example.aaa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

// Активность просмотра пользователя
public class PersonActivity extends AppCompatActivity {
    User user;
    ImageView imageView;
    TextView fioEdit, sumEdit, dateEdit, methodEdit, infoEdit;
    Button deleteBtn, backBtn;
    DBUsers dbConnector;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_person);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        user = (User) getIntent().getSerializableExtra("USER");
        imageView = findViewById(R.id.iconView);
        fioEdit = findViewById(R.id.fioEdit);
        sumEdit = findViewById(R.id.sumPersonEdit);
        dateEdit = findViewById(R.id.datePersonEdit);
        methodEdit = findViewById(R.id.methodPersonEdit);
        infoEdit = findViewById(R.id.infoPersonEdit);
        deleteBtn = findViewById(R.id.deletePersonButton);
        backBtn = findViewById(R.id.backPersonActivity);

        dbConnector = new DBUsers(this);
        mContext = this;

        if (user.getAnon() == 0)
            fioEdit.setText(String.format("%s %s %s", user.getSurname(), user.getName(), user.getOtch()));
        else fioEdit.setText("Аноним");
        sumEdit.setText(user.getSum() + " p.");
        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(user.getDate());
        dateEdit.setText(date);
        methodEdit.setText(user.getMeth() == 0 ? "Наличные" : "Перевод");
        infoEdit.setText(user.getInfo().isEmpty() ? "" : ("Контактная информация:\n" + user.getInfo()));
        int[] arr = {R.drawable.black_avatar, R.drawable.blue_avatar, R.drawable.green_avatar,
                R.drawable.gray, R.drawable.red_avatar, R.drawable.yellow_avatar};
        imageView.setImageResource(arr[new Random().nextInt(arr.length)]);

        backBtn.setOnClickListener(v -> finish());

        deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(mContext).setTitle("Подтверждение удаления").
                setMessage("Вы уверены, что хотите удалить данные пользователя?")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Удалить", (dialog, which) -> {
                    dbConnector.delete(user.getId());
                    Toast.makeText(mContext, "Успешно удалено", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setCancelable(false)
                .show());
    }
}