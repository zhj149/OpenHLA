/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.federation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import hla.rti1516.IllegalTimeArithmetic;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.MobileFederateServices;

public class FederationExecutionTimeManager
{
  protected FederationExecution federationExecution;

  protected MobileFederateServices mobileFederateServices;

  protected LogicalTime galt;

  protected ReadWriteLock timeLock = new ReentrantReadWriteLock(true);

  protected Set<FederateProxy> timeRegulatingFederates =
    new HashSet<FederateProxy>();
  protected Set<FederateProxy> timeConstrainedFederates =
    new HashSet<FederateProxy>();

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final Marker marker;

  public FederationExecutionTimeManager(
    FederationExecution federationExecution,
    MobileFederateServices mobileFederateServices)
  {
    this.federationExecution = federationExecution;
    this.mobileFederateServices = mobileFederateServices;

    galt = mobileFederateServices.timeFactory.makeInitial();

    marker = federationExecution.getMarker();
  }

  public ReadWriteLock getTimeLock()
  {
    return timeLock;
  }

  public LogicalTime getGALT()
  {
    return galt;
  }

  public void enableTimeRegulation(FederateProxy federateProxy,
                                   LogicalTimeInterval lookahead)
  {
    timeLock.writeLock().lock();
    try
    {
      timeRegulatingFederates.add(federateProxy);

      federateProxy.enableTimeRegulation(galt, lookahead);
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error("unable to enable time regulation", ita);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void disableTimeRegulation(FederateProxy federateProxy)
  {
    timeLock.writeLock().lock();
    try
    {
      timeRegulatingFederates.remove(federateProxy);

      federateProxy.disableTimeRegulation();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void enableTimeConstrained(FederateProxy federateProxy)
  {
    timeLock.writeLock().lock();
    try
    {
      timeConstrainedFederates.add(federateProxy);

      federateProxy.enableTimeConstrained(galt);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void disableTimeConstrained(FederateProxy federateProxy)
  {
    timeLock.writeLock().lock();
    try
    {
      timeConstrainedFederates.remove(federateProxy);

      federateProxy.disableTimeConstrained();
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void modifyLookahead(FederateProxy federateProxy,
                              LogicalTimeInterval lookahead)
  {
    timeLock.writeLock().lock();
    try
    {
      timeRegulatingFederates.add(federateProxy);

      federateProxy.modifyLookahead(lookahead);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  public void timeAdvanceRequest(FederateProxy federateProxy, LogicalTime time)
  {
    timeAdvanceRequest(federateProxy, time, false);
  }

  public void timeAdvanceRequestAvailable(FederateProxy federateProxy,
                                          LogicalTime time)
  {
    timeAdvanceRequest(federateProxy, time, true);
  }

  protected void timeAdvanceRequest(FederateProxy federateProxy,
                                    LogicalTime time, boolean available)
  {
    timeLock.writeLock().lock();
    try
    {
      if (available)
      {
        time = min(time, galt);
      }
      else
      {
        assert galt.compareTo(time) >= 0;
      }

      federateProxy.timeAdvanceRequest(time);

      boolean galtAdvanced = false;

      if (timeRegulatingFederates.contains(federateProxy))
      {
        LogicalTime newGALT = mobileFederateServices.timeFactory.makeFinal();
        for (FederateProxy f : timeRegulatingFederates)
        {
          newGALT = min(newGALT, f.getLITS());
        }

        galtAdvanced = galt.compareTo(newGALT) < 0;
        if (galtAdvanced)
        {
          galt = newGALT;
        }
      }

      if (galtAdvanced)
      {
        federationExecution.getFederatesLock().writeLock().lock();
        try
        {
          for (FederateProxy f : federationExecution.getFederates().values())
          {
            f.galtAdvanced(galt);
          }
        }
        finally
        {
          federationExecution.getFederatesLock().writeLock().unlock();
        }
      }
    }
    catch (IllegalTimeArithmetic ita)
    {
      log.error("unable to request time advance", ita);
    }
    finally
    {
      timeLock.writeLock().unlock();
    }
  }

  protected LogicalTime min(LogicalTime lhs, LogicalTime rhs)
  {
    return lhs.compareTo(rhs) <= 0 ? lhs : rhs;
  }
}
