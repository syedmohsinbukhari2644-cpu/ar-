package com.shahg2644.BeautyCameraTester

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var processedPreview: ImageView
    private lateinit var renderer: BeautyRendererView
    private val executor = Executors.newSingleThreadExecutor()
    private var landmarker: FaceLandmarker? = null
    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) startCamera() else toast("Camera permission is required") }

    override fun onCreate(state: Bundle?) { super.onCreate(state); window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); createUi(); if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera() else requestCamera.launch(Manifest.permission.CAMERA) }
    private fun createUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(0xff090909.toInt()) }; val frame = FrameLayout(this); processedPreview = ImageView(this).apply { scaleType = ImageView.ScaleType.CENTER_CROP; setBackgroundColor(0xff090909.toInt()) }; renderer = BeautyRendererView(this); frame.addView(processedPreview, FrameLayout.LayoutParams(-1, -1)); root.addView(frame, LinearLayout.LayoutParams(-1, 0, 1f))
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 8, 20, 14); setBackgroundColor(0xee090909.toInt()) }; val metrics = TextView(this).apply { text = "LIVE  |  FACE --  |  FPS --  |  DETECTOR -- ms"; setTextColor(0xffdddddd.toInt()) }; panel.addView(metrics)
        slider(panel, "Beauty", 35) { renderer.beauty = it / 100f }; slider(panel, "Brightness", 5) { renderer.brightness = it / 100f }; slider(panel, "Warmth", 5) { renderer.warmth = it / 100f }; val buttons = LinearLayout(this).apply { gravity = Gravity.CENTER }; buttons.addView(Button(this).apply { text = "BEFORE"; setOnClickListener { renderer.filterEnabled = !renderer.filterEnabled; text = if (renderer.filterEnabled) "BEFORE" else "AFTER" } }, LinearLayout.LayoutParams(0, -2, 1f)); buttons.addView(Button(this).apply { text = "SAVE"; setOnClickListener { saveScreenshot() } }, LinearLayout.LayoutParams(0, -2, 1f)); panel.addView(buttons); root.addView(panel); setContentView(root); renderer.onMetrics = { face, fps, detector -> runOnUiThread { metrics.text = "LIVE  |  FACE $face  |  FPS $fps  |  DETECTOR $detector ms" } }
    }
    private fun slider(parent: LinearLayout, label: String, initial: Int, changed: (Int) -> Unit) { val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }; val value = TextView(this).apply { text = "$label  $initial"; setTextColor(0xffffffff.toInt()) }; val bar = SeekBar(this).apply { max = 100; progress = initial; setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener { override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) { value.text = "$label  $p"; changed(p) }; override fun onStartTrackingTouch(s: SeekBar) = Unit; override fun onStopTrackingTouch(s: SeekBar) = Unit }) }; row.addView(value, LinearLayout.LayoutParams(0, -2, .34f)); row.addView(bar, LinearLayout.LayoutParams(0, -2, .66f)); parent.addView(row) }
    private fun startCamera() { try { val options = FaceLandmarker.FaceLandmarkerOptions.builder().setBaseOptions(BaseOptions.builder().setModelAssetPath("face_landmarker.task").build()).setRunningMode(RunningMode.LIVE_STREAM).setNumFaces(1).setResultListener { result, _ -> renderer.landmarks = result.faceLandmarks().firstOrNull()?.map { floatArrayOf(it.x(), it.y()) } ?: emptyList() }.setErrorListener { toast("Face detector: ${it.message}") }.build(); landmarker = FaceLandmarker.createFromOptions(this, options) } catch (_: Exception) { toast("Model missing: face_landmarker.task") }; val provider = ProcessCameraProvider.getInstance(this); provider.addListener({ val cameraProvider = provider.get(); val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(executor) { image -> val bitmap = image.toBitmap(); landmarker?.detectAsync(BitmapImageBuilder(bitmap).build(), SystemClock.uptimeMillis()); val filtered = renderer.processFrame(bitmap); runOnUiThread { processedPreview.setImageBitmap(filtered) }; image.close() } }; cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, analysis) }, ContextCompat.getMainExecutor(this)) }
    private fun saveScreenshot() { val bitmap = Bitmap.createBitmap(window.decorView.width.coerceAtLeast(1), window.decorView.height.coerceAtLeast(1), Bitmap.Config.ARGB_8888); android.view.PixelCopy.request(window, bitmap, { code -> if (code == android.view.PixelCopy.SUCCESS) contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME, "beauty-${System.currentTimeMillis()}.jpg"); put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/BeautyCameraTester") })?.let { contentResolver.openOutputStream(it)?.use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out) } }; toast(if (code == android.view.PixelCopy.SUCCESS) "Saved to Pictures" else "Screenshot failed") }, Handler(mainLooper)) }
    private fun toast(message: String) = runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    override fun onDestroy() { landmarker?.close(); executor.shutdown(); super.onDestroy() }
}

private fun ImageProxy.toBitmap(): Bitmap { val y = planes[0].buffer; val u = planes[1].buffer; val v = planes[2].buffer; val ySize = y.remaining(); val vSize = v.remaining(); val uSize = u.remaining(); val nv21 = ByteArray(ySize + vSize + uSize); y.get(nv21, 0, ySize); v.get(nv21, ySize, vSize); u.get(nv21, ySize + vSize, uSize); val jpeg = java.io.ByteArrayOutputStream(); android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null).compressToJpeg(android.graphics.Rect(0, 0, width, height), 85, jpeg); val raw = android.graphics.BitmapFactory.decodeByteArray(jpeg.toByteArray(), 0, jpeg.size()); val matrix = Matrix().apply { postRotate(imageInfo.rotationDegrees.toFloat()); postScale(-1f, 1f) }; return Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true) }
