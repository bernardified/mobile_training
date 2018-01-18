package com.shopback.speeddial;

/**
 * Created by bernardkoh on 17/1/18.
 */

class Contact {
    private String name;
    private String number;

    Contact(String name, String number){
        this.name = name;
        this.number = number;
    }

    Contact(){

    }

    String getName() {
        return name;
    }

    String getNumber() {
        return number;
    }
}
