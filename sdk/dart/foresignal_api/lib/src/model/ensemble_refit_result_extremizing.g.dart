// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ensemble_refit_result_extremizing.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EnsembleRefitResultExtremizingCWProxy {
  EnsembleRefitResultExtremizing a(num a);

  EnsembleRefitResultExtremizing enabled(bool enabled);

  EnsembleRefitResultExtremizing nResolutions(int nResolutions);

  EnsembleRefitResultExtremizing distinctForecasterTypes(
    int distinctForecasterTypes,
  );

  EnsembleRefitResultExtremizing logScoreImprovement(num? logScoreImprovement);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResultExtremizing(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResultExtremizing(...).copyWith(id: 12, name: "My name")
  /// ```
  EnsembleRefitResultExtremizing call({
    num a,
    bool enabled,
    int nResolutions,
    int distinctForecasterTypes,
    num? logScoreImprovement,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEnsembleRefitResultExtremizing.copyWith(...)` or call `instanceOfEnsembleRefitResultExtremizing.copyWith.fieldName(value)` for a single field.
class _$EnsembleRefitResultExtremizingCWProxyImpl
    implements _$EnsembleRefitResultExtremizingCWProxy {
  const _$EnsembleRefitResultExtremizingCWProxyImpl(this._value);

  final EnsembleRefitResultExtremizing _value;

  @override
  EnsembleRefitResultExtremizing a(num a) => call(a: a);

  @override
  EnsembleRefitResultExtremizing enabled(bool enabled) =>
      call(enabled: enabled);

  @override
  EnsembleRefitResultExtremizing nResolutions(int nResolutions) =>
      call(nResolutions: nResolutions);

  @override
  EnsembleRefitResultExtremizing distinctForecasterTypes(
    int distinctForecasterTypes,
  ) => call(distinctForecasterTypes: distinctForecasterTypes);

  @override
  EnsembleRefitResultExtremizing logScoreImprovement(
    num? logScoreImprovement,
  ) => call(logScoreImprovement: logScoreImprovement);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResultExtremizing(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResultExtremizing(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EnsembleRefitResultExtremizing call({
    Object? a = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? nResolutions = const $CopyWithPlaceholder(),
    Object? distinctForecasterTypes = const $CopyWithPlaceholder(),
    Object? logScoreImprovement = const $CopyWithPlaceholder(),
  }) {
    return EnsembleRefitResultExtremizing(
      a: a == const $CopyWithPlaceholder() || a == null
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as num,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      nResolutions:
          nResolutions == const $CopyWithPlaceholder() || nResolutions == null
          ? _value.nResolutions
          // ignore: cast_nullable_to_non_nullable
          : nResolutions as int,
      distinctForecasterTypes:
          distinctForecasterTypes == const $CopyWithPlaceholder() ||
              distinctForecasterTypes == null
          ? _value.distinctForecasterTypes
          // ignore: cast_nullable_to_non_nullable
          : distinctForecasterTypes as int,
      logScoreImprovement: logScoreImprovement == const $CopyWithPlaceholder()
          ? _value.logScoreImprovement
          // ignore: cast_nullable_to_non_nullable
          : logScoreImprovement as num?,
    );
  }
}

extension $EnsembleRefitResultExtremizingCopyWith
    on EnsembleRefitResultExtremizing {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEnsembleRefitResultExtremizing.copyWith(...)` or `instanceOfEnsembleRefitResultExtremizing.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EnsembleRefitResultExtremizingCWProxy get copyWith =>
      _$EnsembleRefitResultExtremizingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EnsembleRefitResultExtremizing _$EnsembleRefitResultExtremizingFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EnsembleRefitResultExtremizing', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'a',
      'enabled',
      'nResolutions',
      'distinctForecasterTypes',
    ],
  );
  final val = EnsembleRefitResultExtremizing(
    a: $checkedConvert('a', (v) => v as num),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    nResolutions: $checkedConvert('nResolutions', (v) => (v as num).toInt()),
    distinctForecasterTypes: $checkedConvert(
      'distinctForecasterTypes',
      (v) => (v as num).toInt(),
    ),
    logScoreImprovement: $checkedConvert(
      'logScoreImprovement',
      (v) => v as num?,
    ),
  );
  return val;
});

Map<String, dynamic> _$EnsembleRefitResultExtremizingToJson(
  EnsembleRefitResultExtremizing instance,
) => <String, dynamic>{
  'a': instance.a,
  'enabled': instance.enabled,
  'nResolutions': instance.nResolutions,
  'distinctForecasterTypes': instance.distinctForecasterTypes,
  'logScoreImprovement': ?instance.logScoreImprovement,
};
