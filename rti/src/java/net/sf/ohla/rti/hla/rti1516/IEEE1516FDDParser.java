/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516;

import java.io.IOException;
import java.net.URL;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import net.sf.ohla.rti.fdd.Dimension;
import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.RTIinternalError;

import hla.rti1516e.exceptions.InconsistentFDD;

public class IEEE1516FDDParser
{
  public static FDD parseFDD(URL url)
    throws ErrorReadingFDD, CouldNotOpenFDD, RTIinternalError
  {
    FDD fdd = new FDD(url.toString());
    try
    {
      SAXParserFactory.newInstance().newSAXParser().parse(url.openStream(), new Handler(fdd));
    }
    catch (IOException ioe)
    {
      throw new CouldNotOpenFDD(ioe.getMessage(), ioe);
    }
    catch (SAXException saxe)
    {
      if (saxe.getCause() instanceof ErrorReadingFDD)
      {
        throw (ErrorReadingFDD) saxe.getCause();
      }
      else
      {
        throw new ErrorReadingFDD(saxe.getMessage(), saxe);
      }
    }
    catch (ParserConfigurationException pce)
    {
      throw new RTIinternalError(pce.getMessage(), pce);
    }
    return fdd;
  }

  private static class Handler
    extends DefaultHandler
  {
    private final FDD fdd;

    private final StringBuilder content = new StringBuilder();

    private boolean saveContent;

    private Stack<ContentHandler> contentHandlerStack = new Stack<ContentHandler>();

    public Handler(FDD fdd)
    {
      this.fdd = fdd;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      if (contentHandlerStack.size() > 0)
      {
        contentHandlerStack.peek().startElement(uri, localName, qName, attributes);
      }
      else if (qName.equals("ObjectClass"))
      {
        contentHandlerStack.push(new ObjectClassHandler(attributes.getValue("name")));
      }
      else if (qName.equals("InteractionClass"))
      {
        contentHandlerStack.push(new InteractionClassHandler(attributes));
      }
      else if (qName.equals("Dimension"))
      {
        Dimension dimension = fdd.addDimension(attributes.getValue("name"));

        String upperBound = attributes.getValue("upperBound");
        if (upperBound != null)
        {
          dimension.setUpperBound(Long.parseLong(upperBound));
        }
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      if (contentHandlerStack.size() > 0)
      {
        contentHandlerStack.peek().endElement(uri, localName, qName);
      }
    }

    @Override
    public void characters(char[] ch, int start, int length)
      throws SAXException
    {
      if (saveContent)
      {
        content.append(ch, start, length);
      }
    }

    private void saveContent()
    {
      saveContent = true;
    }

    private String getContent()
    {
      saveContent = false;

      String content = this.content.toString();
      this.content.setLength(0);
      return content;
    }

    private Set<String> getDimensions(String s)
    {
      Set<String> dimensions;

      if (s == null)
      {
        dimensions = null;
      }
      else
      {
        dimensions = new HashSet<String>();

        for (String dimension : s.split(" "))
        {
          if (!dimension.equals("NA"))
          {
            dimensions.add(dimension);
          }
        }
      }

      return dimensions;
    }

    private abstract class ContentHandler
    {
      public abstract void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException;

      public abstract void endElement(String uri, String localName, String qName)
        throws SAXException;
    }

    private class ObjectClassHandler
      extends ContentHandler
    {
      private final ObjectClass objectClass;

      public ObjectClassHandler(String objectClassName)
        throws SAXException
      {
        this(objectClassName, null);
      }

      private ObjectClassHandler(String objectClassName, ObjectClass superObjectClass)
        throws SAXException
      {
        try
        {
          objectClass = fdd.addObjectClass(objectClassName, superObjectClass);
        }
        catch (InconsistentFDD ifdd)
        {
          throw new SAXException(ifdd);
        }
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
      {
        if (qName.equals("Attribute"))
        {
          try
          {
            fdd.addAttribute(objectClass, attributes.getValue("name"), getDimensions(attributes.getValue("dimensions")),
                             attributes.getValue("transportation"), attributes.getValue("order"));
          }
          catch (InconsistentFDD ifdd)
          {
            throw new SAXException(ifdd);
          }
        }
        else if (qName.equals("ObjectClass"))
        {
          contentHandlerStack.push(new ObjectClassHandler(attributes.getValue("name"), objectClass));
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName)
        throws SAXException
      {
        if (qName.equals("ObjectClass"))
        {
          contentHandlerStack.pop();
        }
      }
    }

    private class InteractionClassHandler
      extends ContentHandler
    {
      private final InteractionClass interactionClass;

      private InteractionClassHandler(Attributes attributes)
        throws SAXException
      {
        this(attributes, null);
      }

      private InteractionClassHandler(Attributes attributes, InteractionClass superInteractionClass)
        throws SAXException
      {
        try
        {
          interactionClass = fdd.addInteractionClass(
            attributes.getValue("name"), superInteractionClass, getDimensions(attributes.getValue("dimensions")),
            attributes.getValue("transportation"), attributes.getValue("order"));
        }
        catch (InconsistentFDD ifdd)
        {
          throw new SAXException(ifdd);
        }
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
      {
        if (qName.equals("Parameter"))
        {
          try
          {
            fdd.addParameter(interactionClass, attributes.getValue("name"));
          }
          catch (InconsistentFDD ifdd)
          {
            throw new SAXException(ifdd);
          }
        }
        else if (qName.equals("InteractionClass"))
        {
          contentHandlerStack.push(new InteractionClassHandler(attributes, interactionClass));
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName)
        throws SAXException
      {
        if (qName.equals("InteractionClass"))
        {
          contentHandlerStack.pop();
        }
      }
    }

    private class DimensionHandler
      extends ContentHandler
    {
      private Dimension dimension;

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
      {
        if (qName.equals("name"))
        {
          saveContent();
        }
        else if (qName.equals("upperBound"))
        {
          saveContent();
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName)
        throws SAXException
      {
        if (qName.equals("name"))
        {
          dimension = fdd.addDimension(getContent());
        }
        else if (qName.equals("upperBound"))
        {
          dimension.setUpperBound(Long.parseLong(getContent()));
        }
        else if (qName.equals("Dimension"))
        {
          contentHandlerStack.pop();
        }
      }
    }
  }
}
