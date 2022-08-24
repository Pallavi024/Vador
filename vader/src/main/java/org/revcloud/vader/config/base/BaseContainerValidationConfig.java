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

package org.revcloud.vader.config.base;

import io.vavr.Tuple2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.config.container.ContainerValidationConfigEx;
import org.revcloud.vader.types.Validator;
import org.revcloud.vader.types.ValidatorEtr;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public abstract class BaseContainerValidationConfig<ContainerValidatableT, FailureT> {
  @Nullable protected Tuple2<@NonNull Integer, @Nullable FailureT> shouldHaveMinBatchSizeOrFailWith;
  @Nullable protected Tuple2<@NonNull Integer, @Nullable FailureT> shouldHaveMaxBatchSizeOrFailWith;

  @Singular
  protected Collection<ValidatorEtr<ContainerValidatableT, FailureT>> withContainerValidatorEtrs;

  @Nullable
  protected Tuple2<
          @NonNull Collection<Validator<? super ContainerValidatableT, @Nullable FailureT>>,
          @Nullable FailureT>
      withContainerValidators;

  @Singular("withContainerValidator")
  protected Map<Validator<? super ContainerValidatableT, FailureT>, FailureT>
      withContainerValidator;

  public List<ValidatorEtr<ContainerValidatableT, @Nullable FailureT>> getContainerValidators() {
    return ContainerValidationConfigEx.getContainerValidatorsEx(this);
  }
}