package it.davidepalladino.airanalyzer.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String id;
    private String username;
    private String email;
    private String name;
    private String surname;
    private String question1;
    private String question2;
    private String question3;

    public User(String id, String username, String email, String name, String surname, String question1, String question2, String question3) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
    }

    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        email = in.readString();
        name = in.readString();
        surname = in.readString();
        question1 = in.readString();
        question2 = in.readString();
        question3 = in.readString();
    }

    public String getId() {
        return id;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(question1);
        dest.writeString(question2);
        dest.writeString(question3);
    }
}