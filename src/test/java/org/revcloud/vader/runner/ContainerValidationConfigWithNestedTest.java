package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_NESTED_BATCH_SIZE_EXCEEDED_2;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_1;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_2;
import static consumer.failure.ValidationFailure.MIN_NESTED_BATCH_SIZE_NOT_MET_1;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.runner.Runner.validateAndFailFastForContainer;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import java.util.Collections;
import java.util.List;
import kotlin.jvm.functions.Function1;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContainerValidationConfigWithNestedTest {

  @DisplayName("Min batch size for nested batch: Depth - 1")
  @Test
  void nestedBatchHeader1() {
    final var header1RootValidationConfig =
        ContainerValidationConfigWithNested.<Header1Root, Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header1Root::getHeader2)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withNestedContainerValidationConfig(
                ContainerValidationConfig.<Header2, ValidationFailure>toValidate()
                    .withBatchMapper(Header2::getHeader3Batch)
                    .shouldHaveMinBatchSize(Tuple.of(3, MIN_NESTED_BATCH_SIZE_NOT_MET_1))
                    .prepare())
            .prepare();
    final var header2ValidationConfig =
        ContainerValidationConfig.<Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header2::getHeader3Batch)
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_2))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var strBatch1ValidationConfig =
        ContainerValidationConfig.<String, ValidationFailure>toValidate()
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();

    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var header3Batch1 = List.of(new Header3(beanBatch1), new Header3(beanBatch2));
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    final var header3Batch2 = List.of(new Header3(beanBatch3), new Header3(beanBatch4));
    final var header2Batch = List.of(new Header2(header3Batch1), new Header2(header3Batch2));
    final List<String> strBatch1 = Collections.emptyList();
    final var header1Root = new Header1Root(header2Batch, strBatch1);

    final var result =
        Runner.validateAndFailFastForContainer(
                header1Root, header1RootValidationConfig, throwableMapper)
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        header2Batch, header2ValidationConfig, throwableMapper))
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        strBatch1, strBatch1ValidationConfig, throwableMapper));

    assertThat(result).contains(MIN_NESTED_BATCH_SIZE_NOT_MET_1);
  }

  @DisplayName("Min batch size for nested batch: Depth - 2")
  @Test
  void nestedBatchHeader2() {
    final var header1RootValidationConfig =
        ContainerValidationConfigWithNested.<Header1Root, Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header1Root::getHeader2)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withNestedContainerValidationConfig(
                ContainerValidationConfig.<Header2, ValidationFailure>toValidate()
                    .withBatchMapper(Header2::getHeader3Batch)
                    .shouldHaveMinBatchSize(Tuple.of(3, MIN_NESTED_BATCH_SIZE_NOT_MET_1))
                    .prepare())
            .prepare();
    final var header2ValidationConfig =
        ContainerValidationConfigWithNested.<Header2, Header3, ValidationFailure>toValidate()
            .withBatchMapper(Header2::getHeader3Batch)
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_2))
            .withNestedContainerValidationConfig(
                ContainerValidationConfig.<Header3, ValidationFailure>toValidate()
                    .withBatchMapper(Header3::getBeanBatch)
                    .shouldHaveMaxBatchSize(Tuple.of(3, MAX_NESTED_BATCH_SIZE_EXCEEDED_2))
                    .withContainerValidator(ignore -> NONE, NONE)
                    .prepare())
            .prepare();
    final var strBatch1ValidationConfig =
        ContainerValidationConfig.<String, ValidationFailure>toValidate()
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();

    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var header3Batch1 = List.of(new Header3(beanBatch1), new Header3(beanBatch2));
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    final var header3Batch2 = List.of(new Header3(beanBatch3), new Header3(beanBatch4));
    final var header2Batch = List.of(new Header2(header3Batch1), new Header2(header3Batch2));
    final List<String> strBatch1 = Collections.emptyList();
    final var header1Root = new Header1Root(header2Batch, strBatch1);

    final var result =
        Runner.validateAndFailFastForContainer(
                header1Root, header1RootValidationConfig, throwableMapper)
            .or(
                () ->
                    validateAndFailFastForContainer(
                        header2Batch, header2ValidationConfig, throwableMapper))
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        strBatch1, strBatch1ValidationConfig, throwableMapper));

    assertThat(result).contains(MAX_NESTED_BATCH_SIZE_EXCEEDED_2);
  }

  @Value
  private static class Bean {}

  @Value
  private static class Header3 {
    List<Bean> beanBatch;
  }

  @Value
  private static class Header2 {
    List<Header3> header3Batch;
  }

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class Header1Root {
    List<Header2> header2;
    List<String> batch1Str;
  }
}