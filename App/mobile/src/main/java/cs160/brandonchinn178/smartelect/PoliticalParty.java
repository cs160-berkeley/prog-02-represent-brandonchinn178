package cs160.brandonchinn178.smartelect;

/**
 * Created by Brandon on 2/20/16.
 */
public enum PoliticalParty {
    DEMOCRAT, REPUBLICAN, INDEPENDENT;

    public static PoliticalParty getEnum(String party) {
        switch (party) {
            case "D":
                return DEMOCRAT;
            case "R":
                return REPUBLICAN;
            default:
                return INDEPENDENT;
        }
    }

    public String getInitial() {
        switch (this) {
            case DEMOCRAT:
                return "D";
            case REPUBLICAN:
                return "R";
            default:
                return "I";
        }
    }

    public int getColor() {
        switch (this) {
            case DEMOCRAT:
                return R.color.democrat;
            case REPUBLICAN:
                return R.color.republican;
            default:
                return R.color.independent;
        }
    }
}
