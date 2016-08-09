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

import com.io7m.jaffirm.core.ContractDoubleCondition;
import com.io7m.jaffirm.core.ContractDoubleConditionType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;

public final class ContractDoubleConditionTest
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testConditionBuilder0()
  {
    final ContractDoubleCondition.Builder b = ContractDoubleCondition.builder();

    this.expected.expect(IllegalStateException.class);
    b.build();
  }

  @Test
  public void testConditionBuilder1()
  {
    final ContractDoubleCondition.Builder b = ContractDoubleCondition.builder();

    b.setPredicate(x -> true);
    this.expected.expect(IllegalStateException.class);
    b.build();
  }

  @Test
  public void testConditionBuilder2()
  {
    final ContractDoubleCondition.Builder b = ContractDoubleCondition.builder();

    b.setDescriber(x -> "description");
    this.expected.expect(IllegalStateException.class);
    b.build();
  }

  @Test
  public void testConditionEquals()
  {
    final DoublePredicate fa = x -> true;
    final DoubleFunction<String> da = x -> "x";
    final DoublePredicate fb = x -> true;
    final DoubleFunction<String> db = x -> "x";

    final ContractDoubleCondition c0 = ContractDoubleCondition.of(fa, da);
    final ContractDoubleCondition c1 = ContractDoubleCondition.of(fa, da);

    Assert.assertEquals(c0, c1);
    Assert.assertEquals(c1, c0);
    Assert.assertEquals((long) c0.hashCode(), (long) c1.hashCode());
    Assert.assertEquals(c0.toString(), c1.toString());

    Assert.assertEquals(ContractDoubleCondition.copyOf(c0), c0);
    Assert.assertEquals(ContractDoubleCondition.copyOf(
      new ContractDoubleConditionType()
      {
        @Override
        public DoublePredicate predicate()
        {
          return fa;
        }

        @Override
        public DoubleFunction<String> describer()
        {
          return da;
        }
      }), c0);
    Assert.assertEquals(ContractDoubleCondition.builder().from(c0).build(), c0);

    Assert.assertNotEquals(c0, Double.valueOf(23));
    Assert.assertNotEquals(c0, null);

    Assert.assertNotEquals(c0, ContractDoubleCondition.of(fa, db));
    Assert.assertNotEquals(c0, ContractDoubleCondition.of(fb, da));
    Assert.assertNotEquals(c0.withPredicate(fb), c0);
    Assert.assertNotEquals(c0.withDescriber(db), c0);

    Assert.assertEquals(c0.withPredicate(c0.predicate()), c0);
    Assert.assertEquals(c0.withDescriber(c0.describer()), c0);
  }
}
