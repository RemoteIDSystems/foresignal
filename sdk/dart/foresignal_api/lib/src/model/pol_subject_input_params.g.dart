// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_subject_input_params.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolSubjectInputParamsCWProxy {
  PolSubjectInputParams stayRadiusM(num? stayRadiusM);

  PolSubjectInputParams minDwell(String? minDwell);

  PolSubjectInputParams maxGap(String? maxGap);

  PolSubjectInputParams eps(num? eps);

  PolSubjectInputParams minPts(int? minPts);

  PolSubjectInputParams lookback(String? lookback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubjectInputParams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubjectInputParams(...).copyWith(id: 12, name: "My name")
  /// ```
  PolSubjectInputParams call({
    num? stayRadiusM,
    String? minDwell,
    String? maxGap,
    num? eps,
    int? minPts,
    String? lookback,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolSubjectInputParams.copyWith(...)` or call `instanceOfPolSubjectInputParams.copyWith.fieldName(value)` for a single field.
class _$PolSubjectInputParamsCWProxyImpl
    implements _$PolSubjectInputParamsCWProxy {
  const _$PolSubjectInputParamsCWProxyImpl(this._value);

  final PolSubjectInputParams _value;

  @override
  PolSubjectInputParams stayRadiusM(num? stayRadiusM) =>
      call(stayRadiusM: stayRadiusM);

  @override
  PolSubjectInputParams minDwell(String? minDwell) => call(minDwell: minDwell);

  @override
  PolSubjectInputParams maxGap(String? maxGap) => call(maxGap: maxGap);

  @override
  PolSubjectInputParams eps(num? eps) => call(eps: eps);

  @override
  PolSubjectInputParams minPts(int? minPts) => call(minPts: minPts);

  @override
  PolSubjectInputParams lookback(String? lookback) => call(lookback: lookback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubjectInputParams(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubjectInputParams(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolSubjectInputParams call({
    Object? stayRadiusM = const $CopyWithPlaceholder(),
    Object? minDwell = const $CopyWithPlaceholder(),
    Object? maxGap = const $CopyWithPlaceholder(),
    Object? eps = const $CopyWithPlaceholder(),
    Object? minPts = const $CopyWithPlaceholder(),
    Object? lookback = const $CopyWithPlaceholder(),
  }) {
    return PolSubjectInputParams(
      stayRadiusM: stayRadiusM == const $CopyWithPlaceholder()
          ? _value.stayRadiusM
          // ignore: cast_nullable_to_non_nullable
          : stayRadiusM as num?,
      minDwell: minDwell == const $CopyWithPlaceholder()
          ? _value.minDwell
          // ignore: cast_nullable_to_non_nullable
          : minDwell as String?,
      maxGap: maxGap == const $CopyWithPlaceholder()
          ? _value.maxGap
          // ignore: cast_nullable_to_non_nullable
          : maxGap as String?,
      eps: eps == const $CopyWithPlaceholder()
          ? _value.eps
          // ignore: cast_nullable_to_non_nullable
          : eps as num?,
      minPts: minPts == const $CopyWithPlaceholder()
          ? _value.minPts
          // ignore: cast_nullable_to_non_nullable
          : minPts as int?,
      lookback: lookback == const $CopyWithPlaceholder()
          ? _value.lookback
          // ignore: cast_nullable_to_non_nullable
          : lookback as String?,
    );
  }
}

extension $PolSubjectInputParamsCopyWith on PolSubjectInputParams {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolSubjectInputParams.copyWith(...)` or `instanceOfPolSubjectInputParams.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolSubjectInputParamsCWProxy get copyWith =>
      _$PolSubjectInputParamsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolSubjectInputParams _$PolSubjectInputParamsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PolSubjectInputParams', json, ($checkedConvert) {
  final val = PolSubjectInputParams(
    stayRadiusM: $checkedConvert('stayRadiusM', (v) => v as num? ?? 100),
    minDwell: $checkedConvert('minDwell', (v) => v as String? ?? '10m'),
    maxGap: $checkedConvert('maxGap', (v) => v as String? ?? '30m'),
    eps: $checkedConvert('eps', (v) => v as num? ?? 150),
    minPts: $checkedConvert('minPts', (v) => (v as num?)?.toInt() ?? 3),
    lookback: $checkedConvert('lookback', (v) => v as String? ?? '28d'),
  );
  return val;
});

Map<String, dynamic> _$PolSubjectInputParamsToJson(
  PolSubjectInputParams instance,
) => <String, dynamic>{
  'stayRadiusM': ?instance.stayRadiusM,
  'minDwell': ?instance.minDwell,
  'maxGap': ?instance.maxGap,
  'eps': ?instance.eps,
  'minPts': ?instance.minPts,
  'lookback': ?instance.lookback,
};
