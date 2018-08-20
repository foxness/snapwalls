package me.nocturnl.inksnap

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class AboutFragment : Fragment()
{
    // todo: fix all instances of SuppressLint
    @SuppressLint("SetTextI18n") // todo: fix
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View
    {
        val v = inflater.inflate(R.layout.fragment_about, container, false)
        
        val appIconView = v.findViewById<ImageView>(R.id.app_icon)
        
        val settingsManager = SettingsManager.getInstance(context!!)
        
        var timesTapped = 0
        
        appIconView.setOnClickListener {
            when (++timesTapped)
            {
                1 -> settingsManager.developerOptionsUnlocked = false
                DEVELOPER_TAP_COUNT -> settingsManager.developerOptionsUnlocked = true
                DEVELOPER_TAP_COUNT + 1 -> settingsManager.developerOptionsUnlocked = false
            }
        }
        
        val appVersionView = v.findViewById<TextView>(R.id.app_version_view)
        appVersionView.text = "version ${BuildConfig.VERSION_NAME}"
        
        return v
    }
    
    companion object
    {
        fun newInstance() = AboutFragment()
        
        private const val DEVELOPER_TAP_COUNT = 23
    }
}