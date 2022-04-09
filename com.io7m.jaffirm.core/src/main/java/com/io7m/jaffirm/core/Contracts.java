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

package com.io7m.jaffirm.core;

import com.io7m.junreachable.UnreachableCodeException;

import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/**
 * Functions to create contracts and conditions.
 */

public final class Contracts
{
  private Contracts()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Construct a predicate from the given predicate function and describer.
   *
   * @param condition The predicate function
   * @param describer The describer
   * @param <T>       The type of values
   *
   * @return A predicate
   */

  public static <T> ContractCondition<T> condition(
    final Predicate<T> condition,
    final Function<T, String> describer)
  {
    return ContractCondition.of(condition, describer);
  }

  /**
   * Construct an {@code int} specialized predicate from the given predicate
   * function and describer.
   *
   * @param condition The predicate function
   * @param describer The describer
   *
   * @return A predicate
   */

  public static ContractIntCondition conditionI(
    final IntPredicate condition,
    final IntFunction<String> describer)
  {
    return ContractIntCondition.of(condition, describer);
  }

  /**
   * Construct a {@code long} specialized predicate from the given predicate
   * function and describer.
   *
   * @param condition The predicate function
   * @param describer The describer
   *
   * @return A predicate
   */

  public static ContractLongCondition conditionL(
    final LongPredicate condition,
    final LongFunction<String> describer)
  {
    return ContractLongCondition.of(condition, describer);
  }

  /**
   * Construct a {@code double} specialized predicate from the given predicate
   * function and describer.
   *
   * @param condition The predicate function
   * @param describer The describer
   *
   * @return A predicate
   */

  public static ContractDoubleCondition conditionD(
    final DoublePredicate condition,
    final DoubleFunction<String> describer)
  {
    return ContractDoubleCondition.of(condition, describer);
  }
}
