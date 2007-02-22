/**
 *IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: RestoreStatus.h
 */

#ifndef RestoreStatus_h
#define RestoreStatus_h

#include <SpecificConfig.h>

#include <string>

// Type safe class used to represent save status of an individual federate.

namespace RTI
{
  class RTI_EXPORT RestoreStatus
  {
  public:
    RestoreStatus(RestoreStatus const& rhs);

    static RestoreStatus const noRestoreInProgress();
    static RestoreStatus const federateRestoreRequestPending();
    static RestoreStatus const federateWaitingForRestoreToBegin();
    static RestoreStatus const federatePreparedToRestore();
    static RestoreStatus const federateRestoring();
    static RestoreStatus const federateWaitingForFederationToRestore();

    std::wstring toString() const;

    RestoreStatus& operator=(RestoreStatus const& rhs);
    bool operator==(RestoreStatus const& rhs) const;
    bool operator!=(RestoreStatus const& rhs) const;

  private:
    RestoreStatus(unsigned _RestoreStatus);
    unsigned _RestoreStatus;
  };

  //
  // These constants save a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  RestoreStatus const NO_RESTORE_IN_PROGRESS =
    RestoreStatus::noRestoreInProgress();
  RestoreStatus const FEDERATE_RESTORE_REQUEST_PENDING =
    RestoreStatus::federateRestoreRequestPending();
  RestoreStatus const FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN =
    RestoreStatus::federateWaitingForRestoreToBegin();
  RestoreStatus const FEDERATE_PREPARED_TO_RESTORE =
    RestoreStatus::federatePreparedToRestore();
  RestoreStatus const FEDERATE_RESTORING =
    RestoreStatus::federateRestoring();
  RestoreStatus const FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE =
    RestoreStatus::federateWaitingForFederationToRestore();

#ifdef USE_INLINE
#include "RestoreStatus.i"
#endif // USE_INLINE

}

#endif // RestoreStatus_h
