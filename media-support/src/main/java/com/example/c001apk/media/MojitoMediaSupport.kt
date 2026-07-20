package com.example.c001apk.media

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.DrawableRes
import net.mikaelzero.mojito.interfaces.ActivityCoverLoader
import net.mikaelzero.mojito.interfaces.IMojitoActivity
import net.mikaelzero.mojito.interfaces.ImageViewLoadFactory
import net.mikaelzero.mojito.loader.ContentLoader
import net.mikaelzero.mojito.view.sketch.core.SketchImageView
import java.io.File

fun String.mayContainCoolApkRichMedia(): Boolean =
    contains("-uhdr", ignoreCase = true) ||
        contains("-xhdr", ignoreCase = true) ||
        contains("-livepic", ignoreCase = true)

class MojitoMediaImageFactory(
    private val delegate: ImageViewLoadFactory,
    private val imageUrl: String,
    private val videoUrlResolver: LivePhotoVideoUrlResolver? = null,
    private val expectMotionPhoto: Boolean = false,
    private val playbackSession: MojitoMediaPlaybackSession = MojitoMediaPlaybackSession(),
    private val pagePosition: Int = 0,
) : ImageViewLoadFactory {

    private var contentLoader: MediaContentLoader? = null
    private var lastBoundFileKey: String? = null
    private val parsedMedia = mutableMapOf<String, EmbeddedMediaInfo>()

    override fun loadSillContent(view: View, uri: Uri) {
        val file = uri.path?.let(::File)
        val fileKey = file?.let { "${it.absolutePath}:${it.length()}" }
        val wasCached = fileKey != null && parsedMedia.containsKey(fileKey)
        val mediaInfo = if (file != null && fileKey != null) {
            parsedMedia.getOrPut(fileKey) {
                MotionPhotoParser.parse(
                    file = file,
                    expectMotionPhoto = expectMotionPhoto,
                    expectUltraHdr = imageUrl.contains("-uhdr", ignoreCase = true) ||
                        imageUrl.contains("-xhdr", ignoreCase = true),
                )
            }
        } else {
            null
        }

        if (BuildConfig.DEBUG && file != null && mediaInfo != null && !wasCached) {
            Log.d(
                TAG,
                "parsed file=${file.name} bytes=${file.length()} hdr=${mediaInfo.hasUltraHdr} " +
                    "motion=${mediaInfo.hasMotionPhoto} declared=${mediaInfo.declaredVideoLength} " +
                    "embedded=${mediaInfo.embeddedVideoLength}",
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            mediaInfo?.hasUltraHdr == true && view is SketchImageView
        ) {
            view.options
                .setBitmapConfig(Bitmap.Config.ARGB_8888)
                .setBitmapPoolDisabled(true)
                .setCacheProcessedImageInDisk(false)
                .setCorrectImageOrientationDisabled(true)
        }

        delegate.loadSillContent(view, uri)
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            mediaInfo?.hasUltraHdr == true && view is SketchImageView
        ) {
            view.postDelayed({
                val bitmap = findBitmap(view.drawable)
                Log.d(
                    TAG,
                    "decoded file=${file?.name} config=${bitmap?.config} gainmap=${bitmap?.hasGainmap()}",
                )
            }, 1200L)
        }
        if (file != null && mediaInfo != null &&
            (mediaInfo.hasMotionPhoto || mediaInfo.hasUltraHdr) && fileKey != lastBoundFileKey
        ) {
            lastBoundFileKey = fileKey
            contentLoader?.bind(file, mediaInfo)
        }
    }

    override fun loadContentFail(view: View, @DrawableRes drawableResId: Int) {
        delegate.loadContentFail(view, drawableResId)
    }

    override fun newContentLoader(): ContentLoader {
        contentLoader?.dispose()
        lastBoundFileKey = null
        return MediaContentLoader(
            delegate.newContentLoader(),
            playbackSession,
            imageUrl,
            videoUrlResolver,
            pagePosition,
        ).also {
            contentLoader = it
        }
    }

    private fun findBitmap(drawable: Drawable?): Bitmap? = when (drawable) {
        is BitmapDrawable -> drawable.bitmap
        is TransitionDrawable -> (drawable.numberOfLayers - 1 downTo 0)
            .firstNotNullOfOrNull { findBitmap(drawable.getDrawable(it)) }
        else -> null
    }

    private companion object {
        const val TAG = "MojitoMedia"
    }
}

class HdrMojitoActivityCoverLoader(
    private val playbackSession: MojitoMediaPlaybackSession,
) : ActivityCoverLoader {

    private var coverView: View? = null

    override fun attach(context: IMojitoActivity) {
        val activity = context.getContext() as? Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity?.window?.colorMode = ActivityInfo.COLOR_MODE_HDR
        }
        coverView = View(context.getContext()).apply {
            isClickable = false
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun providerView(): View = checkNotNull(coverView)

    override fun move(moveX: Float, moveY: Float) = Unit

    override fun fingerRelease(isToMax: Boolean, isToMin: Boolean) = Unit

    override fun pageChange(totalSize: Int, position: Int) {
        playbackSession.selectPage(position)
    }
}
