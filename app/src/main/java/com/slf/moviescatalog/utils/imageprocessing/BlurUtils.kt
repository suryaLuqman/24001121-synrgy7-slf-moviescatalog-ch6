package com.slf.moviescatalog.utils.imageprocessing

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

object BlurUtils {
    fun blurBitmap(context: Context, bitmap: Bitmap, blurRadius: Int): Bitmap {
        val outputBitmap = Bitmap.createBitmap(bitmap)
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createFromBitmap(rs, outputBitmap)
        val script = ScriptIntrinsicBlur.create(rs, input.element)
        script.setRadius(blurRadius.toFloat())
        script.setInput(input)
        script.forEach(output)
        output.copyTo(outputBitmap)
        return outputBitmap
    }
}