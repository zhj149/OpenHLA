package hla.rti.jlc;

import hla.rti.AttributeHandleSet;
import hla.rti.FederateHandleSet;
import hla.rti.RTIinternalError;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;

public interface RtiFactory
{
  RTIambassadorEx createRtiAmbassador()
    throws RTIinternalError;

  AttributeHandleSet createAttributeHandleSet();

  FederateHandleSet createFederateHandleSet();

  SuppliedAttributes createSuppliedAttributes();

  SuppliedParameters createSuppliedParameters();

  String RtiName();

  String RtiVersion();

  long getMinExtent();

  long getMaxExtent();
}