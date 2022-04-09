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

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import static com.io7m.jaffirm.core.SafeApplication.applyDescriberChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberDChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberIChecked;
import static com.io7m.jaffirm.core.SafeApplication.applyDescriberLChecked;
import static com.io7m.jaffirm.core.SafeApplication.failedPredicate;

final class Violations
{
  private final String[] messages;
  private int count;

  private Violations(final int expected)
  {
    this.messages = new String[expected];
    this.count = 0;
  }

  static Violations singleViolation(
    final String message)
  {
    final Violations violations = new Violations(1);
    violations.messages[0] = message;
    violations.count = 1;
    return violations;
  }

  @SafeVarargs
  static <T> Violations innerCheckAll(
    final T value,
    final ContractConditionType<T>... conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractConditionType<T> condition = conditions[index];
      final Predicate<T> predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] = failedPredicate(e);
        violations.countUp();
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] =
          applyDescriberChecked(value, condition.describer());
        violations.countUp();
      }
    }
    return violations;
  }

  static Violations innerCheckAllInt(
    final int value,
    final ContractIntConditionType... conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractIntConditionType condition = conditions[index];
      final IntPredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] = failedPredicate(e);
        violations.countUp();
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] =
          applyDescriberIChecked(value, condition.describer());
        violations.countUp();
      }
    }
    return violations;
  }

  static Violations innerCheckAllLong(
    final long value,
    final ContractLongConditionType... conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractLongConditionType condition = conditions[index];
      final LongPredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] = failedPredicate(e);
        violations.countUp();
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] =
          applyDescriberLChecked(value, condition.describer());
        violations.countUp();
      }
    }
    return violations;
  }

  static Violations innerCheckAllDouble(
    final double value,
    final ContractDoubleConditionType... conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractDoubleConditionType condition = conditions[index];
      final DoublePredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] = failedPredicate(e);
        violations.countUp();
        continue;
      }

      if (!ok) {
        violations = maybeAllocate(violations, count);
        violations.messages()[index] =
          applyDescriberDChecked(value, condition.describer());
        violations.countUp();
      }
    }
    return violations;
  }

  private static Violations maybeAllocate(
    final Violations violations,
    final int count)
  {
    return violations == null ? new Violations(count) : violations;
  }

  String[] messages()
  {
    return this.messages;
  }

  int count()
  {
    return this.count;
  }

  private void countUp()
  {
    ++this.count;
  }
}
