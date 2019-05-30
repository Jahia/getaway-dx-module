import org.apache.commons.lang.StringUtils
import org.jahia.api.Constants
import org.jahia.data.templates.JahiaTemplatesPackage
import org.jahia.osgi.BundleResource
import org.jahia.registries.ServicesRegistry
import org.jahia.services.content.JCRContentUtils
import org.jahia.services.content.JCRNodeWrapper
import org.jahia.services.content.JCRPublicationService
import org.jahia.services.content.JCRSessionFactory
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRValueFactoryImpl
import org.jdom.Document
import org.jdom.Element
import org.jdom.Namespace
import org.jdom.input.SAXBuilder
import org.osgi.framework.Bundle
import org.xml.sax.SAXParseException

import javax.jcr.PropertyType
import javax.jcr.Value

def logger = log

String siteKey = siteKey
if (StringUtils.isNotBlank(siteKey)) {
    final String contentFolderPath = String.format("/sites/%s/contents", siteKey)
    final String folderPath = String.format("/sites/%s/contents/getaway", siteKey)
    reset(folderPath, logger)
    populate(contentFolderPath, folderPath, logger)
} else {
    logger.error("Sitekey undefined")
}

private Document loadXml() {
    final JahiaTemplatesPackage currentModule = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById("getaway-dx-module")
    if (currentModule == null) return null
    final Bundle bundle = currentModule.getBundle()
    if (bundle == null) return null
    final URL xmlFile = bundle.getEntry("META-INF/groovyConsole/populate.xml")
    if (xmlFile == null) return null
    final BundleResource bundleResource = new BundleResource(xmlFile, bundle)

    final SAXBuilder saxBuilder = new SAXBuilder();
    saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    return saxBuilder.build(bundleResource.getInputStream());
}

private populate(String contentFolderPath, String folderPath, logger) {
    Document document = loadXml()
    if (document == null) {
        logger.error("Impossible to load the XML file")
        return
    }
    final JCRSessionWrapper editSession = JCRSessionFactory.getInstance().getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, null)
    final JCRNodeWrapper folderNode = editSession.getNode(contentFolderPath).addNode("getaway", "jnt:contentFolder")
	folderNode.addMixin("gamix:appURL");
    folderNode.setProperty("liveURL","http://localhost:3000")
    final JCRNodeWrapper destinationsNode = folderNode.addNode("destinations", "jnt:contentFolder")
    editSession.save()
    populateDestinations(destinationsNode, document.rootElement.getChildren("destination", document.getRootElement().getNamespace()), logger)
    JCRPublicationService.getInstance().publishByMainId(folderNode.getIdentifier())
}

private populateDestinations(JCRNodeWrapper folderNode, List<Element> destinations, logger) {
    for (Element dest : destinations)
        addDestination(folderNode, dest, logger)
    folderNode.getSession().save()
}

private addDestination(JCRNodeWrapper folderNode, Element dest, logger) {
    final Namespace ns = dest.getNamespace()
    String name = dest.getAttributeValue("name")
    def nodeName = JCRContentUtils.findAvailableNodeName(folderNode, JCRContentUtils.generateNodeName(name))
    JCRNodeWrapper node = folderNode.addNode(nodeName, "gant:destination")
    node.setProperty("destinationname", name)
    node.setProperty("country", dest.getAttributeValue("country"))
    node.setProperty("highlight", Boolean.parseBoolean(dest.getAttributeValue("highlight")))
    final Element mainPic = dest.getChild("main-pic", ns)
    if (mainPic != null) {
        def mainPicPath = mainPic.getTextTrim()
        if(mainPicPath.startsWith('/sites')) {
            node.setProperty("photos", [folderNode.getSession().getNode(mainPicPath).getIdentifier()] as String[])
        } else {
            def imagePath = mainPicPath.substring(mainPicPath.indexOf("/files/images/"));
            def destImagPath = folderNode.getResolveSite().getPath() + imagePath
            if(!folderNode.getSession().nodeExists(destImagPath)) {
                folderNode.getSession().getWorkspace().copy(mainPicPath, destImagPath)
            }
            node.setProperty("photos", [folderNode.getSession().getNode(destImagPath).getIdentifier()] as String[])
        }
    }
    final Element headline = dest.getChild("headline", ns)
    if (headline != null)
        node.setProperty("headline", headline.getTextTrim())
    else node.setProperty("headline", generateLipsum("words", 10, false))
    final Element outline = dest.getChild("outline", ns)
    if (outline != null)
        node.setProperty("outline", outline.getTextTrim())
    else node.setProperty("outline", generateLipsum())
    final Element landmarks = dest.getChild("landmarks", ns)
    if (landmarks != null) {
        ArrayList values = new ArrayList<Value>()
        for (Element lm : (landmarks.getChildren() as List<Element>))
            values.add(JCRValueFactoryImpl.getInstance().createValue(lm.getTextTrim(), PropertyType.STRING))
        if (!values.isEmpty()) node.setProperty("landmarks", values.toArray() as Value[])
    }
    final Element infos = dest.getChild("infos", ns)
    if (infos != null) {
        final String area = infos.getAttributeValue("area")
        if (StringUtils.isNotBlank(area)) node.setProperty("area", Double.parseDouble(area))
        final String elevation = infos.getAttributeValue("elevation")
        if (StringUtils.isNotBlank(elevation)) node.setProperty("elevation", Long.parseLong(elevation))
        final Element geoPos = infos.getChild("geoPos", ns)
        if (geoPos != null) {
            node.addMixin("jmix:geotagged")
            node.setProperty("j:latitude", geoPos.getAttributeValue("latitude"))
            node.setProperty("j:longitude", geoPos.getAttributeValue("longitude"))
        }
        final Element population = infos.getChild("population", ns)
        if (population != null) {
            node.setProperty("populationCount", Integer.parseInt(population.getAttributeValue("count")))
            final String year = population.getAttributeValue("year")
            if (StringUtils.isNotBlank(year)) node.setProperty("populationDate", Integer.parseInt(year))
        }
    }
}

private populateLandmarks(JCRNodeWrapper folderNode, List landmarks, logger) {

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