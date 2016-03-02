package cs160.brandonchinn178.smartelect;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class PhoneListenerService extends WearableListenerService {
    private static final String START_DETAILED = "/start_detailed";
    private static final String START_RANDOM = "/start_random";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String data = new String(messageEvent.getData());
        switch (messageEvent.getPath()) {
            case START_DETAILED:
                handleDetailed(data);
                break;
            case START_RANDOM:
                handleRandom(data);
                break;
            default:
                super.onMessageReceived(messageEvent);

        }
    }

    private void handleDetailed(String data) {
        Intent intent = new Intent(this, DetailedActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("name", data);
        startActivity(intent);
    }

    private void handleRandom(String data) {
        Intent intent = new Intent(this, CongressionalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isRandom", true);
        startActivity(intent);
    }
}
