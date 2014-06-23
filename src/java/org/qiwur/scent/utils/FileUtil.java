package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class FileUtil {

  public static String getFileNameFromUri(String uri) {
    return getFileNameFromUri(uri, null);
  }

  public static String getFileNameFromUri(String uri, String suffix) {
    String file = DigestUtils.md5Hex(uri);

    if (StringUtils.isNotEmpty(suffix)) {
      file += ".";
      file += suffix;
    }

    return file;
  }

  public static File forceMkdir(String directory) throws IOException {
    File file = new File(directory);

    if (!file.exists()) {
      FileUtils.forceMkdir(file);
    }

    return file;
  }

  public static String getDirForPage(String uri, String baseDir) throws IOException {
    String dir = baseDir + File.separator + getFileNameFromUri(uri);
    return forceMkdir(dir).getAbsolutePath();
  }

  public static String getFileForPage(String uri, String baseDir, String suffix) throws IOException {
    return getDirForPage(uri, baseDir) + File.separator + getFileNameFromUri(uri, suffix);
  }

  public static File createFileForPage(String uri, String baseDir, String suffix) throws IOException {
    File file = new File(getFileForPage(uri, baseDir, suffix));

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
    getFileForPage("http://baidu.com/", "web", "html");
  }
}
