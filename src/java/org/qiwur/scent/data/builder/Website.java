package org.qiwur.scent.data.builder;

public class Website {
  String domain = null;
  String name = null;

  Website() {

  }

  public Website(String domain, String name) {
    this.domain = domain;
    this.name = name;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "domain : " + domain + ", name : " + name;
  }
}
