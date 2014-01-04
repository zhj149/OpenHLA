/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: ServiceGroupIndicator.h
 */

#ifndef ServiceGroupIndicator_h
#define ServiceGroupIndicator_h

#include <SpecificConfig.h>

#include <string>

// Type safe class used to represent the service group

namespace RTI
{
  class RTI_EXPORT ServiceGroupIndicator
  {
  public:
    ServiceGroupIndicator(ServiceGroupIndicator const& rhs);

    static ServiceGroupIndicator const federationManagement();
    static ServiceGroupIndicator const declarationManagement();
    static ServiceGroupIndicator const objectManagement();
    static ServiceGroupIndicator const ownershipManagement();
    static ServiceGroupIndicator const timeManagement();
    static ServiceGroupIndicator const dataDistributionManagement();
    static ServiceGroupIndicator const supportServices();

    std::wstring toString() const;

    ServiceGroupIndicator& operator=(ServiceGroupIndicator const& rhs);
    bool operator==(ServiceGroupIndicator const& rhs) const;
    bool operator!=(ServiceGroupIndicator const& rhs) const;

  private:
    ServiceGroupIndicator(unsigned _ServiceGroupIndicator);
    unsigned _ServiceGroupIndicator;
  };

  //
  // These constants save a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  ServiceGroupIndicator const FEDERATION_MANAGEMENT =
    ServiceGroupIndicator::federationManagement();
  ServiceGroupIndicator const DECLARATION_MANAGEMENT =
    ServiceGroupIndicator::declarationManagement();
  ServiceGroupIndicator const OBJECT_MANAGEMENT =
    ServiceGroupIndicator::objectManagement();
  ServiceGroupIndicator const OWNERSHIP_MANAGEMENT =
    ServiceGroupIndicator::ownershipManagement();
  ServiceGroupIndicator const TIME_MANAGEMENT =
    ServiceGroupIndicator::timeManagement();
  ServiceGroupIndicator const DATA_DISTRIBUTION_MANAGEMENT =
    ServiceGroupIndicator::dataDistributionManagement();
  ServiceGroupIndicator const SUPPORT_SERVICES =
    ServiceGroupIndicator::supportServices();

#ifdef USE_INLINE
#include "ServiceGroupIndicator.i"
#endif // USE_INLINE

}

#endif // ServiceGroupIndicator_h
