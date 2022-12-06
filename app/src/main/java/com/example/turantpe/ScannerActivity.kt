package com.example.turantpe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_scanner.*
import java.io.IOException


class ScannerActivity : AppCompatActivity() {
    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private val REQUEST_CAMERA_PERMISSION = 201
    var btnAction: Button? = null
    var intentData = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        initialiseDetectorsAndSources()
    }

    private fun initialiseDetectorsAndSources() {
        Toast.makeText(
            this, "Barcode scanner started",
            Toast.LENGTH_SHORT
        )
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()
        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val camSource = cameraSource

                        if (camSource != null) {
                            camSource.start(surfaceView!!.holder)
                        }

                    } else {
                        ActivityCompat.requestPermissions(
                            MainActivity(),
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                        )
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                val camSource = cameraSource
                if (camSource != null) {
                    camSource.stop()
                }
            }
        })
        val barSource = barcodeDetector
        if (barSource != null) {
            barSource.setProcessor(object : Detector.Processor<Barcode?> {
                override fun release() {
                    Toast.makeText(
                        applicationContext,
                        "To prevent memory leaks barcode scanner has been stopped",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun receiveDetections(detections: Detector.Detections<Barcode?>) {
                    val barcodes: SparseArray<Barcode?>? = detections.getDetectedItems()
                    if (barcodes?.size() != 0) {
                        txtBarcodeValue!!.post {
                            if (barcodes?.valueAt(0)?.email != null) {
                                txtBarcodeValue!!.removeCallbacks(null)
                                intentData = barcodes?.valueAt(0)?.email?.address.toString()
                                txtBarcodeValue!!.text = intentData
//                                btnAction!!.text = "ADD CONTENT TO THE MAIL"
                            } else {
//                                btnAction!!.text = "LAUNCH URL"

                                val qrValue = barcodes?.valueAt(0)?.displayValue.toString()
                                if (intentData != qrValue) {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(qrValue))
                                    startActivity(browserIntent)
                                    intentData = qrValue
                                    txtBarcodeValue!!.text = intentData
                                }
                            }
                        }
                    }
                }
            })
        }

    }

    override fun onPause() {
        super.onPause()
        cameraSource!!.release()
    }

    override fun onResume() {
        super.onResume()
        initialiseDetectorsAndSources()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, MainActivity::class.java)
        // start your next activity
        startActivity(intent)
    }
}