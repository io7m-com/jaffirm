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
import com.io7m.jaffirm.core.InvariantViolationException;
import com.io7m.jaffirm.core.Invariants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

public final class InvariantsTest
{
  @Test
  public void testUnreachable()
    throws Exception
  {
    final Constructor<Invariants> c = Invariants.class.getDeclaredConstructor();
    c.setAccessible(true);

    Assertions.assertThrows(InvocationTargetException.class, c::newInstance);
  }

  @Test
  public void testInvariantsViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Invariants.checkInvariants(Integer.valueOf(23), Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testInvariantsViolationDescriberException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariants(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new RuntimeException("OUCH");
            })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariants(
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
  public void testInvariantsViolationPredicateException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariants(
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
  public void testInvariantsViolation()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariants(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsViolationMulti()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariants(
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
  public void testInvariantsViolationMultiLast()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariants(
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
  public void testInvariantsViolationMultiFirst()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariants(
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
  public void testInvariantsMulti()
  {
    final Integer r = Invariants.checkInvariants(
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
  public void testInvariantsCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Invariants.checkInvariants(
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
  public void testInvariantsIntViolation()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
          x -> x < 23,
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsIntViolationMulti()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
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
  public void testInvariantsIntViolationDescriberException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsIntViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testInvariantsIntViolationPredicateException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsIntViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));
  }

  @Test
  public void testInvariantsIntMulti()
  {
    final int r = Invariants.checkInvariantsI(22, Contracts.conditionI(
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
  public void testInvariantsIntViolationMultiLast()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
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
  public void testInvariantsIntViolationMultiFirst()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsI(23, Contracts.conditionI(
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
  public void testInvariantsIntCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Invariants.checkInvariantsI(
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
  public void testInvariantsLongViolation()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> String.format("Value %d must be < 23", Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsLongViolationMulti()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsL(
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
  public void testInvariantsLongMulti()
  {
    final long r =
      Invariants.checkInvariantsL(
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
  public void testInvariantsLongViolationMultiLast()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantsL(
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
  public void testInvariantsLongViolationMultiFirst()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantsL(
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
  public void testInvariantsLongViolationDescriberException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsLongViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Invariants.checkInvariantsL(23L, Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testInvariantsLongViolationPredicateException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsL(23L, Contracts.conditionL(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> String.format(
            "Value %f must be < 23",
            Double.valueOf((double) x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsLongViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Invariants.checkInvariantsL(23L, Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format(
          "Value %f must be < 23",
          Double.valueOf((double) x)))));
  }

  @Test
  public void testInvariantsLongCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Invariants.checkInvariantsL(
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
  public void testInvariantsDoubleViolation()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsDoubleViolationMulti()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantsD(
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
  public void testInvariantsDoubleViolationMultiLast()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantsD(
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
  public void testInvariantsDoubleViolationMultiFirst()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantsD(
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
  public void testInvariantsDoubleViolationDescriberException()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsDoubleViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Invariants.checkInvariantsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testInvariantsDoubleViolationPredicateException()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantsD(23.0, Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantsDoubleViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Invariants.checkInvariantsD(23.0, Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));
  }

  @Test
  public void testInvariantsDoubleMulti()
  {
    final double r =
      Invariants.checkInvariantsD(
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
  public void testInvariantsDoubleCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Invariants.checkInvariantsD(
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
  public void testInvariantViolation()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariant(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantViolationDescriberException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariant(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new RuntimeException("OUCH");
            })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariant(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new Error("OUCH");
            })));
  }

  @Test
  public void testInvariantViolationPredicateException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariant(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new RuntimeException("OUCH");
            },
            x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariant(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new Error("OUCH");
            },
            x -> "x")));
  }

  @Test
  public void testInvariant()
  {
    final Integer r = Invariants.checkInvariant(
      Integer.valueOf(22),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testInvariantSupplierException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariant(false, () -> {
          throw new RuntimeException("OUCH");
        }));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantIntViolation()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantI(23, Contracts.conditionI(
          x -> x < 23,
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantIntViolationDescriberException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantIntViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariantI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testInvariantIntViolationPredicateException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantI(23, Contracts.conditionI(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantIntViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariantI(23, Contracts.conditionI(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testInvariantInt()
  {
    final int r = Invariants.checkInvariantI(22, Contracts.conditionI(
      x -> x < 23,
      x -> String.format("Value %d must be < 23", Integer.valueOf(x))));

    Assertions.assertEquals(22L, (long) r);
  }

  @Test
  public void testInvariantIntOther()
  {
    final int r =
      Invariants.checkInvariantI(22, true, x -> "OK");
    Assertions.assertEquals(22L, (long) r);
  }

  @Test
  public void testInvariantLongViolation()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> String.format("Value %d must be < 23", Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantLongViolationDescriberException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantLongViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariantL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testInvariantLongViolationPredicateException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantL(23L, Contracts.conditionL(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantLongViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariantL(23L, Contracts.conditionL(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testInvariantLong()
  {
    final long r = Invariants.checkInvariantL(22L, Contracts.conditionL(
      x -> x < 23L,
      x -> String.format("Value %d must be < 23", Long.valueOf(x))));

    Assertions.assertEquals(22L, r);
  }

  @Test
  public void testInvariantLongOther()
  {
    final long r =
      Invariants.checkInvariantL(22L, true, x -> "OK");
    Assertions.assertEquals(22L, r);
  }

  @Test
  public void testInvariantDoubleViolation()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantDoubleViolationDescriberException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantDoubleViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariantD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testInvariantDoubleViolationPredicateException()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantD(23.0, Contracts.conditionD(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantDoubleViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Invariants.checkInvariantD(23.0, Contracts.conditionD(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testInvariantDouble()
  {
    final double r =
      Invariants.checkInvariantD(22.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> String.format("Value %f must be < 23", Double.valueOf(x))));

    Assertions.assertEquals(22.0, r, 0.00000001);
  }

  @Test
  public void testInvariantDoubleOther()
  {
    final double r = Invariants.checkInvariantD(
      22.0, true, x -> "OK");
    Assertions.assertEquals(22.0, r, 0.00000001);
  }

  @Test
  public void testInvariantSimpleViolation0()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariant(false, () -> "Value must true"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantSimpleViolation1()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariant(false, "Value must true"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantSimpleViolation2()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariant(
          Integer.valueOf(23),
          23 < 23,
          x -> String.format(
            "Value %d must < 23",
            x)));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantSimple0()
  {
    Invariants.checkInvariant(true, () -> "Value must true");
  }

  @Test
  public void testInvariantSimple1()
  {
    Invariants.checkInvariant(true, "Value must true");
  }

  @Test
  public void testInvariantSimple2()
  {
    final Integer r = Invariants.checkInvariant(
      Integer.valueOf(22),
      true,
      x -> String.format("Value %d must < 23", x));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testInvariantV()
  {
    final Integer r = Invariants.checkInvariantV(
      Integer.valueOf(22),
      true,
      "Value %d must be < 23",
      Integer.valueOf(22));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testInvariantVFailed()
  {
    final InvariantViolationException ex =
      Assertions.assertThrows(
        InvariantViolationException.class,
        () -> Invariants.checkInvariantV(Integer.valueOf(22), false, "Failed"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testInvariantVNoValue()
  {
    Invariants.checkInvariantV(
      true,
      "Value %d must be < 23",
      Integer.valueOf(22));
  }

  @Test
  public void testInvariantVNoValueFailed()
  {
    final InvariantViolationException ex = Assertions.assertThrows(
      InvariantViolationException.class,
      () -> Invariants.checkInvariantV(
        false,
        "Value %d must be < 23",
        Integer.valueOf(22)));

    Assertions.assertEquals(1, ex.violations());
  }
}
