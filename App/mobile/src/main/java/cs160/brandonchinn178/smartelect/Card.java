package cs160.brandonchinn178.smartelect;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

/**
 * Created by Brandon on 2/21/16.
 */
public class Card {
    private String name;
    private boolean isSenator;
    private String imageUrl;
    private PoliticalParty party;
    private String tweet;
    private String website;
    private String email;

    public Card(String name, boolean isSenator, String imageUrl, PoliticalParty party,
                String tweet, String website, String email) {
        this.name = name;
        this.isSenator = isSenator;
        this.imageUrl = imageUrl;
        this.party = party;
        this.tweet = tweet;
        this.website = website;
        this.email = email;
    }

    /**
     * Renders the card and adds it as a child of the given View
     */
    public void render(Activity activity, LinearLayout container) {
        LinearLayout card = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.template_card, container, false);

        // load image
        ImageView imageView = (ImageView) card.findViewById(R.id.photo);
        Ion.with(imageView).load(imageUrl);

        // set name
        TextView nameView = (TextView) card.findViewById(R.id.name);
        Spannable nameLabel = getNameLabel(activity);
        nameView.setText(nameLabel);

        // set tweet
        TextView tweetView = (TextView) card.findViewById(R.id.tweet);
        tweetView.setText(tweet);

        // set website
        TextView websiteView = (TextView) card.findViewById(R.id.website);
        websiteView.setText(website);

        // set email
        TextView emailView = (TextView) card.findViewById(R.id.email);
        emailView.setText(email);

        container.addView(card);
    }

    private Spannable getNameLabel(Activity activity) {
        String title;
        if (isSenator) {
            title = "Sen. ";
        } else {
            title = "Rep. ";
        }

        Spannable nameLabel = new SpannableString(title + name + " (" + party.getInitial() + ")");
        int partyLabelIndex = title.length() + name.length() + 2;
        nameLabel.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(activity, party.getColor())),
                partyLabelIndex, partyLabelIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        return nameLabel;
    }
}
