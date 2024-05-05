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
import com.io7m.jaffirm.core.PostconditionViolationException;
import com.io7m.jaffirm.core.Postconditions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

public final class PostconditionsTest
{
  @Test
  public void testUnreachable()
    throws Exception
  {
    final Constructor<Postconditions> c = Postconditions.class.getDeclaredConstructor();
    c.setAccessible(true);

    Assertions.assertThrows(InvocationTargetException.class, c::newInstance);
  }

  @Test
  public void testPostconditionsViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Postconditions.checkPostconditions(Integer.valueOf(23), Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPostconditionsViolationDescriberException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new RuntimeException("OUCH");
            })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostconditions(
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
  public void testPostconditionsViolationPredicateException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditions(
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
  public void testPostconditionsViolation()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditions(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsViolationMulti()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditions(
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
  public void testPostconditionsViolationMultiLast()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditions(
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
  public void testPostconditionsViolationMultiFirst()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditions(
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
  public void testPostconditionsMulti()
  {
    final Integer r = Postconditions.checkPostconditions(
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
  public void testPostconditionsCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Postconditions.checkPostconditions(
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
  public void testPostconditionsIntViolation()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
          x -> x < 23,
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsIntViolationMulti()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
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
  public void testPostconditionsIntViolationDescriberException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsIntViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPostconditionsIntViolationPredicateException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsIntViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));
  }

  @Test
  public void testPostconditionsIntMulti()
  {
    final int r = Postconditions.checkPostconditionsI(22, Contracts.conditionI(
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
  public void testPostconditionsIntViolationMultiLast()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
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
  public void testPostconditionsIntViolationMultiFirst()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsI(23, Contracts.conditionI(
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
  public void testPostconditionsIntCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Postconditions.checkPostconditionsI(
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
  public void testPostconditionsLongViolation()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> String.format("Value %d must be < 23", Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsLongViolationMulti()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsL(
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
  public void testPostconditionsLongMulti()
  {
    final long r =
      Postconditions.checkPostconditionsL(
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
  public void testPostconditionsLongViolationMultiLast()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionsL(
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
  public void testPostconditionsLongViolationMultiFirst()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionsL(
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
  public void testPostconditionsLongViolationDescriberException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsLongViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Postconditions.checkPostconditionsL(23L, Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPostconditionsLongViolationPredicateException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsL(23L, Contracts.conditionL(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> String.format(
            "Value %f must be < 23",
            Double.valueOf((double) x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsLongViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Postconditions.checkPostconditionsL(23L, Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format(
          "Value %f must be < 23",
          Double.valueOf((double) x)))));
  }

  @Test
  public void testPostconditionsLongCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Postconditions.checkPostconditionsL(
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
  public void testPostconditionsDoubleViolation()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsDoubleViolationMulti()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionsD(
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
  public void testPostconditionsDoubleViolationMultiLast()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionsD(
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
  public void testPostconditionsDoubleViolationMultiFirst()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionsD(
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
  public void testPostconditionsDoubleViolationDescriberException()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsDoubleViolationDescriberError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Postconditions.checkPostconditionsD(23.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        })));
  }

  @Test
  public void testPostconditionsDoubleViolationPredicateException()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionsD(23.0, Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionsDoubleViolationPredicateError()
  {
    final Error ex = Assertions.assertThrows(
      Error.class,
      () -> Postconditions.checkPostconditionsD(23.0, Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %f must be < 23", Double.valueOf(x)))));
  }

  @Test
  public void testPostconditionsDoubleMulti()
  {
    final double r =
      Postconditions.checkPostconditionsD(
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
  public void testPostconditionsDoubleCalled()
  {
    final AtomicInteger called = new AtomicInteger(0);

    Postconditions.checkPostconditionsD(
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
  public void testPostconditionViolation()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostcondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> String.format(
              "Value %d must be < 23",
              x))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionViolationDescriberException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostcondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new RuntimeException("OUCH");
            })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostcondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> x.intValue() < 23,
            x -> {
              throw new Error("OUCH");
            })));
  }

  @Test
  public void testPostconditionViolationPredicateException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostcondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new RuntimeException("OUCH");
            },
            x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostcondition(
          Integer.valueOf(23),
          Contracts.condition(
            x -> {
              throw new Error("OUCH");
            },
            x -> "x")));
  }

  @Test
  public void testPostcondition()
  {
    final Integer r = Postconditions.checkPostcondition(
      Integer.valueOf(22),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPostconditionSupplierException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostcondition(false, () -> {
          throw new RuntimeException("OUCH");
        }));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionIntViolation()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionI(23, Contracts.conditionI(
          x -> x < 23,
          x -> String.format("Value %d must be < 23", Integer.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionIntViolationDescriberException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionIntViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostconditionI(23, Contracts.conditionI(
          x -> x < 23,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testPostconditionIntViolationPredicateException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionI(23, Contracts.conditionI(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionIntViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostconditionI(23, Contracts.conditionI(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testPostconditionInt()
  {
    final int r = Postconditions.checkPostconditionI(22, Contracts.conditionI(
      x -> x < 23,
      x -> String.format("Value %d must be < 23", Integer.valueOf(x))));

    Assertions.assertEquals(22L, (long) r);
  }

  @Test
  public void testPostconditionIntOther()
  {
    final int r =
      Postconditions.checkPostconditionI(22, true, x -> "OK");
    Assertions.assertEquals(22L, (long) r);
  }

  @Test
  public void testPostconditionLongViolation()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> String.format("Value %d must be < 23", Long.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionLongViolationDescriberException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionLongViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostconditionL(23L, Contracts.conditionL(
          x -> x < 23L,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testPostconditionLongViolationPredicateException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionL(23L, Contracts.conditionL(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionLongViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostconditionL(23L, Contracts.conditionL(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testPostconditionLong()
  {
    final long r = Postconditions.checkPostconditionL(22L, Contracts.conditionL(
      x -> x < 23L,
      x -> String.format("Value %d must be < 23", Long.valueOf(x))));

    Assertions.assertEquals(22L, r);
  }

  @Test
  public void testPostconditionLongOther()
  {
    final long r =
      Postconditions.checkPostconditionL(22L, true, x -> "OK");
    Assertions.assertEquals(22L, r);
  }

  @Test
  public void testPostconditionDoubleViolation()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> String.format("Value %f must be < 23", Double.valueOf(x)))));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionDoubleViolationDescriberException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> {
            throw new RuntimeException("OUCH");
          })));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionDoubleViolationDescriberError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostconditionD(23.0, Contracts.conditionD(
          x -> x < 23.0,
          x -> {
            throw new Error("OUCH");
          })));
  }

  @Test
  public void testPostconditionDoubleViolationPredicateException()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionD(23.0, Contracts.conditionD(
          x -> {
            throw new RuntimeException("OUCH");
          },
          x -> "x")));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionDoubleViolationPredicateError()
  {
    final Error ex =
      Assertions.assertThrows(
        Error.class,
        () -> Postconditions.checkPostconditionD(23.0, Contracts.conditionD(
          x -> {
            throw new Error("OUCH");
          },
          x -> "x")));
  }

  @Test
  public void testPostconditionDouble()
  {
    final double r =
      Postconditions.checkPostconditionD(22.0, Contracts.conditionD(
        x -> x < 23.0,
        x -> String.format("Value %f must be < 23", Double.valueOf(x))));

    Assertions.assertEquals(22.0, r, 0.00000001);
  }

  @Test
  public void testPostconditionDoubleOther()
  {
    final double r = Postconditions.checkPostconditionD(
      22.0, true, x -> "OK");
    Assertions.assertEquals(22.0, r, 0.00000001);
  }

  @Test
  public void testPostconditionSimpleViolation0()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostcondition(false, () -> "Value must true"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionSimpleViolation1()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostcondition(false, "Value must true"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionSimpleViolation2()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostcondition(
          Integer.valueOf(23),
          23 < 23,
          x -> String.format(
            "Value %d must < 23",
            x)));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionSimple0()
  {
    Postconditions.checkPostcondition(true, () -> "Value must true");
  }

  @Test
  public void testPostconditionSimple1()
  {
    Postconditions.checkPostcondition(true, "Value must true");
  }

  @Test
  public void testPostconditionSimple2()
  {
    final Integer r = Postconditions.checkPostcondition(
      Integer.valueOf(22),
      true,
      x -> String.format("Value %d must < 23", x));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPostconditionV()
  {
    final Integer r = Postconditions.checkPostconditionV(
      Integer.valueOf(22),
      true,
      "Value %d must be < 23",
      Integer.valueOf(22));

    Assertions.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPostconditionVFailed()
  {
    final PostconditionViolationException ex =
      Assertions.assertThrows(
        PostconditionViolationException.class,
        () -> Postconditions.checkPostconditionV(Integer.valueOf(22), false, "Failed"));

    Assertions.assertEquals(1, ex.violations());
  }

  @Test
  public void testPostconditionVNoValue()
  {
    Postconditions.checkPostconditionV(
      true,
      "Value %d must be < 23",
      Integer.valueOf(22));
  }

  @Test
  public void testPostconditionVNoValueFailed()
  {
    final PostconditionViolationException ex = Assertions.assertThrows(
      PostconditionViolationException.class,
      () -> Postconditions.checkPostconditionV(
        false,
        "Value %d must be < 23",
        Integer.valueOf(22)));

    Assertions.assertEquals(1, ex.violations());
  }
}
