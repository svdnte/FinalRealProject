package com.example.aaa;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class DatabaseAutoBackupManager {
    private static final String TAG = "DbBackupManager";
    private static final String BACKUP_DIR_NAME = "backups";
    private static final int MAX_BACKUPS = 3; // Храним 3 последних бэкапа

    private final Context context;
    private final String databaseName;

    public DatabaseAutoBackupManager(Context context, String databaseName) {
        this.context = context.getApplicationContext();
        this.databaseName = databaseName;
    }

    public void executeAutoBackup() {
        try {
            // 1. Получаем исходный файл БД
            File sourceDb = context.getDatabasePath(databaseName);
            if (!sourceDb.exists()) {
                Log.e(TAG, "Source DB file not found: " + sourceDb.getPath());
                return;
            }

            // 2. Создаем целевую директорию
            File backupDir = createBackupDirectory();
            if (backupDir == null) {
                Log.e(TAG, "Failed to create backup directory");
                return;
            }

            // 3. Генерируем имя файла с датой
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String backupFileName = databaseName.replace(".db", "") + "_" + timestamp + ".db";

            // 4. Копируем файл
            File backupFile = new File(backupDir, backupFileName);
            copyDatabaseFile(sourceDb, backupFile);
            Log.i(TAG, "Backup created: " + backupFile.getAbsolutePath());

            // 5. Очищаем старые бэкапы
            cleanupOldBackups(backupDir);

        } catch (Exception e) {
            Log.e(TAG, "Auto backup failed", e);
        }
    }

    private File createBackupDirectory() {
        File backupDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        // Создаем директорию если не существует
        if (!backupDir.exists()) {
            if (!backupDir.mkdirs()) {
                return null;
            }
        }
        return backupDir;
    }

    private void copyDatabaseFile(File source, File dest) throws IOException {
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void cleanupOldBackups(File backupDir) {
        File[] backups = backupDir.listFiles((dir, name) ->
                name.startsWith(databaseName.replace(".db", "")) && name.endsWith(".db"));

        if (backups != null && backups.length > MAX_BACKUPS) {
            // Сортируем по дате изменения (старые сначала)
            Arrays.sort(backups, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

            // Удаляем лишние бэкапы
            for (int i = 0; i < backups.length - MAX_BACKUPS; i++) {
                if (!backups[i].delete()) {
                    Log.w(TAG, "Failed to delete old backup: " + backups[i].getName());
                }
            }
        }
    }
}