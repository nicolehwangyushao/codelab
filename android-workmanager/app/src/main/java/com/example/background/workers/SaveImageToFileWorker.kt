package com.example.background.workers

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Images.Media.insertImage
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.background.KEY_IMAGE_URI
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "SaveImageToFileWorker"

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    private val title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())
    @RequiresApi(Build.VERSION_CODES.S)
    override suspend fun doWork(): Result {
//        makeStatusNotification("Saving image", applicationContext)
        setForeground(getForegroundInfo())
        sleep()

        val resolver = applicationContext.contentResolver
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap =
                BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val imageUrl = insertImage(
                resolver, bitmap, title, dateFormatter.format(
                    Date()
                )
            )
            if (!imageUrl.isNullOrEmpty()) {
                val output = workDataOf(KEY_IMAGE_URI to imageUrl)
                Result.success(output)
            } else {
                Log.e(TAG, "Writing to MediaStore failed")
                Result.failure()
            }
        } catch (e: ForegroundServiceStartNotAllowedException) {
            Log.e(TAG, "Error applying blur")
            Result.failure()
        }
    }
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return makeStatusNotification("Saving image", applicationContext)
    }

    fun canAddFish(tankSize: Double, currentFish: List<Int>, fishSize: Int = 2, hasDecorations: Boolean = true) : Boolean {
        var capacity = tankSize
        if (hasDecorations) {
            capacity = capacity * 80/100
        }

        return currentFish.sum()+fishSize <= capacity
    }

}