// File RTI13.h

#ifndef RTI13_h
#define RTI13_h

// Identification of the API version number.
#define HLA_SPECIFICATION_NAME  "1.3"
#define HLA_API_MAJOR_VERSION   7
#define HLA_API_MINOR_VERSION   0

#ifndef _WIN32
// no special qualfifers are necessary on non-WIN32 platforms
#define RTI_EXPORT
#define RTI_EXPORT_FEDTIME

#else // _WIN32

#ifdef _MSC_VER
#ifdef RTI_DISABLE_WARNINGS

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

//
// On platforms that support both the standard C++ version of ostream
// (std::ostream), and the legacy ostream in the global namespace, an RTI library
// will contain two versions of each RTI function that relies on the ostream
// class. This allows federates to work with either new or old ostreams. When a
// federate includes RTI header files, it should define RTI_USES_STD_FSTREAM if
// and only if it is using std::ostream, and therefore wishes to use the
// std::ostream versions of these RTI functions.
//
#ifdef RTI_USES_STD_FSTREAM
#include <fstream>
#define RTI_STD std
#else
#include <fstream.h>
#define RTI_STD /* nothing */
#endif

#include "baseTypes13.h"
#include "RTItypes13.h"

namespace rti13
{
  struct RTIambPrivateRefs;
  struct RTIambPrivateData;

  // Vendor-specific name and version of the RTI implementation
  const char* RTIname();
  const char* RTIversion(); // identical to MOM attributes of same name

  class RTI_EXPORT RTIambassador
  {
public:
#include "RTIambServices13.h"
    RTIambPrivateData*  privateData;

private:
    RTIambPrivateRefs*  privateRefs;
  };

  class RTI_EXPORT  FederateAmbassador
  {
public:
#include "federateAmbServices13.h"
  };

}

RTI_STD::ostream& RTI_EXPORT operator <<(RTI_STD::ostream& os, rti13::Exception* e);
RTI_STD::ostream& RTI_EXPORT operator <<(RTI_STD::ostream& os, rti13::Exception& const e);
RTI_STD::ostream& RTI_EXPORT operator <<(RTI_STD::ostream& os, const rti13::FedTime& time);

#endif // RTI13_h
