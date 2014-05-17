/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: RangeBounds.h
 */

#ifndef RangeBounds_h
#define RangeBounds_h

#include <SpecificConfig.h>

namespace RTI
{
  class RTI_EXPORT RangeBounds
  {
  public:
    RangeBounds();
    RangeBounds(unsigned long lowerBound, unsigned long upperBound);
    RangeBounds(RangeBounds const& rhs);
    ~RangeBounds()
      throw();

    unsigned long getLowerBound() const;
    unsigned long getUpperBound() const;
    void setLowerBound(unsigned long lowerBound);
    void setUpperBound(unsigned long upperBound);

    RangeBounds &operator=(RangeBounds const& rhs);

  private:
    unsigned long _lowerBound;
    unsigned long _upperBound;
  };

#ifdef USE_INLINE
#include "RangeBounds.i"
#endif // USE_INLINE

}

#endif // RangeBounds_h
