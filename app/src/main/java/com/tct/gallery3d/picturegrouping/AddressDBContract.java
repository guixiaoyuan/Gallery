package com.tct.gallery3d.picturegrouping;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.location.Address;
import android.provider.BaseColumns;
import android.util.Log;

public class AddressDBContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public AddressDBContract() {}

    /* Inner class that defines the table contents */
    public static abstract class AddressTable implements BaseColumns {
        public static final String TABLE_NAME = "addresses";

        public static final String _LATITUDE_HASH = "latitudeHash";
        public static final String _LONGITUDE_HASH = "longitudeHash";
        
        // The location list may contain either:
        // 1. A list of address proposals, representing a single couple of coordinates
        //    Each address proposal being separated by a '\n'
        //    Each field of the address being separated by a '@'
        //    Each field also includes a 'quality' value, used for sorting
        // 2. An empty list, meaning that the Geocoder replied successfully but with empty data
        // 3. null, meaning that there was an error when using the Geocoder:
        //    Either the Geocoder is not present
        //    Or Google was not accessible (timeout)
        //    Or there was no network, etc.
        //
        // When a location is marked as null, the location cache is supposed
        // to retry later, but not too often (say once a day maximum)
        public static final String _ADDRESS_SET = "addressSet";
        // For 'retry'
        public static final String _FAIL_COUNT = "failCount";
        public static final String _FAIL_TIMESTAMP = "failTimestamp";
    }
    
    private final static String UNESCAPED_ESCAPE = "#";
    private final static String ESCAPED_ESCAPE = "##";
    
    private final static String UNESCAPED_FIELD_SEPARATOR = "@";
    private final static String ESCAPED_FIELD_SEPARATOR = "#f";
    
    private final static String UNESCAPED_LINE_SEPARATOR = "\n";
    private final static String ESCAPED_LINE_SEPARATOR = "#l";
    
    private static class FindReplace {
        final Pattern mPattern;
        final String mReplace;
        
        FindReplace(String find, String replace){
            mPattern = Pattern.compile(Pattern.quote(find));
            mReplace = replace;
        }
    }
    
    private static final FindReplace[] mEscapeReplace = new FindReplace[]{
        new FindReplace(UNESCAPED_ESCAPE,    ESCAPED_ESCAPE), // KEEP FIRST !!!
        new FindReplace(UNESCAPED_LINE_SEPARATOR, ESCAPED_LINE_SEPARATOR),
        new FindReplace(UNESCAPED_FIELD_SEPARATOR, ESCAPED_FIELD_SEPARATOR)
    };

    private static final FindReplace[] mUnescapeReplace = new FindReplace[]{
        new FindReplace(ESCAPED_LINE_SEPARATOR, UNESCAPED_LINE_SEPARATOR),
        new FindReplace(ESCAPED_FIELD_SEPARATOR, UNESCAPED_FIELD_SEPARATOR),
        new FindReplace(ESCAPED_ESCAPE,    UNESCAPED_ESCAPE) // KEEP LAST !!!
    };
    
    private static String findReplace(final String string, final FindReplace[] tokens){
        String outString = string;
        
        if (PictureGrouping.DEBUG_STRING_ESCAPE){
            Log.v(PictureGrouping.TAG, "AddressDBContract.findReplace(" + outString + ")");
        }
        for (int i=0; i < tokens.length; i++){
            Matcher matcher = tokens[i].mPattern.matcher(outString);
            outString = matcher.replaceAll(tokens[i].mReplace);
            if (PictureGrouping.DEBUG_STRING_ESCAPE){
                Log.v(PictureGrouping.TAG, "AddressDBContract.findReplace(replace by " + tokens[i].mReplace + " => " + outString + ")");
            }
        }
        
        return outString;
    }
    
    private static String escapeString(String string){
        if (string == null || string.length() <= 0){
            //Log.e(Poladroid.TAG, "*** MiningAgent.escapeString(string: " + string + "): string is null or empty");
            return string;
        }
        
        //String outString = new String(string);
        //outString = outString.replaceAll(Pattern.quote("#"), "##");
        //outString = outString.replaceAll(Pattern.quote("\n"), "#n");
        String outString = findReplace(string, mEscapeReplace);
        if (PictureGrouping.DEBUG_STRING_ESCAPE){
            Log.d(PictureGrouping.TAG, "AddressDBContract.escapeString(string: " + string + ") => " + outString);
        }
        return outString;
    }
    
    private static String unescapeString(String string){
        if (string == null || string.length() <= 0){
            //Log.e(Poladroid.TAG, "*** AddressDBContract.unescapeString(string: " + string + "): string is null or empty");
            return string;
        }
        
        //String outString = new String(string);
        //outString = outString.replaceAll(Pattern.quote("##"), "#");
        //outString = outString.replaceAll(Pattern.quote("#n"), "\n");
        String outString = findReplace(string, mUnescapeReplace);
        if (PictureGrouping.DEBUG_STRING_ESCAPE){
            Log.d(PictureGrouping.TAG, "AddressDBContract.unescapeString(string: " + string + ") => " + outString);
        }
        return outString;
    }
    
    private static void checkEscapeUnescapeString(final String unescapedString, final String escapedString){
        String outString1 = AddressDBContract.escapeString(unescapedString);
        if (! outString1.equals(escapedString)){
            Log.e(PictureGrouping.TAG, "*** AddressDBContract.checkEscapeUnescapeString(unescaped: " + unescapedString +
                  " => " + escapedString + "): test failed '" + outString1 + "'");
            throw new Error();
        }
        
        String outString2 = AddressDBContract.unescapeString(outString1);
        if (! outString2.equals(unescapedString)){
            Log.e(PictureGrouping.TAG, "*** AddressDBContract.checkEscapeUnescapeString(escaped: " + escapedString +
                  " => " + unescapedString + "): test failed '" + outString2 + "'");
            throw new Error();
        }
    }
    
    

    
    static void testEscapeUnescapeString(){
        Log.d(PictureGrouping.TAG, "AddressDBContract.testEscapeUnescapeString(){");
        
        checkEscapeUnescapeString("ah ah ah", "ah ah ah"); 
        checkEscapeUnescapeString("ah#ah#ah", "ah##ah##ah"); 
        checkEscapeUnescapeString("ah\nah\nah", "ah#lah#lah"); 
        checkEscapeUnescapeString("ah\nah#ah", "ah#lah##ah"); 

        Log.d(PictureGrouping.TAG, "} AddressDBContract.testEscapeUnescapeString()");
    } 

    static String packAddress(Address address) {
        StringBuilder builder = new StringBuilder();

        builder.append(UNESCAPED_LINE_SEPARATOR);
        builder.append(address.getAddressLine(0));//0 lin0
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getAddressLine(1));//1 line1
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getAddressLine(2));//2 line2
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getCountryName());//3 country
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getLatitude());//4 Latitude
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getLongitude());//5 Longitude
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getFeatureName());//6 featurename
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getAdminArea());//7 adminarea
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getLocality());//8 locality
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getThoroughfare());//9 throughfare
        builder.append(UNESCAPED_FIELD_SEPARATOR);
        builder.append(address.getCountryCode());//10 countrycode


        return builder.toString().replaceAll(UNESCAPED_LINE_SEPARATOR, "");
    }

    static Address unPackAddress(String string) {
        if (string == null) return null;
        Address address = new Address(Locale.getDefault());
        if (address == null) return null;

        String[] fields = string.split(Pattern.quote(UNESCAPED_FIELD_SEPARATOR));
        address.setAddressLine(0,fields[0]);
        address.setAddressLine(1,fields[1]);
        address.setAddressLine(2,fields[2]);
        address.setCountryName(fields[3]);
        address.setLatitude(Double.valueOf(fields[4]));
        address.setLongitude(Double.valueOf(fields[5]));
        address.setFeatureName(fields[6]);
        address.setAdminArea(fields[7]);
        address.setLocality(fields[8]);
        address.setThoroughfare(fields[9]);
        address.setCountryCode(fields[10]);
        return address;

    }

    static String packAddressSet(Set<QualityAddress> addressSet){
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (QualityAddress address : addressSet){
            if (first){
                first = false;
            }
            else {
                builder.append(UNESCAPED_LINE_SEPARATOR);
            }
            builder.append(escapeString(Float.toString(address.mBaseQuality)));
            for (int i=0; i < address.mAreas.length; i++){
                builder.append(UNESCAPED_FIELD_SEPARATOR);
                if (address.mAreas[i] != null){
                    builder.append(escapeString(address.mAreas[i]));
                }
                builder.append(UNESCAPED_FIELD_SEPARATOR);
                builder.append(Float.toString(address.mAreasQuality[i]));
            }
            builder.append(UNESCAPED_FIELD_SEPARATOR);
            builder.append(escapeString(address.mCountryCode));
        }
        
        return builder.toString();
    }
    
    
    static Set<QualityAddress> unpackAddressSet(String string){
        Set<QualityAddress> addressSet = new HashSet<QualityAddress>();
        
        if (string != null && ! string.isEmpty()){
            String[] lines = string.split(Pattern.quote(UNESCAPED_LINE_SEPARATOR));
            for (String line : lines){
                //Log.d(Poladroid.TAG, "Line: '" + line + "'");
                if (line != null && ! line.isEmpty()){
                    String[] fields = line.split(Pattern.quote(UNESCAPED_FIELD_SEPARATOR));
                    //for (String field : fields){
                    //    Log.d(Poladroid.TAG, "Field: '" + field + "'");
                    //}
                    try {
                        QualityAddress fullAddress = new QualityAddress();
                        if (fields.length != 1 + 2 * fullAddress.mAreas.length + 1){
                            throw new Exception("Wrong size for '" + line + "', from '" + string + "': " + fields.length + " vs " + (1 + 2 * fullAddress.mAreas.length));
                        }
                        fullAddress.mBaseQuality = (float) Double.parseDouble(fields[0]);
                        for (int i=0; i < fullAddress.mAreas.length; i++){
                            if (fields[1+2*i+0] == null || fields[1+2*i+0].length() == 0){
                                fullAddress.mAreas[i] = null;
                            }
                            else {
                                fullAddress.mAreas[i] = unescapeString(fields[1+2*i+0]);
                            }
                            fullAddress.mAreasQuality[i] = (float) Double.parseDouble(fields[1+2*i+1]);
                        }
                        fullAddress.mCountryCode = unescapeString(fields[1+2*fullAddress.mAreas.length]);
                        addressSet.add(fullAddress);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return addressSet;
    }
}

/* EOF */

