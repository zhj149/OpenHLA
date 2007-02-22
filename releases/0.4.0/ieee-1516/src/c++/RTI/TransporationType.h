/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: TransportationType.h
 */

#ifndef TransportationType_h
#define TransportationType_h

#include <SpecificConfig.h>
#include <SpecificTypedefs.h> // for EncodedData

#include <string>

// Type safe class used to represent type of data transportation.

namespace RTI
{
  class RTI_EXPORT TransportationType
  {
  public:
    TransportationType(EncodedData const& rhs);
    TransportationType(TransportationType const& rhs)
      throw();

    static TransportationType const reliable()
      throw();
    static TransportationType const bestEffort()
      throw();

    EncodedData encode() const
      throw();

    std::wstring toString() const;

    TransportationType& operator=(TransportationType const& rhs)
      throw();
    bool operator==(TransportationType const& rhs) const
      throw();
    bool operator!=(TransportationType const& rhs) const
      throw();

  private:
    TransportationType (unsigned transportationType)
      throw();
    unsigned _transportationType;
  };

  //
  // These constants save a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  TransportationType const RELIABLE =
    TransportationType::reliable();
  TransportationType const BEST_EFFORT =
    TransportationType::bestEffort();

#ifdef USE_INLINE
#include "TransportationType.i"
#endif // USE_INLINE

}

#endif // TransportationType_h
