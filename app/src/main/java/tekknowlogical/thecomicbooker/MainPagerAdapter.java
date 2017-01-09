package tekknowlogical.thecomicbooker;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;

/**
 * Created by Tim on 1/2/2017.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {

    Context context;

    public MainPagerAdapter(FragmentManager fm, Context context) {

        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch(position) {

            case 0: return MyLibraryFragment.newInstance("Hello","Timmeh");
            default:return MyLibraryFragment.newInstance("This is", "Default");
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch(position) {
            case 0:
                return context.getString(R.string.my_library);
            case 1:
                return context.getString(R.string.my_account);
            default:
                return null;
        }
    }


}
