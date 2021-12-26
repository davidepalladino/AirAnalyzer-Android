package it.davidepalladino.airanalyzer.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MeasureAverage implements Parcelable {
    @SerializedName("Hour")
    protected String hour;
    @SerializedName("Temperature")
    protected float temperature;
    @SerializedName("Humidity")
    protected float humidity;

    public MeasureAverage(String hour, float temperature, float humidity) {
        this.hour = hour;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    protected MeasureAverage(Parcel in) {
        hour = in.readString();
        temperature = in.readFloat();
        humidity = in.readFloat();
    }

    public String getHour() {
        return hour;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public static final Creator<MeasureAverage> CREATOR = new Creator<MeasureAverage>() {
        @Override
        public MeasureAverage createFromParcel(Parcel in) {
            return new MeasureAverage(in);
        }

        @Override
        public MeasureAverage[] newArray(int size) {
            return new MeasureAverage[size];
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
        dest.writeString(hour);
        dest.writeFloat(temperature);
        dest.writeFloat(humidity);
    }
}
