package org.qiwur.scent.diagnosis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.utils.FileUtil;

import com.google.common.collect.Lists;

public class ScentDiagnoser {

  protected static final Logger logger = LogManager.getLogger(ScentDiagnoser.class);

<<<<<<< HEAD
  private final Document doc;
  private final Configuration conf;
  private final String docTemplate = "wwwroot/template/diagnosis.template.html";
  private final String baseDir;
  private final String pageUri;
  private final List<DiagnosisFormatter> formatters = Lists.newArrayList();
=======
  private Document doc;
  private Configuration conf;
  private String docTemplate = "wwwroot/template/diagnosis.template.html";
  private String baseDir = "/tmp/web/diagnosis";
  private String pageUri;
  private List<DiagnosisFormatter> formatters = Lists.newArrayList();
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

  public ScentDiagnoser(Document doc, Configuration conf) {
    Validate.notNull(doc);
    Validate.notNull(conf);

    this.doc = doc;
    this.conf = conf;

<<<<<<< HEAD
    this.baseDir = conf.get("scent.web.cache.file.dir", "/tmp/web");
=======
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
    this.pageUri = doc.baseUri();
  }

  public void addFormatters(DiagnosisFormatter... formatters) {
    for (DiagnosisFormatter formatter : formatters) {
      addFormatter(formatter);
    }
  }

  public void addFormatter(DiagnosisFormatter formatter) {
    Validate.notNull(formatter);

    formatters.add(formatter);
  }

  public void diagnose() {
    for (DiagnosisFormatter formatter : formatters) {
      formatter.process();
    }

    try {
      Document diagnosisDoc = Jsoup.parse(new File(docTemplate), "utf-8", false);

      Element menu = diagnosisDoc.getElementById("diagosis-menu");
      menu.appendElement("ul");
      for (DiagnosisFormatter formatter : formatters) {
<<<<<<< HEAD
        menu.appendElement("li")
          .appendElement("a")
          .text(formatter.caption())
          .attr("href", "#" + formatter.caption());
=======
        menu.appendElement("li").appendElement("a")
          .text(formatter.getClass().getSimpleName())
          .attr("href", "#" + formatter.getClass().getName());
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
      }

      Element body = diagnosisDoc.getElementById("diagosis-body");
      for (DiagnosisFormatter formatter : formatters) {
<<<<<<< HEAD
        body.appendElement("div")
          .attr("class", "back-to-top")
          .appendElement("a").attr("name", formatter.caption())
          .appendElement("a")
          .text("⇡ 回顶部 ⇡")
          .attr("href", "#");

=======
        body.appendElement("a").attr("name", formatter.getClass().getName());
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
        body.append(formatter.asTable());
        body.append("<br /><br />");
      }

      write(diagnosisDoc.toString());
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private void write(String content) throws IOException {
<<<<<<< HEAD
    File file = new File(FileUtil.getFileForPage(pageUri, baseDir, "diag"));
=======
    File file = new File(FileUtil.getFileForPage(pageUri, baseDir));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
    FileUtils.write(file, content);
  }
}
