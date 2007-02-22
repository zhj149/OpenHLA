package hla.rti1516.jlc.omt;

import net.sf.ohla.rti1516.jlc.impl.OHLAHLAASCIIchar;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAASCIIstring;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAboolean;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAbyte;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAfixedRecord;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAfloat32BE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAfloat32LE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAfloat64BE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAfloat64LE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAhandle;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAinteger16BE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAinteger16LE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAinteger32BE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAinteger32LE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAinteger64BE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAinteger64LE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAlogicalTime;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAoctet;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAoctetPairBE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAunicodeString;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAopaqueData;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAoctetPairLE;
import net.sf.ohla.rti1516.jlc.impl.OHLAHLAvariableArray;

import hla.rti1516.LogicalTime;
import hla.rti1516.jlc.DataElementFactory;
import hla.rti1516.jlc.EncoderFactory;
import hla.rti1516.jlc.HLAASCIIchar;
import hla.rti1516.jlc.HLAASCIIstring;
import hla.rti1516.jlc.HLAboolean;
import hla.rti1516.jlc.HLAbyte;
import hla.rti1516.jlc.HLAfixedRecord;
import hla.rti1516.jlc.HLAfloat32BE;
import hla.rti1516.jlc.HLAfloat32LE;
import hla.rti1516.jlc.HLAfloat64BE;
import hla.rti1516.jlc.HLAfloat64LE;
import hla.rti1516.jlc.HLAhandle;
import hla.rti1516.jlc.HLAinteger16BE;
import hla.rti1516.jlc.HLAinteger16LE;
import hla.rti1516.jlc.HLAinteger32BE;
import hla.rti1516.jlc.HLAinteger32LE;
import hla.rti1516.jlc.HLAinteger64BE;
import hla.rti1516.jlc.HLAinteger64LE;
import hla.rti1516.jlc.HLAlogicalTime;
import hla.rti1516.jlc.HLAoctet;
import hla.rti1516.jlc.HLAoctetPairBE;
import hla.rti1516.jlc.HLAoctetPairLE;
import hla.rti1516.jlc.HLAopaqueData;
import hla.rti1516.jlc.HLAunicodeString;
import hla.rti1516.jlc.HLAvariableArray;

public class OMTEncoderFactory
  implements EncoderFactory
{
  private static final EncoderFactory OMT_FACTORY = new OMTEncoderFactory();

  public static EncoderFactory getInstance()
  {
    return OMT_FACTORY;
  }

  public HLAinteger32BE createHLAinteger32BE()
  {
    return new OHLAHLAinteger32BE();
  }

  public HLAinteger32BE createHLAinteger32BE(int myNumber)
  {
    return new OHLAHLAinteger32BE(myNumber);
  }

  public HLAinteger32LE createHLAinteger32LE()
  {
    return new OHLAHLAinteger32LE();
  }

  public HLAinteger32LE createHLAinteger32LE(int i)
  {
    return new OHLAHLAinteger32LE(i);
  }

  public HLAinteger64BE createHLAinteger64BE()
  {
    return new OHLAHLAinteger64BE();
  }

  public HLAinteger64BE createHLAinteger64BE(long l)
  {
    return new OHLAHLAinteger64BE(l);
  }

  public HLAinteger64LE createHLAinteger64LE()
  {
    return new OHLAHLAinteger64LE();
  }

  public HLAinteger64LE createHLAinteger64LE(long l)
  {
    return new OHLAHLAinteger64LE(l);
  }

  public HLAlogicalTime createHLAlogicalTime()
  {
    return new OHLAHLAlogicalTime();
  }

  public HLAlogicalTime createHLAlogicalTime(LogicalTime logicalTime)
  {
    byte[] buffer = new byte[logicalTime.encodedLength()];
    logicalTime.encode(buffer, 0);
    return new OHLAHLAlogicalTime(buffer);
  }

  public HLAoctet createHLAoctet()
  {
    return new OHLAHLAoctet();
  }

  public HLAoctet createHLAoctet(byte b)
  {
    return new OHLAHLAoctet(b);
  }

  public HLAoctetPairBE createHLAoctetPairBE()
  {
    return new OHLAHLAoctetPairBE();
  }

  public HLAoctetPairBE createHLAoctetPairBE(short s)
  {
    return new OHLAHLAoctetPairBE(s);
  }

  public HLAoctetPairLE createHLAoctetPairLE()
  {
    return new OHLAHLAoctetPairLE();
  }

  public HLAoctetPairLE createHLAoctetPairLE(short s)
  {
    return new OHLAHLAoctetPairLE(s);
  }

  public HLAopaqueData createHLAopaqueData()
  {
    return new OHLAHLAopaqueData();
  }

  public HLAopaqueData createHLAopaqueData(byte[] b)
  {
    return new OHLAHLAopaqueData(b);
  }

  public HLAunicodeString createHLAunicodeString()
  {
    return new OHLAHLAunicodeString();
  }

  public HLAunicodeString createHLAunicodeString(String s)
  {
    return new OHLAHLAunicodeString(s);
  }

  public HLAASCIIchar createHLAASCIIchar()
  {
    return new OHLAHLAASCIIchar();
  }

  public HLAASCIIchar createHLAASCIIchar(byte b)
  {
    return new OHLAHLAASCIIchar(b);
  }

  public HLAASCIIstring createHLAASCIIstring()
  {
    return new OHLAHLAASCIIstring();
  }

  public HLAASCIIstring createHLAASCIIstring(String s)
  {
    return new OHLAHLAASCIIstring(s);
  }

  public HLAboolean createHLAboolean()
  {
    return new OHLAHLAboolean();
  }

  public HLAboolean createHLAboolean(boolean b)
  {
    return new OHLAHLAboolean(b);
  }

  public HLAbyte createHLAbyte()
  {
    return new OHLAHLAbyte();
  }

  public HLAbyte createHLAbyte(byte b)
  {
    return new OHLAHLAbyte(b);
  }

  public HLAfixedRecord createHLAfixedRecord()
  {
    return new OHLAHLAfixedRecord();
  }

  public HLAfloat32BE createHLAfloat32BE()
  {
    return new OHLAHLAfloat32BE();
  }

  public HLAfloat32BE createHLAfloat32BE(float f)
  {
    return new OHLAHLAfloat32BE(f);
  }

  public HLAfloat32LE createHLAfloat32LE()
  {
    return new OHLAHLAfloat32LE();
  }

  public HLAfloat32LE createHLAfloat32LE(float f)
  {
    return new OHLAHLAfloat32LE(f);
  }

  public HLAfloat64BE createHLAfloat64BE()
  {
    return new OHLAHLAfloat64BE();
  }

  public HLAfloat64BE createHLAfloat64BE(double d)
  {
    return new OHLAHLAfloat64BE(d);
  }

  public HLAfloat64LE createHLAfloat64LE()
  {
    return new OHLAHLAfloat64LE();
  }

  public HLAfloat64LE createHLAfloat64LE(double d)
  {
    return new OHLAHLAfloat64LE(d);
  }

  public HLAhandle createHLAhandle()
  {
    return new OHLAHLAhandle();
  }

  public HLAhandle createHLAhandle(byte[] b)
  {
    return new OHLAHLAhandle(b);
  }

  public HLAinteger16BE createHLAinteger16BE()
  {
    return new OHLAHLAinteger16BE();
  }

  public HLAinteger16BE createHLAinteger16BE(short s)
  {
    return new OHLAHLAinteger16BE(s);
  }

  public HLAinteger16LE createHLAinteger16LE()
  {
    return new OHLAHLAinteger16LE();
  }

  public HLAinteger16LE createHLAinteger16LE(short s)
  {
    return new OHLAHLAinteger16LE(s);
  }

  public HLAvariableArray createHLAvariableArray()
  {
    return new OHLAHLAvariableArray();
  }

  public HLAvariableArray createHLAvariableArray(DataElementFactory factory)
  {
    return new OHLAHLAvariableArray(factory);
  }
}
