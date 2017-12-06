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

import com.io7m.jaffirm.core.ContractCondition;
import com.io7m.jaffirm.core.ContractDoubleCondition;
import com.io7m.jaffirm.core.ContractIntCondition;
import com.io7m.jaffirm.core.ContractLongCondition;
import com.io7m.jaffirm.core.Contracts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

public final class ContractsTest
{
  @Test
  public void testContractUnreachable()
    throws Exception
  {
    final Constructor<Contracts> c = Contracts.class.getDeclaredConstructor();
    c.setAccessible(true);

    Assertions.assertThrows(InvocationTargetException.class, c::newInstance);
  }

  @Test
  public void testContractCondition()
  {
    final Predicate<Object> predicate = x -> true;
    final Function<Object, String> describer = x -> "x";

    Assertions.assertEquals(
      ContractCondition.of(predicate, describer),
      Contracts.condition(predicate, describer));
  }

  @Test
  public void testContractConditionInt()
  {
    final IntPredicate predicate = x -> true;
    final IntFunction<String> describer = x -> "x";

    Assertions.assertEquals(
      ContractIntCondition.of(predicate, describer),
      Contracts.conditionI(predicate, describer));
  }

  @Test
  public void testContractConditionLong()
  {
    final LongPredicate predicate = x -> true;
    final LongFunction<String> describer = x -> "x";

    Assertions.assertEquals(
      ContractLongCondition.of(predicate, describer),
      Contracts.conditionL(predicate, describer));
  }

  @Test
  public void testContractConditionDouble()
  {
    final DoublePredicate predicate = x -> true;
    final DoubleFunction<String> describer = x -> "x";

    Assertions.assertEquals(
      ContractDoubleCondition.of(predicate, describer),
      Contracts.conditionD(predicate, describer));
  }
}
