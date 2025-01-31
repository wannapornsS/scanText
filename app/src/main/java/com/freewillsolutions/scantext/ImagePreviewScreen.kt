package com.freewillsolutions.scantext

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.freewillsolutions.scantext.MainScreen.Callback
import com.freewillsolutions.scantext.databinding.ScreenImagePreviewBinding


class ImagePreviewScreen : Fragment(), View.OnTouchListener {
    private var _binding: ScreenImagePreviewBinding? = null
    private val binding get() = _binding!!

    private var arrayBitmap = arrayListOf<Bitmap>()
    private var bitmap : Bitmap? = null
    private var adapter : AdapterImageZoomInOut? = null

    private var callback: Callback? = null

    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f

    companion object {
        @JvmStatic
        fun newInstance(data: ArrayList<Bitmap>) = ImagePreviewScreen().apply {
            this.arrayBitmap = data
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ScreenImagePreviewBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setImageAdapter()

//        binding.imagePreview.setImageBitmap(bitmap)
//        binding.imagePreview.setOnTouchListener(this)

    }


    private fun setImageAdapter(){

        val gridLayoutManager = GridLayoutManager(requireContext(), 1, LinearLayoutManager.HORIZONTAL, false)

        val linearSnapHelper: LinearSnapHelper = SnapHelperOneByOne()
        linearSnapHelper.attachToRecyclerView(binding.recyclerViewImagePreview)

        adapter = AdapterImageZoomInOut{}
        binding.recyclerViewImagePreview.layoutManager = gridLayoutManager
        binding.recyclerViewImagePreview.adapter = adapter
        adapter?.setData(arrayBitmap)


    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onAttach(ac: Activity) {
        super.onAttach(ac)
        callback = ac as Callback
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        view.scaleType = ImageView.ScaleType.MATRIX
        val scale: Float

        dumpEvent(event)

        Log.d("event.action", "${event}")

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                matrix.set(view.imageMatrix)
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 5f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    matrix.postTranslate(event.x - start.x, event.y - start.y)
                } else if (mode == ZOOM) {
                    val newDist = spacing(event)
                    if (newDist > 5f) {
                        matrix.set(savedMatrix)
                        scale = newDist / oldDist
                        matrix.postScale(scale, scale, mid.x, mid.y)
                    }
                }
            }
        }

        view.imageMatrix = matrix
        return true
    }


    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    private fun dumpEvent(event: MotionEvent) {
        val names = arrayOf("DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?")
        val sb = StringBuilder()
        val action = event.action
        val actionCode = action and MotionEvent.ACTION_MASK
        sb.append("event ACTION_").append(names[actionCode])

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action shr MotionEvent.ACTION_POINTER_ID_SHIFT)
            sb.append(")")
        }

        sb.append("[")
        for (i in 0 until event.pointerCount) {
            sb.append("#").append(i)
            sb.append("(pid ").append(event.getPointerId(i))
            sb.append(")=").append(event.getX(i).toInt())
            sb.append(",").append(event.getY(i).toInt())
            if (i + 1 < event.pointerCount) sb.append(";")
        }

        sb.append("]")
        Log.d("Touch Events ---------", sb.toString())
    }



}