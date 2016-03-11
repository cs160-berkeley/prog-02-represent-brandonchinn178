package cs160.brandonchinn178.smartelect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CongressionalActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener {
    private static JsonObject electionResults;
    private LinearLayout container;
    private GoogleApiClient mGoogleApiClient;
    protected ApiClient mApiClient;
    private ArrayList<Card> cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congressional);
        container = (LinearLayout) findViewById(R.id.container);
        container.setVisibility(View.GONE);

        mApiClient = new ApiClient(this);

        // Create an instance of GoogleAPIClient (http://developer.android.com/training/location/retrieve-current.html)
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        // initialize election results
        if (electionResults == null) {
            initElectionResults();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("IS_RANDOM", false)) {
            showToast("Randomizing...");
            int zipCode = ZipCode.getRandom(this);
            setUpZipCode(zipCode);
        } else if (intent.getBooleanExtra("IS_CURRENT", true)) {
            try {
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                // if current location is null, wait until it's updated
                if (currentLocation == null) {
                    LocationRequest mLocationRequest = LocationRequest.create().setNumUpdates(1);
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    setUpCurrentLocation(location);
                                }
                            }
                    );
                } else {
                    setUpCurrentLocation(currentLocation);
                }
            } catch (SecurityException e) {
                showToast("You do not have the Location permission enabled");
                fadeProgressBar();
            }
        } else {
            int zipCode = intent.getIntExtra("ZIP_CODE", 0);
            setUpZipCode(zipCode);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        showToast("Error: " + connectionResult.getErrorMessage());
    }

    /** SET UP METHODS **/

    private void setUpCurrentLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        ApiClient.District district = mApiClient.getDistrict(latitude, longitude);

        if (district == null) {
            showToast("You have no internet or you are not in the US");
            fadeProgressBar();
            return;
        }

        addTitle(district.getLabel());

        // get senators and representative and add card
        cards = mApiClient.getCards(latitude, longitude);

        for (Card card : cards) {
            card.render(this, container);
        }

        fadeProgressBar();
        startWatch(latitude, longitude, null);
    }

    private void setUpZipCode(int zipCode) {
        cards = mApiClient.getSenators(zipCode);
        if (cards == null) {
            showToast("There is something wrong with your network");
            fadeProgressBar();
            return;
        } else if (cards.size() == 0) {
            addTitle(getString(R.string.zip_code_error, zipCode));
            fadeProgressBar();
            return;
        } else {
            addTitle("ZIP code: " + zipCode);
            for (Card card : cards) {
                card.render(this, container);
            }
        }

        ArrayList<ApiClient.District> districts = mApiClient.getDistricts(zipCode);

        for (ApiClient.District district : districts) {
            addTitle(district.getLabel());

            Card card = mApiClient.getRepresentative(district);
            card.render(this, container);
            cards.add(card);
        }

        fadeProgressBar();
        startWatch(null, null, zipCode);
    }

    private void addTitle(String title) {
        TextView titleView = (TextView) getLayoutInflater().inflate(R.layout.template_title, container, false);
        titleView.setText(title);
        container.addView(titleView);
    }

    private void startWatch(Double latitude, Double longitude, Integer zipCode) {
        Intent intent = new Intent(this, PhoneToWatchService.class);
        JsonObject data = new JsonObject();
        JsonArray delegates = new JsonArray();
        JsonObject election;

        // add delegates
        for (Card card : cards) {
            JsonObject jsonCard = new JsonObject();

            jsonCard.addProperty("id", card.id);
            jsonCard.addProperty("name", card.name);
            jsonCard.addProperty("isSenator", card.isSenator);
            String party = card.party.getInitial().equals("D") ? "Democrat" : "Republican";
            jsonCard.addProperty("party", party);
            delegates.add(jsonCard);
        }

        // add county votes
        String county;
        if (zipCode == null) {
            county = mApiClient.getCounty(latitude, longitude);
        } else {
            county = mApiClient.getCounty(zipCode);
        }

        JsonElement jsonResult = electionResults.get(county);
        if (jsonResult != null) {
            election = jsonResult.getAsJsonObject();
            election.addProperty("name", county);
        } else {
            Log.d("county", "No county data for " + county);
            election = new JsonObject();
        }

        data.add("delegates", delegates);
        data.add("election", election);
        Log.d("startWatch", data.toString());
        intent.putExtra("PATH", "start_watch");
        intent.putExtra("DATA", data.toString());
        startService(intent);
    }

    protected void sendPhoto(String id, Bitmap bitmap) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        Asset image = Asset.createFromBytes(byteStream.toByteArray());

        String path = "/photos/" + id;
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(path).setUrgent();
        dataMapRequest.getDataMap().putAsset("image", image);

        Log.d("sendPhoto", path + " | " + image);
        Wearable.DataApi.putDataItem(mGoogleApiClient, dataMapRequest.asPutDataRequest());
    }

    /**
     * Fade the progress bar. Source: http://developer.android.com/training/animation/crossfade.html
     */
    private void fadeProgressBar() {
        container.setAlpha(0f);
        container.setVisibility(View.VISIBLE);

        long duration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        container.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);

        final View loading = findViewById(R.id.loading);
        loading.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loading.setVisibility(View.GONE);
                    }
                });
    }

    /** BUTTON METHODS **/

    // gets the Card object that represented this View
    private Card getCard(View v) {
        View parent = (View) v.getParent();
        while (parent.getId() != R.id.container) {
            parent = (View) parent.getParent();
        }

        String id = (String) parent.getTag();
        for (Card card : cards) {
            if (card.id.equals(id)) {
                return card;
            }
        }
        return null;
    }

    public void openWebsite(View v) {
        Card card = getCard(v);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + card.website));

        if (getPackageManager().queryIntentActivities(webIntent, 0).size() > 0) {
            startActivity(webIntent);
        } else {
            showToast("No web browsers installed");
        }
    }

    public void openMail(View v) {
        Card card = getCard(v);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{card.email});

        if (getPackageManager().queryIntentActivities(emailIntent, 0).size() > 0) {
            startActivity(emailIntent);
        } else {
            showToast("No email clients installed");
        }
    }

    public void startDetailedActivity(View v) {
        Card card = getCard(v);
        Intent intent = new Intent(this, DetailedActivity.class);
        intent.putExtra("DELEGATE_ID", card.id);
        startActivity(intent);
    }

    /** HELPER METHODS **/

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // source: http://stackoverflow.com/a/19945484/4966649
    private void initElectionResults() {
        String json = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.election_results_2012);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            electionResults = new JsonParser().parse(json).getAsJsonObject();
        } catch (IOException ex) {}
    }
}
