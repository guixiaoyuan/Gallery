package com.tct.gallery3d.app.section;

import android.location.Address;
import android.text.TextUtils;

import com.tct.gallery3d.app.Log;

import java.util.List;
import java.util.Locale;

public class SectionInfo {

    /**
     * Invalid location.
     */
    public static final int INVALID_LOCATION = -1;
    /**
     * The section time.
     */
    private String mTime;

    /**
     * The section latitude.
     */
    private double mLatitude = INVALID_LOCATION;

    /**
     * The section longitude.
     */
    private double mLongitude = INVALID_LOCATION;

    /**
     * The section address.
     */
    private Address mAddress;
    private AddressChangedListener mListener;

    private static final String DEFAULT_STRING = "";

    public SectionInfo() {
    }

    public SectionInfo(String time, Address address) {
        mTime = time;
        mAddress = address;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public String getAddress() {
        String result = DEFAULT_STRING;
        if (mAddress != null) {
            String temp0;
            String temp1;
            String temp2;
            String country;

            String separator = DEFAULT_STRING;
            Locale locale = Locale.getDefault();
            if (locale == Locale.CHINA || locale == Locale.CHINESE || locale == Locale.SIMPLIFIED_CHINESE || locale ==
                    Locale.TRADITIONAL_CHINESE) {
                result = DEFAULT_STRING;
            } else {
                separator = ", ";
            }

            temp0 = mAddress.getAddressLine(0);
            temp1 = mAddress.getLocality();
            temp2 = mAddress.getAddressLine(2);
            country = mAddress.getCountryName();
            if (temp0.equals(country)) {
                result = temp1 + separator + temp2;
            } else if (temp2.equals(country)) {
                result = temp0 + separator + temp1;
            }
        }
        return result;
    }

    public void setAddress(Address address) {
        mAddress = address;
        if (mListener != null) {
            mListener.onAddressChange();
        }
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public boolean hasCoordinates() {
        return mLatitude != INVALID_LOCATION && mLongitude != INVALID_LOCATION;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SectionInfo) {
            return mTime.equals(((SectionInfo) obj).getTime());
        }
        return super.equals(obj);
    }

    public interface AddressChangedListener {
        void onAddressChange();
    }

    public void setOnAddressChangedListener(AddressChangedListener listener) {
        mListener = listener;
    }
}
