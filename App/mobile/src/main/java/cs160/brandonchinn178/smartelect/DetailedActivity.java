package cs160.brandonchinn178.smartelect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class DetailedActivity extends AppCompatActivity {
    private LinearLayout container;
    private ApiClient mCongressClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
        container = (LinearLayout) findViewById(R.id.container);
        mCongressClient = new ApiClient(this);
        LayoutInflater inflater = getLayoutInflater();

        Intent intent = getIntent();
        String id = intent.getStringExtra("DELEGATE_ID");
        String[] data = mCongressClient.getDetailed(id);

        // set name
        final TextView nameView = (TextView) findViewById(R.id.name);
        final String nameData = data[0];
        nameView.setText(nameData);

        // abbreviate "Sen." or "Rep." if text wrapping
        // source: http://stackoverflow.com/questions/12477026/how-to-understand-that-android-finished-drawing-a-view
        nameView.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (nameView.getLineCount() > 1) {
                            String[] nameSplit = nameData.split(" ", 2);
                            String name = nameSplit[0].substring(0, 3) + ". " + nameSplit[1];
                            nameView.setText(name);
                        }
                        // removeGlobalOnLayoutListern deprecated in API level 16
                        nameView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                });

        // set party
        TextView partyView = (TextView) findViewById(R.id.party);
        partyView.setText(getString(R.string.party_label, data[1]));

        // set end of term
        TextView termEndView = (TextView) findViewById(R.id.term_end);
        termEndView.setText(getString(R.string.term_end_label, data[2]));

        // load image
        ImageView imageView = (ImageView) findViewById(R.id.photo);
        Ion.with(imageView)
                .placeholder(R.drawable.flag_background)
                .load(data[3]);

        // load committees
        ArrayList<String> committees = mCongressClient.getCommittees(id);
        int index = container.indexOfChild(findViewById(R.id.committees_header));
        for (String committee : committees) {
            TextView committeeView = (TextView) inflater.inflate(R.layout.template_committee, container, false);
            committeeView.setText(committee);
            container.addView(committeeView, ++index);
        }

        // load recent bills
        ArrayList<Bill> bills = mCongressClient.getBills(id);
        for (Bill bill : bills) {
            LinearLayout billView = (LinearLayout) inflater.inflate(R.layout.template_bills, container, false);
            ((TextView) billView.findViewById(R.id.name)).setText(bill.name);
            String date = getString(R.string.date_introduced_label, bill.date_introduced);
            ((TextView) billView.findViewById(R.id.date)).setText(date);
            container.addView(billView);
        }
    }
}
