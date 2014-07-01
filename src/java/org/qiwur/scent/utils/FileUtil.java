package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

public class FileUtil {
<<<<<<< HEAD
=======
  public static String getFileNameFromUri(String uri) {
    String file = DigestUtils.md5Hex(uri);
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

  public static String getFileNameFromUri(String uri) {
    return getFileNameFromUri(uri, null);
  }

<<<<<<< HEAD
  public static String getFileNameFromUri(String uri, String suffix) {
    String file = DigestUtils.md5Hex(uri);

    if (StringUtils.isNotEmpty(suffix)) {
      file += ".";
      file += suffix;
    }

    return file;
  }

=======
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
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

<<<<<<< HEAD
  public static String getFileForPage(String uri, String baseDir, String suffix) throws IOException {
    return getDirForPage(uri, baseDir) + File.separator + getFileNameFromUri(uri, suffix);
  }

  public static File createFileForPage(String uri, String baseDir, String suffix) throws IOException {
    File file = new File(getFileForPage(uri, baseDir, suffix));
=======
  public static String getFileForPage(String uri, String baseDir) throws IOException {
    return getDirForPage(uri, baseDir) + File.separator + getFileNameFromUri(uri);
  }

  public static File createFileForPage(String uri, String directory) throws IOException {
    File file = new File(getFileForPage(uri, directory));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

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
<<<<<<< HEAD
    getFileForPage("http://baidu.com/", "web", "html");
=======
    getFileForPage("http://baidu.com/", "web");
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
  }
}
