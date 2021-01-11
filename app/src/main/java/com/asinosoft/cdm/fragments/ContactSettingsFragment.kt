package com.asinosoft.cdm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.asinosoft.cdm.LockableScrollView
import com.asinosoft.cdm.R
import kotlinx.android.synthetic.main.settings_layout.*

interface ScrollViewListener{
    fun onScrolledToTop()
}

class ContactSettingsFragment : Fragment(){
    lateinit var scrollView: LockableScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollView = view.findViewById(R.id.scrollView)
        scrollView.setScrollingEnabled(false)

        scrollView.setOnScrollChangeListener{
            view, scrollX,  scrollY, oldScrollX,  oldScrollY ->
            if(oldScrollY > 0 && scrollY == 0){
                (activity as ScrollViewListener).onScrolledToTop()
                //scrollView.setScrollingEnabled(false)
            }
        }
    }

    fun enableScroll(enable : Boolean){
        scrollView?.let {
            it.setScrollingEnabled(enable)
        }
    }
}