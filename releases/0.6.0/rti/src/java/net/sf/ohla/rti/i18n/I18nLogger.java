/*
 * Copyright (c) 2005-2011, Michael Newcomb
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

package net.sf.ohla.rti.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.ext.LoggerWrapper;
import org.slf4j.spi.LocationAwareLogger;

public class I18nLogger
  extends LoggerWrapper
  implements Logger
{
  private static final String FQCN = I18nLogger.class.getName();

  private final Marker marker;

  private I18nLogger(Marker marker, Logger logger)
  {
    super(logger, LoggerWrapper.class.getName());

    this.marker = marker;
  }

  public void trace(Enum<?> key, Object arg)
  {
    if (logger.isTraceEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, arg);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.TRACE_INT, translatedMessage, null, null);
      }
      else
      {
        logger.trace(marker, translatedMessage);
      }
    }
  }

  public void trace(Enum<?> key, Object arg1, Object arg2)
  {
    if (logger.isTraceEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, arg1, arg2);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.TRACE_INT, translatedMessage, null, null);
      }
      else
      {
        logger.trace(marker, translatedMessage);
      }
    }
  }

  public void trace(Enum<?> key, Object... args)
  {
    if (logger.isTraceEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, args);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.TRACE_INT, translatedMessage, null, null);
      }
      else
      {
        logger.trace(marker, translatedMessage);
      }
    }
  }

  public void debug(Enum<?> key, Object arg)
  {
    if (logger.isDebugEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, arg);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.DEBUG_INT, translatedMessage, null, null);
      }
      else
      {
        logger.debug(marker, translatedMessage);
      }
    }
  }

  public void debug(Enum<?> key, Object arg1, Object arg2)
  {
    if (logger.isDebugEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, arg1, arg2);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.DEBUG_INT, translatedMessage, null, null);
      }
      else
      {
        logger.debug(marker, translatedMessage);
      }
    }
  }

  public void debug(Enum<?> key, Object... args)
  {
    if (logger.isDebugEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, args);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.DEBUG_INT, translatedMessage, null, null);
      }
      else
      {
        logger.debug(marker, translatedMessage);
      }
    }
  }

  public void info(Enum<?> key, Object... args)
  {
    if (logger.isInfoEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, args);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.INFO_INT, translatedMessage, null, null);
      }
      else
      {
        logger.info(marker, translatedMessage);
      }
    }
  }

  public void warn(Enum<?> key, Object... args)
  {
    if (logger.isWarnEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, args);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.WARN_INT, translatedMessage, null, null);
      }
      else
      {
        logger.warn(marker, translatedMessage);
      }
    }
  }
  public void warn(Enum<?> key, Throwable t, Object... args)
  {
    if (logger.isWarnEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, args);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.WARN_INT, translatedMessage, null, t);
      }
      else
      {
        logger.warn(marker, translatedMessage, t);
      }
    }
  }

  public void error(Enum<?> key, Object... args)
  {
    if (logger.isErrorEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, args);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.ERROR_INT, translatedMessage, null, null);
      }
      else
      {
        logger.error(marker, translatedMessage);
      }
    }
  }

  public void error(Enum<?> key, Throwable t, Object... args)
  {
    if (logger.isErrorEnabled())
    {
      String translatedMessage = I18n.MESSAGE_CONVEYOR.getMessage(key, args);
      if (instanceofLAL)
      {
        ((LocationAwareLogger) logger).log(marker, FQCN, LocationAwareLogger.ERROR_INT, translatedMessage, null, t);
      }
      else
      {
        logger.error(marker, translatedMessage, t);
      }
    }
  }

  public static I18nLogger getLogger(Class<?> c)
  {
    return new I18nLogger(null, LoggerFactory.getLogger(c));
  }

  public static I18nLogger getLogger(Marker marker, Class<?> c)
  {
    return new I18nLogger(marker, LoggerFactory.getLogger(c));
  }
}
