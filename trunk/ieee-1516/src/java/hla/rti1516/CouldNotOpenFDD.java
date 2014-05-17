package hla.rti1516;

public final class CouldNotOpenFDD
  extends RTIexception
{
  public CouldNotOpenFDD(String message)
  {
    super(message);
  }

  public CouldNotOpenFDD(String message, Throwable cause)
  {
    super(message, cause);
  }

  public CouldNotOpenFDD(Throwable cause)
  {
    super(cause);
  }
}
