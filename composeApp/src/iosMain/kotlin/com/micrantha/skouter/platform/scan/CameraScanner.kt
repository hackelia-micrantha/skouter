package com.micrantha.skouter.platform.scan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toSkiaRect
import androidx.compose.ui.interop.UIKitView
import co.touchlab.stately.freeze
import com.micrantha.skouter.platform.asException
import com.micrantha.skouter.platform.scan.components.CameraScannerDispatch
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.CoreGraphics.CGRect
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.CoreVideo.kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange
import platform.Foundation.NSError
import platform.ImageIO.kCGImagePropertyOrientationDown
import platform.ImageIO.kCGImagePropertyOrientationLeft
import platform.ImageIO.kCGImagePropertyOrientationRight
import platform.ImageIO.kCGImagePropertyOrientationUp
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import org.jetbrains.skia.Rect as RectF

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraScanner(
    modifier: Modifier,
    regionOfInterest: Rect?,
    onCameraImage: CameraScannerDispatch
) {
    val stream = rememberCameraStream(regionOfInterest?.toSkiaRect(), onCameraImage) ?: return

    DisposableEffect(stream) {

        stream.setup()
        stream.start()

        onDispose {
            stream.stop()
        }
    }

    UIKitView(
        factory = stream::preview,
        onResize = stream::resize,
        modifier = modifier
    )
}


@Composable
private fun rememberCameraStream(
    regionOfInterest: RectF? = null,
    onCameraImage: CameraScannerDispatch
): CameraStream? = remember {
    AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)?.let {
        CameraStream(it, regionOfInterest, onCameraImage)
    }
}

@OptIn(ExperimentalForeignApi::class)
class CameraStream(
    private val device: AVCaptureDevice,
    private val regionOfInterest: RectF? = null,
    private val onCameraImage: CameraScannerDispatch
) : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

    private val session by lazy { AVCaptureSession() }
    private val scope by lazy { CoroutineScope(Dispatchers.Default) + Job() }
    private var cameraPreviewLayer: AVCaptureVideoPreviewLayer? = null
    private val dispatchQueue by lazy { dispatch_queue_create("videoDataOutputQueue", null) }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        didOutputSampleBuffer.image(fromConnection.videoOrientation)?.let {
            dispatch_async(dispatch_get_main_queue()) {
                scope.launch {
                    onCameraImage(it)
                }
            }
        }
    }


    private fun CMSampleBufferRef?.image(orientation: AVCaptureVideoOrientation): CameraImage? {
        val pixelBuffer = CMSampleBufferGetImageBuffer(this) ?: return null
        pixelBuffer.freeze()
        return CameraImage(
            pixelBuffer, when (orientation) {
                AVCaptureVideoOrientationPortraitUpsideDown -> kCGImagePropertyOrientationDown
                AVCaptureVideoOrientationLandscapeLeft -> kCGImagePropertyOrientationLeft
                AVCaptureVideoOrientationLandscapeRight -> kCGImagePropertyOrientationRight
                else -> kCGImagePropertyOrientationUp
            }
        )
    }

    fun resize(view: UIView, rect: CValue<CGRect>) {
        try {
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            view.layer.setFrame(rect)
            cameraPreviewLayer?.setFrame(rect)
        } finally {
            CATransaction.commit()
        }
    }

    fun setup() {
        val err = memScoped {
            alloc<ObjCObjectVar<NSError?>>()
        }

        val input = AVCaptureDeviceInput(device, err.ptr)
        err.value?.let { throw it.asException() }

        if (session.canAddInput(input).not())
            throw IllegalStateException("cannot add input to video session")

        session.addInput(input)

        val output = AVCaptureVideoDataOutput().apply {
            setSampleBufferDelegate(this@CameraStream, dispatchQueue)
            if (availableVideoCVPixelFormatTypes.contains(
                    kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange
                )
            ) {
                setVideoSettings(
                    mapOf(
                        kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange
                    )
                )
            }
        }

        if (session.canAddOutput(output)) {
            session.addOutput(output)
        }
    }

    fun start() {
        dispatch_async(dispatchQueue) {
            session.startRunning()
        }
    }

    fun preview(): UIView {
        val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
            connection?.videoOrientation = AVCaptureVideoOrientationPortrait
        }
        cameraPreviewLayer = previewLayer
        val view = UIView()
        view.layer.addSublayer(previewLayer)
        return view
    }

    fun stop() {
        session.stopRunning()
    }
}
