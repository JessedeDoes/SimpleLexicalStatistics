package fuzzy;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class EvaluationReport
{
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();
    Document document;

    XPathExpression simpleErrorXPath;
    XPathExpression truthPath;
    XPathExpression ocrPath;

    public class Error
    {
       String truth;
       String ocr;
       boolean inDictionary;
       public Error(String o, String t, boolean inD)
       {
          truth=t; ocr=o; inDictionary=inD;
       } 
       public boolean equals(Error other)
       {
          return ocr.equals(other.ocr) && truth.equals(other.truth);
       }
    }

    List<Error> errors = new ArrayList<Error>();
    Map<String,Set<Error>> errorMap = new HashMap<String,Set<Error>>();
    	  
    public void addError(Error e)
    {
       errors.add(e);

       Set<Error> errorSet = errorMap.get(e.ocr);
       if (errorSet == null)
       {
          errorSet = new HashSet<Error>();
          errorMap.put(e.ocr,errorSet);
       }
       errorSet.add(e);
    }


    public boolean hasError(String s)
    {
       return errorMap.containsKey(s);
    } 
    public EvaluationReport(String fileName)
    {
               try
                {
                        document =   ParseUtils.parse(fileName);
                        System.err.println("parsed " + fileName);
                        simpleErrorXPath = xpath.compile("//error[count(./ocr)=1 and count(./truth)=1]");
                        truthPath = xpath.compile("./truth/tokenizedText/text()");
                        ocrPath = xpath.compile("./ocr/tokenizedText/text()");
			NodeList l = (NodeList) simpleErrorXPath.evaluate(document, XPathConstants.NODESET);
                        System.err.println("# Simple errors: "  + l.getLength());
                        for (int i=0; i < l.getLength(); i++)
                        {
                            Element error = (Element) l.item(i);
                            List<Element> truth = ParseUtils.getElementsByTagname(error,"truth", false);
                            List<Element> ocr = ParseUtils.getElementsByTagname(error,"ocr", false);
                            try 
                            {
                              String t = ParseUtils.getElementContent(truth.get(0), "tokenizedText").toLowerCase();
                              String o = ParseUtils.getElementContent(ocr.get(0), "tokenizedText").toLowerCase();
			      String oInD = ocr.get(0).getAttribute("inDictionary");
                              if (t.length() > 0 && o.length() > 0)
                              {
                                 Error er = new Error(o,t,"true".equals(oInD));
                                 addError(er);
                              }
                            } catch (Exception e)
                            {
                            }
                        }
                        
                } catch (Exception e)
                {
                        e.printStackTrace();
                        //System.exit(1);
                }

    }

	public void printErrors()
	{
           int k=1;
           for (Error e: errors)
 	   {
               System.out.println(k++ + "\t" + e.ocr + "\t" + e.truth + "\t" + e.inDictionary);
           }
	}

        public List<String> getStrings(Object d, XPathExpression e)
        {
                List<String> strings = new ArrayList<String>();
                try
                {
                        NodeList l = (NodeList) e.evaluate(d,XPathConstants.NODESET);
                        for (int i=0; i < l.getLength(); i++)
                                strings.add(l.item(i).getNodeValue());
                } catch (XPathExpressionException e1)
                {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                }
                return strings;
        }

    public static void main(String[] args) 
    {
       EvaluationReport e = new EvaluationReport(args[0]);
       e.printErrors();
    }
}
