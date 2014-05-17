/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: RTI/SpecificConfig.h
 */

// Purpose: This file contains definitions that are used to isolate
// platform-specific elements of the API.  It is not implementation-specific.

#ifndef RTI_SpecificConfig_h
#define RTI_SpecificConfig_h

#ifndef _WIN32
// no special qualfifers are necessary on non-WIN32 platforms
#define RTI_EXPORT
#define RTI_EXPORT_FEDTIME

#else // _WIN32

#ifdef _MSC_VER
#ifdef RTI_DISABLE_WARNINGS
// disable warning about truncating template instantiation symbol names
#pragma warning(disable: 4786)

// disable warning about exceptions not being part of a method's signature
#pragma warning(disable : 4290)

// disable warnings about a "dllexport" class using a regular class
#pragma warning(disable : 4251)

#endif // RTI_DISABLE_WARNINGS
#endif // _MSC_VER

//
// On Windows, BUILDING_RTI should be defined only when compiling the RTI DLL
// (i.e. by RTI developers). BUILDING_FEDTIME should be defined only when building
// a libfedtime DLL. STATIC_RTI should be defined when building a static (non-DLL)
// RTI library, or when building a federate that wants to statically link to an
// RTI library. STATIC_FEDTIME should be defined when building a static (non-DLL)
// fedtime library, or when building a federate that wants to statically link to a
// fedtime library.
//

#ifdef STATIC_RTI
#define RTI_EXPORT
#else
#ifdef BUILDING_RTI
// define the proper qualifiers to import/export symbols from/to DLL
#define RTI_EXPORT  __declspec(dllexport)
#else // !BUILDING_RTI
#define RTI_EXPORT  __declspec(dllimport)
#endif // BUILDING_RTI
#endif // STATIC_RTI

#ifdef STATIC_FEDTIME
#define RTI_EXPORT_FEDTIME
#else
#ifdef BUILDING_FEDTIME
// define the proper qualifiers to import/export symbols from/to DLL
#define RTI_EXPORT_FEDTIME  __declspec(dllexport)
#else // !BUILDING_FEDTIME
#define RTI_EXPORT_FEDTIME  __declspec(dllimport)
#endif // BUILDING_FEDTIME
#endif // STATIC_FEDTIME

#endif // !_WIN32

#endif // RTI_SpecificConfig_h
