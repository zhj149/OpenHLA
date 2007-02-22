/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: SaveFailureReason.h
 */

#ifndef SaveFailureReason_h
#define SaveFailureReason_h

#include <SpecificConfig.h>

#include <string>

// Type safe class used to represent type of data order.

namespace RTI
{
  class RTI_EXPORT SaveFailureReason
  {
  public:
    SaveFailureReason(SaveFailureReason const& rhs);

    static SaveFailureReason const rtiUnableToSave();
    static SaveFailureReason const federateReportedFailureDuringSave();
    static SaveFailureReason const federateResignedDuringSave();
    static SaveFailureReason const rtiDetectedFailureDuringSave();
    static SaveFailureReason const saveTimeCannotBeHonored();

    std::wstring toString() const;

    SaveFailureReason& operator=(SaveFailureReason const& rhs);
    bool operator==(SaveFailureReason const& rhs) const;
    bool operator!=(SaveFailureReason const& rhs) const;

  private:
    SaveFailureReason(unsigned saveFailureReason);
    unsigned _saveFailureReason;
  };

  //
  // These constants save a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  SaveFailureReason const UNABLE_TO_SAVE =
    SaveFailureReason::rtiUnableToSave();
  SaveFailureReason const FEDERATE_REPORTED_FAILURE_DURING_SAVE =
    SaveFailureReason::federateReportedFailureDuringSave();
  SaveFailureReason const FEDERATE_RESIGNED_DURING_SAVE =
    SaveFailureReason::federateResignedDuringSave();
  SaveFailureReason const DETECTED_FAILURE_DURING_SAVE =
    SaveFailureReason::rtiDetectedFailureDuringSave();
  SaveFailureReason const SAVE_TIME_CANNOT_BE_HONORED =
    SaveFailureReason::saveTimeCannotBeHonored();

#ifdef USE_INLINE
#include "SaveFailureReason.i"
#endif // USE_INLINE

}

#endif // SaveFailureReason_h
