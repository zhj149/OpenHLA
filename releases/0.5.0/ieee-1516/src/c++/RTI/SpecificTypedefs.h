/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: SpecificTypedefs.h
 */

//
// Purpose: This file contains definitions for RTI types that are specific to the
// RTI implementation. The parameterized classes Handle and Value enable
// the RTI implementation to insert a private member. The private member
// declaration must be exposed in this header file. These implementation specific
// declarations and definitions should not be utilized since it is non-standard
// with respect to RTI implementations.
//
#ifndef SpecificTypedefs_h
#define SpecificTypedefs_h

//
// The following type definitions correspond to standard types used in the API
// (e.g., FederateHandle) that are described through the use of RTI implementation
// specific representations. The following definitions represent an example of
// specific representations that could be used by an RTI implementation.
//
#include <SpecificConfig.h>

// include <Value.h>
template<class T, int i> class Value;

// This class is an example of something that is RTI implementor specific.
#include <VariableLengthValueClass.h>

//
// The existence of these typedefs is required by the API Specification. However,
// their particular definition is implementation-specific.RTI-user code merely
// uses the typedefs. What their underlying type is, however, is up to the RTI
// implementor.
//
typedef Value<VariableLengthValueClass, 1> AttributeValue;
typedef Value<VariableLengthValueClass, 2> ParameterValue;
typedef Value<VariableLengthValueClass, 3> UserSuppliedTag;
typedef Value<VariableLengthValueClass, 4> EncodedData;

// The names of these classes are left up to the RTI implementor.
class FederateHandleFactory;
class ObjectClassHandleFactory;
class InteractionClassHandleFactory;
class ObjectInstanceHandleFactory;
class AttributeHandleFactory;
class ParameterHandleFactory;
class DimensionHandleFactory;
class MessageRetractionHandleFactory;
class RegionHandleFactory;

// include <Handle.h>
template<class ImplementationSpecificHandleClass, class EncodedHandleClass,
         class ImplementationSpecificHandleClassFriend, int i> class Handle;

//
// The existence of these typedefs is required by the API Specification. However,
// their particular definition is implementation-specific.
//
typedef Handle<long, EncodedData, FederateHandleFactory, 1> FederateHandle;
typedef Handle<long, EncodedData, ObjectClassHandleFactory, 2> ObjectClassHandle;
typedef Handle<long, EncodedData, InteractionClassHandleFactory, 3> InteractionClassHandle;
typedef Handle<long, EncodedData, ObjectInstanceHandleFactory, 4> ObjectInstanceHandle;
typedef Handle<long, EncodedData, AttributeHandleFactory, 5> AttributeHandle;
typedef Handle<long, EncodedData, ParameterHandleFactory, 6> ParameterHandle;
typedef Handle<long, EncodedData, DimensionHandleFactory, 7> DimensionHandle;
typedef Handle<long, EncodedData, MessageRetractionHandleFactory, 8> MessageRetractionHandle;
typedef Handle<long, EncodedData, RegionHandleFactory, 11> RegionHandle;

#endif // SpecificTypedefs_h
