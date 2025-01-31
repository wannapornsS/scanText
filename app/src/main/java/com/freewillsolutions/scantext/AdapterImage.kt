package com.freewillsolutions.scantext

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.freewillsolutions.scantext.databinding.ItemImageBinding


class AdapterImage(private var callback :(Int) -> Unit) : RecyclerView.Adapter<AdapterImage.ViewHolder>()/*, View.OnTouchListener*/{
    class ViewHolder(v : View) : RecyclerView.ViewHolder(v)
    private var context: Context? = null
    private var list = arrayListOf<Bitmap>()
    private lateinit var binding : ItemImageBinding

    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f

    fun addData(bitmap : Bitmap){
        this.list.add(bitmap)
        notifyItemInserted(list.size-1)
    }

    fun setData(bitmap : ArrayList<Bitmap>){
        this.list = bitmap
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding.root)
    }

    override fun getItemViewType(position: Int): Int {
        return list.size
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        binding.imageScanner.setOnTouchListener(this)
        binding.imageScanner.setImageBitmap(list[position])

        binding.imageScanner.setOnLongClickListener{
            callback.invoke(position)
            true
        }

    }

   /* private fun spacing(event: MotionEvent): Float {
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
    }*/
}

