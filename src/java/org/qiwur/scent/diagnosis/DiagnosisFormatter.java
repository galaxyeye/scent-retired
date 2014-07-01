package org.qiwur.scent.diagnosis;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;

public class DiagnosisFormatter implements Configurable {

  protected static final Logger logger = LogManager.getLogger(DiagnosisFormatter.class);

  private Configuration conf;
  private List<String> headers = Lists.newArrayList();
  private List<List<String>> rows = Lists.newArrayList();
  private String caption = this.getClass().getName();
  private String description = "";

  public DiagnosisFormatter(Configuration conf) {
    Validate.notNull(conf);
    this.conf = conf;
  }

  public void process() {
  }

  public String asTable() {
    StringBuilder table = new StringBuilder();
    table.append("<table class='table tablesorter'>");

    table.append("<caption>" + caption + "</caption>");

    if (!headers.isEmpty()) {
    String th = "<tr>";
      for (String header : headers) {
        th += String.format("<th>%s</th>", header);
      }
      th += "</tr>";
      table.append(th);
    }

    for (List<String> row : rows) {
      String r = "<tr>";

      for (int i = 0; i < row.size(); ++i) {
        if (i == 0) {
<<<<<<< HEAD
          r += String.format("<th title='%s'>%s</th>", row.get(i), StringUtils.substring(row.get(i), 0, 30));
        }
        else if (i == row.size() - 1) {
          r += String.format("<td>%s</td>", StringUtils.substring(row.get(i), 0, 1500));
        }
        else {
          r += String.format("<td title='%s'>%s</td>", row.get(i), StringUtils.substring(row.get(i), 0, 20));
=======
          r += String.format("<th>%s</th>", StringUtils.substring(row.get(0), 0, 20));
        }
        else {
          r += String.format("<td>%s</td>", row.get(i));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
        }
      }

      r += "</tr>";
      table.append(r);
    }

    table.append("</table>");

<<<<<<< HEAD
    if (StringUtils.isEmpty(description)) {
      description = String.format("total %s columns, %s rows", headers.size(), rows.size());
    }
    table.append("<p>" + description.replaceAll("\n", "<br />") + "</p>");
=======
    if (StringUtils.isNotEmpty(description)) {
      table.append("<p>" + description.replaceAll("\n", "<br />") + "</p>");
    }
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

    return table.toString();
  }

<<<<<<< HEAD
  public String caption() {
    return this.caption;
  }

=======
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
  protected void setCaption(String caption) {
    this.caption = caption;
  }

  protected void setDescription(String description) {
    this.description = description;
  }

<<<<<<< HEAD
  protected void buildHeader(String... columnValues) {
    buildHeader(Arrays.asList(columnValues));
  }

  protected void buildHeader(Collection<String> columnNames) {
    headers.addAll(columnNames);
  }

  protected void buildRow(String... columnValues) {
    buildRow(Arrays.asList(columnValues));
  }

  protected void buildRow(Collection<String> columnValues) {
    List<String> row = Lists.newArrayList();
=======
  protected void buildHeader(String name, String... columnValues) {
    buildHeader(name, Arrays.asList(columnValues));
  }

  protected void buildHeader(String name, Collection<String> columnNames) {
    headers.add(name);
    headers.addAll(columnNames);
  }

  protected void buildHeader(Collection<String> columnNames) {
    this.buildHeader("", columnNames);
  }

  protected void buildRow(String name, String... columnValues) {
    buildRow(name, Arrays.asList(columnValues));
  }

  protected void buildRow(String name, Collection<String> columnValues) {
    List<String> row = Lists.newArrayList();
    row.add(name);
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
    row.addAll(columnValues);
    rows.add(row);
  }

<<<<<<< HEAD
=======
  protected void buildRow(Collection<String> columnValues) {
    this.buildRow("", columnValues);
  }

>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }
}
