package cs160.brandonchinn178.smartelect;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
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
    public String id;
    public boolean isSenator;
    public String name;
    public String party;
    public Drawable image;

    public DelegateCard(String id, String name, String party, boolean isSenator) {
        this.id = id;
        this.isSenator = isSenator;
        this.name = name;
        this.party = party;
        this.image = null;
    }
}
