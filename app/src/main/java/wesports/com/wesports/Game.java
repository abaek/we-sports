package wesports.com.wesports;

import com.google.android.gms.location.places.Place;

import java.util.Date;

/**
 * Created by Eric on 2015-07-18.
 */

public class Game {
    String game;
    Place place;
    int minPeople;
    int maxPeople;
    Date date;
    String description;

    public Game(
            String game,
            Place place,
            int minPeople,
            int maxPeople,
            Date date,
            String description
    ){
        this.game = game;
        this.place = place;
        this.minPeople = minPeople;
        this.maxPeople = maxPeople;
        this.date = date;
        this.description = description;
    }
}
