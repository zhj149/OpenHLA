/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: LogicalTime.h
 */

#ifndef LogicalTime_h
#define LogicalTime_h

class LogicalTimeInterval;

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
  class EncodedLogicalTime;

  class RTI_EXPORT LogicalTime
  {
  public:
    virtual ~LogicalTime()
      throw();

    virtual void setInitial() = 0;
    virtual bool isInitial() = 0;
    virtual void setFinal() = 0;
    virtual bool isFinal() = 0;
    virtual void setTo(LogicalTime const& value)
      throw(InvalidLogicalTime) = 0;
    virtual void increaseBy(LogicalTimeInterval const& addend)
      throw(IllegalTimeArithmetic, InvalidLogicalTimeInterval) = 0;
    virtual void decreaseBy(LogicalTimeInterval const& subtrahend)
      throw(IllegalTimeArithmetic, InvalidLogicalTimeInterval) = 0;
    virtual std::auto_ptr<LogicalTimeInterval> subtract(LogicalTime const& subtrahend) const
      throw(InvalidLogicalTime) = 0;
    virtual bool isGreaterThan(LogicalTime const& value) const
      throw(InvalidLogicalTime) = 0;
    virtual bool isLessThan(LogicalTime const& value) const
      throw(InvalidLogicalTime) = 0;
    virtual bool isEqualTo(LogicalTime const& value) const
      throw(InvalidLogicalTime) = 0;
    virtual bool isGreaterThanOrEqualTo(LogicalTime const& value) const
      throw(InvalidLogicalTime) = 0;
    virtual bool isLessThanOrEqualTo(LogicalTime const& value) const
      throw(InvalidLogicalTime) = 0;
    virtual std::auto_ptr<EncodedLogicalTime> encode() const = 0;
    virtual wstring toString() const = 0;
  };
}

#endif // LogicalTime_h
