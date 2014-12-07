package org.qiwur.scent.data.feature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.classifier.BlockRule;
import org.qiwur.scent.classifier.PhraseRule;
import org.qiwur.scent.classifier.ScentRule;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.parser.Parser;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

class PhraseFeatureParser {

	final Logger logger = LogManager.getLogger(PhraseFeatureParser.class);

	final String[] featureFiles;
  final Queue<String> imports = Queues.newArrayDeque();
  final Set<String> processedImports = Sets.newHashSet();
  final Multimap<BlockRule, String> includes = TreeMultimap.create();
  final Multimap<BlockRule, String> excludes = TreeMultimap.create();

  final Multimap<String, BlockRule> blockRules = TreeMultimap.create();

	public PhraseFeatureParser(String... featureFiles) {
    this.featureFiles = featureFiles;
	}

  public Multimap<String, BlockRule> parse() {
    for (String featureFile : featureFiles) {
      Validate.notEmpty(featureFile);

      File file = new File(featureFile);
      if (file.exists()) {
        imports.add(getImportString(file.getAbsolutePath(), ""));
      }
      else {
        logger.error("file not exsits : {}", featureFile);
      }
    }

    while(!imports.isEmpty()) {
      String importString = imports.remove();
      ArrayList<String> files = parseImportString(importString);

      try {
        Document doc = Jsoup.parse(new FileInputStream(files.get(0)), "utf-8", files.get(0), Parser.xmlParser());

        parse(doc);
      } catch (FileNotFoundException e) {
        logger.error(e);
      } catch (IOException e) {
        logger.error(e);
      } 
      finally {
        processedImports.add(importString);
      }
    }

    return blockRules;
  }

  public String report() {
    StringBuilder sb = new StringBuilder();

    sb.append("imported files : \n");
    for (String file : processedImports) {
      sb.append("\t");
      sb.append(parseImportString(file).get(0));
      sb.append("\n");
    }

    sb.append("\n");
    sb.append("rules report : \n");

    for (Entry<String, BlockRule> rule : blockRules.entries()) {
      sb.append("\t");
      sb.append(rule.getKey());
      sb.append(":");
      sb.append(rule.getValue().getRules().size());
      sb.append("\n");
    }

    return sb.toString();
  }

  private void parse(Document doc) {
    // parse elements without file dependency
    parseStep1(doc);
    // parse elements with file dependency
    parseStep2();
  }

  private String getImportString(String fromFile, String toFile) {
    final String format = "%" + Integer.SIZE + "d%s%s";
    return String.format(format, fromFile.length(), fromFile, toFile);
  }

  private ArrayList<String> parseImportString(String importString) {
    int toFileLength = Integer.parseInt(importString.substring(0, Integer.SIZE).trim());
    int pos = Integer.SIZE + toFileLength;

//    System.out.printf("%d, %d, %d\n", Integer.SIZE, toFileLength, pos);

    ArrayList<String> output = Lists.newArrayList();
    output.add(importString.substring(Integer.SIZE, pos));
    output.add(importString.substring(pos));

    return output;
  }

  private void parseImport(Element eleImport) {
    // find file in the same dir
    String baseDir = StringUtils.substring(eleImport.baseUri(), 0, StringUtils.lastIndexOf(eleImport.baseUri(), File.separator));
    String path = baseDir + File.separator + eleImport.attr("file");

    File file = new File(path);

    if (!file.exists()) {
      logger.error("file not exsits : {}, {}", eleImport.attr("file"), file);
      return;
    }

    // logger.debug("import feature file {}", file.getAbsoluteFile());

    if (!processedImports.contains(file)) {
      imports.add(getImportString(file.getAbsolutePath(), eleImport.baseUri()));
    }
  }

  private String getBlockRuleId(Element eleBlock) {
    String label = eleBlock.attr("label");
    return eleBlock.baseUri() + "/" + label + "#" + eleBlock.attr("id");
  }

  private void parseStep1(Document doc) {
    for (Element eleImport : doc.select("block-features import")) {
      parseImport(eleImport);
    }

    for (Element eleBlock : doc.select("block-features block")) {
      String label = eleBlock.attr("label");
      String id = getBlockRuleId(eleBlock);
      String leafOnly = eleBlock.attr("leaf-only");
      String domain = eleBlock.attr("domain");
      BlockRule blockRule = new BlockRule(id, label, leafOnly.equals("true"), domain);

//      if (blockRules.containsEntry(label, blockRule)) {
//        logger.debug("block rule {} is already exist!", id);
//        continue;
//      }

      for (Element eleInc : doc.select("block-features include-block")) {
        includes.put(blockRule, eleInc.attr("label"));
      }

      for (Element eleExc : doc.select("block-features exclude-block")) {
        excludes.put(blockRule, eleExc.attr("label"));
      }

      for (Element elePhrase : eleBlock.getElementsByTag("phrase")) {
        // since the name can be an css id or class, we humanize it
        String name = StringUtil.humanize(elePhrase.attr("name"));
        double score = StringUtil.tryParseDouble(elePhrase.attr("score"));

        if (!name.isEmpty() && score != 0) {
          blockRule.addRule(new PhraseRule(name, score));
        }
      }

      // logger.debug("parse block rule {}, rules : {}", id, blockRule.getRules().size());

      blockRules.put(label, blockRule);
    }
  }

  private void parseStep2() {
    Multimap<BlockRule, PhraseRule> addons = LinkedListMultimap.create();

    for (Entry<BlockRule, String> includeBlock : includes.entries()) {
      BlockRule toRule = includeBlock.getKey();
      String fromLabel = includeBlock.getValue();

      for (BlockRule from : blockRules.get(fromLabel)) {
        for (ScentRule rule : from.getRules()) {
          PhraseRule r = (PhraseRule)rule;
          
          if (r.score() > 0) {
            addons.put(toRule, new PhraseRule(r.phrase(), r.score()));
          }
        }
      }
    }

    for (Entry<BlockRule, String> excludeBlock : excludes.entries()) {
      String fromLabel = excludeBlock.getValue();
      BlockRule toRule = excludeBlock.getKey();

      for (BlockRule from : blockRules.get(fromLabel)) {
        for (ScentRule rule : from.getRules()) {
          PhraseRule r = (PhraseRule)rule;

          // the factor -1.0 is the only difference between include and exclude
          if (r.score() > 0) {
            addons.put(toRule, new PhraseRule(r.phrase(), -1.0 * r.score()));
          }
        }
      }
    }

    for (Entry<BlockRule, PhraseRule> entries : addons.entries()) {
      entries.getKey().addRule(entries.getValue());
    }
  }
}
