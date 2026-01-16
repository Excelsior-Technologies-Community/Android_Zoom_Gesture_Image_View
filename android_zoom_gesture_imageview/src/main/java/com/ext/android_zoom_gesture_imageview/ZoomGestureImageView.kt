package com.ext.android_zoom_gesture_imageview



import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ZoomGestureImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    // Touch states
    private enum class Mode {
        NONE, DRAG, ZOOM
    }
    private var mode = Mode.NONE

    // Zoom parameters
    private var minScale = 1f
    private var maxScale = 5f
    private var currentScale = 1f

    // Translation limits
    private var viewWidth = 0
    private var viewHeight = 0
    private var intrinsicImageWidth = 0f
    private var intrinsicImageHeight = 0f

    // Touch points
    private val startPoint = PointF()
    private val midPoint = PointF()
    private var oldDist = 1f

    // Gesture detectors
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // Custom attributes
    private var enableZoom = true
    private var enablePan = true
    private var doubleTapToZoom = true

    init {
        scaleType = ScaleType.MATRIX

        // Load custom attributes
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ZoomGestureImageView,
            0, 0
        ).apply {
            try {
                minScale = getFloat(R.styleable.ZoomGestureImageView_minZoom, 1f)
                maxScale = getFloat(R.styleable.ZoomGestureImageView_maxZoom, 5f)
                enableZoom = getBoolean(R.styleable.ZoomGestureImageView_enableZoom, true)
                enablePan = getBoolean(R.styleable.ZoomGestureImageView_enablePan, true)
                doubleTapToZoom = getBoolean(R.styleable.ZoomGestureImageView_doubleTapToZoom, true)
            } finally {
                recycle()
            }
        }

        // Initialize scale gesture detector
        scaleDetector = ScaleGestureDetector(context, ScaleListener())

        // Initialize gesture detector for double tap
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (doubleTapToZoom) {
                    if (currentScale > minScale) {
                        // Zoom out to min scale
                        resetZoom()
                    } else {
                        // Zoom in to 2x or max scale
                        val targetScale = min(maxScale, 2.5f)
                        zoomToPoint(targetScale / currentScale, e.x, e.y)
                    }
                    return true
                }
                return false
            }
        })
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        drawable?.let {
            intrinsicImageWidth = it.intrinsicWidth.toFloat()
            intrinsicImageHeight = it.intrinsicHeight.toFloat()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        if (currentScale == 1f) {
            fitImageToView()
        }
    }

    private fun fitImageToView() {
        if (intrinsicImageWidth == 0f || intrinsicImageHeight == 0f) return

        val scaleX = viewWidth.toFloat() / intrinsicImageWidth
        val scaleY = viewHeight.toFloat() / intrinsicImageHeight
        val scale = min(scaleX, scaleY)

        matrix.setScale(scale, scale)

        // Center the image
        val redundantXSpace = viewWidth - (scale * intrinsicImageWidth)
        val redundantYSpace = viewHeight - (scale * intrinsicImageHeight)
        matrix.postTranslate(redundantXSpace / 2f, redundantYSpace / 2f)

        imageMatrix = matrix
        currentScale = scale
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!enableZoom && !enablePan) return super.onTouchEvent(event)

        // Handle scale gestures
        if (enableZoom) {
            scaleDetector.onTouchEvent(event)
        }

        // Handle double tap
        gestureDetector.onTouchEvent(event)

        val currentPoint = PointF(event.x, event.y)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                startPoint.set(event.x, event.y)
                mode = Mode.DRAG
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(midPoint, event)
                    mode = Mode.ZOOM
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = Mode.NONE
            }

            MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.DRAG && enablePan) {
                    matrix.set(savedMatrix)
                    val dx = currentPoint.x - startPoint.x
                    val dy = currentPoint.y - startPoint.y
                    matrix.postTranslate(dx, dy)
                    fixTranslation()
                } else if (mode == Mode.ZOOM && enableZoom) {
                    val newDist = spacing(event)
                    if (newDist > 10f) {
                        matrix.set(savedMatrix)
                        val scale = newDist / oldDist
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y)
                        fixScaleAndTranslation()
                    }
                }
            }
        }

        imageMatrix = matrix
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = currentScale * scaleFactor

            if (newScale in minScale..maxScale) {
                matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                fixScaleAndTranslation()
                imageMatrix = matrix
            }
            return true
        }
    }

    private fun fixScaleAndTranslation() {
        val values = FloatArray(9)
        matrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        currentScale = scaleX

        // Constrain scale
        if (currentScale < minScale) {
            val scale = minScale / currentScale
            matrix.postScale(scale, scale, viewWidth / 2f, viewHeight / 2f)
            currentScale = minScale
        } else if (currentScale > maxScale) {
            val scale = maxScale / currentScale
            matrix.postScale(scale, scale, viewWidth / 2f, viewHeight / 2f)
            currentScale = maxScale
        }

        fixTranslation()
    }

    private fun fixTranslation() {
        val values = FloatArray(9)
        matrix.getValues(values)

        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val scaledImageWidth = intrinsicImageWidth * currentScale
        val scaledImageHeight = intrinsicImageHeight * currentScale

        var fixTransX = 0f
        var fixTransY = 0f

        // Fix horizontal translation
        if (scaledImageWidth <= viewWidth) {
            // Center if image is smaller than view
            fixTransX = (viewWidth - scaledImageWidth) / 2f - transX
        } else {
            // Constrain within bounds if image is larger
            if (transX > 0) {
                fixTransX = -transX
            } else if (transX + scaledImageWidth < viewWidth) {
                fixTransX = viewWidth - scaledImageWidth - transX
            }
        }

        // Fix vertical translation
        if (scaledImageHeight <= viewHeight) {
            // Center if image is smaller than view
            fixTransY = (viewHeight - scaledImageHeight) / 2f - transY
        } else {
            // Constrain within bounds if image is larger
            if (transY > 0) {
                fixTransY = -transY
            } else if (transY + scaledImageHeight < viewHeight) {
                fixTransY = viewHeight - scaledImageHeight - transY
            }
        }

        matrix.postTranslate(fixTransX, fixTransY)
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2f, y / 2f)
    }

    private fun zoomToPoint(scale: Float, focusX: Float, focusY: Float) {
        matrix.postScale(scale, scale, focusX, focusY)
        fixScaleAndTranslation()
        imageMatrix = matrix
    }

    /**
     * Reset zoom to original size
     */
    fun resetZoom() {
        fitImageToView()
    }

    /**
     * Get current zoom scale
     */
    fun getCurrentScale(): Float = currentScale

    /**
     * Set zoom programmatically
     */
    fun setZoom(scale: Float, focusX: Float = viewWidth / 2f, focusY: Float = viewHeight / 2f) {
        val targetScale = max(minScale, min(maxScale, scale))
        val scaleChange = targetScale / currentScale
        zoomToPoint(scaleChange, focusX, focusY)
    }
}