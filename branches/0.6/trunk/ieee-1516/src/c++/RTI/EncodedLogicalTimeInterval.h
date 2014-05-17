/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: EncodedLogicalTimeInterval.h
 */

#ifndef EncodedLogicalTimeInterval_h
#define EncodedLogicalTimeInterval_h

#include <SpecificConfig.h>

namespace RTI
{
  RTI_EXPORT class EncodedLogicalTimeInterval
  {
  public:
    virtual ~EncodedLogicalTimeInterval()
      throw() {}

    virtual void const* data() const = 0;
    virtual size_t size() const = 0;
  };
}

#endif // EncodedLogicalTimeInterval_h
