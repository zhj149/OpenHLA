/*
 * Copyright (c) 2005-2010, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.hla.rti1516e;

import java.io.IOException;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import hla.rti1516e.exceptions.CouldNotOpenFDD;
import hla.rti1516e.exceptions.CouldNotOpenMIM;
import hla.rti1516e.exceptions.DesignatorIsHLAstandardMIM;
import hla.rti1516e.exceptions.ErrorReadingFDD;
import hla.rti1516e.exceptions.ErrorReadingMIM;
import hla.rti1516e.exceptions.InconsistentFDD;
import hla.rti1516e.exceptions.RTIinternalError;

public class IEEE1516eFDDParser
{
  public static List<FDD> parseFDDs(URL[] urls)
    throws InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, RTIinternalError
  {
    List<FDD> additionalFDDs;
    if (urls == null || urls.length == 0)
    {
      additionalFDDs = null;
    }
    else
    {
      additionalFDDs = new ArrayList<FDD>(urls.length);
      for (URL additionalFomModule : urls)
      {
        additionalFDDs.add(parseFDD(additionalFomModule));
      }
    }
    return additionalFDDs;
  }

  public static FDD parseFDD(URL fom)
    throws InconsistentFDD, ErrorReadingFDD, CouldNotOpenFDD, RTIinternalError
  {
    FDD fdd = new FDD(fom.toString());
    try
    {
      SAXParserFactory.newInstance().newSAXParser().parse(fom.openStream(), new Handler(fdd));
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
      else if (saxe.getCause() instanceof InconsistentFDD)
      {
        throw (InconsistentFDD) saxe.getCause();
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

  public static FDD parseMIM(URL mim)
    throws ErrorReadingMIM, CouldNotOpenMIM, DesignatorIsHLAstandardMIM, RTIinternalError
  {
    FDD fdd = new FDD(mim.toString());
    try
    {
      SAXParserFactory.newInstance().newSAXParser().parse(mim.openStream(), new Handler(fdd));
    }
    catch (IOException ioe)
    {
      throw new CouldNotOpenMIM(ioe.getMessage(), ioe);
    }
    catch (SAXException saxe)
    {
      if (saxe.getCause() instanceof ErrorReadingFDD)
      {
        throw new ErrorReadingMIM(saxe.getCause().getMessage(), saxe.getCause());
      }
      else if (saxe.getCause() instanceof InconsistentFDD)
      {
        throw new ErrorReadingMIM(saxe.getCause().getMessage(), saxe.getCause());
      }
      else
      {
        throw new ErrorReadingMIM(saxe.getMessage(), saxe);
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
      else if (qName.equals("objectClass"))
      {
        contentHandlerStack.push(new ObjectClassHandler(null));
      }
      else if (qName.equals("interactionClass"))
      {
        contentHandlerStack.push(new InteractionClassHandler(null));
      }
      else if (qName.equals("dimension"))
      {
        contentHandlerStack.push(new DimensionHandler());
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
      private final ObjectClass superObjectClass;

      private String objectClassName;

      private ObjectClass objectClass;

      private ObjectClassHandler(ObjectClass superObjectClass)
      {
        this.superObjectClass = superObjectClass;
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
      {
        if (qName.equals("name"))
        {
          saveContent();
        }
        else if (qName.equals("attribute"))
        {
          contentHandlerStack.push(new AttributeHandler());
        }
        else if (qName.equals("objectClass"))
        {
          contentHandlerStack.push(new ObjectClassHandler(objectClass));
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName)
        throws SAXException
      {
        if (qName.equals("name"))
        {
          objectClassName = getContent();

          if (superObjectClass == null)
          {
            // only one object class has no root

            if (!FDD.HLA_OBJECT_ROOT.equals(objectClassName))
            {
              throw new SAXException(new ErrorReadingFDD(objectClassName + " does not have parent ObjectClass"));
            }
          }

          try
          {
            objectClass = fdd.addObjectClass(objectClassName, superObjectClass);
          }
          catch (InconsistentFDD ifdd)
          {
            throw new SAXException(ifdd);
          }
        }
        else if (qName.equals("objectClass"))
        {
          contentHandlerStack.pop();
        }
      }

      private class AttributeHandler
        extends ContentHandler
      {
        private final Set<String> dimensions = new HashSet<String>();

        private String attributeName;
        private String transportationTypeName;
        private String orderTypeName;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
          throws SAXException
        {
          if (qName.equals("name"))
          {
            saveContent();
          }
          else if (qName.equals("dimension"))
          {
            saveContent();
          }
          else if (qName.equals("transportation"))
          {
            saveContent();
          }
          else if (qName.equals("order"))
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
            attributeName = getContent();
          }
          else if (qName.equals("dimension"))
          {
            dimensions.add(getContent());
          }
          else if (qName.equals("transportation"))
          {
            transportationTypeName = getContent();
          }
          else if (qName.equals("order"))
          {
            orderTypeName = getContent();
          }
          else if (qName.equals("attribute"))
          {
            try
            {
              fdd.addAttribute(objectClass, attributeName, dimensions, transportationTypeName, orderTypeName);
            }
            catch (InconsistentFDD ifdd)
            {
              throw new SAXException(ifdd);
            }

            contentHandlerStack.pop();
          }
        }
      }
    }

    private class InteractionClassHandler
      extends ContentHandler
    {
      private final InteractionClass superInteractionClass;

      private final Set<String> dimensions = new HashSet<String>();

      private String interactionClassName;
      private String transportationTypeName;
      private String orderTypeName;

      private InteractionClass interactionClass;

      private InteractionClassHandler(InteractionClass superInteractionClass)
      {
        this.superInteractionClass = superInteractionClass;
      }

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException
      {
        if (qName.equals("name"))
        {
          saveContent();
        }
        else if (qName.equals("dimension"))
        {
          saveContent();
        }
        else if (qName.equals("transportation"))
        {
          saveContent();
        }
        else if (qName.equals("order"))
        {
          saveContent();
        }
        else if (qName.equals("parameter"))
        {
          contentHandlerStack.push(new ParameterHandler());
        }
        else if (qName.equals("interactionClass"))
        {
          contentHandlerStack.push(new InteractionClassHandler(interactionClass));
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName)
        throws SAXException
      {
        if (qName.equals("name"))
        {
          interactionClassName = getContent();
        }
        else if (qName.equals("dimension"))
        {
          dimensions.add(getContent());
        }
        else if (qName.equals("transportation"))
        {
          transportationTypeName = getContent();
        }
        else if (qName.equals("order"))
        {
          orderTypeName = getContent();

          try
          {
            interactionClass = fdd.addInteractionClass(
              interactionClassName, superInteractionClass, dimensions, transportationTypeName, orderTypeName);
          }
          catch (InconsistentFDD ifdd)
          {
            throw new SAXException(ifdd);
          }
        }
        else if (qName.equals("interactionClass"))
        {
          contentHandlerStack.pop();
        }
      }

      private class ParameterHandler
        extends ContentHandler
      {
        private String name;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
          throws SAXException
        {
          if (qName.equals("name"))
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
            name = getContent();
          }
          else if (qName.equals("parameter"))
          {
            try
            {
              fdd.addParameter(interactionClass, name);
            }
            catch (InconsistentFDD ifdd)
            {
              throw new SAXException(ifdd);
            }

            contentHandlerStack.pop();
          }
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
        else if (qName.equals("dimension"))
        {
          contentHandlerStack.pop();
        }
      }
    }
  }
}
