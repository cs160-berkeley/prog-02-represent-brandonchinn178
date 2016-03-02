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
 * Created by joleary and noon on 2/19/16 at very late in the night. (early in the morning?)
 */
public class WatchToPhoneService extends Service implements GoogleApiClient.ConnectionCallbacks {
    private GoogleApiClient mWatchApiClient;
    private List<Node> nodes = new ArrayList<>();
    private Bundle extras;

    @Override
    public void onCreate() {
        super.onCreate();
        //initialize the googleAPIClient for message passing
        mWatchApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        //and actually connect it
        mWatchApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWatchApiClient.disconnect();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        Wearable.NodeApi.getConnectedNodes(mWatchApiClient)
            .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                    if (extras == null) {
                        return;
                    }

                    nodes = getConnectedNodesResult.getNodes();
                    switch (extras.getString("PATH")) {
                        case "detailed":
                            sendMessage("/start_detailed", "Dianne Feinstein");
                            break;
                        case "random":
                            sendMessage("/start_random", "");
                            break;
                    }

                    _this.stopSelf();
                }
            });
    }

    @Override //we need this to implement GoogleApiClient.ConnectionsCallback
    public void onConnectionSuspended(int i) {}

    private void sendMessage(final String path, final String text) {
        for (Node node : nodes) {
            Wearable.MessageApi.sendMessage(mWatchApiClient, node.getId(), path, text.getBytes());
        }
    }

}
