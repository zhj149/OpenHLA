/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: Typedefs.h
 */

// Purpose: This file contains the standard RTI types that are prefixed
// with the string "RTI". These definitions/declarations are standard
// for all RTI implementations.
//
// The types declared here require the use of some Specific types.

#ifndef Typedefs_h
#define Typedefs_h

#include <set>
#include <map>
#include <vector>

#include <SpecificTypedefs.h>

namespace RTI
{
  typedef std::set<AttributeHandle> AttributeHandleSet;
  typedef std::set<ParameterHandle> ParameterHandleSet;
  typedef std::set<FederateHandle> FederateHandleSet;
  typedef std::set<DimensionHandle> DimensionHandleSet;
  typedef std::set<RegionHandle> RegionHandleSet;

  // AttributeHandleValueMap implements a constrained set of
  // (attribute handle and value) pairs
  typedef std::map<AttributeHandle, AttributeValue> AttributeHandleValueMap;

  // ParameterHandleValueMap implements a constrained set of
  // (parameter handle and value) pairs
  typedef std::map<ParameterHandle, VariableLengthData> ParameterHandleValueMap;

  // AttributeHandleSetRegionHandleSetPairVector implements a collection of
  // (attribute handle set and region set) pairs
  typedef std::pair<AttributeHandleSet, RegionHandleSet> AttributeHandleSetRegionHandleSetPair;
  typedef std::vector<AttributeHandleSetRegionHandleSetPair> AttributeHandleSetRegionHandleSetPairVector;

  // FederateHandleSaveStatusPairVector implements a collection of
  // (federate handle and save status) pairs
  typedef std::pair<FederateHandle, SaveStatus> FederateHandleSaveStatusPair;
  typedef std::vector<FederateHandleSaveStatusPair> FederateHandleSaveStatusPairVector;

  // FederateHandleRestoreStatusPairVector implements a collection of
  // (federate handle and restore status) pairs
  typedef std::pair<FederateHandle, RestoreStatus> FederateHandleRestoreStatusPair;
  typedef std::vector<FederateHandleRestoreStatusPair> FederateHandleRestoreStatusPairVector;
}

#endif // Typedefs_h
