import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.jahia.api.Constants
import org.jahia.services.content.JCRContentUtils
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRPublicationService
import org.jahia.services.content.JCRSessionFactory
import org.jahia.services.content.JCRSessionWrapper
import org.xml.sax.SAXParseException

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
// NYC
Map d_nyc = new HashMap()
destinations.add(d_nyc)
d_nyc.put("name", "New York City")
d_nyc.put("highlight", true)
d_nyc.put("latitude", "40.6536")
d_nyc.put("longitude", "-73.5672")
d_nyc.put("country", "US")
d_nyc.put("main-pic", "/sites/digitall/files/images/backgrounds/road-street-desert-industry.jpg")

List landmarks = new ArrayList()
config.put("landmarks", landmarks)
List outlines = new ArrayList()
config.put("outlines", outlines)

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
    populateDestinations(destinationsNode, config.get("destinations") as List, logger)
    populateLandmarks(landmarksNode, config.get("landmarks") as List, logger)
    populateOutlines(outlinesNode, config.get("outlines") as List, logger)
    JCRPublicationService.getInstance().publishByMainId(folderNode.getIdentifier())
}

private populateDestinations(JCRNodeWrapper folderNode, List destinations, logger) {
    for (Map dest : destinations)
        addDestination(folderNode, dest, logger)
    folderNode.getSession().save()
}

private addDestination(JCRNodeWrapper folderNode, Map dest, logger) {
    String name = dest.get("name")
    def nodeName = JCRContentUtils.findAvailableNodeName(folderNode, JCRContentUtils.generateNodeName(name))
    JCRNodeWrapper node = folderNode.addNode(nodeName, "gant:destination")
    node.setProperty("name", name)
    node.setProperty("highlight", dest.get("highlight"))
    node.setProperty("headline", generateLipsum(1))
    node.setProperty("description", generateLipsum())
    node.setProperty("facts", generateLipsum(2))
    node.addMixin("jmix:geotagged")
    node.setProperty("j:latitude", dest.get("latitude"))
    node.setProperty("j:longitude", dest.get("longitude"))
    node.setProperty("country", dest.get("country"))
    node.setProperty("photos", [folderNode.getSession().getNode(dest.get("main-pic")).getIdentifier()] as String[])
}

private populateLandmarks(JCRNodeWrapper folderNode, List landmarks, logger) {

}

private populateOutlines(JCRNodeWrapper folderNode, List outlines, logger) {

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