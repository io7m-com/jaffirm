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
import com.io7m.jaffirm.core.PreconditionViolationException;
import com.io7m.jaffirm.core.Preconditions;
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

public final class PreconditionsTest
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testUnreachable()
    throws Exception
  {
    final Constructor<Preconditions> c = Preconditions.class.getDeclaredConstructor();
    c.setAccessible(true);

    this.expected.expect(InvocationTargetException.class);
    c.newInstance();
  }

  @Test
  public void testPreconditionsViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionsViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionsViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPreconditionsViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPreconditionsViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPreconditionsViolationMulti()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() > 23,
        x -> String.format("Value %d must be > 23", x)),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPreconditionsViolationMultiLast()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() == 23,
        x -> String.format("Value %d must be == 23", x)),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPreconditionsViolationMultiFirst()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)),
      Contracts.condition(
        x -> x.intValue() == 23,
        x -> String.format("Value %d must be == 23", x)));
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

    Assert.assertEquals(Integer.valueOf(22), r);
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionsIntViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPreconditionsIntViolationMulti()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> x > 23,
        x -> format("Value %d must be > 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPreconditionsIntViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionsIntViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionsIntViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPreconditionsIntViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPreconditionsIntMulti()
  {
    final int r = Preconditions.checkPreconditionsI(
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
  public void testPreconditionsIntViolationMultiLast()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> x == 23,
        x -> format("Value %d must be == 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPreconditionsIntViolationMultiFirst()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x == 23,
        x -> format("Value %d must be == 23", Integer.valueOf(x))));
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionsLongViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPreconditionsLongViolationMulti()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> x > 23L,
        x -> format("Value %d must be > 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPreconditionsLongMulti()
  {
    final long r = Preconditions.checkPreconditionsL(
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
  public void testPreconditionsLongViolationMultiLast()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> x == 23L,
        x -> format("Value %d must be == 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPreconditionsLongViolationMultiFirst()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x == 23L,
        x -> format("Value %d must be == 23", Long.valueOf(x))));
  }

  @Test
  public void testPreconditionsLongViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionsLongViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionsLongViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf((double) x))));
  }

  @Test
  public void testPreconditionsLongViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionsL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf((double) x))));
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionsDoubleViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPreconditionsDoubleViolationMulti()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x > 23.0,
        x -> format("Value %f must be > 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPreconditionsDoubleViolationMultiLast()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x == 23.0,
        x -> format("Value %f must be == 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPreconditionsDoubleViolationMultiFirst()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x == 23.0,
        x -> format("Value %f must be == 23", Double.valueOf(x))));
  }

  @Test
  public void testPreconditionsDoubleViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionsDoubleViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionsDoubleViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPreconditionsDoubleViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionsD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPreconditionsDoubleMulti()
  {
    final double r = Preconditions.checkPreconditionsD(
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPreconditionViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPrecondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPreconditionViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPrecondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPrecondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPrecondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPreconditionViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPrecondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPrecondition()
  {
    final Integer r = Preconditions.checkPrecondition(
      Integer.valueOf(22),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));

    Assert.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPreconditionSupplierException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPrecondition(
      false, () -> {
        throw new RuntimeException("OUCH");
      });
  }

  @Test
  public void testPreconditionIntViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPreconditionIntViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionIntViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionIntViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionI(
      23,
      Contracts.conditionI(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPreconditionIntViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionI(
      23,
      Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPreconditionInt()
  {
    final int r = Preconditions.checkPreconditionI(
      22,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));

    Assert.assertEquals(22L, (long) r);
  }

  @Test
  public void testPreconditionIntOther()
  {
    final int r = Preconditions.checkPreconditionI(
      22, true, x -> "OK");
    Assert.assertEquals(22L, (long) r);
  }

  @Test
  public void testPreconditionLongViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPreconditionLongViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionLongViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionLongViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPreconditionLongViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPreconditionLong()
  {
    final long r = Preconditions.checkPreconditionL(
      22L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));

    Assert.assertEquals(22L, r);
  }

  @Test
  public void testPreconditionLongOther()
  {
    final long r = Preconditions.checkPreconditionL(
      22L, true, x -> "OK");
    Assert.assertEquals(22L, r);
  }

  @Test
  public void testPreconditionDoubleViolation()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPreconditionDoubleViolationDescriberException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPreconditionDoubleViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPreconditionDoubleViolationPredicateException()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPreconditionD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPreconditionDoubleViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Preconditions.checkPreconditionD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPreconditionDouble()
  {
    final double r = Preconditions.checkPreconditionD(
      22.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));

    Assert.assertEquals(22.0, r, 0.0);
  }

  @Test
  public void testPreconditionDoubleOther()
  {
    final double r = Preconditions.checkPreconditionD(
      22.0, true, x -> "OK");
    Assert.assertEquals(22.0, r, 0.0);
  }

  @Test
  public void testPreconditionSimpleViolation0()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPrecondition(false, () -> "Value must true");
  }

  @Test
  public void testPreconditionSimpleViolation1()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPrecondition(false, "Value must true");
  }

  @Test
  public void testPreconditionSimpleViolation2()
  {
    this.expected.expect(PreconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Preconditions.checkPrecondition(
      Integer.valueOf(23), 23 < 23, x -> String.format("Value %d must < 23", x));
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
      Integer.valueOf(22), true, x -> String.format("Value %d must < 23", x));

    Assert.assertEquals(Integer.valueOf(22), r);
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
