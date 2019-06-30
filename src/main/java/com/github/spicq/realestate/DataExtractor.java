package com.github.spicq.realestate;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Sebastien Picq on 28/06/2019
 *
 *
 *
 * <p>
 * <p>
 * <p>
 * $RCSfile$
 * $Revision$
 * $Date$
 */
public class DataExtractor {
    private static Logger log = Logger.getLogger(DataExtractor.class.getName());

    private static final String realEstatePropertyFileName = "realEstate.properties";

    Properties realEstateProperties;
    List<RealEstateExtractor> realEstateExtractors;

    public DataExtractor() throws IOException {
        this.realEstateProperties = getRealEstateProperties();
        this.realEstateExtractors = getRealEstateExtractors();
    }

    private Properties getRealEstateProperties() throws IOException {
        InputStream propertyFileInputStream = DataExtractor.class.getClassLoader().getResourceAsStream(realEstatePropertyFileName);
        Properties properties = new Properties();
        properties.load(propertyFileInputStream);
        return properties;
    }

    private List<RealEstateExtractor> getRealEstateExtractors() {
        List<RealEstateExtractor> result = new ArrayList<>();

        Reflections reflections = new Reflections("com.github.spicq");
        Set<Class<? extends RealEstateExtractor>> realEstateExtractorClasses = reflections.getSubTypesOf(RealEstateExtractor.class);
        for (Class<? extends RealEstateExtractor> realEstateExtractorClass:realEstateExtractorClasses) {
            if ( ! Modifier.isAbstract(realEstateExtractorClass.getModifiers())) {
                try {
                    RealEstateExtractor realEstateExtractor = realEstateExtractorClass.newInstance();
                    if (realEstateExtractor!=null) {
                        result.add(realEstateExtractor);
                    }
                } catch (Exception e) {
                    log.severe("getRealEstateExtractors() Could not instantiate RealEstateExtractor "+realEstateExtractorClass+". Error:"+e.getMessage());
                }
            }
        }
        return result;
    }

    public void extractRealEstates() {
        for (String urlKey:realEstateProperties.stringPropertyNames()) {
            if (urlKey.startsWith("url")) {
                String mainListUrl = realEstateProperties.getProperty(urlKey);
                RealEstateExtractor realEstateExtractor = getAppropriateRealEstateExtractorForUrl(mainListUrl);
                if (realEstateExtractor!=null) {
                    extractRealEstates(realEstateExtractor, mainListUrl);
                }
            }
        }
        closeConnectionIfAny();
    }

    private void extractRealEstates(RealEstateExtractor realEstateExtractor, String mainListUrl) {
        List<RealEstate> realEstates=null;
        int pageNum=1;
        do {
            realEstates = extractRealEstates(realEstateExtractor, mainListUrl, pageNum);
            if (realEstates!=null) {
                processRealEstates(realEstates);
            }
            pageNum++;
        } while (realEstates!=null && !realEstates.isEmpty());
    }

    private void processRealEstates(List<RealEstate> realEstates) {
        Connection sqlConnection = getConnection();
        if (sqlConnection==null) {
            RealEstate.writeToCsv("realEstate.csv", realEstates);
        } else {
            QueryRunner queryRunner = new QueryRunner();
            try {
                queryRunner.batch(sqlConnection, RealEstate.getSqlInsertStatement(), getParams(realEstates));
            } catch (SQLException e) {
                log.severe("processRealEstates() failed inserting realEstates in database. Error:"+e.getMessage());
            }
        }
    }

    private Object[][] getParams(List<RealEstate> realEstates) {
        Object[][] params = new Object[realEstates.size()][];
        int i=0;
        for (RealEstate realEstate:realEstates) {
            params[i] = realEstate.getSqlParams();
            i++;
        }
        return params;
    }

    private Connection connection;
    private Connection getConnection() {
        if (isValidConnection()) {
            return connection;
        }
        int numTries=0;
        while (!isValidConnection() && numTries<5) {
            this.connection = getNewConnection();
            numTries++;
        }
        return this.connection;
    }

    private Connection getNewConnection() {
        try {
            return DriverManager.getConnection(realEstateProperties.getProperty("db-connection-url"),
                    realEstateProperties.getProperty("db-user-name"), realEstateProperties.getProperty("db-password"));
        } catch (Exception e) {
            log.severe("Could not connect to database ! Error:" + e.getMessage());
            return null;
        }
    }

    private boolean isValidConnection() {
        try {
            return connection!=null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void closeConnectionIfAny() {
        if (connection!=null) {
            DbUtils.closeQuietly(connection);
            this.connection=null;
        }
    }

    private List<RealEstate> extractRealEstates(RealEstateExtractor realEstateExtractor, String mainListUrl, int pageNum) {
        List<RealEstate> result = new ArrayList<>();
        try {
            String pagedUrl = realEstateExtractor.getPageUrl(mainListUrl, pageNum);
            if (pagedUrl==null) return result;
            Document doc = Jsoup.connect(pagedUrl).get();
            Elements elements = realEstateExtractor.extractRealEstateElements(doc);
            elements.forEach(el->extractRealEstate(realEstateExtractor, result, el));
        } catch (IOException e) {
            log.severe("extractRealEstateStream failed: " + e.getMessage());
        }
        return result;
    }

    public RealEstateExtractor getAppropriateRealEstateExtractorForUrl(String url) {
        for (RealEstateExtractor realEstateExtractor:realEstateExtractors) {
            if (realEstateExtractor.matchesUrl(url)) {
                return realEstateExtractor;
            }
        }
        log.severe("getAppropriateRealEstateExtractorForUrl() failed: No RealEstateExtractor found for URL="+url);
        return null;
    }

    private void extractRealEstate(RealEstateExtractor realEstateExtractor, List<RealEstate> realEstateList, Element el) {
        RealEstate realEstate = realEstateExtractor.extractRealEstate(el);
        if (realEstate!=null) {
            realEstateList.add(realEstate);
        }
    }

    public static void main(String[] args) throws IOException {
        DataExtractor dataExtractor = new DataExtractor();
        dataExtractor.extractRealEstates();

    }
}
