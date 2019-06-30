package com.github.spicq.realestate;

import com.csvreader.CsvWriter;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Sebastien Picq on 28/06/2019
 * <p>
 * <p>
 * <p>
 * $RCSfile$
 * $Revision$
 * $Date$
 */
public class RealEstate {
    private static Logger log = Logger.getLogger(RealEstate.class.getName());
    private static NumberFormat priceFormat = DecimalFormat.getCurrencyInstance(Locale.FRANCE), doubleFormat = DecimalFormat.getNumberInstance(Locale.FRANCE);

    public enum PropertyType {
        Flat,House,ParkingBox,Land,Business,Loft,Building,Castle,Hostel,Program
    }

    private String id, postCode;
    private double surface, price;
    private int numRooms, numBedRooms;
    private PropertyType propertyType;
    String detailUrl, title;

    public RealEstate(String id, String postCode, double surface, double price, int numRooms, int numBedRooms, PropertyType propertyType, String detailUrl, String title) {
        this.id = id;
        this.postCode = postCode;
        this.surface = surface;
        this.price = price;
        this.numRooms = numRooms;
        this.numBedRooms = numBedRooms;
        this.propertyType = propertyType;
        this.detailUrl = detailUrl;
        this.title = title;
    }
    private static List<String> attributeNames=null;
    private static List<String> getAttributeNames() {
        if (attributeNames==null) {
            attributeNames = Arrays.asList("id,postCode,surface,price,numRooms,numBedRooms,pricePerM2,propertyTypeAsString,detailUrl,title".split(","));
        }
        return attributeNames;
    }

    public double getPricePerM2() {
        return surface==0?0:price/surface;
    }

    public String getId() {
        return id;
    }

    public RealEstate setId(String id) {
        this.id = id;
        return this;
    }

    public String getPostCode() {
        return postCode;
    }

    public RealEstate setPostCode(String postCode) {
        this.postCode = postCode;
        return this;
    }

    public double getSurface() {
        return surface;
    }

    public RealEstate setSurface(double surface) {
        this.surface = surface;
        return this;
    }

    public double getPrice() {
        return price;
    }

    public RealEstate setPrice(double price) {
        this.price = price;
        return this;
    }

    public int getNumRooms() {
        return numRooms;
    }

    public RealEstate setNumRooms(int numRooms) {
        this.numRooms = numRooms;
        return this;
    }

    public int getNumBedRooms() {
        return numBedRooms;
    }

    public RealEstate setNumBedRooms(int numBedRooms) {
        this.numBedRooms = numBedRooms;
        return this;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }
    public String getPropertyTypeAsString() {
        return propertyType==null?"":propertyType.toString();
    }

    public RealEstate setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
        return this;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public RealEstate setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public RealEstate setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RealEstate)) return false;
        RealEstate that = (RealEstate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(", ", RealEstate.class.getSimpleName() + "[", "]");
        for (String attrName:getAttributeNames()) {
            result.add(attrName+"='"+getStringValue(attrName+"'"));
        }
        return result.toString();
    }

    public static void writeToCsv(String outputFile, List<RealEstate> realEstates) {
        boolean alreadyExists = new java.io.File(outputFile).exists();

        try {
            // use FileWriter constructor that specifies open for appending
            CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');

            // if the file didn't already exist then we need to write out the header line
            if (!alreadyExists)
            {
                for (String attrName:getAttributeNames()) {
                    csvOutput.write(attrName);
                }
                csvOutput.endRecord();
            }
            // else assume that the file already has the correct header line

            for (RealEstate realEstate:realEstates) {
                for (String attrName:getAttributeNames()) {
                    csvOutput.write(realEstate.getStringValue(attrName));
                }
                csvOutput.endRecord();
            }

            csvOutput.close();
        } catch (IOException e) {
            log.severe("writeToCsv failed with error:"+e.getMessage());
        }
    }

    public static double readPrice(String price) {
        return read(price, priceFormat).doubleValue();
    }
    public static double readDouble(String doubleValue) {
        return read(doubleValue, doubleFormat).doubleValue();
    }
    public static int readInt(String intValue) {
        return read(intValue, doubleFormat).intValue();
    }

    private static Number read(String val, NumberFormat formatter) {
        try {
            return val==null||val.isEmpty()?0.0:formatter.parse(val);
        } catch (ParseException e) {
            log.severe("read failed reading "+val+", error:"+e.getMessage());
            return 0.0;
        }
    }

    private static List<String> columnNames=null;
    private static List<String> getColumnNames() {
        if (columnNames==null) {
            columnNames = new ArrayList<>();
            for (String attrName:getAttributeNames()) {
                columnNames.add(getColumnName(attrName));
            }
        }
        return columnNames;
    }

    private static String getColumnName(String attrName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, attrName);
    }

    private String getStringValue(String attributeName) {
        Object result = getObjectValue(attributeName);
        return result==null?"":String.valueOf(result);
    }
    private Object getObjectValue(String attributeName) {
        Method getter = getAttributeGetter(attributeName);
        try {
            Object result = getter==null?null:getter.invoke(this);
            return result;
        } catch (Exception e) {
            log.severe("getObjectValue() failed for attrName="+attributeName+". Error:"+e.getMessage());
            return null;
        }
    }

    public static String getSqlInsertStatement() {
        return "INSERT INTO REALESTATE("+ StringUtils.join(getColumnNames(),',')+") VALUES ("+StringUtils.repeat("?",",", getColumnNames().size())+")";
    }

    public Object[] getSqlParams() {
        List<Object> params = new ArrayList<>();
        for (String attrName:getAttributeNames()) {
            params.add(getObjectValue(attrName));
        }
        return params.toArray();
    }

    private Method getAttributeGetter(String attrName) {
        String methodName = "get"+StringUtils.capitalize(attrName);
        Method method = getCachedMethodByName(getClass(), methodName);
        return method;
    }

    private static Map<String,Object> methodCache = new HashMap<>();
    public static Method getCachedMethodByName(Class clazz, String methodName, Class<?>... parameterTypes) {
        String key = clazz.getName()+"|"+methodName;
        for (Class paramType:parameterTypes) {
            key+="|"+paramType.getName();
        }
        Object method = methodCache.get(key);

        if (method != null) {
            return method instanceof Method ? (Method)method:null;
        }

        method = getMethodByName(clazz, methodName, parameterTypes) ;

        if (method!=null) {
            methodCache.put(key,method);
            return (Method)method;
        } else {
            methodCache.put(key, Boolean.FALSE);   // to avoid looking again and again for inexisting method
            return null;
        }
    }
    private static Method getMethodByName(Class objectClass, String methodName, Class<?>... parameterTypes) {
        try {
            return objectClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            log.warning("Could not find method "+methodName+" with params "+parameterTypes+" in class "+objectClass+" : "+ e.getMessage());
            return null;
        }
    }
}
