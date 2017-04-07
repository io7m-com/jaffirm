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
import com.io7m.jaffirm.core.PostconditionViolationException;
import com.io7m.jaffirm.core.Postconditions;
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

public final class PostconditionsTest
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testUnreachable()
    throws Exception
  {
    final Constructor<Postconditions> c = Postconditions.class.getDeclaredConstructor();
    c.setAccessible(true);

    this.expected.expect(InvocationTargetException.class);
    c.newInstance();
  }

  @Test
  public void testPostconditionsViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionsViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionsViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new Error("OUCH");
        },
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPostconditionsViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPostconditionsViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPostconditionsViolationMulti()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() > 23,
        x -> String.format("Value %d must be > 23", x)),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPostconditionsViolationMultiLast()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() == 23,
        x -> String.format("Value %d must be == 23", x)),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPostconditionsViolationMultiFirst()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditions(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)),
      Contracts.condition(
        x -> x.intValue() == 23,
        x -> String.format("Value %d must be == 23", x)));
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

    Assert.assertEquals(Integer.valueOf(22), r);
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPostconditionsIntViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPostconditionsIntViolationMulti()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> x > 23,
        x -> format("Value %d must be > 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPostconditionsIntViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionsIntViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionsIntViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPostconditionsIntViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPostconditionsIntMulti()
  {
    final int r = Postconditions.checkPostconditionsI(
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
  public void testPostconditionsIntViolationMultiLast()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> x == 23,
        x -> format("Value %d must be == 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPostconditionsIntViolationMultiFirst()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))),
      Contracts.conditionI(
        x -> x == 23,
        x -> format("Value %d must be == 23", Integer.valueOf(x))));
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPostconditionsLongViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPostconditionsLongViolationMulti()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> x > 23L,
        x -> format("Value %d must be > 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPostconditionsLongMulti()
  {
    final long r = Postconditions.checkPostconditionsL(
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
  public void testPostconditionsLongViolationMultiLast()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> x == 23L,
        x -> format("Value %d must be == 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPostconditionsLongViolationMultiFirst()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))),
      Contracts.conditionL(
        x -> x == 23L,
        x -> format("Value %d must be == 23", Long.valueOf(x))));
  }

  @Test
  public void testPostconditionsLongViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionsLongViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionsLongViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf((double) x))));
  }

  @Test
  public void testPostconditionsLongViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionsL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf((double) x))));
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPostconditionsDoubleViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPostconditionsDoubleViolationMulti()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(2));

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x > 23.0,
        x -> format("Value %f must be > 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPostconditionsDoubleViolationMultiLast()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x == 23.0,
        x -> format("Value %f must be == 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPostconditionsDoubleViolationMultiFirst()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))),
      Contracts.conditionD(
        x -> x == 23.0,
        x -> format("Value %f must be == 23", Double.valueOf(x))));
  }

  @Test
  public void testPostconditionsDoubleViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionsDoubleViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionsDoubleViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPostconditionsDoubleViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionsD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPostconditionsDoubleMulti()
  {
    final double r = Postconditions.checkPostconditionsD(
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

    Assert.assertEquals(3L, (long) called.get());
  }

  @Test
  public void testPostconditionViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostcondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));
  }

  @Test
  public void testPostconditionViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostcondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostcondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostcondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostconditionViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostcondition(
      Integer.valueOf(23),
      Contracts.condition(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostcondition()
  {
    final Integer r = Postconditions.checkPostcondition(
      Integer.valueOf(22),
      Contracts.condition(
        x -> x.intValue() < 23,
        x -> String.format("Value %d must be < 23", x)));

    Assert.assertEquals(Integer.valueOf(22), r);
  }

  @Test
  public void testPostconditionSupplierException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostcondition(
      false, () -> {
        throw new RuntimeException("OUCH");
      });
  }

  @Test
  public void testPostconditionIntViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));
  }

  @Test
  public void testPostconditionIntViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionIntViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionI(
      23,
      Contracts.conditionI(
        x -> x < 23,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionIntViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionI(
      23,
      Contracts.conditionI(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostconditionIntViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionI(
      23,
      Contracts.conditionI(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostconditionInt()
  {
    final int r = Postconditions.checkPostconditionI(
      22,
      Contracts.conditionI(
        x -> x < 23,
        x -> format("Value %d must be < 23", Integer.valueOf(x))));

    Assert.assertEquals(22L, (long) r);
  }

  @Test
  public void testPostconditionIntOther()
  {
    final int r = Postconditions.checkPostconditionI(
      22, true, x -> "OK");
    Assert.assertEquals(22L, (long) r);
  }

  @Test
  public void testPostconditionLongViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));
  }

  @Test
  public void testPostconditionLongViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionLongViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionL(
      23L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionLongViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostconditionLongViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionL(
      23L,
      Contracts.conditionL(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostconditionLong()
  {
    final long r = Postconditions.checkPostconditionL(
      22L,
      Contracts.conditionL(
        x -> x < 23L,
        x -> format("Value %d must be < 23", Long.valueOf(x))));

    Assert.assertEquals(22L, r);
  }

  @Test
  public void testPostconditionLongOther()
  {
    final long r = Postconditions.checkPostconditionL(
      22L, true, x -> "OK");
    Assert.assertEquals(22L, r);
  }

  @Test
  public void testPostconditionDoubleViolation()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));
  }

  @Test
  public void testPostconditionDoubleViolationDescriberException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new RuntimeException("OUCH");
        }));
  }

  @Test
  public void testPostconditionDoubleViolationDescriberError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionD(
      23.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> {
          throw new Error("OUCH");
        }));
  }

  @Test
  public void testPostconditionDoubleViolationPredicateException()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostconditionD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new RuntimeException("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostconditionDoubleViolationPredicateError()
  {
    this.expected.expect(Error.class);

    Postconditions.checkPostconditionD(
      23.0,
      Contracts.conditionD(
        x -> {
          throw new Error("OUCH");
        },
        x -> "x"));
  }

  @Test
  public void testPostconditionDouble()
  {
    final double r = Postconditions.checkPostconditionD(
      22.0,
      Contracts.conditionD(
        x -> x < 23.0,
        x -> format("Value %f must be < 23", Double.valueOf(x))));

    Assert.assertEquals(22.0, r, 0.0);
  }

  @Test
  public void testPostconditionDoubleOther()
  {
    final double r = Postconditions.checkPostconditionD(
      22.0, true, x -> "OK");
    Assert.assertEquals(22.0, r, 0.0);
  }

  @Test
  public void testPostconditionSimpleViolation0()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostcondition(false, () -> "Value must true");
  }

  @Test
  public void testPostconditionSimpleViolation1()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostcondition(false, "Value must true");
  }

  @Test
  public void testPostconditionSimpleViolation2()
  {
    this.expected.expect(PostconditionViolationException.class);
    this.expected.expect(new ViolationMatcher(1));

    Postconditions.checkPostcondition(
      Integer.valueOf(23), 23 < 23, x -> String.format("Value %d must < 23", x));
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
