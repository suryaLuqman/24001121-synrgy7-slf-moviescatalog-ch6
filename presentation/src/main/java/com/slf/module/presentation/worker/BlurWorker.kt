package com.slf.module.presentation.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.slf.module.utils.imageprocessing.BlurUtils
import com.slf.module.utils.FileUtils

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(KEY_IMAGE_URI) ?: return Result.failure()
        return try {
            val resolver = appContext.contentResolver
            val bitmap = FileUtils.getBitmapFromUri(resolver, Uri.parse(resourceUri))
//            Log.d("BlurWorker", "Bitmap successfully loaded from URI")
            val blurredBitmap = BlurUtils.blurBitmap(appContext, bitmap, 25)
//            Log.d("BlurWorker", "Bitmap successfully blurred")
            val outputUri = FileUtils.writeBitmapToFile(appContext, blurredBitmap)
//            Log.d("BlurWorker", "Blurred Bitmap successfully written to file: $outputUri")
            Result.success(workDataOf(KEY_BLURRED_IMAGE_URI to outputUri.toString()))
        } catch (throwable: Throwable) {
            Log.e("BlurWorker", "Error applying blur", throwable)
            Result.failure()
        }
    }

    companion object {
        const val KEY_IMAGE_URI = "IMAGE_URI"
        const val KEY_BLURRED_IMAGE_URI = "BLURRED_IMAGE_URI"
    }
}
