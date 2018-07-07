package space.foxness.snapwalls

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import space.foxness.snapwalls.Util.toast

class QueueActivity : AppCompatActivity()
{
    private lateinit var settingsManager: SettingsManager
    private lateinit var fragmentManager: FragmentManager
    
    private lateinit var currentType: SettingsManager.AutosubmitType
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        
        setContentView(SINGLE_FRAGMENT_LAYOUT)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        settingsManager = SettingsManager.getInstance(this)
        updateCurrentType()
        
        fragmentManager = supportFragmentManager
        var fragment: Fragment? = fragmentManager.findFragmentById(FRAGMENT_CONTAINER)
        
        if (fragment == null)
        {
            fragment = createFragment()
            fragmentManager.beginTransaction().add(FRAGMENT_CONTAINER, fragment).commit()
        }
    }

    override fun onStart()
    {
        if (currentType != settingsManager.autosubmitType)
        {
            // doing this before super.onStart() to prevent the execution of the old fragment's onStart()
            
            val oldType = currentType
            updateCurrentType()
            val fragment = createFragment()
            fragmentManager.beginTransaction().replace(FRAGMENT_CONTAINER, fragment).commit()

            toast("$oldType to $currentType")
        }
        
        super.onStart()
    }
    
    private fun createFragment(): QueueFragment
    {
        return when (currentType)
        {
            SettingsManager.AutosubmitType.Manual -> ManualQueueFragment()
            SettingsManager.AutosubmitType.Periodic -> PeriodicQueueFragment()
        }
    }
    
    private fun updateCurrentType()
    {
        currentType = settingsManager.autosubmitType
    }

    companion object
    {
        private const val SINGLE_FRAGMENT_LAYOUT = R.layout.activity_fragment
        private const val FRAGMENT_CONTAINER = R.id.fragment_container
    }
}