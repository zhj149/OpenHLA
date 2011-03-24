/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: 1516.h
 */

//
// This file is simply a convenience provided for those developers that would like
// to include everything all at once
//
#ifndef 1516_h
#define 1516_h

#include <string>
#include <set>
#include <map>
#include <vector>
#include <memory>

// This file contains platform specific configuration info.
#include <SpecificConfig.h>

// This file contains standard RTI type declarations/definitions.
#include <exception.h>

#include <Handle.h>
#include <Value.h>
#include <ResignAction.h>
#include <TransportationType.h>
#include <OrderType.h>
#include <SaveFailureReason.h>
#include <SaveStatus.h>
#include <SynchronizationFailureReason.h>
#include <RestoreStatus.h>
#include <RestoreFailureReason.h>
#include <ServiceGroupIndicator.h>
#include <RangeBounds.h>

// This file has RTI implementation specific declarations/definitions.
#include <SpecificTypedefs.h>

//
// This file contains standard RTI type declarations/definitions which depend on
// RTI implementation specific declarations/definitions.
//
#include <Typedefs.h>

#include <LogicalTime.h>
#include <LogicalTimeFactory.h>
#include <LogicalTimeInterval.h>
#include <LogicalTimeIntervalFactory.h>

namespace RTI
{
  static char const* const HLA_VERSION = "1516.1.5";
}

#include <FederateAmbassador.h>
#include <RTIambassador.h>

//
// This file provides RTI implementation specific decalarations and definitions
// that need to follow the other header files.
//
#include <SpecificPostamble.h>

#endif // 1516_h
