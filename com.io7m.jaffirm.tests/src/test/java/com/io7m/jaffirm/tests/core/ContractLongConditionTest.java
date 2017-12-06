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

import com.io7m.jaffirm.core.ContractLongCondition;
import com.io7m.jaffirm.core.ContractLongConditionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.LongFunction;
import java.util.function.LongPredicate;

public final class ContractLongConditionTest
{
  @Test
  public void testConditionBuilder0()
  {
    final ContractLongCondition.Builder b = ContractLongCondition.builder();
    Assertions.assertThrows(IllegalStateException.class, b::build);
  }

  @Test
  public void testPredicateBuilder1()
  {
    final ContractLongCondition.Builder b = ContractLongCondition.builder();

    b.setPredicate(x -> true);
    Assertions.assertThrows(IllegalStateException.class, b::build);
  }

  @Test
  public void testConditionBuilder2()
  {
    final ContractLongCondition.Builder b = ContractLongCondition.builder();

    b.setDescriber(x -> "description");
    Assertions.assertThrows(IllegalStateException.class, b::build);
  }

  @Test
  public void testConditionEquals()
  {
    final LongPredicate fa = x -> true;
    final LongFunction<String> da = x -> "x";
    final LongPredicate fb = x -> true;
    final LongFunction<String> db = x -> "x";

    final ContractLongCondition c0 = ContractLongCondition.of(fa, da);
    final ContractLongCondition c1 = ContractLongCondition.of(fa, da);

    Assertions.assertEquals(c0, c1);
    Assertions.assertEquals(c1, c0);
    Assertions.assertEquals((long) c0.hashCode(), (long) c1.hashCode());
    Assertions.assertEquals(c0.toString(), c1.toString());

    Assertions.assertEquals(ContractLongCondition.copyOf(c0), c0);
    Assertions.assertEquals(ContractLongCondition.copyOf(
      new ContractLongConditionType()
      {
        @Override
        public LongPredicate predicate()
        {
          return fa;
        }

        @Override
        public LongFunction<String> describer()
        {
          return da;
        }
      }), c0);
    Assertions.assertEquals(
      ContractLongCondition.builder().from(c0).build(),
      c0);

    Assertions.assertNotEquals(c0, Long.valueOf(23));
    Assertions.assertNotEquals(c0, null);

    Assertions.assertNotEquals(c0, ContractLongCondition.of(fa, db));
    Assertions.assertNotEquals(c0, ContractLongCondition.of(fb, da));
    Assertions.assertNotEquals(c0.withPredicate(fb), c0);
    Assertions.assertNotEquals(c0.withDescriber(db), c0);

    Assertions.assertEquals(c0.withPredicate(c0.predicate()), c0);
    Assertions.assertEquals(c0.withDescriber(c0.describer()), c0);
  }
}
