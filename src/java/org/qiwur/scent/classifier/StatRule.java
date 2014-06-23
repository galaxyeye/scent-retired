package org.qiwur.scent.classifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.Validate;
import org.qiwur.scent.classifier.statistics.StatIndicator;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;

public class StatRule extends ScentRule {

  private final StatIndicator indicator;
  private final double min;
  private final double max;
  private final double nag_score;
  private Map<String, String> references = new HashMap<String, String>();

  public StatRule(StatIndicator indicator, double min, double max, double score, double nag_score) {
    super(String.format("%s-%d-%d", indicator.name(), (int)min, (int)max), score);

    this.indicator = indicator;
    this.min = min < MIN_SCORE ? MIN_SCORE : min;
    this.max = max >  MAX_SCORE ?  MAX_SCORE : max;
    this.nag_score = nag_score;

    var("$_result", "0.0");
  }

  public void ref(String ref, String var) {
    references.put(ref, var);
  }

  public double getScore(Element ele) {
    if (ele == null) return 0.0;

    double value = 0.0;

    if (indicator.isSimple()) {
      value = ele.indic(indicator.name());
    }
    else {
      value = eval(ele, indicator.script());
    }

    double result = nag_score;
    if (value >= min && value <= max) {
      result = score();
    }

    return result;
  }

  private double eval(Element ele, String script) {
    Validate.notNull(ele);

    double result = 0.0;

    // TODO : use engine cache to enhance performance
    ScriptEngineManager factory = new ScriptEngineManager();
    ScriptEngine engine = factory.getEngineByName("JavaScript");

    // inject variables
    for (Entry<String, String> entry : variables().entrySet()) {
      engine.put(entry.getKey(), entry.getValue());
    }

    for (String indicator : Indicator.names) {
      engine.put(variablize(indicator), ele.indic(indicator));
    }

    // reference variables : $_1, $_2, $_3, ... 
    for (Entry<String, String> entry : references.entrySet()) {
      String var = variablize(entry.getValue());
      engine.put(entry.getKey(), engine.get(var));
    }

    try {
      engine.eval(script);
      result = Double.parseDouble(engine.get("$_result").toString());
    }
    catch (ScriptException e) {
      logger.error(e);
    }
    catch (Exception e) {
      logger.error(e);
    }

    return result;
  }

  private String variablize(String indicator) {
    return "$" + indicator.replaceAll("-", "_");
  }

  @Override
  public String toString() {
    String report = String.format("\n\nmin : %6.2f max : %6.2f score : %6.2f -score : %6.2f\n" 
          + "\n\nindicators : %s"
          + "\n\nvariables : %s"
          + "\n\nreferences : %s",
        min,
        max,
        score(),
        nag_score,
        indicator.toString(),
        variables().toString(),
        references.toString()
    );

    return report;
  }
}
