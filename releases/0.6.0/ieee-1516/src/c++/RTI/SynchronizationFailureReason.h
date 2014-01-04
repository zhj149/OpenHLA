/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: SynchronizationFailureReason.h
 */

#ifndef SynchronizationFailureReason_h
#define SynchronizationFailureReason_h

#include <SpecificConfig.h>

#include <string>

// Type safe class used to represent type of data order.

namespace RTI
{
  class RTI_EXPORT SynchronizationFailureReason
  {
  public:
    SynchronizationFailureReason(SynchronizationFailureReason const& rhs);

    static SynchronizationFailureReason const synchronizationPointLabelNotUnique();
    static SynchronizationFailureReason const synchronizationSetMemberNotJoined();

    std::wstring toString() const;

    SynchronizationFailureReason& operator=(SynchronizationFailureReason const& rhs);
    bool operator==(SynchronizationFailureReason const& rhs) const;
    bool operator!=(SynchronizationFailureReason const& rhs) const;

  private:
    SynchronizationFailureReason(unsigned SynchronizationFailureReason);
    unsigned _SynchronizationFailureReason;
  };

  //
  // These constants Synchronization a little typing for users. They can be used
  // much like a enum, but in a type-safe way
  //
  SynchronizationFailureReason const SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE =
    SynchronizationFailureReason::synchronizationPointLabelNotUnique();
  SynchronizationFailureReason const SYNCHRONIZATION_SET_MEMBER_NOT_JOINED =
    SynchronizationFailureReason::synchronizationSetMemberNotJoined();

#ifdef USE_INLINE
#include "SynchronizationFailureReason.i"
#endif // USE_INLINE

}

#endif // SynchronizationFailureReason_h
