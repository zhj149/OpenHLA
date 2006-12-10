package net.sf.ohla.rti1516.federate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.ohla.rti1516.OHLARegionHandleSet;
import net.sf.ohla.rti1516.fdd.FDD;
import net.sf.ohla.rti1516.messages.CommitRegionModifications;
import net.sf.ohla.rti1516.messages.CreateRegion;
import net.sf.ohla.rti1516.messages.DeleteRegion;
import net.sf.ohla.rti1516.messages.GetRangeBounds;

import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleSetFactory;
import hla.rti1516.AttributeRegionAssociation;
import hla.rti1516.DimensionHandle;
import hla.rti1516.DimensionHandleSet;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InvalidDimensionHandle;
import hla.rti1516.InvalidRegion;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.RTIinternalError;
import hla.rti1516.RangeBounds;
import hla.rti1516.RegionDoesNotContainSpecifiedDimension;
import hla.rti1516.RegionHandle;
import hla.rti1516.RegionHandleSet;
import hla.rti1516.RegionInUseForUpdateOrSubscription;
import hla.rti1516.RegionNotCreatedByThisFederate;

public class RegionManager
{
  protected IoSession rtiSession;
  protected FDD fdd;
  protected AttributeHandleSetFactory attributeHandleSetFactory;

  protected ReadWriteLock regionsLock = new ReentrantReadWriteLock(true);
  protected Map<RegionHandle, Region> regions =
    new HashMap<RegionHandle, Region>();

  public RegionManager(IoSession rtiSession, FDD fdd,
                       AttributeHandleSetFactory attributeHandleSetFactory)
  {
    this.rtiSession = rtiSession;
    this.fdd = fdd;
    this.attributeHandleSetFactory = attributeHandleSetFactory;
  }

  public RegionHandle createRegion(DimensionHandleSet dimensionHandles)
    throws RTIinternalError
  {
    try
    {
      CreateRegion createRegion = new CreateRegion(dimensionHandles);
      WriteFuture writeFuture = rtiSession.write(createRegion);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      Object response = createRegion.getResponse();
      assert response instanceof RegionHandle : "unexpected response";

      RegionHandle regionHandle = (RegionHandle) response;

      regionsLock.writeLock().lock();
      try
      {
        regions.put(regionHandle, new Region(
          regionHandle, dimensionHandles, fdd));
      }
      finally
      {
        regionsLock.writeLock().unlock();
      }

      return regionHandle;
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
  }

  public RegionHandleSet getIntersectingRegions(
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> rangeBounds)
  {
    RegionHandleSet intersectingRegions = new OHLARegionHandleSet();

    regionsLock.readLock().lock();
    try
    {
      for (Region region : regions.values())
      {
        for (Map<DimensionHandle, RangeBounds> value : rangeBounds.values())
        {
          if (region.intersects(value))
          {
            intersectingRegions.add(region.getRegionHandle());
          }
        }
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return intersectingRegions;
  }

  public RangeBounds getRangeBounds(RegionHandle regionHandle,
                                    DimensionHandle dimensionHandle)
    throws RegionDoesNotContainSpecifiedDimension, RTIinternalError
  {
    RangeBounds rangeBounds = null;

    regionsLock.readLock().lock();
    try
    {
      Region region = regions.get(regionHandle);
      if (region != null)
      {
        rangeBounds = region.getRangeBounds(dimensionHandle);
      }
      else
      {
        try
        {
          GetRangeBounds getRangeBounds =
            new GetRangeBounds(regionHandle, dimensionHandle);
          WriteFuture writeFuture = rtiSession.write(getRangeBounds);

          // TODO: set timeout
          //
          writeFuture.join();

          if (!writeFuture.isWritten())
          {
            throw new RTIinternalError("error communicating with RTI");
          }

          // TODO: set timeout
          //
          Object response = getRangeBounds.getResponse();
          if (response instanceof RangeBounds)
          {
            rangeBounds = (RangeBounds) response;
          }
          else if (response instanceof RegionDoesNotContainSpecifiedDimension)
          {
            throw new RegionDoesNotContainSpecifiedDimension(
              (RegionDoesNotContainSpecifiedDimension) response);
          }
          else
          {
            assert false : String.format("unexpected response: %s", response);
          }
        }
        catch (InterruptedException ie)
        {
          throw new RTIinternalError("interrupted awaiting timeout", ie);
        }
        catch (ExecutionException ee)
        {
          throw new RTIinternalError("unable to get response", ee);
        }
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return rangeBounds;
  }

  public void setRangeBounds(RegionHandle regionHandle,
                             DimensionHandle dimensionHandle,
                             RangeBounds rangeBounds)
    throws RegionNotCreatedByThisFederate,
           RegionDoesNotContainSpecifiedDimension
  {
    regionsLock.readLock().lock();
    try
    {
      getRegion(regionHandle).setRangeBounds(dimensionHandle, rangeBounds);
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public Map<RegionHandle, Map<DimensionHandle, RangeBounds>> commitRegionModifications(
    RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate, RTIinternalError
  {
    Map<RegionHandle, Map<DimensionHandle, RangeBounds>> regionModifications =
      new HashMap<RegionHandle, Map<DimensionHandle, RangeBounds>>();

    regionsLock.readLock().lock();
    try
    {
      for (RegionHandle regionHandle : regionHandles)
      {
        Map<DimensionHandle, RangeBounds> committedRegionModifications =
          getRegion(regionHandle).commit();
        if (!committedRegionModifications.isEmpty())
        {
          regionModifications.put(regionHandle, committedRegionModifications);
        }
      }

      try
      {
        CommitRegionModifications commitRegionModifications =
          new CommitRegionModifications(regionModifications);
        WriteFuture writeFuture = rtiSession.write(commitRegionModifications);

        // TODO: set timeout
        //
        writeFuture.join();

        if (!writeFuture.isWritten())
        {
          throw new RTIinternalError("error communicating with RTI");
        }

        // TODO: set timeout
        //
        commitRegionModifications.await();
      }
      catch (InterruptedException ie)
      {
        throw new RTIinternalError("interrupted awaiting timeout", ie);
      }
      catch (ExecutionException ee)
      {
        throw new RTIinternalError("unable to get response", ee);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }

    return regionModifications;
  }

  public void deleteRegion(RegionHandle regionHandle)
    throws InvalidRegion, RegionNotCreatedByThisFederate,
           RegionInUseForUpdateOrSubscription, RTIinternalError
  {
    regionsLock.writeLock().lock();
    try
    {
      Region region = getRegion(regionHandle);
      region.checkIfInUse();

      DeleteRegion deleteRegion = new DeleteRegion(regionHandle);
      WriteFuture writeFuture = rtiSession.write(deleteRegion);

      // TODO: set timeout
      //
      writeFuture.join();

      if (!writeFuture.isWritten())
      {
        throw new RTIinternalError("error communicating with RTI");
      }

      // TODO: set timeout
      //
      deleteRegion.await();

      regions.remove(regionHandle);
    }
    catch (InterruptedException ie)
    {
      throw new RTIinternalError("interrupted awaiting timeout", ie);
    }
    catch (ExecutionException ee)
    {
      throw new RTIinternalError("unable to get response", ee);
    }
    finally
    {
      regionsLock.writeLock().unlock();
    }
  }

  public void associateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeRegionAssociation attributeRegionAssociation)
    throws RegionNotCreatedByThisFederate
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfRegionNotCreatedByThisFederate(attributeRegionAssociation.regions);

      for (RegionHandle regionHandle : attributeRegionAssociation.regions)
      {
        regions.get(regionHandle).associateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation.attributes);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void unassociateRegionsForUpdates(
    ObjectInstanceHandle objectInstanceHandle,
    AttributeRegionAssociation attributeRegionAssociation)
    throws RegionNotCreatedByThisFederate
  {
    regionsLock.readLock().lock();
    try
    {
      checkIfRegionNotCreatedByThisFederate(attributeRegionAssociation.regions);

      for (RegionHandle regionHandle : attributeRegionAssociation.regions)
      {
        regions.get(regionHandle).unassociateRegionsForUpdates(
          objectInstanceHandle, attributeRegionAssociation.attributes);
      }
    }
    finally
    {
      regionsLock.readLock().unlock();
    }
  }

  public void subscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeRegionAssociation attributeRegionAssociation, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : attributeRegionAssociation.regions)
    {
      // TODO: don't forget passive

      getRegion(regionHandle).subscribe(
        objectClassHandle, attributeRegionAssociation.attributes);
    }
  }

  public void unsubscribeObjectClassAttributesWithRegions(
    ObjectClassHandle objectClassHandle,
    AttributeRegionAssociation attributeRegionAssociation)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : attributeRegionAssociation.regions)
    {
      getRegion(regionHandle).unsubscribe(
        objectClassHandle, attributeRegionAssociation.attributes);
    }
  }

  public void subscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles, boolean passive)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      getRegion(regionHandle).subscribe(interactionClassHandle);
    }
  }

  public void unsubscribeInteractionClassWithRegions(
    InteractionClassHandle interactionClassHandle,
    RegionHandleSet regionHandles)
    throws InvalidRegion, RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      getRegion(regionHandle).unsubscribe(interactionClassHandle);
    }
  }

  protected Region getRegion(RegionHandle regionHandle)
    throws RegionNotCreatedByThisFederate
  {
    Region region = regions.get(regionHandle);
    if (region == null)
    {
      throw new RegionNotCreatedByThisFederate(
        String.format("%s", regionHandle));
    }
    return region;
  }

  protected void checkIfRegionNotCreatedByThisFederate(
    RegionHandle regionHandle)
  throws RegionNotCreatedByThisFederate
  {
    if (!regions.containsKey(regionHandle))
    {
      throw new RegionNotCreatedByThisFederate(
        String.format("%s", regionHandle));
    }
  }

  protected void checkIfRegionNotCreatedByThisFederate(
    RegionHandleSet regionHandles)
  throws RegionNotCreatedByThisFederate
  {
    for (RegionHandle regionHandle : regionHandles)
    {
      if (!regions.containsKey(regionHandle))
      {
        throw new RegionNotCreatedByThisFederate(
          String.format("%s", regionHandle));
      }
    }
  }

  protected class Region
  {
    protected RegionHandle regionHandle;
    protected DimensionHandleSet dimensionHandles;

    protected ReadWriteLock rangeBoundsLock = new ReentrantReadWriteLock(true);
    protected Map<DimensionHandle, RangeBounds> rangeBounds =
      new HashMap<DimensionHandle, RangeBounds>();
    protected Map<DimensionHandle, RangeBounds> uncommittedRangeBounds =
      new HashMap<DimensionHandle, RangeBounds>();

    protected ReadWriteLock associatedObjectsLock =
      new ReentrantReadWriteLock(true);
    protected Map<ObjectInstanceHandle, AttributeHandleSet> associatedObjects =
      new HashMap<ObjectInstanceHandle, AttributeHandleSet>();

    protected ReadWriteLock subscriptionLock = new ReentrantReadWriteLock(true);

    protected Map<ObjectClassHandle, AttributeHandleSet> subscribedObjectClasses =
      new HashMap<ObjectClassHandle, AttributeHandleSet>();

    protected Set<InteractionClassHandle> subscribedInteractionClasses =
      new HashSet<InteractionClassHandle>();

    public Region(RegionHandle regionHandle,
                  DimensionHandleSet dimensionHandles, FDD fdd)
      throws RTIinternalError
    {
      this.regionHandle = regionHandle;
      this.dimensionHandles = dimensionHandles;

      // initialize our range bounds to the dimension defaults
      //
      for (DimensionHandle dimensionHandle : dimensionHandles)
      {
        try
        {
          rangeBounds.put(dimensionHandle, fdd.getRangeBounds(dimensionHandle));
        }
        catch (InvalidDimensionHandle idh)
        {
          // should never occur
          //
          throw new RTIinternalError("unexpected exception", idh);
        }
      }
    }

    public RegionHandle getRegionHandle()
    {
      return regionHandle;
    }

    public DimensionHandleSet getDimensionHandles()
    {
      return dimensionHandles;
    }

    public RangeBounds getRangeBounds(DimensionHandle dimensionHandle)
      throws RegionDoesNotContainSpecifiedDimension
    {
      rangeBoundsLock.readLock().lock();
      try
      {
        RangeBounds rangeBounds = this.rangeBounds.get(dimensionHandle);
        if (rangeBounds == null)
        {
          throw new RegionDoesNotContainSpecifiedDimension(
            String.format("%s", dimensionHandle));
        }
        return clone(rangeBounds);
      }
      finally
      {
        rangeBoundsLock.readLock().unlock();
      }
    }

    public void setRangeBounds(DimensionHandle dimensionHandle,
                               RangeBounds rangeBounds)
      throws RegionDoesNotContainSpecifiedDimension
    {
      // make sure we have this range bound
      //
      getRangeBounds(dimensionHandle);

      // hold onto it until a commit occurs
      //
      uncommittedRangeBounds.put(dimensionHandle, clone(rangeBounds));
    }

    public AttributeHandleSet getAssociatedAttributeHandles(
      ObjectInstanceHandle objectInstanceHandle)
    {
      return associatedObjects.get(objectInstanceHandle);
    }

    public Map<DimensionHandle, RangeBounds> commit()
    {
      Map<DimensionHandle, RangeBounds> committedRangeBounds =
        uncommittedRangeBounds;

      update(uncommittedRangeBounds);

      // start fresh
      //
      uncommittedRangeBounds = new HashMap<DimensionHandle, RangeBounds>();

      return committedRangeBounds;
    }

    public void update(Map<DimensionHandle, RangeBounds> rangeBounds)
    {
      this.rangeBounds.putAll(rangeBounds);
    }

    public void checkIfInUse()
      throws RegionInUseForUpdateOrSubscription
    {
      if (!associatedObjects.isEmpty() || !subscribedObjectClasses.isEmpty() ||
          !subscribedInteractionClasses.isEmpty())
      {
        throw new RegionInUseForUpdateOrSubscription(String.format(
          "associated objects(%d), subscribed object classes(%d), subscribed interaction classes(%d)",
          associatedObjects.size(), subscribedObjectClasses.size(),
          subscribedInteractionClasses.size()));
      }
    }

    public void associateRegionsForUpdates(ObjectInstanceHandle objectInstanceHandle,
                                           AttributeHandleSet attributeHandles)
    {
      AttributeHandleSet existingAttributeHandles =
        associatedObjects.get(objectInstanceHandle);
      if (existingAttributeHandles == null)
      {
        existingAttributeHandles = attributeHandleSetFactory.create();
        associatedObjects.put(objectInstanceHandle, existingAttributeHandles);
      }

      existingAttributeHandles.addAll(attributeHandles);
    }

    public void unassociateRegionsForUpdates(ObjectInstanceHandle objectInstanceHandle,
                                             AttributeHandleSet attributeHandles)
    {
      AttributeHandleSet existingAttributeHandles =
        associatedObjects.get(objectInstanceHandle);
      if (existingAttributeHandles != null)
      {
        existingAttributeHandles.removeAll(attributeHandles);

        if (existingAttributeHandles.isEmpty())
        {
          associatedObjects.remove(objectInstanceHandle);
        }
      }
    }

    public void subscribe(ObjectClassHandle objectClassHandle,
                          AttributeHandleSet attributeHandles)
      throws RegionNotCreatedByThisFederate
    {
      AttributeHandleSet existingAttributeHandles =
        subscribedObjectClasses.get(objectClassHandle);
      if (existingAttributeHandles == null)
      {
        existingAttributeHandles = attributeHandleSetFactory.create();
        subscribedObjectClasses.put(objectClassHandle,
                                    existingAttributeHandles);
      }

      existingAttributeHandles.addAll(attributeHandles);
    }

    public void unsubscribe(ObjectClassHandle objectClassHandle,
                            AttributeHandleSet attributeHandles)
      throws RegionNotCreatedByThisFederate
    {
      AttributeHandleSet existingAttributeHandles =
        subscribedObjectClasses.get(objectClassHandle);
      if (existingAttributeHandles != null)
      {
        existingAttributeHandles.removeAll(attributeHandles);

        if (existingAttributeHandles.isEmpty())
        {
          subscribedObjectClasses.remove(objectClassHandle);
        }
      }
    }

    public void subscribe(InteractionClassHandle interactionClassHandle)
      throws RegionNotCreatedByThisFederate
    {
      subscribedInteractionClasses.add(interactionClassHandle);
    }

    public void unsubscribe(InteractionClassHandle interactionClassHandle)
      throws RegionNotCreatedByThisFederate
    {
      subscribedInteractionClasses.remove(interactionClassHandle);
    }

    public boolean intersects(Map<DimensionHandle, RangeBounds> rangeBounds)
    {
      boolean intersect = false;

      for (Iterator<Map.Entry<DimensionHandle, RangeBounds>> i =
        rangeBounds.entrySet().iterator(); i.hasNext() && !intersect;)
      {
        Map.Entry<DimensionHandle, RangeBounds> entry = i.next();

        RangeBounds rb = this.rangeBounds.get(entry.getKey());
        intersect = rb != null && intersects(rb, entry.getValue());
      }

      return intersect;
    }

    protected boolean intersects(RangeBounds lhs, RangeBounds rhs)
    {
      return (lhs.lower < rhs.upper && rhs.lower < lhs.upper) ||
             lhs.lower == rhs.lower;
    }

    protected RangeBounds clone(RangeBounds rangeBounds)
    {
      RangeBounds newRangeBounds = new RangeBounds();
      newRangeBounds.lower = rangeBounds.lower;
      newRangeBounds.upper = rangeBounds.upper;
      return newRangeBounds;
    }
  }
}
