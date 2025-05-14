package com.example.aaa;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class Export {
    public static boolean downloadDb(Context context){

        try {
            File dbFile = context.getDatabasePath("database.db");
            File exportDir = new File(context.getExternalFilesDir(null), "ExportsDB");

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, "backup_" + System.currentTimeMillis() + ".db");
            Files.copy(dbFile.toPath(), file.toPath());

            return true;

        } catch (Exception e) {
            Log.e("EEE", Objects.requireNonNull(e.getMessage()));
            return false;
        }
    }

    public static File saveFile(Context context){
        try {
            File dbFile = context.getDatabasePath("database.db");
            File exportDir = new File(context.getExternalFilesDir(null), "ExportsDB");

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            File file = new File(exportDir, "backup_" + System.currentTimeMillis() + ".db");
            Files.copy(dbFile.toPath(), file.toPath());

        } catch (Exception e) {
            Log.e("EEE", Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    public static void share(File file, Context context, Activity activity){
        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider", // должен совпадать с authorities в FileProvider
                file
        );

        ShareCompat.IntentBuilder.from(activity)
                .setType(getMimeType(file))
                .setStream(fileUri)
                .setChooserTitle("Поделиться файлом")
                .startChooser();

    }
    private static String getMimeType(File file) {
        String fileName = file.getName();
        if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".db")) {
            return "application/x-sqlite3";
        } else {
            return "*/*";
        }
    }
}
