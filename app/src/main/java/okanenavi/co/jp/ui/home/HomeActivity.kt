package okanenavi.co.jp.ui.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import okanenavi.co.jp.databinding.ActivityHomeBinding


class HomeActivity : AppCompatActivity() {
    companion object {
        private val TABS = listOf("支出", "収入", "振替")
    }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val homePagerAdapter = HomePagerAdapter(this)
        binding.viewPager.adapter = homePagerAdapter

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = TABS[position]
        }.attach()

        binding.fab.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }
    }
}