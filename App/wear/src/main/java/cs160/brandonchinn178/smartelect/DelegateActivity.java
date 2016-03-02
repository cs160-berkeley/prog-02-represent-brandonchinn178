package cs160.brandonchinn178.smartelect;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

import java.util.ArrayList;

public class DelegateActivity extends ShakeableActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        // init cards here
        ArrayList<Object> cards = new ArrayList<>();
        cards.add(new DelegateCard(
                "Dianne Feinstein",
                "Democrat",
                "https://upload.wikimedia.org/wikipedia/commons/7/7d/Dianne_Feinstein,_official_Senate_photo_2.jpg",
                true
        ));
        cards.add(new DelegateCard(
                "Barbara Boxer",
                "Democrat",
                "https://upload.wikimedia.org/wikipedia/commons/1/18/Barbara_Boxer,_Official_Portrait,_112th_Congress.jpg",
                true
        ));
        cards.add(new DelegateCard(
                "Mimi Walter",
                "Republican",
                "https://upload.wikimedia.org/wikipedia/commons/1/16/Mimi_Walters_official_congressional_photo_2.jpg",
                false
        ));
        cards.add(new ElectionResult("Orange County, CA", 44.8, 53.0));

        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new CardPagerAdapter(this, getFragmentManager(), cards));
    }

    protected void onClick(String name) {
        Intent intent = new Intent(this, WatchToPhoneService.class);
        intent.putExtra("PATH", "detailed");
        startService(intent);
    }
}
