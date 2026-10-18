// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_status_components_inner_gating.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkStatusComponentsInnerGatingCWProxy {
  BenchmarkStatusComponentsInnerGating proposerAutoExecution(
    BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum?
    proposerAutoExecution,
  );

  BenchmarkStatusComponentsInnerGating llmEnsembleWeight(
    BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum?
    llmEnsembleWeight,
  );

  BenchmarkStatusComponentsInnerGating findingCaveat(String? findingCaveat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkStatusComponentsInnerGating(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkStatusComponentsInnerGating(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkStatusComponentsInnerGating call({
    BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum?
    proposerAutoExecution,
    BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum?
    llmEnsembleWeight,
    String? findingCaveat,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkStatusComponentsInnerGating.copyWith(...)` or call `instanceOfBenchmarkStatusComponentsInnerGating.copyWith.fieldName(value)` for a single field.
class _$BenchmarkStatusComponentsInnerGatingCWProxyImpl
    implements _$BenchmarkStatusComponentsInnerGatingCWProxy {
  const _$BenchmarkStatusComponentsInnerGatingCWProxyImpl(this._value);

  final BenchmarkStatusComponentsInnerGating _value;

  @override
  BenchmarkStatusComponentsInnerGating proposerAutoExecution(
    BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum?
    proposerAutoExecution,
  ) => call(proposerAutoExecution: proposerAutoExecution);

  @override
  BenchmarkStatusComponentsInnerGating llmEnsembleWeight(
    BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum?
    llmEnsembleWeight,
  ) => call(llmEnsembleWeight: llmEnsembleWeight);

  @override
  BenchmarkStatusComponentsInnerGating findingCaveat(String? findingCaveat) =>
      call(findingCaveat: findingCaveat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkStatusComponentsInnerGating(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkStatusComponentsInnerGating(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkStatusComponentsInnerGating call({
    Object? proposerAutoExecution = const $CopyWithPlaceholder(),
    Object? llmEnsembleWeight = const $CopyWithPlaceholder(),
    Object? findingCaveat = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkStatusComponentsInnerGating(
      proposerAutoExecution:
          proposerAutoExecution == const $CopyWithPlaceholder()
          ? _value.proposerAutoExecution
          // ignore: cast_nullable_to_non_nullable
          : proposerAutoExecution
                as BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum?,
      llmEnsembleWeight: llmEnsembleWeight == const $CopyWithPlaceholder()
          ? _value.llmEnsembleWeight
          // ignore: cast_nullable_to_non_nullable
          : llmEnsembleWeight
                as BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum?,
      findingCaveat: findingCaveat == const $CopyWithPlaceholder()
          ? _value.findingCaveat
          // ignore: cast_nullable_to_non_nullable
          : findingCaveat as String?,
    );
  }
}

extension $BenchmarkStatusComponentsInnerGatingCopyWith
    on BenchmarkStatusComponentsInnerGating {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkStatusComponentsInnerGating.copyWith(...)` or `instanceOfBenchmarkStatusComponentsInnerGating.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkStatusComponentsInnerGatingCWProxy get copyWith =>
      _$BenchmarkStatusComponentsInnerGatingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkStatusComponentsInnerGating
_$BenchmarkStatusComponentsInnerGatingFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BenchmarkStatusComponentsInnerGating', json, (
  $checkedConvert,
) {
  final val = BenchmarkStatusComponentsInnerGating(
    proposerAutoExecution: $checkedConvert(
      'proposerAutoExecution',
      (v) => $enumDecodeNullable(
        _$BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnumEnumMap,
        v,
      ),
    ),
    llmEnsembleWeight: $checkedConvert(
      'llmEnsembleWeight',
      (v) => $enumDecodeNullable(
        _$BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnumEnumMap,
        v,
      ),
    ),
    findingCaveat: $checkedConvert('findingCaveat', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$BenchmarkStatusComponentsInnerGatingToJson(
  BenchmarkStatusComponentsInnerGating instance,
) => <String, dynamic>{
  'proposerAutoExecution':
      ?_$BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnumEnumMap[instance
          .proposerAutoExecution],
  'llmEnsembleWeight':
      ?_$BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnumEnumMap[instance
          .llmEnsembleWeight],
  'findingCaveat': ?instance.findingCaveat,
};

const _$BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnumEnumMap = {
  BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum.allowed:
      'allowed',
  BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum.degraded:
      'degraded',
};

const _$BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnumEnumMap = {
  BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum.allowed: 'allowed',
  BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum.degraded:
      'degraded',
};
