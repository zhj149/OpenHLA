/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++ API
 *
 * File: exception.h
 */

#ifndef exception_h
#define exception_h

#include <SpecificConfig.h>

#include <string>

//
// ==================================================================================================
//  The exception class follows the interface of the C++ standard exception class. The key
//  method, what, returns a null terminated character string that describes details of the exception
//  that has occured.
// ==================================================================================================
//

namespace RTI
{
  class RTI_EXPORT exception
  {
  public:
    exception();
    exception(exception const& rhs);
    virtual ~exception() throw();

    exception &operator =(exception const& rhs);
    virtual char const* what() const
      throw() = 0;
  };

#define EXCEPTION(A)                            \
  class A : public exception                    \
  {                                             \
  public:                                       \
    A(std::string const& message)               \
      throw();                                  \
    A(A const& rhs)                             \
      throw();                                  \
    virtual char const* what() const            \
      throw();                                  \
    A& operator=(A const& rhs)                  \
      throw();                                  \
                                                \
  private:                                      \
    string _msg;                                \
  };

  EXCEPTION(ArrayIndexOutOfBounds)
  EXCEPTION(AsynchronousDeliveryAlreadyDisabled)
  EXCEPTION(AsynchronousDeliveryAlreadyEnabled)
  EXCEPTION(AttributeAcquisitionWasNotCanceled)
  EXCEPTION(AttributeAcquisitionWasNotRequested)
  EXCEPTION(AttributeAlreadyBeingAcquired)
  EXCEPTION(AttributeAlreadyBeingDivested)
  EXCEPTION(AttributeAlreadyOwned)
  EXCEPTION(AttributeDivestitureWasNotRequested)
  EXCEPTION(AttributeNotDefined)
  EXCEPTION(AttributeNotOwned)
  EXCEPTION(AttributeNotPublished)
  EXCEPTION(AttributeNotRecognized)
  EXCEPTION(AttributeNotSubscribed)
  EXCEPTION(AttributeRelevanceAdvisorySwitchIsOff)
  EXCEPTION(AttributeRelevanceAdvisorySwitchIsOn)
  EXCEPTION(AttributeScopeAdvisorySwitchIsOff)
  EXCEPTION(AttributeScopeAdvisorySwitchIsOn)
  EXCEPTION(CouldNotDecode)
  EXCEPTION(CouldNotDiscover)
  EXCEPTION(CouldNotOpenFDD)
  EXCEPTION(CouldNotInitiateRestore)
  EXCEPTION(DeletePrivilegeNotHeld)
  EXCEPTION(RequestForTimeConstrainedPending)
  EXCEPTION(NoRequestToEnableTimeConstrainedWasPending)
  EXCEPTION(RequestForTimeRegulationPending)
  EXCEPTION(NoRequestToEnableTimeRegulationWasPending)
  EXCEPTION(ErrorReadingFDD)
  EXCEPTION(FederateAlreadyExecutionMember)
  EXCEPTION(FederateHasNotBegunSave)
  EXCEPTION(FederateInternalError)
  EXCEPTION(FederateNotExecutionMember)
  EXCEPTION(FederateOwnsAttributes)
  EXCEPTION(FederateServiceInvocationsAreBeingReportedViaMOM)
  EXCEPTION(FederateUnableToUseTime)
  EXCEPTION(FederatesCurrentlyJoined)
  EXCEPTION(FederationExecutionAlreadyExists)
  EXCEPTION(FederationExecutionDoesNotExist)
  EXCEPTION(IllegalName)
  EXCEPTION(IllegalTimeArithmetic)
  EXCEPTION(InteractionClassNotDefined)
  EXCEPTION(InteractionClassNotPublished)
  EXCEPTION(InteractionClassNotRecognized)
  EXCEPTION(InteractionClassNotSubscribed)
  EXCEPTION(InteractionParameterNotDefined)
  EXCEPTION(InteractionParameterNotRecognized)
  EXCEPTION(InteractionRelevanceAdvisorySwitchIsOff)
  EXCEPTION(InteractionRelevanceAdvisorySwitchIsOn)
  EXCEPTION(InTimeAdvancingState)
  EXCEPTION(InvalidAttributeHandle)
  EXCEPTION(InvalidDimensionHandle)
  EXCEPTION(InvalidFederateHandle)
  EXCEPTION(InvalidInteractionClassHandle)
  EXCEPTION(InvalidLogicalTime)
  EXCEPTION(InvalidLogicalTimeInterval)
  EXCEPTION(InvalidLookahead)
  EXCEPTION(InvalidObjectClassHandle)
  EXCEPTION(InvalidOrderName)
  EXCEPTION(InvalidOrderType)
  EXCEPTION(InvalidParameterHandle)
  EXCEPTION(InvalidRangeBound)
  EXCEPTION(InvalidRegion)
  EXCEPTION(InvalidRegionContext)
  EXCEPTION(InvalidRetractionHandle)
  EXCEPTION(InvalidString)
  EXCEPTION(InvalidTransportationName)
  EXCEPTION(InvalidTransportationType)
  EXCEPTION(JoinedFederateIsNotInTimeAdvancingState)
  EXCEPTION(LogicalTimeAlreadyPassed)
  EXCEPTION(LowerBoundOutOfRange)
  EXCEPTION(MessageCanNoLongerBeRetracted)
  EXCEPTION(NameNotFound)
  EXCEPTION(ObjectClassNotDefined)
  EXCEPTION(ObjectClassNotKnown)
  EXCEPTION(ObjectClassNotPublished)
  EXCEPTION(ObjectClassNotSubscribed)
  EXCEPTION(ObjectClassRelevanceAdvisorySwitchIsOff)
  EXCEPTION(ObjectClassRelevanceAdvisorySwitchIsOn)
  EXCEPTION(ObjectInstanceNameInUse)
  EXCEPTION(ObjectInstanceNameNotReserved)
  EXCEPTION(ObjectInstanceNotKnown)
  EXCEPTION(OwnershipAcquisitionPending)
  EXCEPTION(RTIinternalError)
  EXCEPTION(RegionDoesNotContainSpecifiedDimension)
  EXCEPTION(RegionInUseForUpdateOrSubscription)
  EXCEPTION(RegionNotCreatedByThisFederate)
  EXCEPTION(RestoreInProgress)
  EXCEPTION(RestoreNotInProgress)
  EXCEPTION(RestoreNotRequested)
  EXCEPTION(SaveInProgress)
  EXCEPTION(SaveNotInitiated)
  EXCEPTION(SaveNotInProgress)
  EXCEPTION(SpecifiedSaveLabelDoesNotExist)
  EXCEPTION(SynchronizationPointLabelNotAnnounced)
  EXCEPTION(SynchronizationSetMemberNotJoined)
  EXCEPTION(TimeConstrainedAlreadyEnabled)
  EXCEPTION(TimeConstrainedIsNotEnabled)
  EXCEPTION(TimeRegulationAlreadyEnabled)
  EXCEPTION(TimeRegulationIsNotEnabled)
  EXCEPTION(UnableToPerformSave)
  EXCEPTION(UnknownName)
  EXCEPTION(UpperBoundOutOfRange)
  EXCEPTION(ValueCountExceeded)
  EXCEPTION(ValueLengthExceeded)

#undef EXCEPTION

#ifdef USE_INLINE
#include "exception.i"
#endif // USE_INLINE

}

#endif // exception_h
