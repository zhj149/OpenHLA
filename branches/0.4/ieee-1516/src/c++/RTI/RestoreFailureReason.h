/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: RestoreFailureReason.h
 */

#ifndef RestoreFailureReason_h
#define RestoreFailureReason_h

#include <SpecificConfig.h>

#include <string>

// Type safe class used to represent the reason a restore failed

namespace RTI
{
  class RTI_EXPORT RestoreFailureReason
  {
  public:
    RestoreFailureReason(RestoreFailureReason const& rhs);

    static RestoreFailureReason const rtiUnableToRestore();
    static RestoreFailureReason const federateReportedFailureDuringRestore();
    static RestoreFailureReason const federateResignedDuringRestore();
    static RestoreFailureReason const rtiDetectedFailureDuringRestore();

    std::wstring toString() const;

    RestoreFailureReason &operator=(RestoreFailureReason const& rhs);
    bool operator==(RestoreFailureReason const& rhs) const;
    bool operator!=(RestoreFailureReason const& rhs) const;

  private:
    RestoreFailureReason(unsigned RestoreFailureReason);
    unsigned  _RestoreFailureReason;
  };

  //
  // These constants Restore a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  RestoreFailureReason const UNABLE_TO_RESTORE =
    RestoreFailureReason::rtiUnableToRestore();
  RestoreFailureReason const FEDERATE_REPORTED_FAILURE_DURING_RESTORE =
    RestoreFailureReason::federateReportedFailureDurinRestore();
  RestoreFailureReason const FEDERATE_RESIGNED_DURING_RESTORE =
    RestoreFailureReason::federateResignedDuringRestor();
  RestoreFailureReason const DETECTED_FAILURE_DURING_RESTORE =
    RestoreFailureReason::rtiDetectedFailureDuringRestore();

#ifdef USE_INLINE
#include "RestoreFailureReason.i"
#endif // USE_INLINE

}

#endif // RestoreFailureReason_h
