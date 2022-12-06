package com.example.turantpe

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.fragment_scanner.*
import java.io.IOException

/**
 * A simple [Fragment] subclass.
 * Use the [ScannerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScannerFragment : Fragment() {

    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private val REQUEST_CAMERA_PERMISSION = 201
    private val qrCodeValue: MutableLiveData<String> = MutableLiveData<String>()

    var intentData = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.example.turantpe.R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        qrCodeValue.setValue(intentData);
        qrCodeValue.observe(viewLifecycleOwner, Observer {
            //Do something with the changed value -> it
//            if(it != "" || it != null){
//                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
//                startActivity(browserIntent)
//            }
            if (it != intentData) {
                Log.d("TAG", it)

            }
        })
        initialiseDetectorsAndSources()

    }

    private fun initViews() {
//        btnAction!!.setOnClickListener {
//            if (intentData.length > 0) {
//
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(intentData)))
//            }
//        }
    }

    //
    private fun initialiseDetectorsAndSources() {
        Toast.makeText(
            context, "Barcode scanner started",
            Toast.LENGTH_SHORT
        )
        barcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(context, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()
        surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            context!!,
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
                        context,
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
//        initialiseDetectorsAndSources()
    }

}