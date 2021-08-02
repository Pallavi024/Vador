@file:JvmName("ContainerValidationConfigEx")

package org.revcloud.vader.runner

import de.cronn.reflection.util.PropertyUtils
import org.revcloud.vader.types.validators.ValidatorEtr

internal fun <ContainerValidatableT, FailureT> BaseContainerValidationConfig<ContainerValidatableT?, FailureT?>.getContainerValidatorsEx(): List<ValidatorEtr<ContainerValidatableT?, FailureT?>> =
  fromValidators1(withContainerValidators) + fromValidators2(withContainerValidator) + withContainerValidatorEtrs

internal fun <ContainerValidatableT, FailureT> ContainerValidationConfig<ContainerValidatableT?, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<ContainerValidatableT>
): Set<String> =
  withBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <ContainerValidatableT, FailureT> ContainerValidationConfigWithNested<ContainerValidatableT?, *, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<ContainerValidatableT>
): Set<String> =
  withBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <NestedContainerValidatableT, FailureT> ContainerValidationConfigWithNested<*, NestedContainerValidatableT?, FailureT?>.getFieldNamesForNestedBatchEx(
  validatableClazz: Class<NestedContainerValidatableT>
): Set<String> =
  withNestedContainerValidationConfig.withBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }
    .toSet()