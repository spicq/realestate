package com.github.spicq.realestate;

import com.github.spicq.realestate.RealEstate.PropertyType;
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
public class SeLogerRealEstateExtractor extends AbstractRealEstateExtractor implements RealEstateExtractor {
    private static final String PAGE_NUM="LISTING-LISTpg";

    @Override
    public Elements extractRealEstateElements(Document document) {
        return document.getElementsByClass("h-fi-pulse");
    }

    @Override
    public RealEstate extractRealEstate(Element realEstateElement) {
        /*
        <div class="h-fi-pulse annonce__detail__sauvegarde" data-tooltip-over="Ajouter aux favoris" data-idannonce="147359587" data-idtiers="143619" data-idagence="94046"
        data-codepostal="06410" data-codeinsee="60018" data-typebien="2" data-typedetransaction="8" data-idtypepublicationsourcecouplage="SL" data-surface="158" data-prix="750&nbsp;000 €"
        data-nb_photos="14" data-position="20" data-nb_pieces="6" data-produitsvisibilite="AD:AC:AW" data-nb_chambres="5" data-idpublication="482137">
         */
        String id = realEstateElement.attr("data-idannonce");
        if (id==null||id.isEmpty()) {
            return null;
        }
        Element detailElement = realEstateElement.previousElementSiblings().select("a.link_AB").first();
        RealEstate realEstate = new RealEstate(id, realEstateElement.attr("data-codepostal"), getAttrDoubleValue(realEstateElement,"data-surface"),
                getPrice(realEstateElement, "data-prix"), getAttrIntValue(realEstateElement, "data-nb_pieces"),
                getAttrIntValue(realEstateElement, "data-nb_chambres"), getPropertyType(realEstateElement), detailElement.attr("href"), detailElement.attr("title"));
        
        return realEstate;
    }

    private PropertyType getPropertyType(Element realEstateElement) {
        String type = realEstateElement.attr("data-typebien");
        int intType = type==null?1:Integer.parseInt(type);
        switch (intType) {
            case 2: return PropertyType.House;
            case 3: return PropertyType.ParkingBox;
            case 4: return PropertyType.Land;
            case 6: case 7: case 8: return PropertyType.Business;
            case 9: return PropertyType.Loft;
            case 11: case 12: return PropertyType.Building;
            case 13: return PropertyType.Castle;
            case 14: return PropertyType.Hostel;
            case 15: return PropertyType.Program;
            case 1: default: return PropertyType.Flat;
        }
    }

    @Override
    public String getPageUrl(String mainListUrl, int pageNum) {
        if (pageNum<=1) return mainListUrl;
        return mainListUrl+"&"+PAGE_NUM+"="+pageNum;
        //return null;   // for now, just one page...
    }

    @Override
    public boolean matchesUrl(String url) {
        return url!=null && url.startsWith("https://www.seloger.com/list.htm");
    }
}
