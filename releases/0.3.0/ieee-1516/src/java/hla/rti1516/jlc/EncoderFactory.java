package hla.rti1516.jlc;

import hla.rti1516.LogicalTime;

/**
 * Factory for the various HLA data types.
 */
public interface EncoderFactory
{
  HLAASCIIchar createHLAASCIIchar();

  HLAASCIIchar createHLAASCIIchar(byte b);

  HLAASCIIstring createHLAASCIIstring();

  HLAASCIIstring createHLAASCIIstring(String s);

  HLAboolean createHLAboolean();

  HLAboolean createHLAboolean(boolean b);

  HLAbyte createHLAbyte();

  HLAbyte createHLAbyte(byte b);

  HLAfixedRecord createHLAfixedRecord();

  HLAfloat32BE createHLAfloat32BE();

  HLAfloat32BE createHLAfloat32BE(float f);

  HLAfloat32LE createHLAfloat32LE();

  HLAfloat32LE createHLAfloat32LE(float f);

  HLAfloat64BE createHLAfloat64BE();

  HLAfloat64BE createHLAfloat64BE(double d);

  HLAfloat64LE createHLAfloat64LE();

  HLAfloat64LE createHLAfloat64LE(double d);

  HLAhandle createHLAhandle();

  HLAhandle createHLAhandle(byte[] b);

  HLAinteger16BE createHLAinteger16BE();

  HLAinteger16BE createHLAinteger16BE(short s);

  HLAinteger16LE createHLAinteger16LE();

  HLAinteger16LE createHLAinteger16LE(short s);

  HLAinteger32BE createHLAinteger32BE();

  HLAinteger32BE createHLAinteger32BE(int i);

  HLAinteger32LE createHLAinteger32LE();

  HLAinteger32LE createHLAinteger32LE(int i);

  HLAinteger64BE createHLAinteger64BE();

  HLAinteger64BE createHLAinteger64BE(long l);

  HLAinteger64LE createHLAinteger64LE();

  HLAinteger64LE createHLAinteger64LE(long l);

  HLAlogicalTime createHLAlogicalTime();

  HLAlogicalTime createHLAlogicalTime(LogicalTime t);

  HLAoctet createHLAoctet();

  HLAoctet createHLAoctet(byte b);

  HLAoctetPairBE createHLAoctetPairBE();

  HLAoctetPairBE createHLAoctetPairBE(short s);

  HLAoctetPairLE createHLAoctetPairLE();

  HLAoctetPairLE createHLAoctetPairLE(short s);

  HLAopaqueData createHLAopaqueData();

  HLAopaqueData createHLAopaqueData(byte[] b);

  HLAunicodeString createHLAunicodeString();

  HLAunicodeString createHLAunicodeString(String s);

  HLAvariableArray createHLAvariableArray();

  HLAvariableArray createHLAvariableArray(DataElementFactory factory);
}
