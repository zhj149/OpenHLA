package hla.rti1516;

public final class ErrorReadingFDD
  extends RTIexception
{
  public ErrorReadingFDD(String message)
  {
    super(message);
  }

  public ErrorReadingFDD(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ErrorReadingFDD(Throwable cause)
  {
    super(cause);
  }
}
