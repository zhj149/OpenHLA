/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: LogicalTimeIntervalFactory.h
 */

#ifndef LogicalTimeIntervalFactory_h
#define LogicalTimeIntervalFactory_h

//
// The classes associated with logical time allow a federation to provide their
// own representation for logical time and logical time interval. The federation
// is responsible to inherit from the abstract classes declared below. The encoded
// time classes are used to hold the arbitrary bit representation of the logical
// time and logical time intervals.
//

namespace RTI
{
  class RTI_EXPORT LogicalTimeIntervalFactory
  {
  public:
    virtual ~LogicalTimeIntervalFactory() throw();

    virtual std::auto_ptr<LogicalTimeInterval> makeZero() = 0;
    virtual std::auto_ptr<LogicalTimeInterval> epsilon() = 0;
    virtual std::auto_ptr<LogicalTimeInterval> decode(
        EncodedLogicalTimeInterval& const encodedLogicalTimeInterval
    ) throw(CouldNotDecode) = 0;
  };
}

#endif // LogicalTimeIntervalFactory_h
