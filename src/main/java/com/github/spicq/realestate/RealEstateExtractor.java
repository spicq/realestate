package com.github.spicq.realestate;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Sebastien Picq on 28/06/2019
 * <p>
 * <p>
 * <p>
 * $RCSfile$
 * $Revision$
 * $Date$
 */
public interface RealEstateExtractor {
    Elements extractRealEstateElements(Document document);
    RealEstate extractRealEstate(Element realEstateElement);
    String getPageUrl(String mainListUrl, int pageNum);
    boolean matchesUrl(String url);
}
