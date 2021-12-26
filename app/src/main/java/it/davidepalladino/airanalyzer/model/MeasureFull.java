package it.davidepalladino.airanalyzer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MeasureFull implements Parcelable {
    @SerializedName("DateAndTime")
    private String dateAndTime;
    @SerializedName("DayWeek")
    private String dayWeek;
    @SerializedName("Temperature")
    private float temperature;
    @SerializedName("Humidity")
    private float humidity;

    public MeasureFull(String dateAndTime, String dayWeek, float temperature, float humidity) {
        this.dateAndTime = dateAndTime;
        this.dayWeek = dayWeek;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    protected MeasureFull(Parcel in) {
        dateAndTime = in.readString();
        dayWeek = in.readString();
        temperature = in.readFloat();
        humidity = in.readFloat();
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public static final Creator<MeasureFull> CREATOR = new Creator<MeasureFull>() {
        @Override
        public MeasureFull createFromParcel(Parcel in) {
            return new MeasureFull(in);
        }

        @Override
        public MeasureFull[] newArray(int size) {
            return new MeasureFull[size];
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
        dest.writeString(dateAndTime);
        dest.writeString(dayWeek);
        dest.writeFloat(temperature);
        dest.writeFloat(humidity);
    }
}