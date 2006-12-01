// File baseTypes.hh
// Included in RTI.hh

#ifndef NULL
#define NULL 0
#endif // NULL

typedef unsigned short UShort;
typedef short          Short;
typedef unsigned long  ULong;
typedef long           Long;
typedef double         Double;
typedef float          Float;

enum Boolean
{
    RTI_FALSE = 0,
    RTI_TRUE
};

class RTI_EXPORT Exception
{
public:
  ULong _serial;
  char* _reason;
  const char* _name;

  Exception(const char* reason);
  Exception(ULong serial, const char* reason = NULL);
  Exception(const Exception& e);

  virtual ~Exception();

  Exception &operator =(const Exception& e);
};

#define RTI_EXCEPT(A)                                                   \
    RTI_EXPORT class A : public Exception                               \
    {                                                                   \
    public:                                                             \
      static RTI_EXPORT const char* _ex;                                \
      A (const char* reason) : Exception(reason)                        \
      {                                                                 \
        _name = _ex;                                                    \
      }                                                                 \
      A(ULong serial, const char *reason = NULL) : Exception(serial, reason) \
      {                                                                 \
        _name = _ex;                                                    \
      }                                                                 \
      A (const Exception& e) : Exception(e)                             \
      {                                                                 \
        _name = _ex;                                                    \
      }                                                                 \
    };
