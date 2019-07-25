package com.uber.pickbot.Adapters;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

import com.uber.pickbot.Past_trips;
import com.uber.pickbot.Upcoming_Trips;

public class ViewpagerAdapter extends FragmentPagerAdapter {

        private Context mContext;

        public ViewpagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        // This determines the fragment for each tab
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new Past_trips();
            }
            else if(position == 1) {
                return new Upcoming_Trips();
            }return null;
        }


        // This determines the number of tabs
        @Override
        public int getCount() {
            return 2;
        }

        // This determines the title for each tab
        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            switch (position) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                default:
                    return null;
            }return null;
        }

    }
