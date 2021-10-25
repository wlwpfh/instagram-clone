package com.godwpfh.instagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.godwpfh.instagram.R

class SettingFragment : Fragment() {
    var fragmentView: View?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView= LayoutInflater.from(activity).inflate(R.layout.fragment_setting,container, true);
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}