package cs160.brandonchinn178.smartelect;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.StandardCharsets;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchListenerService extends WearableListenerService {
    private static final String START_CANDIDATES = "/start_candidates";
    private static final String START_VOTES = "/start_votes";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
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
        if (data.equals("current")) {
            intent.putExtra("IS_CURRENT", true);
        } else {
            intent.putExtra("IS_CURRENT", false);
            intent.putExtra("ZIP_CODE", Integer.parseInt(data));
        }
        startActivity(intent);
    }
}