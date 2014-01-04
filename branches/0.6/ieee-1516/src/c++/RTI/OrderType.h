/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: OrderType.h
 */

#ifndef OrderType_h
#define OrderType_h

#include <SpecificConfig.h>
#include <SpecificTypedefs.h> // for EncodedData

#include <string>

// Type safe class used to represent type of data order.

namespace RTI
{
  class RTI_EXPORT OrderType
  {
  public:
    OrderType(EncodedData const& theEncodedOrderType);
    OrderType(OrderType const& rhs)
      throw();

    static OrderType const receive () throw();
    static OrderType const timestamp () throw();

    EncodedData encode() const
      throw();

    std::wstring toString() const;

    OrderType& operator=(OrderType const& rhs)
      throw();
    bool operator==(OrderType const& rhs) const
      throw();
    bool operator!=(OrderType const& rhs) const
      throw();

  private:
    OrderType(unsigned orderType)
      throw();
    unsigned _orderType;
  };

  //
  // These constants save a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  OrderType const RECEIVE = OrderType::receive();
  OrderType const TIMESTAMP = OrderType::timestamp();

#ifdef USE_INLINE
#include "OrderType.i"
#endif // USE_INLINE

}

#endif // OrderType_h
