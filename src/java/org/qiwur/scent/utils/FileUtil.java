package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public class FileUtil {
  public static String getFileNameFromUri(String uri) {
    String file = DigestUtils.md5Hex(uri);

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

  public static String getFileForPage(String uri, String baseDir) throws IOException {
    return getDirForPage(uri, baseDir) + File.separator + getFileNameFromUri(uri);
  }

  public static File createFileForPage(String uri, String directory) throws IOException {
    File file = new File(getFileForPage(uri, directory));

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
    getFileForPage("http://baidu.com/", "web");
  }
}
