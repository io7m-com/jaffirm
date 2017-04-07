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
import java.util.function.Supplier;

import static com.io7m.jaffirm.core.SafeApplication.applyDescriberChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberDChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberIChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberLChecked;
import static com.io7m.jaffirm.core.SafeApplication.applySupplierChecked;
import static com.io7m.jaffirm.core.SafeApplication.failedPredicate;
import static com.io7m.jaffirm.core.Violations.innerCheckAll;
import static com.io7m.jaffirm.core.Violations.innerCheckAllDouble;
import static com.io7m.jaffirm.core.Violations.innerCheckAllInt;
import static com.io7m.jaffirm.core.Violations.innerCheckAllLong;
import static com.io7m.jaffirm.core.Violations.singleViolation;

/**
 * Functions to check preconditions.
 */

public final class Preconditions
{
  private Preconditions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * <p>Evaluate all of the given {@code conditions} using {@code value} as
   * input.</p>
   *
   * <p>All of the conditions are evaluated and the function throws {@link
   * PreconditionViolationException} if any of the conditions are false, or
   * raise an exception that is not of type {@link Error}. Exceptions of type
   * {@link Error} are propagated immediately, without any further contract
   * checking.</p>
   *
   * @param value      The value
   * @param conditions The set of conditions
   * @param <T>        The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  @SafeVarargs
  public static <T> T checkPreconditions(
    final T value,
    final ContractConditionType<T>... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAll(value, conditions);
    if (violations != null) {
      throw failed(value, violations);
    }
    return value;
  }

  /**
   * An {@code int} specialized version of {@link #checkPreconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  public static int checkPreconditionsI(
    final int value,
    final ContractIntConditionType... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAllInt(value, conditions);
    if (violations != null) {
      throw failed(Integer.valueOf(value), violations);
    }
    return value;
  }

  /**
   * A {@code long} specialized version of {@link #checkPreconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  public static long checkPreconditionsL(
    final long value,
    final ContractLongConditionType... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAllLong(value, conditions);
    if (violations != null) {
      throw failed(Long.valueOf(value), violations);
    }
    return value;
  }

  /**
   * A {@code double} specialized version of {@link #checkPreconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PreconditionViolationException If any of the conditions are false
   */

  public static double checkPreconditionsD(
    final double value,
    final ContractDoubleConditionType... conditions)
    throws PreconditionViolationException
  {
    final Violations violations = innerCheckAllDouble(value, conditions);
    if (violations != null) {
      throw failed(Double.valueOf(value), violations);
    }
    return value;
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PreconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static <T> T checkPrecondition(
    final T value,
    final ContractConditionType<T> condition)
    throws PreconditionViolationException
  {
    return checkPrecondition(
      value, condition.predicate(), condition.describer());
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PreconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static <T> T checkPrecondition(
    final T value,
    final Predicate<T> predicate,
    final Function<T, String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw failed(value, singleViolation(failedPredicate(e)));
    }

    return innerCheck(value, ok, describer);
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PreconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static <T> T checkPrecondition(
    final T value,
    final boolean condition,
    final Function<T, String> describer)
  {
    return innerCheck(value, condition, describer);
  }

  /**
   * A specialized version of {@link #checkPrecondition(Object, boolean,
   * Function)} that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description
   *
   * @throws PreconditionViolationException Iff {@code predicate == false}
   */

  public static void checkPrecondition(
    final boolean condition,
    final String message)
    throws PreconditionViolationException
  {
    if (!condition) {
      throw failed("<unspecified>", singleViolation(message));
    }
  }

  /**
   * A specialized version of {@link #checkPrecondition(Object, boolean,
   * Function)} that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description supplier
   *
   * @throws PreconditionViolationException Iff {@code predicate == false}
   */

  public static void checkPrecondition(
    final boolean condition,
    final Supplier<String> message)
    throws PreconditionViolationException
  {
    if (!condition) {
      throw failed(
        "<unspecified>", singleViolation(applySupplierChecked(message)));
    }
  }

  /**
   * <p>A version of {@link #checkPrecondition(boolean, String)} that constructs
   * a description message from the given format string and arguments.</p>
   *
   * <p>Note that the use of variadic arguments may entail allocating memory on
   * virtual machines that fail to eliminate the allocations with <i>escape
   * analysis</i>.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param format    The format string
   * @param objects   The format string arguments
   * @param <T>       The precise type of values
   *
   * @return {@code value}
   *
   * @since 1.1.0
   */

  public static <T> T checkPreconditionV(
    final T value,
    final boolean condition,
    final String format,
    final Object... objects)
  {
    if (!condition) {
      throw failed(value, singleViolation(String.format(format, objects)));
    }
    return value;
  }

  /**
   * <p>A version of {@link #checkPrecondition(boolean, String)} that constructs
   * a description message from the given format string and arguments.</p>
   *
   * <p>Note that the use of variadic arguments may entail allocating memory on
   * virtual machines that fail to eliminate the allocations with <i>escape
   * analysis</i>.</p>
   *
   * @param condition The predicate
   * @param format    The format string
   * @param objects   The format string arguments
   *
   * @since 1.1.0
   */

  public static void checkPreconditionV(
    final boolean condition,
    final String format,
    final Object... objects)
  {
    checkPreconditionV("<unspecified>", condition, format, objects);
  }

  /**
   * An {@code int} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static int checkPreconditionI(
    final int value,
    final ContractIntConditionType condition)
    throws PreconditionViolationException
  {
    return checkPreconditionI(
      value, condition.predicate(), condition.describer());
  }

  /**
   * An {@code int} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static int checkPreconditionI(
    final int value,
    final IntPredicate predicate,
    final IntFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw failed(
        Integer.valueOf(value),
        singleViolation(failedPredicate(e)));
    }

    return innerCheckI(value, ok, describer);
  }

  /**
   * An {@code int} specialized version of {@link #checkPrecondition(Object,
   * boolean, Function)}.
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static int checkPreconditionI(
    final int value,
    final boolean condition,
    final IntFunction<String> describer)
  {
    return innerCheckI(value, condition, describer);
  }

  /**
   * A {@code long} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static long checkPreconditionL(
    final long value,
    final ContractLongConditionType condition)
    throws PreconditionViolationException
  {
    return checkPreconditionL(
      value, condition.predicate(), condition.describer());
  }

  /**
   * A {@code long} specialized version of {@link #checkPrecondition(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static long checkPreconditionL(
    final long value,
    final LongPredicate predicate,
    final LongFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw failed(
        Long.valueOf(value),
        singleViolation(failedPredicate(e)));
    }

    return innerCheckL(value, ok, describer);
  }

  /**
   * A {@code long} specialized version of {@link #checkPrecondition(Object,
   * Predicate, Function)}
   *
   * @param condition The predicate
   * @param value     The value
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static long checkPreconditionL(
    final long value,
    final boolean condition,
    final LongFunction<String> describer)
  {
    return innerCheckL(value, condition, describer);
  }

  /**
   * A {@code double} specialized version of {@link #checkPrecondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static double checkPreconditionD(
    final double value,
    final ContractDoubleConditionType condition)
    throws PreconditionViolationException
  {
    return checkPreconditionD(
      value, condition.predicate(), condition.describer());
  }

  /**
   * A {@code double} specialized version of {@link #checkPrecondition(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static double checkPreconditionD(
    final double value,
    final DoublePredicate predicate,
    final DoubleFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw failed(
        Double.valueOf(value),
        singleViolation(failedPredicate(e)));
    }

    return innerCheckD(value, ok, describer);
  }

  /**
   * A {@code double} specialized version of {@link #checkPrecondition(Object,
   * boolean, Function)}
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PreconditionViolationException If the predicate is false
   */

  public static double checkPreconditionD(
    final double value,
    final boolean condition,
    final DoubleFunction<String> describer)
  {
    return innerCheckD(value, condition, describer);
  }

  private static <T> T innerCheck(
    final T value,
    final boolean condition,
    final Function<T, String> describer)
  {
    if (!condition) {
      throw failed(
        value,
        singleViolation(applyDescriberChecked(value, describer)));
    }
    return value;
  }

  private static int innerCheckI(
    final int value,
    final boolean condition,
    final IntFunction<String> describer)
  {
    if (!condition) {
      throw failed(
        Integer.valueOf(value),
        singleViolation(applyDescriberIChecked(value, describer)));
    }
    return value;
  }

  private static long innerCheckL(
    final long value,
    final boolean condition,
    final LongFunction<String> describer)
  {
    if (!condition) {
      throw failed(
        Long.valueOf(value),
        singleViolation(applyDescriberLChecked(value, describer)));
    }
    return value;
  }

  private static double innerCheckD(
    final double value,
    final boolean condition,
    final DoubleFunction<String> describer)
  {
    if (!condition) {
      throw failed(
        Double.valueOf(value),
        singleViolation(applyDescriberDChecked(value, describer)));
    }
    return value;
  }

  private static <T> PreconditionViolationException failed(
    final T value,
    final Violations violations)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Precondition violation.");
    sb.append(System.lineSeparator());

    sb.append("  Received: ");
    sb.append(value);
    sb.append(System.lineSeparator());

    sb.append("  Violated conditions: ");
    sb.append(System.lineSeparator());

    final String[] messages = violations.messages();
    for (int index = 0; index < messages.length; ++index) {
      if (messages[index] != null) {
        sb.append("    [");
        sb.append(index);
        sb.append("]: ");
        sb.append(messages[index]);
        sb.append(System.lineSeparator());
      }
    }

    throw new PreconditionViolationException(sb.toString(), violations.count());
  }
}
