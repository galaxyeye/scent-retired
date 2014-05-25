package org.qiwur.scent.data.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WebsiteFactory {

  final static Logger logger = LogManager.getLogger(WebsiteFactory.class);

  public static final String configFile = "conf/known-websites.xml";
  public static final String learningFile = "conf/websites.learning.txt";

  // maintains all websites
  ArrayList<Website> websites = new ArrayList<Website>();
  // an index to websites
  Map<String, Website> domain2websites = new HashMap<String, Website>();
  // maintains all learned websites
  ArrayList<Website> learnedWebsites = new ArrayList<Website>();

  private static WebsiteFactory instance = null;

  public static WebsiteFactory getInstance() {
    if (instance == null) {
      instance = new WebsiteFactory();

      try {
        instance.load();
      } catch (IOException | ParserConfigurationException | SAXException e) {
        e.printStackTrace();
      }
    }

    return instance;
  }

  WebsiteFactory() {
  }

  public String configFile() {
    return configFile;
  }

  public Website get(String domain) {
    return domain2websites.get(domain);
  }

  public String getName(String domain) {
    Website w = domain2websites.get(domain);

    if (w != null)
      return w.getName();

    return "";
  }

  private void load(String file) throws IOException, ParserConfigurationException, SAXException {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = db.parse(new File(configFile));

    parse(doc);

    rebuild();
  }

  private void load() throws IOException, ParserConfigurationException, SAXException {
    if (configFile != null) {
      load(configFile);
    }
  }

  private void rebuild() {
    for (Website website : websites) {
      domain2websites.put(website.getDomain(), website);
    }
  }

  private void parse(Document doc) {
    Node rootNode = doc.getFirstChild();
    NodeList websiteNodes = rootNode.getChildNodes();

    for (int i = 0; i < websiteNodes.getLength(); ++i) {
      Node websiteNode = websiteNodes.item(i);

      if (websiteNode.getNodeType() != Node.ELEMENT_NODE || !websiteNode.getNodeName().equals("website")) {
        continue;
      }

      String domain = websiteNode.getAttributes().getNamedItem("domain").getNodeValue();
      String name = null;

      NodeList childNodes = websiteNode.getChildNodes();
      for (int j = 0; j < childNodes.getLength(); ++j) {
        Node childNode = childNodes.item(j);

        if (childNode.getNodeType() != Node.ELEMENT_NODE || !childNode.getNodeName().equals("name")) {
          continue;
        }

        name = childNode.getTextContent();
      }

      if (StringUtils.isNotEmpty(domain) && StringUtils.isNotEmpty(name)) {
        websites.add(new Website(domain, name));
      }
    }
  }

  public static void main(String[] args) {
    logger.debug("\n");
    logger.debug(getInstance().getName("yixun.com"));
    logger.debug(getInstance().websites.toString());
    logger.debug(getInstance().domain2websites.toString());
  }
}
