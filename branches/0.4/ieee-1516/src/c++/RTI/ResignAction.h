/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: ResignAction.h
 */

#ifndef ResignAction_h
#define ResignAction_h

#include <SpecificConfig.h>

#include <string>

// Type safe class used to represent action taken on resign.

namespace RTI
{
  class RTI_EXPORT ResignAction
  {
  public:
    ResignAction(ResignAction const& rhs);

    static ResignAction const unconditionallyDivestAttributes();
    static ResignAction const deleteObjects();
    static ResignAction const cancelPendingOwnershipAcquisitions();
    static ResignAction const deleteObjectsThenDivest();
    static ResignAction const cancelThenDeleteThenDivest();
    static ResignAction const noAction();

    std::wstring toString() const;

    ResignAction& operator=(ResignAction const& rhs);
    bool operator==(ResignAction const& rhs) const;
    bool operator!=(ResignAction const& rhs) const;

  private:
    ResignAction(unsigned _resignAction);

    unsigned _resignAction;
  };

  //
  // These constants save a little typing for users. They can be used much like a
  // enum, but in a type-safe way
  //
  ResignAction const UNCONDITIONALLY_DIVEST_ATTRIBUTES =
    ResignAction::unconditionallyDivestAttributes();
  ResignAction const DELETE_OBJECTS =
    ResignAction::deleteObjects();
  ResignAction const CANCEL_PENDING_OWNERSHIP_ACQUISITIONS =
    ResignAction::cancelPendingOwnershipAcquisitions();
  ResignAction const DELETE_OBJECTS_THEN_DIVEST =
    ResignAction::deleteObjectsThenDivest();
  ResignAction const CANCEL_THEN_DELETE_THEN_DIVEST =
    ResignAction::cancelThenDeleteThenDivest();
  ResignAction const NO_ACTION =
    ResignAction::noAction();

#ifdef USE_INLINE
#include "ResignAction.i"
#endif // USE_INLINE

}

#endif // ResignAction_h
