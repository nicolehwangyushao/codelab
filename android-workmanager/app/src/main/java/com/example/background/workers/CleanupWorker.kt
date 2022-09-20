package com.example.background.workers

import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.SyncStateContract
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.OUTPUT_PATH
import com.google.common.util.concurrent.ListenableFuture
import java.io.File

private const val TAG = "CleanupWorker"

class CleanupWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun doWork(): Result {
//        makeStatusNotification("Cleaning up old temporary files", applicationContext)
        sleep()
        return try {
            setForeground(getForegroundInfo())
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = entry.delete()
                            Log.i(TAG, "Deleted $name - $deleted")
                        }
                    }
                }
            }
            Result.success()
        } catch (e: ForegroundServiceStartNotAllowedException) {
            Log.e(TAG, "Error applying blur")
            Result.failure()
        }
    }
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return makeStatusNotification("Cleaning up old temporary files", applicationContext)
    }
}