package tech.muso.stonky.android

// use kotlin android extensions to do findViewById() once, and cache results for us.
// this also handles loading in all the imports for the view classes
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import tech.muso.stonky.android.main.DemoPageAdapter
import tech.muso.stonky.android.main.TouchLockSemaphore
import tech.muso.demo.repos.AuthenticationRepository
import tech.muso.stonky.android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        fun start(activity: AppCompatActivity) {
            val intent = Intent(activity, MainActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // fade into activity
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val demoPageAdapter = DemoPageAdapter(this, supportFragmentManager,
            // Demonstration of Observer pattern with typical Java Style interface callbacks.
            TouchLockSemaphore(object : TouchLockSemaphore.Listener {
                override fun onLockChanged(isLocked: Boolean) {
                    // enable viewpager swipe if not locked.
                    binding.viewPager.isUserInputEnabled = !isLocked
                }
            })
        )

//        binding.title.text = tech.muso.stonky.common.getPlatformName()
        binding.viewPager.adapter = demoPageAdapter
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.registerOnPageChangeCallback(demoPageAdapter.callback)

        // use mediator to connect viewpager and our empty TabLayout in XML
        val mediator: TabLayoutMediator = TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            // onConfigureTab
            demoPageAdapter.link(tab, position)
        }.apply {
            attach()
        } // attach() call once set up.

        // null/false/true boolean to avoid recalling code below when already unlocked
        var prevUnlockState: Boolean? = null

        // observe our authentication state, and when we unlock, move to the stocks tab.
        AuthenticationRepository.isAppUnlocked.observe(this,
            Observer<Boolean> { isUnlocked ->
                if (prevUnlockState == false && isUnlocked) {
                    Snackbar.make(binding.viewPager, "PIN ENTRY SUCCESS", Snackbar.LENGTH_SHORT).show()
                    binding.viewPager.setCurrentItem(1, true)
                    binding.viewPager.isUserInputEnabled = true
                }
                if (prevUnlockState == true && isUnlocked == false) {
                    Snackbar.make(binding.viewPager, "LOCKED AFTER 60 SECONDS", Snackbar.LENGTH_SHORT).show()
                    binding.viewPager.setCurrentItem(0, true)
                }

                prevUnlockState = isUnlocked
            }
        )

        binding.viewPager.setOnTouchListener { v, event ->
            return@setOnTouchListener AuthenticationRepository.isTouchLocked
        }
    }
}