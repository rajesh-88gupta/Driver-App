package com.seentechs.newtaxidriver.trips.tripsdetails

/**
 * @package com.seentechs.newtaxidriver.trips.tripsdetails
 * @subpackage tripsdetails model
 * @category ViewPagerAdapter
 * @author Seen Technologies
 *
 */

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import java.util.ArrayList

/* ************************************************************
                ViewPagerAdapter
Its used to show all the trips details view page
*************************************************************** */
internal class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }
}

