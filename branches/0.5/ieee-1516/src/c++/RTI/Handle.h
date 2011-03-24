/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: Handle.h
 */

#ifndef Handle_h
#define Handle_h

#include <SpecificConfig.h>

#include <string>

namespace RTI
{
  //
  // The RTIhandle class is used to provide the common interface to the different
  // RTI handle types used in the API. This interface includes a constructor,
  // assignment, equality, inequality, and less than operators. The encode method
  // returns a type safe EncodedHandleClass instance that can be used to exchange
  // handles between federates as attributes and parameters. The constructor takes a
  // EncodedHandleClass which enables the type safe creation of a RTIhandle from
  // encoded data passed to a federate. The template parameter class
  // ImplementationSpecificHandleClass provides RTI implementations the ability to
  // customize a private class member for their particular implementation.The int
  // template type parameter provides the ability to support strong typing.
  //
  template<class ImplementationSpecificHandleClass, class EncodedHandleClass,
           class ImplementationSpecificHandleClassFriend, int i>
    class RTI_EXPORT Handle
    {
    public:
      explicit Handle(EncodedHandleClass encodedHandle);
      Handle(Handle const& rhs);
      ~Handle()
        throw();

      EncodedHandleClass encode() const;

      std::wstring toString() const;

      Handle& operator=(Handle const& rhs);
      bool operator==(Handle const& rhs) const;
      bool operator!=(Handle const& rhs) const;
      bool operator<(Handle const& rhs) const;

    private:
      ImplementationSpecificHandleClass _impl;

      // This class is the only class which can construct an Handle
      friend ImplementationSpecificHandleClassFriend;

      Handle(ImplementationSpecificHandleClass const& impl);
    };

#ifdef USE_INLINE
#include "Handle.i"
#endif // USE_INLINE

#ifdef TEMPLATES_REQUIRE_SOURCE
#include "Handle.cpp"
#endif // TEMPLATES_REQUIRE_SOURCE
}

#endif // Handle_h
