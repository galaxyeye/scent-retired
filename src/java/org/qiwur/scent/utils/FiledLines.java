package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.google.common.io.Files;

public class FiledLines {

  protected static final Logger logger = LogManager.getLogger(FiledLines.class);

  private Comparator<String> wordsComparator = null;

  private Map<String, TreeMultiset<String>> filedLines = new HashMap<String, TreeMultiset<String>>();

  public FiledLines() {

  }

  public FiledLines(String... files) throws IOException {
    if (files.length > 0) load(files);
  }

  public FiledLines(Comparator<String> wordsComparator, String... files) throws IOException {
    this.wordsComparator = wordsComparator;
    if (files.length > 0) load(files);
  }

  public FiledLines(Comparator<String> wordsComparator) {
    this.wordsComparator = wordsComparator;
  }

  public Multiset<String> getLines(String file) {
    return filedLines.get(file);
  }

  public Multiset<String> firstFileLines() {
    if (filedLines.isEmpty())
      return TreeMultiset.create();

    return filedLines.values().iterator().next();
  }

  public boolean add(String file, String text) {
    Multiset<String> lines = getLines(file);

    if (lines == null) {
      return false;
    }

    return lines.add(text);
  }

  public boolean addAll(String file, Collection<String> texts) {
    Multiset<String> lines = getLines(file);

    if (lines == null) {
      return false;
    }

    return lines.addAll(texts);
  }

  public boolean remove(String file, String text) {
    Multiset<String> lines = getLines(file);

    if (lines == null) {
      return false;
    }

    return lines.remove(text);
  }

  public boolean contains(String file, String text) {
    Multiset<String> conf = getLines(file);
    if (conf != null)
      return conf.contains(text);

    return false;
  }

  public void load(String... files) throws IOException {
    if (files.length == 0) {
      logger.error("file not set");
    }

    // logger.info("load files : {}", Arrays.asList(files));

    for (String file : files) {
      if (file != null && file.length() > 0) {
        TreeMultiset<String> values = TreeMultiset.create();
  
        if (wordsComparator != null) {
          values = TreeMultiset.create(wordsComparator);
        }
  
        List<String> lines = Files.readLines(new File(file), Charsets.UTF_8);

        for (String line : lines) {
          line = line.trim();
          if (!line.startsWith("#")) {
            values.add(line);
          }
        }

        // logger.debug("load file {} with lines : \n{}", file, values);

        filedLines.put(file, values);
      }
      else {
        logger.error("bad file name");
      }
    }
  }

  public void save(String file) throws IOException {
    PrintWriter pw = new PrintWriter(new FileWriter(file));

    for (String line : filedLines.get(file).elementSet()) {
      pw.println(line);
    }

    pw.close();
  }

  public void saveAll() throws IOException {
    for (String file : filedLines.keySet()) {
      save(file);
    }
  }
}
