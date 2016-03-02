package cs160.brandonchinn178.smartelect;

/**
 * Created by Brandon on 2/28/16.
 */
public class ElectionResult {
    public String county;
    public double obama;
    public double romney;

    public ElectionResult(String county, double obama, double romney) {
        this.county = county;
        this.obama = obama;
        this.romney = romney;
    }
}
