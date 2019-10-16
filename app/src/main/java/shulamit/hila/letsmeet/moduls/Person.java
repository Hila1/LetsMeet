package shulamit.hila.letsmeet.moduls;

public class Person {
    private String name;
    private String number;
/**the class define a person with a name and number
 * */
    public Person(String name, String number) {//,String imgURL
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


}
