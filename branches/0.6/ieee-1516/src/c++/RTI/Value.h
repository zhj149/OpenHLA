/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: Value.h
 */

#ifndef Value_h
#define Value_h

#include <SpecificConfig.h>

//
// The Value class is used to as a generic value holder that contains a
// pointer to the memory location and the size of the value. This class is used
// for attributes, parameters, and user supplied tags. The constructor takes a
// pointer to the data value and the size of the data. The key methods on this
// class is the data method which returns a constant pointer to the value memory
// location, and the size method which returns the size in bytes of the value. The
// templatized class T provides RTI implementations the ability to customize their
// particular implementation. The int template type parameter provides the ability
// to support strong typing.
//

namespace RTI
{
  template<class T, int i> RTI_EXPORT class Value
  {
  public:
    Value(void const* data, size_t size);
    Value(Value const& rhs);
    ~Value()
      throw();

    void const* data() const;
    size_t size() const;

    Value& operator=(Value const& rhs);

  private:
    T _impl;
  };

#ifdef USE_INLINE
#include "Value.i"
#endif // USE_INLINE

#ifdef TEMPLATES_REQUIRE_SOURCE
#include "Value.cpp"
#endif // TEMPLATES_REQUIRE_SOURCE

}

#endif // Value_h
