package com.github.spicq.realestate;

import org.jsoup.nodes.Element;

/**
 * @author Sebastien Picq on 28/06/2019
 * <p>
 * <p>
 * <p>
 * $RCSfile$
 * $Revision$
 * $Date$
 */
public abstract class AbstractRealEstateExtractor implements RealEstateExtractor {
    public double getAttrDoubleValue(Element el, String attrName) {
        return RealEstate.readDouble(el.attr(attrName));
    }
    public int getAttrIntValue(Element el, String attrName) {
        return RealEstate.readInt(el.attr(attrName));
    }

    public double getPrice(Element el, String attrName) {
        return RealEstate.readPrice(el.attr(attrName));
    }
}
