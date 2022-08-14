/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("BaseValidationConfigEx")

package org.revcloud.vader.runner.config

import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import net.jodah.typetools.TypeResolver
import org.revcloud.vader.specs.factory.SpecFactory
import org.revcloud.vader.specs.specs.BaseSpec
import org.revcloud.vader.types.Validator
import java.lang.reflect.Type
import java.util.Optional
import java.util.function.Predicate

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getSpecsEx(): List<BaseSpec<ValidatableT, FailureT>> {
  val specFactory = SpecFactory<ValidatableT, FailureT?>()
  return (specify?.invoke(specFactory)?.map { it.done() as BaseSpec<ValidatableT, FailureT> } ?: emptyList()) +
    withSpecs.map { it.invoke(specFactory).done() as BaseSpec<ValidatableT, FailureT> }
}

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getPredicateOfSpecForTestEx(
  nameForTest: String
): Optional<Predicate<ValidatableT?>> {
  // TODO 29/04/21 gopala.akshintala: Move this duplicate-check to ValidationConfig `prepare`
  val specNameToSpecs =
    specs.groupingBy { it.nameForTest }.eachCount().filter { it.value > 1 }.keys.filterNotNull()
  if (specNameToSpecs.isNotEmpty()) {
    throw IllegalArgumentException("Specs with Duplicate NamesForTest found: $specNameToSpecs")
  }
  return Optional.ofNullable(specs.first { it.nameForTest == nameForTest }?.toPredicate())
}

internal fun <ValidatableT> BaseValidationConfig<ValidatableT, *>.getRequiredFieldNamesEx(beanClass: Class<ValidatableT>): Set<String> =
  (shouldHaveFieldsOrFailWith.keys + (shouldHaveFieldsOrFailWithFn?._1 ?: emptyList()) + shouldHaveFieldOrFailWithFn.keys)
    .map { PropertyUtils.getPropertyName(beanClass, it) }.toSet()

internal fun <ValidatableT> getValidatableType(config: BaseValidationConfig<ValidatableT, *>): Type? {
  (config.withValidators?._1?.firstOrNull() ?: config.withValidator.keys.firstOrNull())?.let { return TypeResolver.resolveRawArguments(Validator::class.java, it.javaClass)[0] }
  // config.withValidatorEtrs.firstOrNull()?.let { return TypeResolver.resolveRawArguments(CheckedFunction1::class.java, it.javaClass)[0] }
  (config.shouldHaveFieldsOrFailWith.keys.firstOrNull() ?: config.shouldHaveFieldsOrFailWithFn?._1?.firstOrNull() ?: config.shouldHaveFieldOrFailWithFn.keys.firstOrNull())?.let { return TypeResolver.resolveRawArguments(TypedPropertyGetter::class.java, it.javaClass)[0] }
  // ! TODO gopala.akshintala 14/08/22: For other fields and FieldConfig
  config.withIdConfigs?.firstOrNull()?.prepare()?.shouldHaveValidSFIdFormatForAllOrFailWith?.keys?.firstOrNull()?._1?.let { return TypeResolver.resolveRawArguments(TypedPropertyGetter::class.java, it.javaClass)[0] }
  // config.withSpecs.firstOrNull()?.let { return TypeResolver.resolveRawArguments(Spec::class.java, it.javaClass)[0] }
  return null
}
