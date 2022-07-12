package okanenavi.co.jp.ui.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePagerAdapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return HomeFragment.newInstance(position + 1)
    }
}