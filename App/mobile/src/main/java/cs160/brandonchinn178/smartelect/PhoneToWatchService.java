package cs160.brandonchinn178.smartelect;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joleary on 2/19/16.
 */
public class PhoneToWatchService extends Service implements GoogleApiClient.ConnectionCallbacks {
    public static final String START_CANDIDATES = "/start_candidates";
    public static final String START_VOTES = "/start_votes";

    private GoogleApiClient mApiClient;
    private List<Node> nodes = new ArrayList<>();
    private Bundle extras;

    @Override
    public void onCreate() {
        super.onCreate();
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mApiClient.connect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            extras = intent.getExtras();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(Bundle bundle) {
        final Service _this = this;
        Wearable.NodeApi.getConnectedNodes(mApiClient)
            .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    if (extras == null) {
                        return;
                    }

                    nodes = getConnectedNodesResult.getNodes();
                    switch (extras.getString("PATH")) {
                        case "candidates":
                            if (extras.getBoolean("IS_CURRENT")) {
                                sendMessage(START_CANDIDATES, "current");
                            } else {
                                sendMessage(START_CANDIDATES, Integer.toString(extras.getInt("ZIP_CODE")));
                            }
                            break;
                    }
                    _this.stopSelf();
                }
            });
    }

    @Override //we need this to implement GoogleApiClient.ConnectionsCallback
    public void onConnectionSuspended(int i) {}

    private void sendMessage(final String path, final String text ) {
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, text.getBytes());
        }
    }

}
