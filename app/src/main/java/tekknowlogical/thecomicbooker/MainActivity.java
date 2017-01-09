package tekknowlogical.thecomicbooker;

import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompatBase;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;

public class MainActivity extends AppCompatActivity
        implements MyLibraryFragment.OnFragmentInteractionListener
        {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.viewpager_tabs);
        tabLayout.setTabTextColors(Color.GRAY, Color.WHITE);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            Log.v("actionbar found!", "Yay it's not null!");
            getSupportActionBar().setElevation(0);
        }

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(),this);
        viewPager.setAdapter(mainPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
