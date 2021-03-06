package org.qiwur.scent.configuration;

import static org.junit.Assert.*;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.qiwur.scent.utils.ScentConfiguration;

public class TestConfiguration {

  @Test
  public void testConfiguration() {
    Configuration conf = ScentConfiguration.create();
    // assertTrue(ClassLoader.getSystemResource("scent-site.xml").toString().contains("scent/conf/scent-site.xml"));

    assertEquals(conf.get("scent.server.wwwroot"), "wwwroot");
    assertFalse(conf.get("scent.conf.domain.name").isEmpty());
  }
}
