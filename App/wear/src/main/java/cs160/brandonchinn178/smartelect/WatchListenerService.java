package cs160.brandonchinn178.smartelect;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchListenerService extends WearableListenerService {
    private static final String START_CANDIDATES = "/start_candidates";
    public static final String PHOTO_LOADED = "cs160.brandonchinn178.smart_elect.PHOTO_LOADED";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
        Log.d("watchListener", "received: " + value);
        switch (messageEvent.getPath()) {
            case START_CANDIDATES:
                handleCandidates(value);
                break;
            default:
                super.onMessageReceived(messageEvent);
        }
    }

    private void handleCandidates(String data) {
        Intent intent = new Intent(this, DelegateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("DATA", data);
        startActivity(intent);
    }
}