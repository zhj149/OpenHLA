/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: SaveStatus.h
 */

#ifndef SaveStatus_h
#define SaveStatus_h

#include <SpecificConfig.h>

#include <string>

// Type safe class used to represent save status of an individual federate.

namespace RTI
{
  class RTI_EXPORT SaveStatus
  {
  public:
    SaveStatus(SaveStatus const& rhs);

    static SaveStatus const noSaveInProgress();
    static SaveStatus const federateInstructedToSave();
    static SaveStatus const federateSaving();
    static SaveStatus const federateWaitingForFederationToSave();

    std::wstring toString() const;

    SaveStatus& operator =(SaveStatus const& rhs);
    bool operator==(SaveStatus const& rhs) const;
    bool operator!=(SaveStatus const& rhs) const;

  private:
    SaveStatus(unsigned _SaveStatus);
    unsigned _SaveStatus;
  };

  //
  // These constants save a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  SaveStatus const NO_SAVE_IN_PROGRESS =
    SaveStatus::noSaveInProgress();
  SaveStatus const FEDERATE_INSTRUCTED_TO_SAVE =
    SaveStatus::federateInstructedToSave();
  SaveStatus const FEDERATE_SAVING =
    SaveStatus::federateSaving();
  SaveStatus const FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE =
    SaveStatus::federateWaitingForFederationToSave();

#ifdef USE_INLINE
#include "SaveStatus.i"
#endif // USE_INLINE

}

#endif // SaveStatus_h
