package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public class FileUtil {
  public static String getFileNameForPage(String url) {
    String file = DigestUtils.md5Hex(url) + ".html";

    return file;
  }

  public static File createTempFileForPage(String url, String directory) throws IOException {
    StringBuilder sb = new StringBuilder();

    sb.append(FileUtils.getTempDirectoryPath());
    sb.append(File.separator);
    if (directory != null) {
      sb.append(directory);
      sb.append(File.separator);
    }

    File file = new File(sb.toString());

    if (!file.exists()) {
      FileUtils.forceMkdir(file);
    }

    sb.append(getFileNameForPage(url));
    file = new File(sb.toString());

    if (!file.exists()) {
      file.createNewFile();
    }

    return file;
  }

  public static void truncateFile(String file) throws FileNotFoundException, IOException {
    new FileOutputStream(file, false).close();
  }

  public static void main(String[] args) throws IOException {
    System.out.println(FileUtils.getTempDirectoryPath());
    System.out.println(File.separator);
    createTempFileForPage("http://baidu.com/", "web");
  }
}
