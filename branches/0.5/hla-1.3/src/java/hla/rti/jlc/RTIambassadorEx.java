package hla.rti.jlc;

import hla.rti.RTIambassador;
import hla.rti.RTIinternalError;
import hla.rti.ConcurrentAccessAttempted;

public interface RTIambassadorEx
  extends RTIambassador
{
  boolean tick(final double min, final double max)
    throws RTIinternalError,
           ConcurrentAccessAttempted;
}