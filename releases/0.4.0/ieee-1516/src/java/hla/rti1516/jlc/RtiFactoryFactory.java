package hla.rti1516.jlc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import hla.rti1516.RTIinternalError;

public class RtiFactoryFactory
{
  public static final String RTI_LIST_PROPERTIES = "RTI-list.properties";

  public static RtiFactory getRtiFactory(String className)
    throws RTIinternalError
  {
    try
    {
      ClassLoader classLoader =
        Thread.currentThread().getContextClassLoader();
      return (RtiFactory) classLoader.loadClass(className).newInstance();
    }
    catch (ClassCastException cce)
    {
      throw new RTIinternalError(String.format(
        "invalid class: '%s' (not an %s)", className, RtiFactory.class), cce);
    }
    catch (ClassNotFoundException cnfe)
    {
      throw new RTIinternalError(String.format(
        "class not found: %s", className), cnfe);
    }
    catch (InstantiationException ie)
    {
      throw new RTIinternalError(String.format(
        "unable to instantiate: %s", className), ie);
    }
    catch (IllegalAccessException iae)
    {
      throw new RTIinternalError(String.format(
        "unable to access: %s", className), iae);
    }
  }

  public static RtiFactory getRtiFactory()
    throws RTIinternalError
  {
    Properties properties = loadProperties();

    RtiFactory rtiFactory;

    String defaultRTI = properties.getProperty("Default");
    if (defaultRTI == null)
    {
      rtiFactory = getRtiFactory("net.sf.ohla.rti1516.jlc.impl.OHLARtiFactory");
    }
    else
    {
      String className =
        properties.getProperty(String.format("%s.factory", defaultRTI));
      if (className == null)
      {
        throw new RTIinternalError(
          String.format("no factory property for default RTI: %s", defaultRTI));
      }
      rtiFactory = getRtiFactory(className);
    }

    return rtiFactory;
  }

  public static Map<String, String> getAvailableRtis()
    throws RTIinternalError
  {
    Properties properties = loadProperties();

    Map<String, String> rtis = new HashMap<String, String>();

    Pattern pattern = Pattern.compile("(\\d)\\.name");
    for (Map.Entry<Object, Object> entry : properties.entrySet())
    {
      Matcher matcher = pattern.matcher(entry.getKey().toString());
      if (matcher.matches())
      {
        String rtiFactoryClassName =
          properties.getProperty(String.format("%s.factory", matcher.group(1)));
        if (rtiFactoryClassName != null)
        {
          rtis.put(entry.getValue().toString(), rtiFactoryClassName);
        }
      }
    }

    return rtis;
  }

  protected static Properties loadProperties()
    throws RTIinternalError
  {
    Properties properties = new Properties();

    InputStream inputStream;
    try
    {
      inputStream = new FileInputStream(
        new File(System.getProperty("user.home"), RTI_LIST_PROPERTIES));
    }
    catch (FileNotFoundException fnfe)
    {
      // check the classpath as well

      inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(
          RTI_LIST_PROPERTIES);
    }

    if (inputStream != null)
    {
      try
      {
        properties.load(inputStream);
        inputStream.close();
      }
      catch (IOException ioe)
      {
        throw new RTIinternalError(
          String.format("unable to read %s", RTI_LIST_PROPERTIES), ioe);
      }
    }

    return properties;
  }
}
