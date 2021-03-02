package org.cdsframework.rckms.compare.xmlunit;

import java.util.function.BiPredicate;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.util.Nodes;

public class ElementPairing
{
  private static final BiPredicate<String, String> CASE_INSENSITIVE_EQUALITY = (s1, s2) -> s1.equalsIgnoreCase(s2);
  private static final BiPredicate<String, String> DEFAULT_EQUALITY = (s1, s2) -> s1.equals(s2);

  public static ElementSelector byNameAndAttribute(String attr)
  {
    return byNameAndAttribute(attr, true);
  }

  public static ElementSelector byNameAndAttribute(String attr, boolean isValueCaseSensitive)
  {
    BiPredicate<String, String> equalityChecker = isValueCaseSensitive ? DEFAULT_EQUALITY : CASE_INSENSITIVE_EQUALITY;
    return byNameAndAttribute(attr, equalityChecker);
  }

  public static ElementSelector byNameAndAttribute(String attr, BiPredicate<String, String> equalityChecker)
  {
    return ElementSelectors.and(
        ElementSelectors.byName,
        (control, variant) -> hasMatchingAttribute(control, variant, attr, equalityChecker)
    );
  }

  private static boolean hasMatchingAttribute(Element control, Element variant, String attr,
      BiPredicate<String, String> equalityChecker)
  {
    QName attrQName = new QName(attr);
    String controlAttr = Nodes.getAttributes(control).get(attrQName);
    if (controlAttr == null)
      return false;
    String variantAttr = Nodes.getAttributes(variant).get(attrQName);
    return variantAttr != null ? equalityChecker.test(controlAttr, variantAttr) : false;
  }

}
