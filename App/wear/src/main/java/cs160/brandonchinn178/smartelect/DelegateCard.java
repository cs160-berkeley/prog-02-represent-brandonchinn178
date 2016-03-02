package cs160.brandonchinn178.smartelect;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Created by Brandon on 2/24/16.
 */
public class DelegateCard implements Serializable {
    public boolean isSenator;
    public String name;
    public String party;
    public String photoUrl;

    public DelegateCard(String name, String party, String photoUrl, boolean isSenator) {
        this.isSenator = isSenator;
        this.name = name;
        this.party = party;
        this.photoUrl = photoUrl;
    }
}
