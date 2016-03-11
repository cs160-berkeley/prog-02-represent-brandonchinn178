package cs160.brandonchinn178.smartelect;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

/**
 * Created by Brandon on 2/21/16.
 */
public class Card {
    public String id;
    public String name;
    public boolean isSenator;
    public String imageUrl;
    public PoliticalParty party;
    public String twitterId;
    public String tweet;
    public String website;
    public String email;
    private LinearLayout card;

    public Card(String id, String firstName, String lastName, boolean isSenator, String imageUrl,
                PoliticalParty party, String twitterId, String website, String email) {
        this.id = id;
        this.name = firstName + " " + lastName;
        this.isSenator = isSenator;
        this.imageUrl = imageUrl;
        this.party = party;
        this.twitterId = twitterId;
        this.tweet = "";
        this.website = website;
        this.email = email;
    }

    @Override
    public String toString() {
        return "Card(" + name + ")";
    }

    /**
     * Renders the card and adds it as a child of the given View
     */
    public void render(final CongressionalActivity activity, LinearLayout container) {
        card = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.template_card, container, false);
        card.setTag(id);

        // load image
        final ImageView imageView = (ImageView) card.findViewById(R.id.photo);
        imageView.setImageResource(R.drawable.flag_background); // placeholder
        Ion.with(activity)
                .load(imageUrl)
                .asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        Resources resources = activity.getResources();
                        BitmapDrawable drawable = new BitmapDrawable(resources, result);
                        imageView.setImageDrawable(drawable);
                        activity.sendPhoto(id, result);
                    }
                });

        // load tweet
        activity.mApiClient.loadLastTweet(this);

        // set name
        TextView nameView = (TextView) card.findViewById(R.id.name);
        Spannable nameLabel = getNameLabel(activity);
        nameView.setText(nameLabel);

        // set website
        TextView websiteView = (TextView) card.findViewById(R.id.website);
        websiteView.setText(website);

        // set email
        TextView emailView = (TextView) card.findViewById(R.id.email);
        emailView.setText(email);

        container.addView(card);
    }

    public void renderTweet() {
        TextView tweetView = (TextView) card.findViewById(R.id.tweet);
        tweetView.setText(tweet);
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
