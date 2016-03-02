package cs160.brandonchinn178.smartelect;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Brandon on 2/24/16.
 */
public class CardPagerAdapter extends FragmentGridPagerAdapter {
    private final DelegateActivity activity;
    private final ArrayList<Object> cards;

    public CardPagerAdapter(DelegateActivity activity, FragmentManager fm, ArrayList<Object> cards) {
        super(fm);
        this.activity = activity;
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
        return CustomCardFragment.create(activity, cards.get(col));
    }

    public static class CustomCardFragment extends CardFragment {
        private DelegateActivity activity;
        private boolean isDelegate;

        public static CardFragment create(DelegateActivity activity, Object data) {
            CustomCardFragment fragment = new CustomCardFragment();
            fragment.activity = activity;
            Bundle args = new Bundle();

            if (data instanceof DelegateCard) {
                DelegateCard card = (DelegateCard) data;
                String title = (card.isSenator) ? "Senator" : "Representative";
                args.putString("CardFragment_title", title);

                String text = card.name + "\n" + card.party;
                args.putString("CardFragment_text", text);

                fragment.isDelegate = true;
            } else if (data instanceof ElectionResult) {
                ElectionResult card = (ElectionResult) data;
                String title = activity.getString(R.string.votes_title);
                args.putString("CardFragment_title", title);

                CharSequence obama = fragment.makeObamaLabel(card.obama);
                CharSequence romney = fragment.makeRomneyLabel(card.romney);
                args.putString("CardFragment_text", card.county + "\n" + obama + "\n" + romney);

                fragment.isDelegate = false;
            }

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateContentView(inflater, container, savedInstanceState);
            if (isDelegate) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView textView = (TextView) v.findViewById(R.id.text);
                        String name = textView.getText().toString().split("\n")[0];
                        activity.onClick(name);
                    }
                });
            }
            return view;
        }

        private CharSequence makeObamaLabel(double percentage) {
            return activity.getString(R.string.obama_label) + ": " + percentage + "%";
        }

        private CharSequence makeRomneyLabel(double percentage) {
            return activity.getString(R.string.romney_label) + ": " + percentage + "%";
        }
    }
}