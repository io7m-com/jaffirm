/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

import com.io7m.jaffirm.core.ContractException;
import com.io7m.jaffirm.core.Contracts;
import com.io7m.jaffirm.core.InvariantViolationException;
import com.io7m.jaffirm.core.Invariants;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public final class InvariantsTest
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testUnreachable()
    throws Exception
  {
    final Constructor<Invariants> c = Invariants.class.getDeclaredConstructor();
    c.setAccessible(true);

    this.expected.expect(InvocationTargetException.class);
    c.newInstance();
  }

  @Test
  public void testInvariantsViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantsViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantsViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testInvariantsViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testInvariantsViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testInvariantsViolationMulti()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() > 23,
        x -> String.format("Value %d must be > 23", x)),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testInvariantsViolationMultiLast()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() == 23,
        x -> String.format("Value %d must be == 23", x)),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testInvariantsViolationMultiFirst()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariants(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)),
      Contracts.condition(
        x -> x.intValue() == 23,
        x -> String.format("Value %d must be == 23", x)));
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

    Assert.assertEquals(Integer.valueOf(22), r);
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testInvariantsIntViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testInvariantsIntViolationMulti()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> x > 23,
        x -> format("Value %d must be > 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testInvariantsIntViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantsIntViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantsIntViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testInvariantsIntViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testInvariantsIntMulti()
  {
    final int r = Invariants.checkInvariantsI(
      22,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));

    Assert.assertEquals(22L, (long) r);
  }

  @Test
  public void testInvariantsIntViolationMultiLast()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> x == 23,
        x -> format("Value %d must be == 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testInvariantsIntViolationMultiFirst()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x == 23,
        x -> format("Value %d must be == 23", Integer.valueOf(x))));
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testInvariantsLongViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testInvariantsLongViolationMulti()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> x > 23L,
        x -> format("Value %d must be > 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testInvariantsLongMulti()
  {
    final long r = Invariants.checkInvariantsL(
      22L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));

    Assert.assertEquals(22L, r);
  }

  @Test
  public void testInvariantsLongViolationMultiLast()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> x == 23L,
        x -> format("Value %d must be == 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testInvariantsLongViolationMultiFirst()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x == 23L,
        x -> format("Value %d must be == 23", Long.valueOf(x))));
  }

  @Test
  public void testInvariantsLongViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantsLongViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantsLongViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf((double) x))));
  }

  @Test
  public void testInvariantsLongViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantsL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf((double) x))));
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testInvariantsDoubleViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testInvariantsDoubleViolationMulti()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> x > 23.0,
        x -> format("Value %f must be > 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testInvariantsDoubleViolationMultiLast()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> x == 23.0,
        x -> format("Value %f must be == 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testInvariantsDoubleViolationMultiFirst()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x == 23.0,
        x -> format("Value %f must be == 23", Double.valueOf(x))));
  }

  @Test
  public void testInvariantsDoubleViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantsDoubleViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantsDoubleViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testInvariantsDoubleViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantsD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testInvariantsDoubleMulti()
  {
    final double r = Invariants.checkInvariantsD(
      22.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));

    Assert.assertEquals(22.0, r, 0.0);
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testInvariantViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariant(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testInvariantViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariant(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariant(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariant(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariantViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariant(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariant()
  {
    final Integer r = Invariants.checkInvariant(
      Integer.valueOf(22),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));

    Assert.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testInvariantSupplierException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariant(
      false, () -> {
        throw new RuntimeException("OUCH");
      });
  }

  @Test
  public void testInvariantIntViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testInvariantIntViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantIntViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantIntViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantI(
      23,
      Contracts.conditionI(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariantIntViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantI(
      23,
      Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariantInt()
  {
    final int r = Invariants.checkInvariantI(
      22,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));

    Assert.assertEquals(22L, (long) r);
  }

  @Test
  public void testInvariantIntOther()
  {
    final int r =
      Invariants.checkInvariantI(22, true, x -> "OK");
    Assert.assertEquals(22L, (long) r);
  }

  @Test
  public void testInvariantLongViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testInvariantLongViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantLongViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantLongViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariantLongViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariantLong()
  {
    final long r = Invariants.checkInvariantL(
      22L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));

    Assert.assertEquals(22L, r);
  }

  @Test
  public void testInvariantLongOther()
  {
    final long r =
      Invariants.checkInvariantL(22L, true, x -> "OK");
    Assert.assertEquals(22L, r);
  }

  @Test
  public void testInvariantDoubleViolation()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testInvariantDoubleViolationDescriberException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testInvariantDoubleViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testInvariantDoubleViolationPredicateException()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariantDoubleViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Invariants.checkInvariantD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testInvariantDouble()
  {
    final double r = Invariants.checkInvariantD(
      22.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));

    Assert.assertEquals(22.0, r, 0.0);
  }

  @Test
  public void testInvariantDoubleOther()
  {
    final double r = Invariants.checkInvariantD(
      22.0, true, x -> "OK");
    Assert.assertEquals(22.0, r, 0.0);
  }

  @Test
  public void testInvariantSimpleViolation0()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariant(false, () -> "Value must true");
  }

  @Test
  public void testInvariantSimpleViolation1()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariant(false, "Value must true");
  }

  @Test
  public void testInvariantSimpleViolation2()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariant(
      Integer.valueOf(23),
      23 < 23,
      x -> String.format("Value %d must < 23", x));
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
      Integer.valueOf(22), true, x -> String.format("Value %d must < 23", x));

    Assert.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testInvariantV()
  {
    final Integer r = Invariants.checkInvariantV(
      Integer.valueOf(22),
      true,
      "Value %d must be < 23",
      Integer.valueOf(22));

    Assert.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testInvariantVFailed()
  {
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantV(
      Integer.valueOf(22),
      false,
      "Failed");
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
    this.expected.expect(InvariantViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Invariants.checkInvariantV(
      false,
      "Value %d must be < 23",
      Integer.valueOf(22));
  }

  private class ViolationMatcher extends TypeSafeMatcher<ContractException>
  {
    private final int expected;
    private int count;

    public ViolationMatcher(final int expected)
    {
      this.expected = expected;
    }

    @Override
    protected boolean matchesSafely(final ContractException exception)
    {
      this.count = exception.violations();
      return this.expected == this.count;
    }

    @Override
    public void describeTo(final Description description)
    {
      description.appendValue(Integer.valueOf(this.count))
        .appendText(" was returned instead of ")
        .appendValue(Integer.valueOf(this.expected));
    }
  }
}
