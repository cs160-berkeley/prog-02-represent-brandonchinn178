package cs160.brandonchinn178.smartelect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.util.ArrayList;

public class DelegateActivity extends ShakeableActivity implements GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient mApiClient;
    // track all the DelegateCards without images
    private ArrayList<DelegateCard> delegateCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        String data = getIntent().getStringExtra("DATA");
        JsonObject jsonData;
        try {
            jsonData = new JsonParser().parse(data).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        delegateCards = new ArrayList<>(3);
        JsonArray delegates = jsonData.getAsJsonArray("delegates");
        for (int i = 0; i < delegates.size(); i++) {
            JsonObject delegate = delegates.get(i).getAsJsonObject();
            delegateCards.add(new DelegateCard(
                    delegate.get("id").getAsString(),
                    delegate.get("name").getAsString(),
                    delegate.get("party").getAsString(),
                    delegate.get("isSenator").getAsBoolean()
            ));
        }

        ArrayList<Object> cards = new ArrayList<>();
        cards.addAll(delegateCards);
        JsonObject election = jsonData.getAsJsonObject("election");
        cards.add(new ElectionResult(
                election.get("name").getAsString(),
                election.get("obama").getAsDouble(),
                election.get("romney").getAsDouble()
        ));

        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new CardPagerAdapter(getFragmentManager(), cards));
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    protected void loadPhoto(final CardPagerAdapter.CustomCardFragment fragment, final DelegateCard card) {
        Log.d("loadPhoto", card.id);
        Uri uri = Uri.parse("wear:/photos/" + card.id);
        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(final DataItemBuffer dataItemBuffer) {
                        if (dataItemBuffer.getCount() == 0) {
                            return;
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Asset asset = DataMapItem.fromDataItem(dataItemBuffer.get(0))
                                        .getDataMap()
                                        .getAsset("image");

                                InputStream stream = Wearable.DataApi.getFdForAsset(mApiClient, asset)
                                        .await()
                                        .getInputStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                                card.image = new BitmapDrawable(getResources(), bitmap);

                                final ImageView imageView = fragment.getImageView();
                                imageView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageDrawable(card.image);
                                    }
                                });

                                dataItemBuffer.release();
                            }
                        }).start();
                    }
                });
    }

    protected void onClick(String id) {
        Intent intent = new Intent(this, WatchToPhoneService.class);
        intent.putExtra("PATH", "detailed");
        intent.putExtra("ID", id);
        startService(intent);
    }
}
