package com.micrantha.skouter.platform.scan

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import com.micrantha.bluebell.data.Log
import com.micrantha.bluebell.platform.toImageBitmap
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRef
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.kCGBitmapByteOrder32Little
import platform.CoreVideo.CVImageBufferRef
import platform.CoreVideo.CVPixelBufferGetBaseAddress
import platform.CoreVideo.CVPixelBufferGetBaseAddressOfPlane
import platform.CoreVideo.CVPixelBufferGetBytesPerRow
import platform.CoreVideo.CVPixelBufferGetHeight
import platform.CoreVideo.CVPixelBufferGetPixelFormatType
import platform.CoreVideo.CVPixelBufferGetWidth
import platform.CoreVideo.CVPixelBufferLockBaseAddress
import platform.CoreVideo.CVPixelBufferRef
import platform.CoreVideo.CVPixelBufferUnlockBaseAddress
import platform.CoreVideo.kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange
import platform.ImageIO.CGImagePropertyOrientation

@OptIn(ExperimentalForeignApi::class)
actual data class CameraImage constructor(
    internal val data: CVImageBufferRef,
    val orientation: CGImagePropertyOrientation
) {
    actual val width by lazy { CVPixelBufferGetWidth(data).toInt() }
    actual val height by lazy { CVPixelBufferGetHeight(data).toInt() }

    private var bytes: ByteArray? = null

    actual fun toByteArray(): ByteArray {
        if (bytes == null) {
            try {
                CVPixelBufferLockBaseAddress(data, 0u)

                bytes = when (CVPixelBufferGetPixelFormatType(data)) {
                    kCVPixelFormatType_420YpCbCr8BiPlanarVideoRange -> yuvToBitmap(data)
                    else -> throw IllegalStateException("invalid pixel format")
                }
            } catch (err: Throwable) {
                Log.e("converting camera image", err)
                throw err
            } finally {
                CVPixelBufferUnlockBaseAddress(data, 0u)
            }
        }
        return bytes!!
    }

    fun asCGImage(): CGImageRef? {
        var colorSpace: CGColorSpaceRef? = null
        var context: CGContextRef? = null
        return try {
            CVPixelBufferLockBaseAddress(data, 0u)

            val bytesPerRow = CVPixelBufferGetBytesPerRow(data)
            val baseAddress = CVPixelBufferGetBaseAddress(data)

            colorSpace = CGColorSpaceCreateDeviceRGB()
            context = CGBitmapContextCreate(
                baseAddress,
                width.toULong(),
                height.toULong(),
                8u,
                bytesPerRow,
                colorSpace,
                kCGBitmapByteOrder32Little or CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value
            )
            CGBitmapContextCreateImage(context)

        } finally {
            CGColorSpaceRelease(colorSpace)
            CGContextRelease(context)
            CVPixelBufferUnlockBaseAddress(data, 0u)
        }
    }

    private fun yuvToBitmap(yuvBuffer: CVPixelBufferRef): ByteArray {
        // Get Y and UV planes
        val yPlane = CVPixelBufferGetBaseAddressOfPlane(yuvBuffer, 0u)!!.reinterpret<ByteVar>()
        val uvPlane = CVPixelBufferGetBaseAddressOfPlane(yuvBuffer, 1u)!!.reinterpret<ByteVar>()

        // Get Y, U, and V plane sizes
        val yPlaneBytesPerRow = width
        val yPlaneHeight = height
        val uvPlaneBytesPerRow = (CVPixelBufferGetWidth(yuvBuffer) / 2u).toInt()
        //val uvPlaneHeight = (CVPixelBufferGetHeight(yuvBuffer) / 2u).toInt()

        // Calculate the size of the resulting BGRA buffer
        val bgraBufferSize = yPlaneBytesPerRow * yPlaneHeight * 4

        // Allocate memory for the BGRA buffer
        val bgraBuffer = nativeHeap.allocArray<ByteVar>(bgraBufferSize)

        // Convert YUV420v to BGRA
        var bgraIndex = 0
        for (yRow in 0 until yPlaneHeight) {
            val yStart = yRow * yPlaneBytesPerRow
            val uvStart = (yRow / 2) * uvPlaneBytesPerRow

            for (x in 0 until yPlaneBytesPerRow) {
                val y = yPlane[yStart + x]
                val uvOffset = (x / 2) * 2
                val u = uvPlane[uvStart + uvOffset]
                val v = uvPlane[uvStart + uvOffset + 1]

                val bgra = yuvToBgra(y.toUByte(), u.toUByte(), v.toUByte())

                bgraBuffer[bgraIndex++] = (bgra and 0xFFu).toByte()
                bgraBuffer[bgraIndex++] = ((bgra shr 8) and 0xFFu).toByte()
                bgraBuffer[bgraIndex++] = ((bgra shr 16) and 0xFFu).toByte()
                bgraBuffer[bgraIndex++] = 0xFFu.toByte() // Set alpha channel to fully opaque
            }
        }

        return bgraBuffer.readBytes(bgraBufferSize)
    }

    private fun yuvToBgra(y: UByte, u: UByte, v: UByte): UInt {
        val c = y - 16u
        val d = u - 128u
        val e = v - 128u

        val r = (298u * c + 409u * e + 128u) shr 8
        val g = (298u * c - 100u * d - 208u * e + 128u) shr 8
        val b = (298u * c + 516u * d + 128u) shr 8

        return (b and 0xFFu) or ((g and 0xFFu) shl 8) or ((r and 0xFFu) shl 16)
    }

    actual fun toImageBitmap(): ImageBitmap = toByteArray().toImageBitmap()
}
