package org.qiwur.scent.feature;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.ObjectCache;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class FeatureManager {

  private static final Logger logger = LogManager.getLogger(FeatureManager.class);

//  public static Multimap<String, String> FeatureFiles = TreeMultimap.create();

  private Set<String> cacheIds = Sets.newHashSet();
  private final Configuration conf;

  private FeatureManager(Configuration conf) {
    this.conf = conf;
  }

  public static FeatureManager create(Configuration conf) {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = FeatureManager.class.getName();

    if (objectCache.getObject(cacheId) != null) {
      return (FeatureManager) objectCache.getObject(cacheId);
    }
    else {
      FeatureManager manager = new FeatureManager(conf);
      objectCache.setObject(cacheId, manager);
      return manager;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends WebFeature> T get(Configuration conf, Class<T> clazz, String... featureFile) {
    return (T) FeatureManager.create(conf).getFeature(clazz.getName(), featureFile);
  }

  public WebFeature getFeature(String clazzName, String... featureFile) {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = clazzName + Lists.newArrayList(featureFile).toString();

    if (objectCache.getObject(cacheId) != null) {
      return (WebFeature) objectCache.getObject(cacheId);
    } else {
      WebFeature feature = getFeatureObject(conf, clazzName, featureFile);
      objectCache.setObject(cacheId, feature);
      cacheIds.add(cacheId);
      return feature;
    }
  }

  public void resetAll() {
    ObjectCache objectCache = ObjectCache.get(conf);

    for (String cacheId : cacheIds) {
      WebFeature feature = (WebFeature) objectCache.getObject(cacheId);
      feature.reset();
    }
  }

  public void loadAll() {
    ObjectCache objectCache = ObjectCache.get(conf);

    for (String cacheId : cacheIds) {
      WebFeature feature = (WebFeature) objectCache.getObject(cacheId);
      feature.load();
    }
  }

  public void reloadAll() {
    ObjectCache objectCache = ObjectCache.get(conf);

    logger.debug("reload features : {}", cacheIds);
    for (String cacheId : cacheIds) {
      WebFeature feature = (WebFeature) objectCache.getObject(cacheId);
      feature.reload();
    }
  }

  protected WebFeature getFeatureObject(Configuration conf, String clazzName, String... featureFile) {
    WebFeature feature = null;

    try {
      Class<?> clazz = Class.forName(clazzName, false, this.getClass().getClassLoader());
      Constructor<?> constructor = clazz.getConstructor(new Class[] { Configuration.class, String[].class });
      feature = (WebFeature) constructor.newInstance(conf, featureFile);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
        | SecurityException | IllegalArgumentException | InvocationTargetException e) {
      logger.error(e);
    }

    return feature;
  }

  public static void main(String[] args) throws Exception {
    Class<?> clazz = Class.forName(HtmlTitleFeature.class.getName(), false, FeatureManager.class.getClassLoader());
    Constructor<?> constructor = clazz.getConstructor(new Class[] { Configuration.class, String[].class });

    WebFeature feature = (WebFeature) constructor.newInstance(null, "balabala");
  }
}
