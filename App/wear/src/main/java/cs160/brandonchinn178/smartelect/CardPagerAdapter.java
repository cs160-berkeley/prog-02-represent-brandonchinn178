package cs160.brandonchinn178.smartelect;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Brandon on 2/24/16.
 */
public class CardPagerAdapter extends FragmentGridPagerAdapter {
    private final ArrayList<Object> cards;

    public CardPagerAdapter(FragmentManager fm, ArrayList<Object> cards) {
        super(fm);
        this.cards = cards;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return cards.size();
    }

    @Override
    public Fragment getFragment(int row, int col) {
        return CustomCardFragment.create(cards.get(col));
    }

    public static class CustomCardFragment extends CardFragment {
        private Object data;
        private View cardView;

        public static CardFragment create(Object data) {
            CustomCardFragment fragment = new CustomCardFragment();
            fragment.data = data;
            return fragment;
        }

        @Override
        public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final DelegateActivity activity = (DelegateActivity) getActivity();
            container.getLayoutParams();

            if (data instanceof DelegateCard) {
                final DelegateCard card = (DelegateCard) data;
                cardView = (LinearLayout) inflater.inflate(R.layout.template_delegate, container, false);
                cardView.setTag(card.id);

                TextView nameView = (TextView) cardView.findViewById(R.id.name);
                nameView.setText(card.name);

                TextView infoView = (TextView) cardView.findViewById(R.id.info);
                String title = card.isSenator ? "Senator" : "Representative";
                String party;
                int color;
                if (card.party.equals("Democrat")) {
                    party = "D";
                    color = R.color.democrat;
                } else {
                    party = "R";
                    color = R.color.republican;
                }
                SpannableString info = new SpannableString(title + " (" + party + ")");
                color = ContextCompat.getColor(activity, color);
                info.setSpan(
                        new ForegroundColorSpan(color),
                        title.length() + 2, title.length() + 3,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                infoView.setText(info);

                // if card doesn't have an image, load it from assets
                if (card.image == null) {
                    ((DelegateActivity) getActivity()).loadPhoto(this, card);
                } else {
                    ImageView imageView = getImageView();
                    imageView.setImageDrawable(card.image);
                }

                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.onClick(card.id);
                    }
                });
            } else {
                final ElectionResult election = (ElectionResult) data;
                cardView = (RelativeLayout) inflater.inflate(R.layout.template_election, container, false);

                ((TextView) cardView.findViewById(R.id.county)).setText(election.county);

                CharSequence obamaLabel = makeObamaLabel();
                ((TextView) cardView.findViewById(R.id.obama_label)).setText(obamaLabel);

                String obama = election.obama + "%";
                ((TextView) cardView.findViewById(R.id.obama)).setText(obama);

                CharSequence romneyLabel = makeRomneyLabel();
                ((TextView) cardView.findViewById(R.id.romney_label)).setText(romneyLabel);

                String romney = election.romney + "%";
                ((TextView) cardView.findViewById(R.id.romney)).setText(romney);
            }

            return cardView;
        }

        public ImageView getImageView() {
            return (ImageView) cardView.findViewById(R.id.photo);
        }

        private CharSequence makeObamaLabel() {
            Activity activity = getActivity();
            SpannableString label = new SpannableString(
                    activity.getString(R.string.obama_label)
            );
            int color = ContextCompat.getColor(activity, R.color.democrat);
            label.setSpan(
                    new ForegroundColorSpan(color),
                    7, 8,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return label;
        }

        private CharSequence makeRomneyLabel() {
            Activity activity = getActivity();
            SpannableString label = new SpannableString(
                    activity.getString(R.string.romney_label)
            );
            int color = ContextCompat.getColor(activity, R.color.republican);
            label.setSpan(
                    new ForegroundColorSpan(color),
                    8, 9,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return label;
        }
    }
}