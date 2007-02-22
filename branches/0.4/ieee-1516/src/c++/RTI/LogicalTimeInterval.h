/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: LogicalTimeInterval.h
 */

#ifndef LogicalTimeInterval_h
#define LogicalTimeInterval_h

//
// The classes associated with logical time allow a federation to provide their
// own representation for logical time and logical time interval. The federation
// is responsible to inherit from the abstract classes declared below. The encoded
// time classes are used to hold the arbitrary bit representation of the logical
// time and logical time intervals.
//

#include <memory>

namespace RTI
{
  class EncodedLogicalTimeInterval;

  class RTI_EXPORT LogicalTimeInterval
  {
  public:
    virtual ~LogicalTimeInterval()
      throw();

    virtual void setZero() = 0;
    virtual bool isZero() = 0;
    virtual bool isEpsilon() = 0;
    virtual void setTo(LogicalTimeInterval const &value)
      throw(InvalidLogicalTimeInterval) = 0;
    virtual std::auto_ptr<LogicalTimeInterval> subtract(LogicalTimeInterval const& subtrahend) const
      throw(InvalidLogicalTimeInterval) = 0;
    virtual bool isGreaterThan(LogicalTimeInterval const &value) const
      throw(InvalidLogicalTimeInterval) = 0;
    virtual bool isLessThan (LogicalTimeInterval const &value) const
      throw(InvalidLogicalTimeInterval) = 0;
    virtual bool isEqualTo (LogicalTimeInterval const &value) const
      throw(InvalidLogicalTimeInterval) = 0;
    virtual bool isGreaterThanOrEqualTo (LogicalTimeInterval const &value) const
      throw(InvalidLogicalTimeInterval) = 0;
    virtual bool isLessThanOrEqualTo (LogicalTimeInterval const &value) const
      throw(InvalidLogicalTimeInterval) = 0;
    virtual std::auto_ptr<EncodedLogicalTimeInterval> encode() const = 0;
    virtual std::wstring toString() const = 0;
  };
}

#endif // LogicalTimeInterval_h
