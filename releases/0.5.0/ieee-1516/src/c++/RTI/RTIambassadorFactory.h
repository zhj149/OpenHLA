/**
 * IEEE 1516.1 High Level Architecture Interface Specification C++API
 *
 * File: RTIambassador.h
 */

#ifndef RTIambassadorFactory_h
#define RTIambassadorFactory_h

#include <memory>
#include <vector>
#include <string>

namespace RTI
{
  class RTIinternalError;
  class RTIambassador;

  class RTIambassadorFactory
  {
  public:
    RTIambassadorFactory();
    virtual ~RTIambassadorFactory()
      throw();

    // 10.35
    std::auto_ptr<RTIambassador> createRTIambassador(std::vector<std::wstring>& args)
      throw(RTIinternalError);
  };
}

#endif // RTIambassadorFactory_h
