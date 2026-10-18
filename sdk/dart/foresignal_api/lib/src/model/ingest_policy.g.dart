// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_policy.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestPolicyCWProxy {
  IngestPolicy mode(IngestPolicyModeEnum mode);

  IngestPolicy keyframe(String? keyframe);

  IngestPolicy minInterval(String? minInterval);

  IngestPolicy minDeviationMeters(num? minDeviationMeters);

  IngestPolicy thresholds(bool? thresholds);

  IngestPolicy edgeMinInterval(String? edgeMinInterval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicy(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicy(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestPolicy call({
    IngestPolicyModeEnum mode,
    String? keyframe,
    String? minInterval,
    num? minDeviationMeters,
    bool? thresholds,
    String? edgeMinInterval,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestPolicy.copyWith(...)` or call `instanceOfIngestPolicy.copyWith.fieldName(value)` for a single field.
class _$IngestPolicyCWProxyImpl implements _$IngestPolicyCWProxy {
  const _$IngestPolicyCWProxyImpl(this._value);

  final IngestPolicy _value;

  @override
  IngestPolicy mode(IngestPolicyModeEnum mode) => call(mode: mode);

  @override
  IngestPolicy keyframe(String? keyframe) => call(keyframe: keyframe);

  @override
  IngestPolicy minInterval(String? minInterval) =>
      call(minInterval: minInterval);

  @override
  IngestPolicy minDeviationMeters(num? minDeviationMeters) =>
      call(minDeviationMeters: minDeviationMeters);

  @override
  IngestPolicy thresholds(bool? thresholds) => call(thresholds: thresholds);

  @override
  IngestPolicy edgeMinInterval(String? edgeMinInterval) =>
      call(edgeMinInterval: edgeMinInterval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicy(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicy(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestPolicy call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? keyframe = const $CopyWithPlaceholder(),
    Object? minInterval = const $CopyWithPlaceholder(),
    Object? minDeviationMeters = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? edgeMinInterval = const $CopyWithPlaceholder(),
  }) {
    return IngestPolicy(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as IngestPolicyModeEnum,
      keyframe: keyframe == const $CopyWithPlaceholder()
          ? _value.keyframe
          // ignore: cast_nullable_to_non_nullable
          : keyframe as String?,
      minInterval: minInterval == const $CopyWithPlaceholder()
          ? _value.minInterval
          // ignore: cast_nullable_to_non_nullable
          : minInterval as String?,
      minDeviationMeters: minDeviationMeters == const $CopyWithPlaceholder()
          ? _value.minDeviationMeters
          // ignore: cast_nullable_to_non_nullable
          : minDeviationMeters as num?,
      thresholds: thresholds == const $CopyWithPlaceholder()
          ? _value.thresholds
          // ignore: cast_nullable_to_non_nullable
          : thresholds as bool?,
      edgeMinInterval: edgeMinInterval == const $CopyWithPlaceholder()
          ? _value.edgeMinInterval
          // ignore: cast_nullable_to_non_nullable
          : edgeMinInterval as String?,
    );
  }
}

extension $IngestPolicyCopyWith on IngestPolicy {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestPolicy.copyWith(...)` or `instanceOfIngestPolicy.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestPolicyCWProxy get copyWith => _$IngestPolicyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestPolicy _$IngestPolicyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestPolicy', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['mode', 'minInterval']);
  final val = IngestPolicy(
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$IngestPolicyModeEnumEnumMap, v),
    ),
    keyframe: $checkedConvert('keyframe', (v) => v as String?),
    minInterval: $checkedConvert('minInterval', (v) => v as String?),
    minDeviationMeters: $checkedConvert('minDeviationMeters', (v) => v as num?),
    thresholds: $checkedConvert('thresholds', (v) => v as bool? ?? false),
    edgeMinInterval: $checkedConvert('edgeMinInterval', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IngestPolicyToJson(IngestPolicy instance) =>
    <String, dynamic>{
      'mode': _$IngestPolicyModeEnumEnumMap[instance.mode]!,
      'keyframe': ?instance.keyframe,
      'minInterval': instance.minInterval,
      'minDeviationMeters': ?instance.minDeviationMeters,
      'thresholds': ?instance.thresholds,
      'edgeMinInterval': ?instance.edgeMinInterval,
    };

const _$IngestPolicyModeEnumEnumMap = {IngestPolicyModeEnum.LATEST: 'LATEST'};
