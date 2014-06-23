package org.qiwur.scent.jsoup.nodes;

import java.util.Map;

import org.qiwur.scent.utils.Validate;

public class Indicator implements Map.Entry<String, Double>, Cloneable {
  // code structure feature
  public static final String CH   = "-char";        // chars
  public static final String CHM  = "-char-max";    // max chars
  public static final String CHA  = "-char-ave";    // ave chars
  public static final String OCH  = "-own-char";    // own chars
  public static final String TB   = "-txt-blk";     // text blocks
  public static final String OTB  = "-own-txt-blk"; // text blocks
  public static final String IMG  = "-img";         // images
  public static final String A    = "-a";           // links
  public static final String SIB  = "-sibling";     // siblings
  public static final String C    = "-child";       // children
  public static final String G    = "-grant-child"; // grand children
  public static final String D    = "-descend";     // descendants
  public static final String DEP  = "-dep";         // element depth
  public static final String SEP  = "-sep";         // separators
  public static final String SEQ  = "-seq";         // sequence

  // vision feature
  public static final String AMW    = "-a-max-w";     // max link width
  public static final String AAW    = "-a-ave-w";     // ave link width
  public static final String AMH    = "-a-max-h";     // max link height
  public static final String AAH    = "-a-ave-h";     // ave link height
  public static final String IMW    = "-img-max-w";   // max image width
  public static final String IAW    = "-img-ave-w";   // ave image width
  public static final String IMH    = "-img-max-h";   // max image height
  public static final String IAH    = "-img-ave-h";   // ave image height

  // temporary variables for internal usage
  public static final String ATW    = "--a-total-w";  // total link width
  public static final String ATH    = "--a-total-h";  // total link width
  public static final String ITW    = "--img-total-w";  // total link width
  public static final String ITH    = "--img-total-h";  // total link width

  public static final String[] names = {
    CH, CHM, CHA, OCH, 
    TB, OTB, IMG, A, SIB, C, G, D, DEP, SEP, SEQ,
    AMW, AAW, AMH, AAH,
    IMW, IAW, IMH, IAH
  };

  /*
   * affect -sep
   * can be modified in configuration step
   * 
   * */
  public static String[] separators = {":", "："};

  private String name;
  private double value;

  /**
   * Create a new indicator from unencoded (raw) name and value.
   * 
   * @param name
   *          indicator name
   * @param value
   *          indicator value
   * @see #createFromEncoded
   */
  public Indicator(String name, double value) {
    Validate.notEmpty(name);
    this.name = name.trim().toLowerCase();
    this.value = value;
  }

  /**
   * Get the indicator name.
   * 
   * @return the indicator name
   */
  public String getKey() {
    return name;
  }

  /**
   * Set the indicator name. Gets normalised as per the constructor method.
   * 
   * @param name
   *          the new name; must not be null
   */
  public void setKey(String name) {
    Validate.notEmpty(name);
    this.name = name.trim().toLowerCase();
  }

  /**
   * Get the indicator value.
   * 
   * @return the indicator value
   */
  public Double getValue() {
    return value;
  }

  /**
   * Set the indicator value.
   * 
   * @param value
   *          the new indicator value; must not be null
   */
  public Double setValue(Double value) {
    if (value == null) value = 0.0;

    double old = this.value;
    this.value = value;
    return old;
  }

  /**
   * Get the string representation of this indicator, implemented as
   * {@link #html()}.
   * 
   * @return string
   */
  public String toString() {
    return name + ":" + value;
  }

  public static Indicator create(String unencodedKey, Double value) {
    return new Indicator(unencodedKey, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Indicator))
      return false;

    Indicator indicator = (Indicator) o;

    if (name != null ? !name.equals(indicator.name) : indicator.name != null)
      return false;
    if (value != indicator.value)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + new Double(value).hashCode();
    return result;
  }

  @Override
  public Indicator clone() {
    try {
      return (Indicator) super.clone(); // only fields are immutable strings name
                                        // and value, so no more deep copy
                                        // required
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

}
