package hla.rti1516.jlc.omt;

import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAASCIIchar;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAASCIIstring;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAboolean;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAbyte;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAfixedRecord;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAfloat32BE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAfloat32LE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAfloat64BE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAfloat64LE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAhandle;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAinteger16BE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAinteger16LE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAinteger32BE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAinteger32LE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAinteger64BE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAinteger64LE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAlogicalTime;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAoctet;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAoctetPairBE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAunicodeString;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAopaqueData;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAoctetPairLE;
import net.sf.ohla.rti.hla.rti1516.jlc.IEEE1516HLAvariableArray;

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
    return new IEEE1516HLAinteger32BE();
  }

  public HLAinteger32BE createHLAinteger32BE(int myNumber)
  {
    return new IEEE1516HLAinteger32BE(myNumber);
  }

  public HLAinteger32LE createHLAinteger32LE()
  {
    return new IEEE1516HLAinteger32LE();
  }

  public HLAinteger32LE createHLAinteger32LE(int i)
  {
    return new IEEE1516HLAinteger32LE(i);
  }

  public HLAinteger64BE createHLAinteger64BE()
  {
    return new IEEE1516HLAinteger64BE();
  }

  public HLAinteger64BE createHLAinteger64BE(long l)
  {
    return new IEEE1516HLAinteger64BE(l);
  }

  public HLAinteger64LE createHLAinteger64LE()
  {
    return new IEEE1516HLAinteger64LE();
  }

  public HLAinteger64LE createHLAinteger64LE(long l)
  {
    return new IEEE1516HLAinteger64LE(l);
  }

  public HLAlogicalTime createHLAlogicalTime()
  {
    return new IEEE1516HLAlogicalTime();
  }

  public HLAlogicalTime createHLAlogicalTime(LogicalTime logicalTime)
  {
    byte[] buffer = new byte[logicalTime.encodedLength()];
    logicalTime.encode(buffer, 0);
    return new IEEE1516HLAlogicalTime(buffer);
  }

  public HLAoctet createHLAoctet()
  {
    return new IEEE1516HLAoctet();
  }

  public HLAoctet createHLAoctet(byte b)
  {
    return new IEEE1516HLAoctet(b);
  }

  public HLAoctetPairBE createHLAoctetPairBE()
  {
    return new IEEE1516HLAoctetPairBE();
  }

  public HLAoctetPairBE createHLAoctetPairBE(short s)
  {
    return new IEEE1516HLAoctetPairBE(s);
  }

  public HLAoctetPairLE createHLAoctetPairLE()
  {
    return new IEEE1516HLAoctetPairLE();
  }

  public HLAoctetPairLE createHLAoctetPairLE(short s)
  {
    return new IEEE1516HLAoctetPairLE(s);
  }

  public HLAopaqueData createHLAopaqueData()
  {
    return new IEEE1516HLAopaqueData();
  }

  public HLAopaqueData createHLAopaqueData(byte[] b)
  {
    return new IEEE1516HLAopaqueData(b);
  }

  public HLAunicodeString createHLAunicodeString()
  {
    return new IEEE1516HLAunicodeString();
  }

  public HLAunicodeString createHLAunicodeString(String s)
  {
    return new IEEE1516HLAunicodeString(s);
  }

  public HLAASCIIchar createHLAASCIIchar()
  {
    return new IEEE1516HLAASCIIchar();
  }

  public HLAASCIIchar createHLAASCIIchar(byte b)
  {
    return new IEEE1516HLAASCIIchar(b);
  }

  public HLAASCIIstring createHLAASCIIstring()
  {
    return new IEEE1516HLAASCIIstring();
  }

  public HLAASCIIstring createHLAASCIIstring(String s)
  {
    return new IEEE1516HLAASCIIstring(s);
  }

  public HLAboolean createHLAboolean()
  {
    return new IEEE1516HLAboolean();
  }

  public HLAboolean createHLAboolean(boolean b)
  {
    return new IEEE1516HLAboolean(b);
  }

  public HLAbyte createHLAbyte()
  {
    return new IEEE1516HLAbyte();
  }

  public HLAbyte createHLAbyte(byte b)
  {
    return new IEEE1516HLAbyte(b);
  }

  public HLAfixedRecord createHLAfixedRecord()
  {
    return new IEEE1516HLAfixedRecord();
  }

  public HLAfloat32BE createHLAfloat32BE()
  {
    return new IEEE1516HLAfloat32BE();
  }

  public HLAfloat32BE createHLAfloat32BE(float f)
  {
    return new IEEE1516HLAfloat32BE(f);
  }

  public HLAfloat32LE createHLAfloat32LE()
  {
    return new IEEE1516HLAfloat32LE();
  }

  public HLAfloat32LE createHLAfloat32LE(float f)
  {
    return new IEEE1516HLAfloat32LE(f);
  }

  public HLAfloat64BE createHLAfloat64BE()
  {
    return new IEEE1516HLAfloat64BE();
  }

  public HLAfloat64BE createHLAfloat64BE(double d)
  {
    return new IEEE1516HLAfloat64BE(d);
  }

  public HLAfloat64LE createHLAfloat64LE()
  {
    return new IEEE1516HLAfloat64LE();
  }

  public HLAfloat64LE createHLAfloat64LE(double d)
  {
    return new IEEE1516HLAfloat64LE(d);
  }

  public HLAhandle createHLAhandle()
  {
    return new IEEE1516HLAhandle();
  }

  public HLAhandle createHLAhandle(byte[] b)
  {
    return new IEEE1516HLAhandle(b);
  }

  public HLAinteger16BE createHLAinteger16BE()
  {
    return new IEEE1516HLAinteger16BE();
  }

  public HLAinteger16BE createHLAinteger16BE(short s)
  {
    return new IEEE1516HLAinteger16BE(s);
  }

  public HLAinteger16LE createHLAinteger16LE()
  {
    return new IEEE1516HLAinteger16LE();
  }

  public HLAinteger16LE createHLAinteger16LE(short s)
  {
    return new IEEE1516HLAinteger16LE(s);
  }

  public HLAvariableArray createHLAvariableArray()
  {
    return new IEEE1516HLAvariableArray();
  }

  public HLAvariableArray createHLAvariableArray(DataElementFactory factory)
  {
    return new IEEE1516HLAvariableArray(factory);
  }
}
