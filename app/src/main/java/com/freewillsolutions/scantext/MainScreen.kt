package com.freewillsolutions.scantext

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.freewillsolutions.scantext.databinding.ScreenMainBinding


class MainScreen : Fragment() {
    private var _binding: ScreenMainBinding? = null
    private val binding get() = _binding!!
    private var callback: Callback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ScreenMainBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lineOpenCamera.setOnClickListener {
            callback?.onClickCamera()
        }


    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(ac: Activity) {
        super.onAttach(ac)
        callback = ac as Callback
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }

    interface Callback {
        fun onClickCamera()
        fun onClickDone()
        fun onClickPreview(bitmap: ArrayList<Bitmap>)
    }
}