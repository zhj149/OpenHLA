package hla.rti1516.jlc;

import hla.rti1516.RTIambassador;
import hla.rti1516.RTIinternalError;

public interface RtiFactory
{
  RTIambassador getRtiAmbassador()
    throws RTIinternalError;

  String RtiName();

  String RtiVersion();
}
