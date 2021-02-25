package org.cdsframework.rckms.compare.xmlunit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NodeNamePredicateTest
{
  @Test
  public void testElement() throws Exception
  {
    String xml = "<root/>";
    Document doc = toDoc(xml);
    Element root = doc.getDocumentElement();

    NodeNamePredicate<Node> predicate = NodeNamePredicate.pathMatching("root");
    assertTrue(predicate.test(root));

    predicate = NodeNamePredicate.pathMatching("root").withAncestry("foo");
    assertFalse(predicate.test(root));
    predicate = NodeNamePredicate.pathMatching("foo/root");
    assertFalse(predicate.test(root));
  }

  @Test
  public void testChildElement() throws Exception
  {
    String xml = "<root><child1><child2/></child1></root>";
    Document doc = toDoc(xml);
    Element root = doc.getDocumentElement();
    Element child1 = (Element) root.getElementsByTagName("child1").item(0);
    Element child2 = (Element) child1.getElementsByTagName("child2").item(0);

    NodeNamePredicate<Node> predicate = NodeNamePredicate.pathMatching("child2");
    assertTrue(predicate.test(child2));

    predicate = NodeNamePredicate.pathMatching("child2").withAncestry("child1");
    assertTrue(predicate.test(child2));
    predicate = NodeNamePredicate.pathMatching("child1/child2");
    assertTrue(predicate.test(child2));

    predicate = NodeNamePredicate.pathMatching("child2").withAncestry("child1", "root");
    assertTrue(predicate.test(child2));
    predicate = NodeNamePredicate.pathMatching("root/child1/child2");
    assertTrue(predicate.test(child2));

    predicate = NodeNamePredicate.pathMatching("child2").withAncestry("root");
    assertFalse(predicate.test(child2));
    predicate = NodeNamePredicate.pathMatching("root/child2");
    assertFalse(predicate.test(child2));
  }

  @Test
  public void testTextNode() throws Exception
  {
    String xml = "<root><child1>TEST</child1></root>";
    Document doc = toDoc(xml);
    Element root = doc.getDocumentElement();
    Element child1 = (Element) root.getElementsByTagName("child1").item(0);
    Node textNode = child1.getChildNodes().item(0);

    NodeNamePredicate<Node> predicate = NodeNamePredicate.pathMatching("#text");
    assertTrue(predicate.test(textNode));

    predicate = NodeNamePredicate.pathMatching("#text").withAncestry("child1");
    assertTrue(predicate.test(textNode));
    predicate = NodeNamePredicate.pathMatching("child1/#text");
    assertTrue(predicate.test(textNode));

    predicate = NodeNamePredicate.pathMatching("#text").withAncestry("root");
    assertFalse(predicate.test(textNode));
    predicate = NodeNamePredicate.pathMatching("root/#text");
    assertFalse(predicate.test(textNode));
  }

  @Test
  public void testAttribute() throws Exception
  {
    String xml = "<root attr=\"foo\"/>";
    Document doc = toDoc(xml);
    Element root = doc.getDocumentElement();
    Node node = root.getAttributeNode("attr");

    NodeNamePredicate<Node> predicate = NodeNamePredicate.pathMatching("attr");
    assertTrue(predicate.test(node));

    predicate = NodeNamePredicate.pathMatching("attr").withAncestry("root");
    assertTrue(predicate.test(node));
    predicate = NodeNamePredicate.pathMatching("root/attr");
    assertTrue(predicate.test(node));
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
