package com.plateonic.android.com.plateonic.utils;

import java.io.Serializable;

/**
 * Created by Oleg on 2/9/14.
 */
public class FacebookDetails implements Serializable {
    // serializable is slow/bad. todo: make parcelable

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String fullName;


    public FacebookDetails(String id, String firstName, String lastName, String fullName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }
}
