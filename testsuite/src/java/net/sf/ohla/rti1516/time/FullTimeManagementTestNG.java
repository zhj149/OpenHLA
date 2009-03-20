package net.sf.ohla.rti1516.time;

import net.sf.ohla.rti.hla.rti1516.Integer64Time;
import net.sf.ohla.rti.hla.rti1516.Integer64TimeInterval;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;

public class FullTimeManagementTestNG
  extends BaseTimeManagementTestNG
{
  protected LogicalTimeInterval lookahead1 = new Integer64TimeInterval(1);
  protected LogicalTimeInterval lookahead2 = new Integer64TimeInterval(2);

  protected Integer64Time zero = new Integer64Time(0);
  protected Integer64Time one = new Integer64Time(1);
  protected Integer64Time two = new Integer64Time(2);
  protected Integer64Time three = new Integer64Time(3);
  protected Integer64Time four = new Integer64Time(4);
  protected Integer64Time five = new Integer64Time(5);
  protected Integer64Time six = new Integer64Time(6);
  protected Integer64Time eight = new Integer64Time(8);
  protected Integer64Time ten = new Integer64Time(10);
  protected Integer64Time eleven = new Integer64Time(11);
  protected Integer64Time twelve = new Integer64Time(12);
  protected Integer64Time fifteen = new Integer64Time(15);
  protected Integer64Time twenty = new Integer64Time(20);
  protected Integer64Time thirty = new Integer64Time(30);
  protected Integer64Time fourty = new Integer64Time(40);
  protected Integer64Time oneHundred = new Integer64Time(100);
  protected ObjectClassHandle testObjectClassHandle;

  protected AttributeHandle attributeHandle1;
  protected AttributeHandle attributeHandle2;
  protected AttributeHandle attributeHandle3;
  protected AttributeHandleSet testObjectAttributeHandles;

  protected InteractionClassHandle testInteractionClassHandle;
  protected ParameterHandle parameterHandle1;
  protected ParameterHandle parameterHandle2;
  protected ParameterHandle parameterHandle3;

  public FullTimeManagementTestNG()
  {
    super(3);
  }

  @BeforeClass
  public void setup()
    throws Exception
  {
    testObjectClassHandle =
      rtiAmbassadors.get(0).getObjectClassHandle(TEST_OBJECT);

    attributeHandle1 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE1);
    attributeHandle2 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE2);
    attributeHandle3 = rtiAmbassadors.get(0).getAttributeHandle(
      testObjectClassHandle, ATTRIBUTE3);

    testObjectAttributeHandles =
      rtiAmbassadors.get(0).getAttributeHandleSetFactory().create();
    testObjectAttributeHandles.add(attributeHandle1);
    testObjectAttributeHandles.add(attributeHandle2);
    testObjectAttributeHandles.add(attributeHandle3);

    rtiAmbassadors.get(0).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(0).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(1).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).publishObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);
    rtiAmbassadors.get(2).subscribeObjectClassAttributes(
      testObjectClassHandle, testObjectAttributeHandles);

    testInteractionClassHandle =
      rtiAmbassadors.get(0).getInteractionClassHandle(TEST_INTERACTION);

    parameterHandle1 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER1);
    parameterHandle2 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER2);
    parameterHandle3 = rtiAmbassadors.get(0).getParameterHandle(
      testInteractionClassHandle, PARAMETER3);

    rtiAmbassadors.get(0).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(0).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(1).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(1).subscribeInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).publishInteractionClass(testInteractionClassHandle);
    rtiAmbassadors.get(2).subscribeInteractionClass(testInteractionClassHandle);
  }

  @Test
  public void testTimeAdvanceRequest()
    throws Exception
  {
    rtiAmbassadors.get(0).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(0).checkTimeRegulationEnabled(zero);

    rtiAmbassadors.get(2).enableTimeRegulation(lookahead1);
    federateAmbassadors.get(2).checkTimeRegulationEnabled(zero);

    rtiAmbassadors.get(1).enableTimeConstrained();
    federateAmbassadors.get(1).checkTimeConstrainedEnabled(zero);

    rtiAmbassadors.get(2).enableTimeConstrained();
    federateAmbassadors.get(2).checkTimeConstrainedEnabled(zero);

    // request advance by regulating-only federate
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(five);

    // should be immediately granted because not constrained
    //
    federateAmbassadors.get(0).checkTimeAdvanceGrant(five);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(three);

    // should NOT be granted because other regulating federate not advanced
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrantNotGranted(zero);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(2).timeAdvanceRequest(two);

    // should be immediately granted because other regulating federate is
    // requesting advance to five
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(two);

    // request advance by regulating-and-constrained federate
    //
    rtiAmbassadors.get(2).timeAdvanceRequest(four);

    // should be immediately granted because other regulating federate is
    // requesting advance to five
    //
    federateAmbassadors.get(2).checkTimeAdvanceGrant(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(three);

    // request advance by constrained-only federate
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(four);

    // should be granted because regulating federates are at four
    //
    federateAmbassadors.get(1).checkTimeAdvanceGrant(four);
  }

  @Test(dependsOnMethods = {"testTimeAdvanceRequest"})
  public void testUpdateAttributeValuesWhileNotTimeAdvancing()
    throws Exception
  {
    ObjectInstanceHandle objectInstanceHandle =
      rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    try
    {
      federateAmbassadors.get(1).checkObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(2).checkObjectInstanceHandle(
        objectInstanceHandle);

      AttributeHandleValueMap attributeValues =
        rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(1);
      attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
      attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
      attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

      rtiAmbassadors.get(0).updateAttributeValues(
        objectInstanceHandle, attributeValues, null);

      // the 2 constrained federates will not receive it because they do not
      // have asynchronous delivery enabled and are not in the time
      // advancing state
      //
      federateAmbassadors.get(1).checkAttributeValuesNotReceived(
        objectInstanceHandle);
      federateAmbassadors.get(2).checkAttributeValuesNotReceived(
        objectInstanceHandle);

      // advance constrained federates so they will be sure to receive the
      // update (because they will be waiting for the remaining regulating
      // federate to advance)
      //
      rtiAmbassadors.get(1).timeAdvanceRequest(six);
      rtiAmbassadors.get(2).timeAdvanceRequest(six);

      // attribute values should have been released
      //
      federateAmbassadors.get(1).checkAttributeValues(
        objectInstanceHandle, attributeValues);
      federateAmbassadors.get(2).checkAttributeValues(
        objectInstanceHandle, attributeValues);

      // finish time advance
      //
      rtiAmbassadors.get(0).timeAdvanceRequest(six);

      federateAmbassadors.get(0).checkTimeAdvanceGrant(six);
      federateAmbassadors.get(1).checkTimeAdvanceGrant(six);
      federateAmbassadors.get(2).checkTimeAdvanceGrant(six);
    }
    finally
    {
      rtiAmbassadors.get(0).deleteObjectInstance(objectInstanceHandle, null);

      // advance constrained federates so they will be sure to receive the
      // update (because they will be waiting for the remaining regulating
      // federate to advance)
      //
      rtiAmbassadors.get(1).timeAdvanceRequest(eight);
      rtiAmbassadors.get(2).timeAdvanceRequest(eight);

      federateAmbassadors.get(1).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(2).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);

      // finish time advance
      //
      rtiAmbassadors.get(0).timeAdvanceRequest(eight);

      federateAmbassadors.get(0).checkTimeAdvanceGrant(eight);
      federateAmbassadors.get(1).checkTimeAdvanceGrant(eight);
      federateAmbassadors.get(2).checkTimeAdvanceGrant(eight);
    }
  }

  @Test(dependsOnMethods = {"testUpdateAttributeValuesWhileNotTimeAdvancing"})
  public void testSendInteractionWhileNotTimeAdvancing()
    throws Exception
  {
    ParameterHandleValueMap parameterValues =
      rtiAmbassadors.get(0).getParameterHandleValueMapFactory().create(1);
    parameterValues.put(parameterHandle1, PARAMETER1_VALUE.getBytes());
    parameterValues.put(parameterHandle2, PARAMETER2_VALUE.getBytes());
    parameterValues.put(parameterHandle3, PARAMETER3_VALUE.getBytes());

    rtiAmbassadors.get(0).sendInteraction(
      testInteractionClassHandle, parameterValues, null);

    // the 2 constrained federates will not receive it because they do not have
    // asynchronous delivery enabled and are not in the time
    // advancing state
    //
    federateAmbassadors.get(1).checkParameterValuesNotReceived();
    federateAmbassadors.get(2).checkParameterValuesNotReceived();

    // advance constrained federates so they will be sure to receive the
    // update (because they will be waiting for the remaining regulating
    // federate to advance)
    //
    rtiAmbassadors.get(1).timeAdvanceRequest(ten);
    rtiAmbassadors.get(2).timeAdvanceRequest(ten);

    // parameter values should have been released
    //
    federateAmbassadors.get(1).checkParameterValues(parameterValues);
    federateAmbassadors.get(2).checkParameterValues(parameterValues);

    // finish time advance
    //
    rtiAmbassadors.get(0).timeAdvanceRequest(ten);

    federateAmbassadors.get(0).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(1).checkTimeAdvanceGrant(ten);
    federateAmbassadors.get(2).checkTimeAdvanceGrant(ten);
  }

  @Test(dependsOnMethods = {"testSendInteractionWhileNotTimeAdvancing"})
  public void testUpdateValuesInTheFuture()
    throws Exception
  {
    ObjectInstanceHandle objectInstanceHandle =
      rtiAmbassadors.get(0).registerObjectInstance(testObjectClassHandle);
    try
    {
      federateAmbassadors.get(1).checkObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(2).checkObjectInstanceHandle(
        objectInstanceHandle);

      AttributeHandleValueMap attributeValues =
        rtiAmbassadors.get(0).getAttributeHandleValueMapFactory().create(1);
      attributeValues.put(attributeHandle1, ATTRIBUTE1_VALUE.getBytes());
      attributeValues.put(attributeHandle2, ATTRIBUTE2_VALUE.getBytes());
      attributeValues.put(attributeHandle3, ATTRIBUTE3_VALUE.getBytes());

      rtiAmbassadors.get(0).updateAttributeValues(
        objectInstanceHandle, attributeValues, null, twelve);

      // the 2 constrained federates will not receive it because they have not
      // advanced to the scheduled time
      //
      federateAmbassadors.get(1).checkAttributeValuesNotReceived(
        objectInstanceHandle);
      federateAmbassadors.get(2).checkAttributeValuesNotReceived(
        objectInstanceHandle);

      rtiAmbassadors.get(0).timeAdvanceRequest(eleven);
      rtiAmbassadors.get(1).timeAdvanceRequest(eleven);
      rtiAmbassadors.get(2).timeAdvanceRequest(eleven);

      federateAmbassadors.get(0).checkTimeAdvanceGrant(eleven);
      federateAmbassadors.get(1).checkTimeAdvanceGrant(eleven);
      federateAmbassadors.get(2).checkTimeAdvanceGrant(eleven);

      // the 2 constrained federates will not receive it because they have not
      // advanced to the scheduled time
      //
      federateAmbassadors.get(1).checkAttributeValuesNotReceived(
        objectInstanceHandle);
      federateAmbassadors.get(2).checkAttributeValuesNotReceived(
        objectInstanceHandle);

      // bring all the federates to the same time
      //
      rtiAmbassadors.get(0).timeAdvanceRequest(twelve);
      rtiAmbassadors.get(1).timeAdvanceRequest(twelve);
      rtiAmbassadors.get(2).timeAdvanceRequest(twelve);

      federateAmbassadors.get(0).checkTimeAdvanceGrant(twelve);
      federateAmbassadors.get(1).checkTimeAdvanceGrant(twelve);
      federateAmbassadors.get(2).checkTimeAdvanceGrant(twelve);

      // attribute values should have been released
      //
      federateAmbassadors.get(1).checkAttributeValues(
        objectInstanceHandle, attributeValues);
      federateAmbassadors.get(2).checkAttributeValues(
        objectInstanceHandle, attributeValues);
    }
    finally
    {
      rtiAmbassadors.get(0).deleteObjectInstance(objectInstanceHandle, null);

      // advance constrained federates so they will be sure to receive the
      // update (because they will be waiting for the remaining regulating
      // federate to advance)
      //
      rtiAmbassadors.get(1).timeAdvanceRequest(fifteen);
      rtiAmbassadors.get(2).timeAdvanceRequest(fifteen);

      federateAmbassadors.get(1).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);
      federateAmbassadors.get(2).checkForRemovedObjectInstanceHandle(
        objectInstanceHandle);

      // finish time advance
      //
      rtiAmbassadors.get(0).timeAdvanceRequest(fifteen);

      federateAmbassadors.get(0).checkTimeAdvanceGrant(fifteen);
      federateAmbassadors.get(1).checkTimeAdvanceGrant(fifteen);
      federateAmbassadors.get(2).checkTimeAdvanceGrant(fifteen);
    }
  }
}
