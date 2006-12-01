// File RTI.hh

#ifndef RTI_hh
#define RTI_hh

#ifdef _WIN32

#ifdef BUILDING_RTI
#define RTI_EXPORT __declspec(dllexport)
#else
#define RTI_EXPORT __declspec(dllimport)
#endif

#ifdef BUILDING_FEDTIME
#define RTI_EXPORT_FEDTIME __declspec(dllexport)
#else
#define RTI_EXPORT_FEDTIME __declspec(dllimport)
#endif

#else
#define RTI_EXPORT
#define RTI_EXPORT_FEDTIME
#endif

#ifdef RTI_USES_STD_FSTREAM
#include <fstream>
#define RTI_STD std
#else
#include <fstream.h>
#define RTI_STD
#endif

#include <math.h>

struct RTIambPrivateRefs;
struct RTIambPrivateData;

class RTI
{
public:
#include "baseTypes.hh"
#include "RTItypes.hh"

  class RTI_EXPORT RTIambassador
  {
  public:
#include "RTIambServices.hh"
    RTIambPrivateData* privateData;

  private:
    RTIambPrivateRefs* privateRefs;
  };

  class RTI_EXPORT FederateAmbassador
  {
  public:
#include "federateAmbServices.hh"
  };
};
#endif
