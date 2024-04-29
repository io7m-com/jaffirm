/*
 * Copyright © 2016 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jaffirm.tests.core;

import com.io7m.jaffirm.core.Contracts;
import com.io7m.jaffirm.core.PreconditionViolationException;
import com.io7m.jaffirm.core.Preconditions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

public final class PreconditionsTest
{
  @Test
  public void testUnreachable()
    throws Exception
  {
    final Constructor<Preconditions> c = Preconditions.class.getDeclaredConstructor();
    c.setAccessible(true);

    Assertions.assertThrows(InvocationTargetException.class, c::newInstance);
  }

  @Test
  public void testPreconditionsViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Preconditions.checkPreconditions(Integer.valueOf(23), Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPreconditionsViolationDescriberException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new RuntimeException("OUCH");
            })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPreconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new Error("OUCH");
            },
            x -> String.format(
              "Value %d must be < 23",
              x))));
  }

  @Test
  public void testPreconditionsViolationPredicateException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new RuntimeException("OUCH");
            },
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsViolation()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsViolationMulti()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() > 23,
            x -> String.format(
              "Value %d must be > 23",
              x)),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(2, ex.violations());
  }

  @Test
  public void testPreconditionsViolationMultiLast()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() == 23,
            x -> String.format(
              "Value %d must be == 23",
              x)),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsViolationMultiFirst()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x)),
          Contracts.condition(
            x -> x.intValue() == 23,
            x -> String.format(
              "Value %d must be == 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsMulti()
  {
    final Integer r = Preconditions.checkPreconditions(
      Integer.valueOf(22),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPreconditionsCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          called.incrementAndGet();
          return x.intValue() == 23;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.condition(
        x -> {
          called.incrementAndGet();
          return x.intValue() == 23;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.condition(
        x -> {
          called.incrementAndGet();
          return x.intValue() == 23;
        },
        x -> {
          throw new AssertionError();
        }));

    Assertions.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionsIntViolation()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
          x -> x < 23,
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsIntViolationMulti()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
          x -> x > 23,
          x -> String.format(
            "Value %d must be > 23",
            Integer.valueOf(x))), Contracts.conditionI(
          x -> x < 23,
          x -> String.format(
            "Value %d must be < 23",
            Integer.valueOf(x)))));

    Assertions.assertEquals(2, ex.violations());
  }

  @Test
  public void testPreconditionsIntViolationDescriberException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsIntViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPreconditionsIntViolationPredicateException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsIntViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));
  }

  @Test
  public void testPreconditionsIntMulti()
  {
    final int r = Preconditions.checkPreconditionsI(22, Contracts.conditionI(
      x -> x < 23,
      x -> String.format(
        "Value %d must be < 23",
        Integer.valueOf(x))), Contracts.conditionI(
      x -> x < 23,
      x -> String.format(
        "Value %d must be < 23",
        Integer.valueOf(x))));

    Assertions.assertEquals(22L, (long) r);
  }

  @Test
  public void testPreconditionsIntViolationMultiLast()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
          x -> x == 23,
          x -> String.format(
            "Value %d must be == 23",
            Integer.valueOf(x))), Contracts.conditionI(
          x -> x < 23,
          x -> String.format(
            "Value %d must be < 23",
            Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsIntViolationMultiFirst()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsI(23, Contracts.conditionI(
          x -> x < 23,
          x -> String.format(
            "Value %d must be < 23",
            Integer.valueOf(x))), Contracts.conditionI(
          x -> x == 23,
          x -> String.format(
            "Value %d must be == 23",
            Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsIntCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> {
          called.incrementAndGet();
          return x == 23;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.conditionI(
        x -> {
          called.incrementAndGet();
          return x == 23;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.conditionI(
        x -> {
          called.incrementAndGet();
          return x == 23;
        },
        x -> {
          throw new AssertionError();
        }));

    Assertions.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionsLongViolation()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> String.format("Value %d must be < 23", Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsLongViolationMulti()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsL(
          23L,
          Contracts.conditionL(
            x -> x > 23L,
            x -> String.format(
              "Value %d must be > 23",
              Long.valueOf(x))),
          Contracts.conditionL(
            x -> x < 23L,
            x -> String.format(
              "Value %d must be < 23",
              Long.valueOf(x)))));

    Assertions.assertEquals(2, ex.violations());
  }

  @Test
  public void testPreconditionsLongMulti()
  {
    final long r =
      Preconditions.checkPreconditionsL(
        22L,
        Contracts.conditionL(
          x -> x < 23L,
          x -> String.format(
            "Value %d must be < 23",
            Long.valueOf(x))),
        Contracts.conditionL(
          x -> x < 23L,
          x -> String.format(
            "Value %d must be < 23",
            Long.valueOf(x))));

    Assertions.assertEquals(22L, r);
  }

  @Test
  public void testPreconditionsLongViolationMultiLast()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionsL(
        23L,
        Contracts.conditionL(
          x -> x == 23L,
          x -> String.format(
            "Value %d must be == 23",
            Long.valueOf(x))),
        Contracts.conditionL(
          x -> x < 23L,
          x -> String.format(
            "Value %d must be < 23",
            Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsLongViolationMultiFirst()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionsL(
        23L,
        Contracts.conditionL(
          x -> x < 23L,
          x -> String.format(
            "Value %d must be < 23",
            Long.valueOf(x))),
        Contracts.conditionL(
          x -> x == 23L,
          x -> String.format(
            "Value %d must be == 23",
            Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsLongViolationDescriberException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsLongViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Preconditions.checkPreconditionsL(23L, Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPreconditionsLongViolationPredicateException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsL(23L, Contracts.conditionL(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> String.format(
            "Value %f must be < 23",
            Double.valueOf((double) x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsLongViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Preconditions.checkPreconditionsL(23L, Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format(
          "Value %f must be < 23",
          Double.valueOf((double) x)))));
  }

  @Test
  public void testPreconditionsLongCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> {
          called.incrementAndGet();
          return x == 23L;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.conditionL(
        x -> {
          called.incrementAndGet();
          return x == 23L;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.conditionL(
        x -> {
          called.incrementAndGet();
          return x == 23L;
        },
        x -> {
          throw new AssertionError();
        }));

    Assertions.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionsDoubleViolation()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsDoubleViolationMulti()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionsD(
          23.0,
          Contracts.conditionD(
            x -> x > 23.0,
            x -> String.format(
              "Value %f must be > 23",
              Double.valueOf(x))),
          Contracts.conditionD(
            x -> x < 23.0,
            x -> String.format(
              "Value %f must be < 23",
              Double.valueOf(x)))));

    Assertions.assertEquals(2, ex.violations());
  }

  @Test
  public void testPreconditionsDoubleViolationMultiLast()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionsD(
        23.0,
        Contracts.conditionD(
          x -> x == 23.0,
          x -> String.format(
            "Value %f must be == 23",
            Double.valueOf(x))),
        Contracts.conditionD(
          x -> x < 23.0,
          x -> String.format(
            "Value %f must be < 23",
            Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsDoubleViolationMultiFirst()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionsD(
        23.0,
        Contracts.conditionD(
          x -> x < 23.0,
          x -> String.format(
            "Value %f must be < 23",
            Double.valueOf(x))),
        Contracts.conditionD(
          x -> x == 23.0,
          x -> String.format(
            "Value %f must be == 23",
            Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsDoubleViolationDescriberException()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsDoubleViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Preconditions.checkPreconditionsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPreconditionsDoubleViolationPredicateException()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionsD(23.0, Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionsDoubleViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Preconditions.checkPreconditionsD(23.0, Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));
  }

  @Test
  public void testPreconditionsDoubleMulti()
  {
    final double r =
      Preconditions.checkPreconditionsD(
        22.0,
        Contracts.conditionD(
          x -> x < 23.0,
          x -> String.format(
            "Value %f must be < 23",
            Double.valueOf(x))),
        Contracts.conditionD(
          x -> x < 23.0,
          x -> String.format(
            "Value %f must be < 23",
            Double.valueOf(x))));

    Assertions.assertEquals(22.0, r, 0.00000001);
  }

  @Test
  public void testPreconditionsDoubleCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> {
          called.incrementAndGet();
          return x == 23.0;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.conditionD(
        x -> {
          called.incrementAndGet();
          return x == 23.0;
        },
        x -> {
          throw new AssertionError();
        }),
      Contracts.conditionD(
        x -> {
          called.incrementAndGet();
          return x == 23.0;
        },
        x -> {
          throw new AssertionError();
        }));

    Assertions.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionViolation()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPrecondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionViolationDescriberException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPrecondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new RuntimeException("OUCH");
            })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPrecondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new Error("OUCH");
            })));
  }

  @Test
  public void testPreconditionViolationPredicateException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPrecondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new RuntimeException("OUCH");
            },
            x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPrecondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new Error("OUCH");
            },
            x -> "x")));
  }

  @Test
  public void testPrecondition()
  {
    final Integer r = Preconditions.checkPrecondition(
      Integer.valueOf(22),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPreconditionSupplierException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPrecondition(false, () -> {
          throw new RuntimeException("OUCH");
        }));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionIntViolation()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionI(23, Contracts.conditionI(
          x -> x < 23,
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionIntViolationDescriberException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionIntViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPreconditionI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testPreconditionIntViolationPredicateException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionI(23, Contracts.conditionI(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionIntViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPreconditionI(23, Contracts.conditionI(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testPreconditionInt()
  {
    final int r = Preconditions.checkPreconditionI(22, Contracts.conditionI(
      x -> x < 23,
      x -> String.format("Value %d must be < 23", Integer.valueOf(x))));

    Assertions.assertEquals(22L, (long) r);
  }

  @Test
  public void testPreconditionIntOther()
  {
    final int r =
      Preconditions.checkPreconditionI(22, true, x -> "OK");
    Assertions.assertEquals(22L, (long) r);
  }

  @Test
  public void testPreconditionLongViolation()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> String.format("Value %d must be < 23", Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionLongViolationDescriberException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionLongViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPreconditionL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testPreconditionLongViolationPredicateException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionL(23L, Contracts.conditionL(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionLongViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPreconditionL(23L, Contracts.conditionL(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testPreconditionLong()
  {
    final long r = Preconditions.checkPreconditionL(22L, Contracts.conditionL(
      x -> x < 23L,
      x -> String.format("Value %d must be < 23", Long.valueOf(x))));

    Assertions.assertEquals(22L, r);
  }

  @Test
  public void testPreconditionLongOther()
  {
    final long r =
      Preconditions.checkPreconditionL(22L, true, x -> "OK");
    Assertions.assertEquals(22L, r);
  }

  @Test
  public void testPreconditionDoubleViolation()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionDoubleViolationDescriberException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionDoubleViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPreconditionD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testPreconditionDoubleViolationPredicateException()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionD(23.0, Contracts.conditionD(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionDoubleViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Preconditions.checkPreconditionD(23.0, Contracts.conditionD(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testPreconditionDouble()
  {
    final double r =
      Preconditions.checkPreconditionD(22.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> String.format("Value %f must be < 23", Double.valueOf(x))));

    Assertions.assertEquals(22.0, r, 0.00000001);
  }

  @Test
  public void testPreconditionDoubleOther()
  {
    final double r = Preconditions.checkPreconditionD(
      22.0, true, x -> "OK");
    Assertions.assertEquals(22.0, r, 0.00000001);
  }

  @Test
  public void testPreconditionSimpleViolation0()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPrecondition(false, () -> "Value must true"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionSimpleViolation1()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPrecondition(false, "Value must true"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionSimpleViolation2()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPrecondition(
          Integer.valueOf(23),
          23 < 23,
          x -> String.format(
            "Value %d must < 23",
            x)));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionSimple0()
  {
    Preconditions.checkPrecondition(true, () -> "Value must true");
  }

  @Test
  public void testPreconditionSimple1()
  {
    Preconditions.checkPrecondition(true, "Value must true");
  }

  @Test
  public void testPreconditionSimple2()
  {
    final Integer r = Preconditions.checkPrecondition(
      Integer.valueOf(22),
      true,
      x -> String.format("Value %d must < 23", x));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPreconditionV()
  {
    final Integer r = Preconditions.checkPreconditionV(
      Integer.valueOf(22),
      true,
      "Value %d must be < 23",
      Integer.valueOf(22));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPreconditionVFailed()
  {
    final PreconditionViolationException ex =
      Assertions.assertThrows(
        PreconditionViolationException.class,
        () -> Preconditions.checkPreconditionV(Integer.valueOf(22), false, "Failed"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPreconditionVNoValue()
  {
    Preconditions.checkPreconditionV(
      true,
      "Value %d must be < 23",
      Integer.valueOf(22));
  }

  @Test
  public void testPreconditionVNoValueFailed()
  {
    final PreconditionViolationException ex = Assertions.assertThrows(
      PreconditionViolationException.class,
      () -> Preconditions.checkPreconditionV(
        false,
        "Value %d must be < 23",
        Integer.valueOf(22)));

    Assertions.assertEquals(1, ex.violations());
  }
}
