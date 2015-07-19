package wesports.com.wesports;

public class Game {
  String type;
  String name;
  String desc;
  People people;
  Long date;
  Place place;
  int bet;
  Contact contact;

  class Place {
    double lon;
    double lat;
  }

  class People {
    int min;
    int max;
  }

  class Contact {
    String phone;
    String email;
    String name;
  }
}
