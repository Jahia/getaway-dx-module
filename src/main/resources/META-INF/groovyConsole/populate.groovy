import org.apache.commons.lang.StringUtils
import org.jahia.api.Constants
import org.jahia.services.content.JCRContentUtils
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRPublicationService
import org.jahia.services.content.JCRSessionFactory
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRValueFactoryImpl
import org.xml.sax.SAXParseException

import javax.jcr.PropertyType
import javax.jcr.Value

def logger = log

Map config = new HashMap()
List destinations = new ArrayList()
config.put("destinations", destinations)
// Geneva
Map d_gva = new HashMap()
destinations.add(d_gva)
d_gva.put("name", "Geneva")
d_gva.put("highlight", true)
d_gva.put("latitude", " 46.1456")
d_gva.put("longitude", "6.5891")
d_gva.put("country", "CH")
d_gva.put("main-pic", "/sites/digitall/files/images/backgrounds/landscape-mountains-nature-clouds.jpg")
d_gva.put("hl_landmarks", Arrays.asList("ChIJxYdJYjpljEcRSJJQjwS5fwM",
        "ChIJHVx0N9VkjEcRgFvJTopTApU",
        "ChIJDd__TB1ljEcRWsj6TSicf-A",
        "ChIJgcoLz_pkjEcRlE0r8mguJiA",
        "ChIJo8di1TFljEcRFU9ttUDPPL0"))

// NYC
Map d_nyc = new HashMap()
destinations.add(d_nyc)
d_nyc.put("name", "New York City")
d_nyc.put("highlight", true)
d_nyc.put("latitude", "40.6536")
d_nyc.put("longitude", "-73.5672")
d_nyc.put("country", "US")
d_nyc.put("main-pic", "/sites/digitall/files/images/slides/office-buildings.jpg")
d_nyc.put("hl_landmarks", Arrays.asList("ChIJKxDbe_lYwokRVf__s8CPn-o",
        "ChIJ4zGFAZpYwokRGUGph3Mf37k",
        "ChIJRcvoOxpawokR7R4dQMXMMPQ",
        "ChIJb8Jg9pZYwokR-qHGtvSkLzs",
        "ChIJaXQRs6lZwokRY6EFpJnhNNE",
        "ChIJPTacEpBQwokRKwIlDXelxkA"))

// Reykjavik
Map d_rkv = new HashMap()
destinations.add(d_rkv)
d_rkv.put("name", "Reykjavik")
d_rkv.put("highlight", false)
d_rkv.put("latitude", "64.1034")
d_rkv.put("longitude", "-21.4893")
d_rkv.put("country", "IS")
d_rkv.put("main-pic", "/sites/digitall/files/images/misc/IMG_4773.JPG")
d_rkv.put("hl_landmarks", Arrays.asList("ChIJndUbV8l01kgRKEyfO5sVMe0",
        "ChIJOyxLnhVCzUgRoOQvu5-Krk0",
        "ChIJtS1DoMx01kgR76qdSMQor_c",
        "ChIJW2giY9UK1kgRQSFXuArgnpA"))

// Paris
Map d_paris = new HashMap()
destinations.add(d_paris)
d_paris.put("name", "Paris")
d_paris.put("highlight", true)
d_paris.put("latitude", "48.8032")
d_paris.put("longitude", "2.7905")
d_paris.put("country", "FR")
d_paris.put("main-pic", "/sites/digitall/files/images/slides/city-sunny-couple-love.jpg")
d_paris.put("hl_landmarks", Arrays.asList("ChIJ442GNENu5kcRGYUrvgqHw88"
        , "ChIJD3uTd9hx5kcR1IQvGfr8dbk"
        , "ChIJiQxv_05u5kcRESFIh6-QTvQ"
        , "ChIJUzCPuddv5kcRasGAnEUUWkU"
        , "ChIJATr1n-Fx5kcRjQb6q6cdQDY"
        , "ChIJK2Gs9DZu5kcRCHzHNohIAUQ"
        , "ChIJLU7jZClu5kcR4PcOOO6p3I0"
        , "ChIJjx37cOxv5kcRPWQuEW5ntdk"))

// Rome
Map d_rome = new HashMap()
destinations.add(d_rome)
d_rome.put("name", "Rome")
d_rome.put("highlight", false)
d_rome.put("latitude", "41.8286")
d_rome.put("longitude", "12.9254")
d_rome.put("country", "IT")
d_rome.put("main-pic", "/sites/digitall/files/images/slides/city-sunny-couple-love.jpg")
d_rome.put("hl_landmarks", Arrays.asList("ChIJIRbrOGZgLxMROSyE2uUHIHA"
        , "ChIJ782pg7NhLxMR5n3swAdAkfo"
        , "ChIJH-4j1LJhLxMR6IviSs42yJ0"
        , "ChIJrRMgU7ZhLxMRxAOFkC7I8Sg"
        , "ChIJ1UCDJ1NgLxMRtrsCzOHxdvY"))

// Montcuq
Map d_mcq = new HashMap()
destinations.add(d_mcq)
d_mcq.put("name", "Montcuq")
d_mcq.put("highlight", true)
d_mcq.put("latitude", "44.2826")
d_mcq.put("longitude", "1.6507")
d_mcq.put("country", "FR")
d_mcq.put("main-pic", "/sites/digitall/files/images/slides/green-landscape.jpg")
List o_mcq = new ArrayList()
d_mcq.put("outlines", o_mcq)
Map o = new HashMap(3)
o_mcq.add(o)
o.put("author", "Daniel Prévost")
o.put("date", 194541995L)
o.put("text", "Mais dites-moi, j’ai l’impression qu’on arrive dans une petite ruelle, j’ai l’impression que Montcuq est très étroit.")

String siteKey = siteKey
if (StringUtils.isNotBlank(siteKey)) {
    final String contentFolderPath = String.format("/sites/%s/contents", siteKey)
    final String folderPath = String.format("/sites/%s/contents/getaway", siteKey)
    reset(folderPath, logger)
    populate(contentFolderPath, folderPath, config, logger)
} else {
    logger.error("Sitekey undefined")
}

private populate(String contentFolderPath, String folderPath, Map config, logger) {
    final JCRSessionWrapper editSession = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, null)
    final JCRNodeWrapper folderNode = editSession.getNode(contentFolderPath).addNode("getaway", "jnt:contentFolder")
    final JCRNodeWrapper destinationsNode = folderNode.addNode("destinations", "jnt:contentFolder")
    final JCRNodeWrapper landmarksNode = folderNode.addNode("landmarks", "jnt:contentFolder")
    final JCRNodeWrapper outlinesNode = folderNode.addNode("outlines", "jnt:contentFolder")
    editSession.save()
    populateDestinations(destinationsNode, outlinesNode, config.get("destinations") as List, logger)
    JCRPublicationService.getInstance().publishByMainId(folderNode.getIdentifier())
}

private populateDestinations(JCRNodeWrapper folderNode, JCRNodeWrapper outlinesNode, List destinations, logger) {
    for (Map dest : destinations)
        addDestination(folderNode, outlinesNode, dest, logger)
    folderNode.getSession().save()
}

private addDestination(JCRNodeWrapper folderNode, JCRNodeWrapper outlinesNode, Map dest, logger) {
    String name = dest.get("name")
    def nodeName = JCRContentUtils.findAvailableNodeName(folderNode, JCRContentUtils.generateNodeName(name))
    JCRNodeWrapper node = folderNode.addNode(nodeName, "gant:destination")
    node.setProperty("destinationname", name)
    node.setProperty("highlight", dest.get("highlight"))
    node.setProperty("headline", generateLipsum(1))
    node.setProperty("description", generateLipsum())
    node.setProperty("facts", generateLipsum(2))
    node.addMixin("jmix:geotagged")
    node.setProperty("j:latitude", dest.get("latitude"))
    node.setProperty("j:longitude", dest.get("longitude"))
    node.setProperty("country", dest.get("country"))
    node.setProperty("photos", [folderNode.getSession().getNode(dest.get("main-pic")).getIdentifier()] as String[])
    if (dest.containsKey("hl_landmarks")) {
        ArrayList landmarks = new ArrayList<Value>()
        for (String placeID : dest.get("hl_landmarks") as List)
            landmarks.add(JCRValueFactoryImpl.getInstance().createValue(placeID, PropertyType.STRING))
        if (!landmarks.isEmpty()) node.setProperty("landmarks", landmarks.toArray() as Value[])
    }
    if (dest.containsKey("outlines")) {
        ArrayList outlines = new ArrayList<Value>()
        for (Map outline : dest.get("outlines") as List) {
            JCRNodeWrapper outlineNode = createOutline(outlinesNode, outline, logger)
            outlines.add(JCRValueFactoryImpl.getInstance().createValue(outlineNode.getIdentifier(), PropertyType.WEAKREFERENCE))
        }
        if (!outlines.isEmpty()) node.setProperty("outlines", outlines.toArray() as Value[])
    }
}

private populateLandmarks(JCRNodeWrapper folderNode, List landmarks, logger) {

}

private JCRNodeWrapper createOutline(JCRNodeWrapper folderNode, Map outline, logger) {
    String text = outline.get("text")
    String nodeName = JCRContentUtils.findAvailableNodeName(folderNode, JCRContentUtils.generateNodeName(text))
    JCRNodeWrapper node = folderNode.addNode(nodeName, "gant:outline")
    node.setProperty("text", text)
    node.setProperty("author", outline.get("author"))
    GregorianCalendar calendar = new GregorianCalendar()
    calendar.setTime(new Date(outline.get("date")))
    node.setProperty("date", calendar)
    return node
}

private reset(String folderPath, logger) {
    final JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null)
    if (liveSession.nodeExists(folderPath)) {
        liveSession.getNode(folderPath).remove()
        liveSession.save()
        logger.info("Deleted Getaway content from the live workspace")
    }
    final JCRSessionWrapper editSession = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, null, null)
    if (editSession.nodeExists(folderPath)) {
        editSession.getNode(folderPath).remove()
        editSession.save()
        logger.info("Deleted Getaway content from the default workspace")
    }
}

private String generateLipsum() { generateLipsum("paras", 5, true) }

private String generateLipsum(int count) { generateLipsum("paras", count, true) }

private String generateLipsum(int count, boolean start) { generateLipsum("paras", count, start) }

/**
 * Use the lipsum generator to generate Lorem Ipsum dummy paragraphs / words / bytes.
 *
 * Lorem Ipsum courtesy of www.lipsum.com by James Wilson
 *
 * @param what in ['paras','words','bytes'], default: 'paras'
 * @param amount of paras/words/bytes, default: 2 (for words minimum is 5, for bytes it is 27)
 * @param start always start with 'Lorem Ipsum', default = true
 * */
private String generateLipsum(String what, int amount, boolean start) {


    def address = "https://www.lipsum.com/feed/xml?what=$what&amount=$amount&start=${start ? 'yes' : 'no'}"
    String xml = new URL(address).text

    try {
        def feed = new XmlSlurper().parseText(xml)

        return feed.lipsum.text()
    } catch (SAXParseException e) {
        logger.error("Error when generating Lorem Ipsum from " + address + " ,response: " + xml, e)
        return "Lorem Ipsum - Failed to generate more"
    }

}