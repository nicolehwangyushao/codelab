package com.example.background.workers

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.background.KEY_IMAGE_URI
import com.google.common.util.concurrent.ListenableFuture

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
//        makeStatusNotification("Blurring image", appContext)
        sleep()
        return try {
            setForeground(getForegroundInfo())
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )
            val output = blurBitmap(picture, appContext)
            //Write bitmap to a temp file
            val outputUri = writeBitmapToFile(appContext, output)

            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

            Result.success(outputData)

        } catch (e: ForegroundServiceStartNotAllowedException) {
            Log.e(TAG, "Error applying blur")
            Result.failure()
        }

    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return makeStatusNotification("Blurring image", applicationContext)
    }
}