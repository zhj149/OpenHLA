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

package net.sf.ohla.rti.federate;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.hla.rti1516e.IEEE1516eRegionHandle;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;
import net.sf.ohla.rti.messages.CommitRegionModifications;
import net.sf.ohla.rti.messages.CreateRegion;
import net.sf.ohla.rti.messages.DeleteRegion;
import net.sf.ohla.rti.proto.FederationExecutionSaveProtos.FederateState.FederateRegionManagerState;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import hla.rti1516e.AttributeRegionAssociation;
import hla.rti1516e.AttributeSetRegionSetPairList;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RangeBounds;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.InvalidRegion;
import hla.rti1516e.exceptions.InvalidRegionContext;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516e.exceptions.RegionInUseForUpdateOrSubscription;
import hla.rti1516e.exceptions.RegionNotCreatedByThisFederate;

public class FederateRegionManager
{
  private final Federate federate;

  private final ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  private final Map<RegionHandle, FederateRegion> regions = new HashMap<>();
  private final Map<RegionHandle, FederateRegion> temporaryRegions = new HashMap<>();

  private int nextRegionHandle;

  public FederateRegionManager(Federate federate)
  {
    this.federate = federate;
  }

  public ReadWriteLock getRegionsLock()
  {
    return regionsLock;
  }

  public Map<RegionHandle, FederateRegion> getRegions()
  {
    return regions;
  }

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws RTIinternalError
  {
    IEEE1516eRegionHandle regionHandle;

    regionsLock.writeLock().lock();
    try
    {
      regionHandle = new IEEE1516eRegionHandle(federate.getFederateHandle(), ++nextRegionHandle);
      FederateRegion federateRegion = new FederateRegion(regionHandle, dimensionHandles, federate.getFDD());
      regions.put(regionHandle, federateRegion);
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }

    federate.getRTIChannel().write(new CreateRegion(regionHandle, dimensionHandles));

    return regionHandle;
  }

  public RegionHandleSet createTemporaryRegions(Collection<Map<DimensionHandle, RangeBounds>> regions)
  {
    RegionHandleSet regionHandles = federate.getRegionHandleSetFactory().create();

    regionsLock.writeLock().lock();
    try
    {
      for (Map<DimensionHandle, RangeBounds> region : regions)
      {
        IEEE1516eRegionHandle regionHandle = new IEEE1516eRegionHandle(federate.getFederateHandle(), ++nextRegionHandle);
        temporaryRegions.put(regionHandle, new FederateRegion(regionHandle, region));
        regionHandles.add(regionHandle);
      }
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }

    return regionHandles;
  }

  public void deleteTemporaryRegions(RegionHandleSet regionHandles)
  {
    regionsLock.writeLock().lock();
    try
    {
      temporaryRegions.keySet().removeAll(regionHandles);
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public DimensionHandleSet getDimensionHandleSet(RegionHandle regionHandle)
    throws InvalidRegion
  {
    DimensionHandleSet dimensionHandles;

    regionsLock.readLock().lock();
    try
    {
      FederateRegion region = regions.get(regionHandle);
      if (region == null)
      {
        region = temporaryRegions.get(regionHandle);
        if (region == null)
        {
          throw new InvalidRegion(I18n.getMessage(ExceptionMessages.INVALID_REGION, regionHandle));
        }
      }

      dimensionHandles = region.getDimensionHandles();
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return dimensionHandles;
  }

  public RangeBounds getRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle)
    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension
  {
    RangeBounds rangeBounds;

    regionsLock.readLock().lock();
    try
    {
      FederateRegion region = regions.get(regionHandle);
      if (region == null)
      {
        region = temporaryRegions.get(regionHandle);
        if (region == null)
        {
          throw new InvalidRegion(I18n.getMessage(ExceptionMessages.INVALID_REGION, regionHandle));
        }
      }

      rangeBounds = region.getRangeBounds(dimensionHandle);
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return rangeBounds;
  }

  public void setRangeBounds(RegionHandle regionHandle, DimensionHandle dimensionHandle, RangeBounds rangeBounds)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionDoesNotContainSpecifiedDimension
  {
    regionsLock.readLock().lock();
    try
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(ExceptionMessages.SET_RANGE_BOUNDS_ON_TEMPORARY_REGION, regionHandle));
      }
      else
      {
        getRegion(regionHandle).setRangeBounds(dimensionHandle, rangeBounds);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> commitRegionModifications(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RTIinternalError
  {
    regionsLock.writeLock().lock();
    try
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        if (temporaryRegions.containsKey(regionHandle))
        {
          throw new InvalidRegion(I18n.getMessage(
            ExceptionMessages.COMMIT_REGION_MODIFICATIONS_ON_TEMPORARY_REGION, regionHandle));
        }

        checkIfRegionNotCreatedByThisFederate(regionHandle);
      }

      Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications = new HashMap<>();
      for (RegionHandle regionHandle : regionHandles)
      {
        Map<DimensionHandle, RangeBounds> committedRegionModifications = regions.get(regionHandle).commit();
        if (!committedRegionModifications.isEmpty())
        {
          regionModifications.put(regionHandle, committedRegionModifications);
        }
      }

      federate.getRTIChannel().write(new CommitRegionModifications(regionModifications));

      return regionModifications;
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription, RTIinternalError
  {
    regionsLock.writeLock().lock();
    try
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(ExceptionMessages.DELETE_TEMPORARY_REGION, regionHandle));
      }
      else
      {
        getRegion(regionHandle).checkIfInUse();

        regions.remove(regionHandle);

        federate.getRTIChannel().write(new DeleteRegion(regionHandle));
      }
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void deleteRegions(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription, RTIinternalError
  {
    regionsLock.writeLock().lock();
    try
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        if (temporaryRegions.containsKey(regionHandle))
        {
          throw new InvalidRegion(I18n.getMessage(ExceptionMessages.DELETE_TEMPORARY_REGION, regionHandle));
        }
        else
        {
          getRegion(regionHandle).checkIfInUse();
        }
      }

      for (RegionHandle regionHandle : regionHandles)
      {
        regions.remove(regionHandle);

        federate.getRTIChannel().write(new DeleteRegion(regionHandle));
      }
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions, boolean passive)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
           InvalidRegionContext, RTIinternalError
  {
    // dangerous method, must be called with proper protection

    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      ObjectClass objectClass = federate.getFDD().getObjectClass(objectClassHandle);
      objectClass.checkIfAttributeNotDefined(attributeRegionAssociation.ahset);

      for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
      {
        if (temporaryRegions.containsKey(regionHandle))
        {
          throw new InvalidRegion(I18n.getMessage(ExceptionMessages.SUBSCRIBE_TO_TEMPORARY_REGION, regionHandle));
        }
        else
        {
          FederateRegion federateRegion = regions.get(regionHandle);
          if (federateRegion == null)
          {
            throw new RegionNotCreatedByThisFederate(I18n.getMessage(
              ExceptionMessages.REGION_NOT_CREATED_BY_THIS_FEDERATE, regionHandle));
          }

          federateRegion.checkIfInvalidRegionContext(objectClass, attributeRegionAssociation.ahset);
        }
      }
    }

    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
      {
        // TODO: don't forget passive

        regions.get(regionHandle).subscribe(objectClassHandle, attributeRegionAssociation.ahset);
      }
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle, AttributeSetRegionSetPairList attributesAndRegions)
    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion, RegionNotCreatedByThisFederate, RTIinternalError
  {
    // dangerous method, must be called with proper protection

    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      federate.getFDD().checkIfAttributeNotDefined(objectClassHandle, attributeRegionAssociation.ahset);

      for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
      {
        if (temporaryRegions.containsKey(regionHandle))
        {
          throw new InvalidRegion(I18n.getMessage(ExceptionMessages.UNSUBSCRIBE_FROM_TEMPORARY_REGION, regionHandle));
        }

        checkIfRegionNotCreatedByThisFederate(regionHandle);
      }
    }

    for (AttributeRegionAssociation attributeRegionAssociation : attributesAndRegions)
    {
      for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
      {
        regions.get(regionHandle).unsubscribe(objectClassHandle, attributeRegionAssociation.ahset);
      }
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClass interactionClass, RegionHandleSet regionHandles, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext
  {
    // dangerous method, must be called with proper protection

    for (RegionHandle regionHandle : regionHandles)
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(ExceptionMessages.SUBSCRIBE_TO_TEMPORARY_REGION, regionHandle));
      }
      else
      {
        FederateRegion federateRegion = regions.get(regionHandle);
        if (federateRegion == null)
        {
          throw new RegionNotCreatedByThisFederate(I18n.getMessage(
            ExceptionMessages.REGION_NOT_CREATED_BY_THIS_FEDERATE, regionHandle));
        }

        federateRegion.checkIfInvalidRegionContext(interactionClass);
      }
    }

    for (RegionHandle regionHandle : regionHandles)
    {
      // TODO: don't forget passive

      regions.get(regionHandle).subscribe(interactionClass.getInteractionClassHandle());
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle, RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    // dangerous method, must be called with proper protection

    for (RegionHandle regionHandle : regionHandles)
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(ExceptionMessages.UNSUBSCRIBE_FROM_TEMPORARY_REGION, regionHandle));
      }

      checkIfRegionNotCreatedByThisFederate(regionHandle);
    }

    for (RegionHandle regionHandle : regionHandles)
    {
      regions.get(regionHandle).unsubscribe(interactionClassHandle);
    }
  }

  public FederateRegion getRegionSafely(RegionHandle regionHandle)
  {
    // dangerous method, must be called with proper protection

    FederateRegion region = regions.get(regionHandle);
    if (region == null)
    {
      region = temporaryRegions.get(regionHandle);

      assert region != null;
    }
    return region;
  }

  public Collection<FederateRegion> getRegionsSafely(RegionHandleSet regionHandles)
  {
    Collection<FederateRegion> regions = new ArrayList<>(regionHandles.size());
    regionsLock.readLock().lock();
    try
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        FederateRegion region = this.regions.get(regionHandle);
        if (region == null)
        {
          region = temporaryRegions.get(regionHandle);

          assert region != null;
        }
        regions.add(region);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
    return regions;
  }

  public void checkIfCanAssociateRegionForUpdates(
    ObjectClass objectClass, AttributeRegionAssociation attributeRegionAssociation)
    throws InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext
  {
    for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(
          ExceptionMessages.ASSOCIATE_ATTRIBUTES_TO_TEMPORARY_REGION, regionHandle));
      }
      else
      {
        FederateRegion federateRegion = regions.get(regionHandle);
        if (federateRegion == null)
        {
          throw new RegionNotCreatedByThisFederate(I18n.getMessage(
            ExceptionMessages.REGION_NOT_CREATED_BY_THIS_FEDERATE, regionHandle));
        }

        federateRegion.checkIfInvalidRegionContext(objectClass, attributeRegionAssociation.ahset);
      }
    }
  }

  public void checkIfCanRegisterObjectInstanceWithRegions(
    ObjectClass objectClass, AttributeRegionAssociation attributeRegionAssociation)
    throws InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext
  {
    for (RegionHandle regionHandle : attributeRegionAssociation.rhset)
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(
          ExceptionMessages.REGISTER_OBJECT_INSTANCE_WITH_TEMPORARY_REGION, regionHandle));
      }
      else
      {
        FederateRegion federateRegion = regions.get(regionHandle);
        if (federateRegion == null)
        {
          throw new RegionNotCreatedByThisFederate(I18n.getMessage(
            ExceptionMessages.REGION_NOT_CREATED_BY_THIS_FEDERATE, regionHandle));
        }

        federateRegion.checkIfInvalidRegionContext(objectClass, attributeRegionAssociation.ahset);
      }
    }
  }

  public void checkIfCanUnassociateRegionForUpdates(RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(
          ExceptionMessages.UNASSOCIATE_ATTRIBUTES_TO_TEMPORARY_REGION, regionHandle));
      }

      checkIfRegionNotCreatedByThisFederate(regionHandle);
    }
  }

  public void checkIfCanSendInteractionWithRegions(InteractionClass interactionClass, RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      if (temporaryRegions.containsKey(regionHandle))
      {
        throw new InvalidRegion(I18n.getMessage(
          ExceptionMessages.SEND_INTERACTION_WITH_TEMPORARY_REGION, regionHandle));
      }
      else
      {
        FederateRegion federateRegion = regions.get(regionHandle);
        if (federateRegion == null)
        {
          throw new RegionNotCreatedByThisFederate(I18n.getMessage(
            ExceptionMessages.REGION_NOT_CREATED_BY_THIS_FEDERATE, regionHandle));
        }

        federateRegion.checkIfInvalidRegionContext(interactionClass);
      }
    }
  }

  public void saveState(CodedOutputStream out)
    throws IOException
  {
    // temporary regions only exist during a 'message' (interaction/reflection) callback
    //
    assert temporaryRegions.isEmpty();

    FederateRegionManagerState.Builder regionManagerState = FederateRegionManagerState.newBuilder();

    regionManagerState.setNextRegionHandle(nextRegionHandle);
    regionManagerState.setRegionStateCount(regions.size());

    out.writeMessageNoTag(regionManagerState.build());

    for (FederateRegion federateRegion : regions.values())
    {
      federateRegion.saveState(out);
    }
  }

  public void restoreState(CodedInputStream in)
    throws IOException
  {
    FederateRegionManagerState regionManagerState = in.readMessage(FederateRegionManagerState.PARSER, null);

    nextRegionHandle = regionManagerState.getNextRegionHandle();

    for (int regionStateCount = regionManagerState.getRegionStateCount(); regionStateCount > 0; --regionStateCount)
    {
      FederateRegion federateRegion = new FederateRegion(in);
      regions.put(federateRegion.getRegionHandle(), federateRegion);
    }
  }

  private FederateRegion getRegion(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    FederateRegion region = regions.get(regionHandle);
    if (region == null)
    {
      throw new RegionNotCreatedByThisFederate(I18n.getMessage(
        ExceptionMessages.REGION_NOT_CREATED_BY_THIS_FEDERATE, regionHandle));
    }
    return region;
  }

  private void checkIfRegionNotCreatedByThisFederate(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    if (!regions.containsKey(regionHandle))
    {
      throw new RegionNotCreatedByThisFederate(I18n.getMessage(
        ExceptionMessages.REGION_NOT_CREATED_BY_THIS_FEDERATE, regionHandle));
    }
  }
}
