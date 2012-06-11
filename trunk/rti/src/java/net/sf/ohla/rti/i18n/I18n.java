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

import java.util.Locale;

import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.MessageConveyor;
import ch.qos.cal10n.MessageConveyorException;
import ch.qos.cal10n.util.MiscUtil;

public class I18n
{
  public static final IMessageConveyor MESSAGE_CONVEYOR;

  public static final String DEFAULT_LOCALE = "en_US";

  static
  {
    // TODO: check for a Locale property?

    Locale locale = Locale.getDefault();

    IMessageConveyor messageConveyor = new MessageConveyor(locale);
    try
    {
      messageConveyor.getMessage(LogMessages.LOCALE_TEST);
    }
    catch (MessageConveyorException mce)
    {
      // might not have a resource bundle for the default locale, try again with the default Locale

      // TODO: check for a default Locale property?

      locale = MiscUtil.toLocale(DEFAULT_LOCALE);

      messageConveyor = new MessageConveyor(locale);

      try
      {
        messageConveyor.getMessage(LogMessages.LOCALE_TEST);
      }
      catch (MessageConveyorException mce2)
      {
        // rethrow the original error
        //
        throw mce;
      }
    }

    MESSAGE_CONVEYOR = messageConveyor;
  }

  public static <E extends Enum<?>> String getMessage(E key, Object... args)
  {
    return MESSAGE_CONVEYOR.getMessage(key, args);
  }
}
