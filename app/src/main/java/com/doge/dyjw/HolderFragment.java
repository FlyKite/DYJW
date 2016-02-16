package com.doge.dyjw;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.doge.dyjw.jiaowu.JiaowuSystemFragment;
import com.doge.dyjw.news.DongyouNewsFragment;
import com.doge.dyjw.trade.TradeFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link HolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HolderFragment extends Fragment {

    private static HolderFragment[] fragment = new HolderFragment[5];
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @sectionNumber int sectionNumber.
     * @return A new instance of fragment DrawerFragmentFactory.
     */
    public static HolderFragment newInstance(int sectionNumber) {
        int position = sectionNumber - 1;
        Bundle bundle = new Bundle();
        if(fragment[position] == null) {
            switch(position) {
                case 0: fragment[position] = new MainCourseFragment(); break;
                case 1: fragment[position] = new TradeFragment(); break;
                case 2: fragment[position] = new JiaowuSystemFragment(); break;
                case 3: fragment[position] = new DongyouNewsFragment();
                    bundle.putInt("which", DongyouNewsFragment.NEWS);
                    fragment[position].setArguments(bundle);
                    break;
                case 4: fragment[position] = new DongyouNewsFragment();
                    bundle.putInt("which", DongyouNewsFragment.JWC);
                    fragment[position].setArguments(bundle);
                    break;
                default: return null;
            }
        }
        return fragment[position];
    }

}
