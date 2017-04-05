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

import com.io7m.jaffirm.core.ContractIntCondition;
import com.io7m.jaffirm.core.ContractIntConditionType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.IntFunction;
import java.util.function.IntPredicate;

public final class ContractIntConditionTest
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testConditionBuilder0()
  {
    final ContractIntCondition.Builder b = ContractIntCondition.builder();

    this.expected.expect(IllegalStateException.class);
    b.build();
  }

  @Test
  public void testConditionBuilder1()
  {
    final ContractIntCondition.Builder b = ContractIntCondition.builder();

    b.setPredicate(x -> true);
    this.expected.expect(IllegalStateException.class);
    b.build();
  }

  @Test
  public void testConditionBuilder2()
  {
    final ContractIntCondition.Builder b = ContractIntCondition.builder();

    b.setDescriber(x -> "description");
    this.expected.expect(IllegalStateException.class);
    b.build();
  }

  @Test
  public void testConditionEquals()
  {
    final IntPredicate fa = x -> true;
    final IntFunction<String> da = x -> "x";
    final IntPredicate fb = x -> true;
    final IntFunction<String> db = x -> "x";

    final ContractIntCondition c0 = ContractIntCondition.of(fa, da);
    final ContractIntCondition c1 = ContractIntCondition.of(fa, da);

    Assert.assertEquals(c0, c1);
    Assert.assertEquals(c1, c0);
    Assert.assertEquals((long) c0.hashCode(), (long) c1.hashCode());
    Assert.assertEquals(c0.toString(), c1.toString());

    Assert.assertEquals(ContractIntCondition.copyOf(c0), c0);
    Assert.assertEquals(ContractIntCondition.copyOf(
      new ContractIntConditionType()
      {
        @Override
        public IntPredicate predicate()
        {
          return fa;
        }

        @Override
        public IntFunction<String> describer()
        {
          return da;
        }
      }), c0);
    Assert.assertEquals(ContractIntCondition.builder().from(c0).build(), c0);

    Assert.assertNotEquals(c0, Integer.valueOf(23));
    Assert.assertNotEquals(c0, null);

    Assert.assertNotEquals(c0, ContractIntCondition.of(fa, db));
    Assert.assertNotEquals(c0, ContractIntCondition.of(fb, da));
    Assert.assertNotEquals(c0.withPredicate(fb), c0);
    Assert.assertNotEquals(c0.withDescriber(db), c0);

    Assert.assertEquals(c0.withPredicate(c0.predicate()), c0);
    Assert.assertEquals(c0.withDescriber(c0.describer()), c0);
  }
}
