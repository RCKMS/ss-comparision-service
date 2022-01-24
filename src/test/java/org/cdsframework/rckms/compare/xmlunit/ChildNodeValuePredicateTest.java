package org.cdsframework.rckms.compare.xmlunit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.util.Predicate;

public class ChildNodeValuePredicateTest
{
  @Test
  public void testWithNoMatchingChild() throws Exception
  {
    String xml = "<root/>";
    Document doc = toDoc(xml);
    Element root = doc.getDocumentElement();

    ChildNodeValuePredicate<Node> predicate =
        ChildNodeValuePredicate.forParent("root").withChildElement("child", "value");
    assertFalse(predicate.test(root));
  }

  @Test
  public void testWithMatchingChildElement() throws Exception
  {
    String xml = "<root><child1><child2/></child1></root>";
    Document doc = toDoc(xml);
    Element root = doc.getDocumentElement();

    ChildNodeValuePredicate<Node> predicate =
        ChildNodeValuePredicate.forParent("root").withChildElement("child1", "value");
    assertFalse(predicate.test(root));

    xml = "<root><child1>test<child2/></child1></root>";
    doc = toDoc(xml);
    root = doc.getDocumentElement();

    predicate = ChildNodeValuePredicate.forParent("root").withChildElement("child1", "value");
    assertFalse(predicate.test(root));

    xml = "<root><child1>value<child2/></child1></root>";
    doc = toDoc(xml);
    root = doc.getDocumentElement();

    predicate = ChildNodeValuePredicate.forParent("root").withChildElement("child1", "value");
    assertTrue(predicate.test(root));
  }

  @Test
  public void testAttribute() throws Exception
  {
    String xml = "<root xmlns=\"org.cdsframework.rckms.output\" attr=\"foo\"/>";
    Document doc = toDoc(xml);
    Element root = doc.getDocumentElement();

    Predicate<Node> predicate =
        ChildNodeValuePredicate.forParent("root").withChildAttribute("attr", "foo");
    assertTrue(predicate.test(root));

    predicate = ChildNodeValuePredicate.forParent("root").withChildAttribute("attrXXX", "foo");
    assertFalse(predicate.test(root));

  }

  static Document toDoc(String xml) throws Exception
  {
    DocumentBuilderFactory dbFactory =
        DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
    return doc;
  }
}
