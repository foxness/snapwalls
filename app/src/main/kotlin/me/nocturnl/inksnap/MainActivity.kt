package me.nocturnl.inksnap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity()
{
    private lateinit var navbar: BottomNavigationView
    
    private lateinit var fragmentManager: FragmentManager
    
    private lateinit var settingsManager: SettingsManager
    
    private var currentFragment: Fragment? = null
    
    private var selectedItemId: Int? = null
    
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsManager = SettingsManager.getInstance(this)
        
        fragmentManager = supportFragmentManager
        
        navbar = findViewById(R.id.main_bottom_navigation)
        navbar.setOnNavigationItemSelectedListener {
            setFragmentBasedOnMenu(it.itemId)
            true
        }
        
        // todo: do this in every fragment
        currentFragment = fragmentManager.findFragmentById(FRAGMENT_CONTAINER)
        
        val itemId = savedInstanceState?.getInt(ARG_SELECTED_ITEM_ID) ?: intent.extractItemId()
        
        if (currentFragment == null)
        {
            setFragmentBasedOnMenu(itemId)
        }
        else
        {
            selectedItemId = itemId
        }
    }

    override fun onNewIntent(newIntent: Intent)
    {
        super.onNewIntent(newIntent)
        
        setFragmentBasedOnMenu(newIntent.extractItemId())
    }

    override fun onBackPressed() // controversial feature
    {
        if (selectedItemId == HOME_ITEM_ID)
        {
            super.onBackPressed()
        }
        else
        {
            setFragmentBasedOnMenu(HOME_ITEM_ID)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.menu_main, menu)
        
        val developerOptionsUnlocked = settingsManager.developerOptionsUnlocked
        
        val testItem = menu.findItem(R.id.menu_main_test)
        val logItem = menu.findItem(R.id.menu_main_log)
        
        testItem.isVisible = developerOptionsUnlocked
        logItem.isVisible = developerOptionsUnlocked
        
        return true
    }

    override fun onResume()
    {
        super.onResume()
        
        if (System.currentTimeMillis() >= KILLSWITCH_DATE)
        {
            val i = KillswitchActivity.newIntent(this)
            startActivity(i)
            finish()
        }
    }

    private fun openSettings()
    {
        val i = SettingsActivity.newIntent(this)
        startActivity(i)
    }

    private fun openLog()
    {
        val i = LogActivity.newIntent(this)
        startActivity(i)
    }

    private fun testButton()
    {
        val nf = NotificationFactory.getInstance(this)
//        nf.showSuccessNotification("testy")
        nf.showErrorNotification()
    }

    private fun openAbout()
    {
        val i = AboutActivity.newIntent(this)
        startActivity(i)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.menu_main_test -> { testButton(); true }
            R.id.menu_main_log -> { openLog(); true }
            R.id.menu_main_settings -> { openSettings(); true }
            R.id.menu_main_about -> { openAbout(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        outState.putInt(ARG_SELECTED_ITEM_ID, selectedItemId ?: HOME_ITEM_ID)
        super.onSaveInstanceState(outState)
    }

    override fun onStart()
    {
        if (selectedItemId == HOME_ITEM_ID)
        {
            val currentType = when (currentFragment)
            {
                is ManualQueueFragment -> SettingsManager.SchedulingType.Manual
                is PeriodicQueueFragment -> SettingsManager.SchedulingType.Periodic
                else -> throw Exception("how?")
            }
            
            if (currentType != settingsManager.schedulingType)
            {
                val newFragment = getQueueFragment()
                setFragment(newFragment)
            }
        }
        
        super.onStart()
    }
    
    private fun getQueueFragment(): QueueFragment
    {
        return when (settingsManager.schedulingType)
        {
            SettingsManager.SchedulingType.Manual -> ManualQueueFragment.newInstance()
            SettingsManager.SchedulingType.Periodic -> PeriodicQueueFragment.newInstance()
        }
    }

    private fun setFragmentBasedOnMenu(itemId: Int)
    {
        if (selectedItemId == itemId)
        {
            return
        }
        
        selectedItemId = itemId
        navbar.selectedItemId = itemId
        
        val newFragment = when (selectedItemId)
        {
            NavbarSelection.Queued.resId -> getQueueFragment()
            NavbarSelection.Posted.resId -> PostedFragment.newInstance()
            NavbarSelection.Failed.resId -> FailedFragment.newInstance()
            
            else -> throw Exception("what")
        }
        
        setFragment(newFragment)
    }
    
    private fun setFragment(newFragment: Fragment)
    {
        if (currentFragment == null)
        {
            fragmentManager.beginTransaction()
                    .add(FRAGMENT_CONTAINER, newFragment)
                    .commit()
        }
        else
        {
            fragmentManager.beginTransaction()
                    .replace(FRAGMENT_CONTAINER, newFragment)
                    .commit()
        }
        
        currentFragment = newFragment
    }

    companion object
    {
        private const val ARG_SELECTED_ITEM_ID = "arg_selected_item_id"
        private const val EXTRA_SELECTED_ITEM_ID = "extra_selected_item_id"
        private const val HOME_ITEM_ID = R.id.action_queue
        private const val FRAGMENT_CONTAINER = R.id.main_fragment_container

        private const val KILLSWITCH_DATE: Long = 1596153600000
        
        enum class NavbarSelection(val resId: Int)
        {
            Queued(R.id.action_queue),
            Posted(R.id.action_posted),
            Failed(R.id.action_failed),
        }
        
        fun newIntent(context: Context, navbarSelection: NavbarSelection = NavbarSelection.Queued) 
                = Intent(context, MainActivity::class.java)
                .apply { putExtra(EXTRA_SELECTED_ITEM_ID, navbarSelection.resId) }
        
        private fun Intent.extractItemId() = getIntExtra(EXTRA_SELECTED_ITEM_ID, HOME_ITEM_ID)
    }
}