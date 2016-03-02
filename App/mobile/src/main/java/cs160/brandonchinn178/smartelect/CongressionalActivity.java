package cs160.brandonchinn178.smartelect;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class CongressionalActivity extends AppCompatActivity {
    private boolean isCurrentLocation;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);
        container = (LinearLayout) findViewById(R.id.container);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("isRandom", false)) {
            int[] randomZipcodes = {
                    90001,
                    90089,
                    92620,
                    94203,
                    94204,
                    94205,
                    94206,
                    94307,
                    94208,
                    94209,
                    94709,
                    94720
            };
            int zipCode = randomZipcodes[new Random().nextInt(3)];
            Toast toast = Toast.makeText(this, "Random Zipcode: " + zipCode, Toast.LENGTH_SHORT);
            toast.show();
            setUpZipCode(zipCode);
        } else {
            isCurrentLocation = intent.getBooleanExtra("IS_CURRENT", true);
            if (isCurrentLocation) {
                setUpCurrentLocation();
            } else {
                int zipCode = intent.getIntExtra("ZIP_CODE", 0);
                setUpZipCode(zipCode);
            }
        }
    }

    // gets the View that represents a Card
    private View getCardView(View v) {
        View parent = (View) v.getParent();
        while (parent.getId() != R.id.container) {
            parent = (View) parent.getParent();
        }
        return parent;
    }

    public void openWebsite(View v) {
        View parent = getCardView(v);
        String website = ((TextView) parent.findViewById(R.id.website)).getText().toString();
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + website));

        if (getPackageManager().queryIntentActivities(webIntent, 0).size() > 0) {
            startActivity(webIntent);
        }
    }

    public void openMail(View v) {
        View parent = getCardView(v);
        String email = ((TextView) parent.findViewById(R.id.email)).getText().toString();
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});

        if (getPackageManager().queryIntentActivities(emailIntent, 0).size() > 0) {
            startActivity(emailIntent);
        }
    }

    public void startDetailedActivity(View v) {
        View parent = getCardView(v);
        Intent intent = new Intent(this, DetailedActivity.class);
        startActivity(intent);
    }

    private void setUpCurrentLocation() {
        // get current location

        // for now, use 45th district (CA) as example
        String state = "CA";
        String districtLabel = getDistrictLabel(45, state);
        addTitle(districtLabel);

        // get senators and representative and add card
        new Card(
                "Dianne Feinstein",
                true,
                "https://upload.wikimedia.org/wikipedia/commons/7/7d/Dianne_Feinstein,_official_Senate_photo_2.jpg",
                PoliticalParty.getEnum("Democrat"),
                "Whether it's the wildflowers or tortoises, the desert has so much unique " +
                        "wildlife and vegetation. #ProtectCADesert",
                "feinstein.senate.gov",
                "d.feinstein@california.gov"
        ).render(this, container);
        new Card(
                "Barbara Boxer",
                true,
                "https://upload.wikimedia.org/wikipedia/commons/1/18/Barbara_Boxer,_Official_Portrait,_112th_Congress.jpg",
                PoliticalParty.getEnum("Democrat"),
                "Great news from @POTUS! New national monuments will permanently protect 1.8 " +
                    "million acres of CA desert. http://lat.ms/...",
                "boxer.senate.gov",
                "b.boxer@california.gov"
        ).render(this, container);
        new Card(
                "Mimi Walters",
                false,
                "https://upload.wikimedia.org/wikipedia/commons/1/16/Mimi_Walters_official_congressional_photo_2.jpg",
                PoliticalParty.getEnum("Republican"),
                "Congratulations to Shalin Shah @ his app Voice for being named winner of #CA45's " +
                    "2015 Congressional App Challenge! #cac...",
                "walters.house.gov",
                "m.walters@california.gov"
        ).render(this, container);

        // start watch
        Intent intent = new Intent(this, PhoneToWatchService.class);
        intent.putExtra("PATH", "candidates");
        intent.putExtra("IS_CURRENT", true);
        startService(intent);
    }

    private void setUpZipCode(int zipCode) {
        // for now, use CA as example
        String state = "CA";
        String zipCodeLabel = "ZIP code: " + Integer.toString(zipCode);
        addTitle(zipCodeLabel);

        // add senators
        new Card(
                "Dianne Feinstein",
                true,
                "https://upload.wikimedia.org/wikipedia/commons/7/7d/Dianne_Feinstein,_official_Senate_photo_2.jpg",
                PoliticalParty.getEnum("Democrat"),
                "Whether it's the wildflowers or tortoises, the desert has so much unique " +
                        "wildlife and vegetation. #ProtectCADesert",
                "feinstein.senate.gov",
                "d.feinstein@california.gov"
        ).render(this, container);
        new Card(
                "Barbara Boxer",
                true,
                "https://upload.wikimedia.org/wikipedia/commons/1/18/Barbara_Boxer,_Official_Portrait,_112th_Congress.jpg",
                PoliticalParty.getEnum("Democrat"),
                "Great news from @POTUS! New national monuments will permanently protect 1.8 " +
                        "million acres of CA desert. http://lat.ms/...",
                "boxer.senate.gov",
                "b.boxer@california.gov"
        ).render(this, container);

        // get districts in given zipcode
        ArrayList<Integer> districts = new ArrayList<>();
        // for now, use 45th district as example
        districts.add(45);

        for (int district : districts) {
            String districtLabel = getDistrictLabel(district, state);
            addTitle(districtLabel);

            // add representative
            new Card(
                    "Mimi Walters",
                    false,
                    "https://upload.wikimedia.org/wikipedia/commons/1/16/Mimi_Walters_official_congressional_photo_2.jpg",
                    PoliticalParty.getEnum("Republican"),
                    "Congratulations to Shalin Shah @ his app Voice for being named winner of #CA45's " +
                            "2015 Congressional App Challenge! #cac...",
                    "walters.house.gov",
                    "m.walters@california.gov"
            ).render(this, container);
        }

        // start watch
        Intent intent = new Intent(this, PhoneToWatchService.class);
        intent.putExtra("PATH", "candidates");
        intent.putExtra("IS_CURRENT", false);
        intent.putExtra("ZIP_CODE", zipCode);
        startService(intent);
    }

    private void addTitle(String title) {
        TextView titleView = (TextView) getLayoutInflater().inflate(R.layout.template_title, container, false);
        titleView.setText(title);
        container.addView(titleView);
    }

    private String getDistrictLabel(int district, String state) {
        String label = Integer.toString(district);
        switch (district % 10) {
            case 1:
                label += "st";
                break;
            case 2:
                label += "nd";
                break;
            case 3:
                label += "rd";
                break;
            default:
                label += "th";
        }
        return label + " District (" + state + ")";
    }
}
