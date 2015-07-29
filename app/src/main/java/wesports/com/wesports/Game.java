package wesports.com.wesports;

public class Game {
  String type;
  String details;
  Long date;
  Place place;
  Contact contact;

  class Place {
    double lon;
    double lat;
  }

  class Contact {
    String phone;
    String email;
    String name;
  }
}
