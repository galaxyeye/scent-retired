package org.qiwur.scent.jsoup.parser;

import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.utils.DescendableLinkedList;
import org.qiwur.scent.utils.Validate;

/**
 * @author Jonathan Hedley
 */
abstract class TreeBuilder {

  CharacterReader reader;
  Tokeniser tokeniser;
  protected Document doc; // current doc we are building into
  protected DescendableLinkedList<Element> stack; // the stack of open elements
  protected String baseUri; // current base uri, for creating new elements
  protected Token currentToken; // currentToken is used only for error tracking.
  protected ParseErrorList errors; // null when not tracking errors
  protected boolean ignoreScript = true; // do not create data node for script
                                         // if true

  public void ignoreScript(boolean ignore) {
    ignoreScript = ignore;
  }

  public boolean ignoreScript() {
    return ignoreScript;
  }

  protected void initialiseParse(String input, String baseUri, ParseErrorList errors) {
    Validate.notNull(input, "String input must not be null");
    Validate.notNull(baseUri, "BaseURI must not be null");

    doc = new Document(baseUri);
    reader = new CharacterReader(input);
    this.errors = errors;
    tokeniser = new Tokeniser(reader, errors);
    stack = new DescendableLinkedList<Element>();
    this.baseUri = baseUri;
  }

  Document parse(String input, String baseUri) {
    return parse(input, baseUri, ParseErrorList.noTracking());
  }

  Document parse(String input, String baseUri, ParseErrorList errors) {
    initialiseParse(input, baseUri, errors);

    runParser();

    return doc;
  }

  protected void runParser() {
    while (true) {
      Token token = tokeniser.read();

      process(token);

      if (token.type == Token.TokenType.EOF)
        break;
    }
  }

  protected abstract boolean process(Token token);

  protected Element currentElement() {
    return stack.getLast();
  }
}
