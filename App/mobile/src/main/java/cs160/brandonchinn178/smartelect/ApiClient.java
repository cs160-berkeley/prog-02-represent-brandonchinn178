package cs160.brandonchinn178.smartelect;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.*;
import com.koushikdutta.ion.Ion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Created by Brandon on 3/3/16.
 */
public class ApiClient {
    private final static int CONGRESS = 0;
    private final static String CONGRESS_URL_BASE = "https://congress.api.sunlightfoundation.com/";
    private final static String CONGRESS_API_KEY = "49510604af3f43c38f7c551f69b4fbe1";

    private final static int GEOCODE = 1;
    private final static String GEOCODE_URL_BASE = "http://maps.googleapis.com/maps/api/geocode/json";

    private static Twitter twitterClient;
    private Context context;

    public ApiClient(Context context) {
        this.context = context;

        if (twitterClient == null) {
            // requires twitter4j.properties in src/main/resources folder
            twitterClient = new TwitterFactory().getInstance();
        }
    }

    /** HELPER METHODS **/

    /**
     * Get the JSON response from accessing the given URL
     *
     * @param type CONGRESS | GEOCODE
     * @param path a path in the form "/path/to/API?key=value"
     * @return the JSON response from accessing the API
     */
    private JsonArray getJson(int type, String path) {
        String url;
        switch (type) {
            case CONGRESS:
                url = CONGRESS_URL_BASE + path + "&apikey=" + CONGRESS_API_KEY;
                break;
            case GEOCODE:
                url = GEOCODE_URL_BASE + path;
                break;
            default:
                url = "";
        }

        try {
            Log.d("getJson", url);
            return Ion.with(context)
                    .load(url)
                    .asJsonObject()
                    .get()
                    .getAsJsonArray("results");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JsonObject getJsonSingle(int type, String path) {
        JsonArray list = getJson(type, path);
        if (list == null) {
            return null;
        } else if (list.size() == 0) {
            return null;
        } else {
            return list.get(0).getAsJsonObject();
        }
    }

    /**
     * Get a Card filled in with data from the given JSON object
     * @param object
     * @return a filled-in Card
     */
    private Card getCard(JsonObject object) {
        String id = object.get("bioguide_id").getAsString();
        String firstName = object.get("first_name").getAsString();
        String lastName = object.get("last_name").getAsString();
        boolean isSenator = object.get("chamber").getAsString().equals("senate");
        PoliticalParty party = PoliticalParty.getEnum(object.get("party").getAsString());
        String email = object.get("oc_email").getAsString();

        String website = object.get("website").getAsString();
        // get rid of "http://" and any "www."
        website = website.split("https?://(www\\.)?")[1];
        // get rid of trailing slash
        website = website.split("/")[0];

        String twitterId;
        JsonElement twitterJson = object.get("twitter_id");
        if (twitterJson.isJsonNull()) {
            twitterId = "";
        } else {
            twitterId = twitterJson.getAsString();
        }

        String imageUrl = getImageUrl(id);

        return new Card(
                id, firstName, lastName, isSenator, imageUrl, party, twitterId, website, email
        );
    }

    /**
     * Return the URL of the delegate's photo with the given ID. Gets image URL
     * from theunitedstates.io API: https://github.com/unitedstates/images#using-the-photos
     *
     * @param id the bioguide ID of the delegate
     * @return the URL of the photo  of the delegate
     */
    private String getImageUrl(String id) {
        return "https://theunitedstates.io/images/congress/450x550/" + id + ".jpg";
    }

    private String reformatDate(String newFormat, String oldDate) {
        SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return new SimpleDateFormat(newFormat).format(oldFormat.parse(oldDate));
        } catch (ParseException e) {
            return "N/A";
        }
    }

    /** PUBLIC METHODS **/

    public District getDistrict(double latitude, double longitude) {
        String path = "districts/locate?latitude=" + latitude + "&longitude=" + longitude;
        JsonObject object = getJsonSingle(CONGRESS, path);

        // location outside of US or other network issue
        if (object == null) {
            return null;
        } else {
            String state = object.get("state").getAsString();
            int district = object.get("district").getAsInt();
            return new District(state, district);
        }
    }

    public ArrayList<District> getDistricts(int zipCode) {
        String path = "districts/locate?zip=" + zipCode;
        JsonArray list = getJson(CONGRESS, path);
        ArrayList<District> districts = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            JsonObject object = list.get(i).getAsJsonObject();
            String state = object.get("state").getAsString();
            int district = object.get("district").getAsInt();
            districts.add(new District(state, district));
        }

        return districts;
    }

    public boolean hasDistricts(int zipCode) {
        String path = "districts/locate?zip=" + zipCode;
        JsonArray list = getJson(CONGRESS, path);
        return list.size() > 0;
    }

    /**
     * Get the 2 senators and 1 representative at the given latitude and longitude
     * @param latitude
     * @param longitude
     * @return an array containing 3 cards, with the two senators first, then the representative last
     */
    public ArrayList<Card> getCards(double latitude, double longitude) {
        String path = "legislators/locate?latitude=" + latitude + "&longitude=" + longitude;
        JsonArray list = getJson(CONGRESS, path);
        ArrayList<Card> cards = new ArrayList<>(3);
        // insert representative at end
        Card representative = null;
        for (int i = 0; i < 3; i++) {
            JsonObject object = list.get(i).getAsJsonObject();
            Card card = getCard(object);
            if (card.isSenator) {
                cards.add(card);
            } else {
                representative = card;
            }
        }

        // just in case
        if (representative != null) {
            cards.add(representative);
        }

        return cards;
    }

    /**
     * Get the senators for the given zip code
     *
     * Note: some zip codes span more than 1 state. In this case, one state will be arbitrarily
     * selected and the two senators from that state will be returned.
     *
     * @param zipCode
     * @return an array containing 2 cards
     */
    public ArrayList<Card> getSenators(int zipCode) {
        String path = "legislators/locate?zip=" + zipCode;
        JsonArray list = getJson(CONGRESS, path);

        if (list == null) {
            return null;
        }

        ArrayList<Card> cards = new ArrayList<>(2);
        String mainState = null;
        for (int i = 0; i < list.size(); i++) {
            JsonObject object = list.get(i).getAsJsonObject();

            if (object.get("chamber").getAsString().equals("house")) {
                continue;
            }

            String state = object.get("state").getAsString();
            if (mainState == null) {
                mainState = state;
                cards.add(getCard(object));
            } else if (mainState.equals(state)) {
                cards.add(getCard(object));
                break;
            }
        }
        return cards;
    }

    public Card getRepresentative(District district) {
        String path = "legislators?district=" + district.district + "&state=" + district.state;
        JsonObject object = getJsonSingle(CONGRESS, path);
        return getCard(object);
    }

    /**
     * Asynchronously load the last tweet into the given Card
     * @param card
     */
    public void loadLastTweet(Card card) {
        new LoadTweetTask().execute(card);
    }

    /**
     * Return information for a detailed view of the delegate with the given ID
     * @param id the bioguide ID of the delegate
     * @return an array of the form (name, party, end of term, imageUrl)
     */
    public String[] getDetailed(String id) {
        String path = "legislators?bioguide_id=" + id;
        JsonObject object = getJsonSingle(CONGRESS, path);

        String title = object.get("chamber").getAsString().equals("house") ? "Representative" : "Senator";
        String firstName = object.get("first_name").getAsString();
        String lastName = object.get("last_name").getAsString();

        String party = object.get("party").getAsString().equals("D") ? "Democrat" : "Republican";

        return new String[] {
                title + " " + firstName + " " + lastName,
                party,
                reformatDate("MMM d, yyyy", object.get("term_end").getAsString()),
                getImageUrl(id)
        };
    }

    public ArrayList<String> getCommittees(String id) {
        String path = "committees?member_ids=" + id + "&subcommittee=false&per_page=all";
        JsonArray list = getJson(CONGRESS, path);
        ArrayList<String> committees = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            JsonObject object = list.get(i).getAsJsonObject();
            String committee = object.get("name").getAsString();
            committees.add(committee);
        }

        return committees;
    }

    public ArrayList<Bill> getBills(String id) {
        String path = "bills?sponsor_id=" + id + "&order=introduced_on&per_page=5";
        JsonArray list = getJson(CONGRESS, path);
        ArrayList<Bill> bills = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            JsonObject object = list.get(i).getAsJsonObject();
            JsonElement titleJson = object.get("short_title");
            String title;
            if (titleJson.isJsonNull()) {
                title = object.get("official_title").getAsString();
            } else {
                title = titleJson.getAsString();
            }
            String date_introduced = object.get("introduced_on").getAsString();
            date_introduced = reformatDate("MMM. d", date_introduced);
            Bill bill = new Bill(title, date_introduced);
            bills.add(bill);
        }

        return bills;
    }

    // return "County, STATE" string
    public String getCounty(double latitude, double longitude) {
        String path = "?latlng=" + latitude + "," + longitude;
        return getCounty(path);
    }

    public String getCounty(int zipCode) {
        String path = "?address=" + zipCode;
        return getCounty(path);
    }

    // private method for getCounty overloaded methods
    private String getCounty(String path) {
        JsonObject address = getJsonSingle(GEOCODE, path);
        JsonArray addressComponents = address.getAsJsonArray("address_components");

        String county = null, state = null;
        for (int i = 0; i < addressComponents.size(); i++) {
            JsonObject component = addressComponents.get(i).getAsJsonObject();
            String type = component.get("types").getAsJsonArray().get(0).getAsString();
            if (type.equals("administrative_area_level_2")) {
                county = component.get("long_name").getAsString();
            } else if (type.equals("administrative_area_level_1")) {
                state = component.get("short_name").getAsString();
            }
        }

        // some zip codes don't include county; re-calculate with latitude/longitude
        if (county == null) {
            JsonObject location = address.getAsJsonObject("geometry").getAsJsonObject("location");
            double latitude = location.get("lat").getAsDouble();
            double longitude = location.get("lng").getAsDouble();
            return getCounty(latitude, longitude);
        } else {
            return county + ", " + state;
        }
    }

    private class LoadTweetTask extends AsyncTask<Card, Void, Card> {
        /**
         * Takes in data as a (screenName, cardId) pair and returns a (tweet, cardId) pair
         * @param data
         */
        @Override
        protected Card doInBackground(Card... data) {
            Card card = data[0];
            try {
                ResponseList<twitter4j.Status> statuses = twitterClient.getUserTimeline(card.twitterId);
                if (statuses.size() > 0) {
                    card.tweet = statuses.get(0).getText();
                }
            } catch (TwitterException e) {}

            return card;
        }

        /**
         * Loads the tweet into the Card
         * @param card
         */
        @Override
        protected void onPostExecute(Card card) {
            card.renderTweet();
        }
    }

    public static class District {
        public String state;
        public int district;

        public District(String state, int district) {
            this.state = state;
            this.district = district;
        }

        public String getLabel() {
            String label = Integer.toString(district);
            switch (district % 10) {
                case 1:
                    label += "st";
                    break;
                case 2:
                    label += "nd";
                    break;
                case 3:
                    label += "rd";
                    break;
                default:
                    label += "th";
            }
            return label + " District (" + state + ")";
        }
    }
}
