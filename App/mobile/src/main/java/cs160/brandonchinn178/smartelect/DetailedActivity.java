package cs160.brandonchinn178.smartelect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

public class DetailedActivity extends AppCompatActivity {
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        container = (LinearLayout) findViewById(R.id.container);
        LayoutInflater inflater = getLayoutInflater();

        // set name
        TextView nameView = (TextView) findViewById(R.id.name);
        nameView.setText("Senator Dianne Feinstein");

        // set party
        TextView partyView = (TextView) findViewById(R.id.party);
        // getString(R.string.party_label)
        partyView.setText("Party: Democrat");

        // set end of term
        TextView termEndView = (TextView) findViewById(R.id.term_end);
        // getString(R.string.term_end_label)
        termEndView.setText("End of Term: Jan. 3, 2019");

        // load image
        ImageView imageView = (ImageView) findViewById(R.id.photo);
        Ion.with(imageView).load("https://upload.wikimedia.org/wikipedia/commons/7/7d/Dianne_Feinstein,_official_Senate_photo_2.jpg");

        // load committees
        String[] committees = {
                "Appropriations Committee",
                "Select Committee on Intelligence",
                "Judiciary Committee",
                "Rules and Administration Committee"
        };
        int index = container.indexOfChild(findViewById(R.id.committees_header));
        for (String committee : committees) {
            TextView committeeView = (TextView) inflater.inflate(R.layout.template_committee, container, false);
            committeeView.setText(committee);
            container.addView(committeeView, ++index);
        }

        // load recent bills
        String[] bills = {
                "S. 252: Interstate Threads Clarification Act of 2016",
                "S. 252: Interstate Threads Clarification Act of 2016",
                "S. 252: Interstate Threads Clarification Act of 2016",
                "S. 252: Interstate Threads Clarification Act of 2016",
                "S. 252: Interstate Threads Clarification Act of 2016",
                "S. 252: Interstate Threads Clarification Act of 2016",
                "S. 252: Interstate Threads Clarification Act of 2016"
        };
        for (String bill : bills) {
            LinearLayout billView = (LinearLayout) inflater.inflate(R.layout.template_bills, container, false);
            ((TextView) billView.findViewById(R.id.name)).setText(bill);
            ((TextView) billView.findViewById(R.id.date)).setText("Introduced Feb. 11");
            container.addView(billView);
        }
    }
}
