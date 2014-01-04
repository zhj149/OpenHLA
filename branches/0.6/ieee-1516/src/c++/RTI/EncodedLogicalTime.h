/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: EncodedLogicalTime.h
 */

#ifndef EncodedLogicalTime_h
#define EncodedLogicalTime_h

#include <SpecificConfig.h>

namespace RTI
{
  RTI_EXPORT class EncodedLogicalTime
  {
  public:
    virtual ~EncodedLogicalTime()
      throw() {}

    virtual void const* data() const = 0;
    virtual size_t size() const = 0;
  };
}

#endif // EncodedLogicalTime_h
