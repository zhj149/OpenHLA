/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: LogicalTimeFactory.h
 */

#ifndef LogicalTimeFactory_h
#define LogicalTimeFactory_h

//
// The classes associated with logical time allow a federation to provide their
// own representation for logical time and logical time interval. The federation
// is responsible to inherit from the abstract classes declared below. The encoded
// time classes are used to hold the arbitrary bit representation of the logical
// time and logical time intervals.
//

namespace RTI
{
  class LogicalTime;
  class CouldNotDecode;
  class InternalError;

  class RTI_EXPORT LogicalTimeFactory
  {
  public:
    virtual ~LogicalTimeFactory()
      throw();

    virtual std::auto_ptr<LogicalTime> makeInitial()
      throw(InternalError) = 0;
    virtual std::auto_ptr<LogicalTime> decode(EncodedLogicalTime const& encodedLogicalTime)
      throw(InternalError, CouldNotDecode) = 0;
  };
}

#endif // LogicalTimeFactory_h
