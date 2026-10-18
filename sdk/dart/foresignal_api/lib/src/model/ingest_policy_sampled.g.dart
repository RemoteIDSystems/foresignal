// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_policy_sampled.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestPolicySampledCWProxy {
  IngestPolicySampled mode(IngestPolicySampledModeEnum mode);

  IngestPolicySampled minInterval(String minInterval);

  IngestPolicySampled keyframe(String? keyframe);

  IngestPolicySampled minDeviationMeters(num? minDeviationMeters);

  IngestPolicySampled thresholds(bool? thresholds);

  IngestPolicySampled edgeMinInterval(String? edgeMinInterval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicySampled(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicySampled(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestPolicySampled call({
    IngestPolicySampledModeEnum mode,
    String minInterval,
    String? keyframe,
    num? minDeviationMeters,
    bool? thresholds,
    String? edgeMinInterval,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestPolicySampled.copyWith(...)` or call `instanceOfIngestPolicySampled.copyWith.fieldName(value)` for a single field.
class _$IngestPolicySampledCWProxyImpl implements _$IngestPolicySampledCWProxy {
  const _$IngestPolicySampledCWProxyImpl(this._value);

  final IngestPolicySampled _value;

  @override
  IngestPolicySampled mode(IngestPolicySampledModeEnum mode) =>
      call(mode: mode);

  @override
  IngestPolicySampled minInterval(String minInterval) =>
      call(minInterval: minInterval);

  @override
  IngestPolicySampled keyframe(String? keyframe) => call(keyframe: keyframe);

  @override
  IngestPolicySampled minDeviationMeters(num? minDeviationMeters) =>
      call(minDeviationMeters: minDeviationMeters);

  @override
  IngestPolicySampled thresholds(bool? thresholds) =>
      call(thresholds: thresholds);

  @override
  IngestPolicySampled edgeMinInterval(String? edgeMinInterval) =>
      call(edgeMinInterval: edgeMinInterval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicySampled(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicySampled(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestPolicySampled call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? minInterval = const $CopyWithPlaceholder(),
    Object? keyframe = const $CopyWithPlaceholder(),
    Object? minDeviationMeters = const $CopyWithPlaceholder(),
    Object? thresholds = const $CopyWithPlaceholder(),
    Object? edgeMinInterval = const $CopyWithPlaceholder(),
  }) {
    return IngestPolicySampled(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as IngestPolicySampledModeEnum,
      minInterval:
          minInterval == const $CopyWithPlaceholder() || minInterval == null
          ? _value.minInterval
          // ignore: cast_nullable_to_non_nullable
          : minInterval as String,
      keyframe: keyframe == const $CopyWithPlaceholder()
          ? _value.keyframe
          // ignore: cast_nullable_to_non_nullable
          : keyframe as String?,
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

extension $IngestPolicySampledCopyWith on IngestPolicySampled {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestPolicySampled.copyWith(...)` or `instanceOfIngestPolicySampled.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestPolicySampledCWProxy get copyWith =>
      _$IngestPolicySampledCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestPolicySampled _$IngestPolicySampledFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestPolicySampled', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['mode', 'minInterval']);
  final val = IngestPolicySampled(
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$IngestPolicySampledModeEnumEnumMap, v),
    ),
    minInterval: $checkedConvert('minInterval', (v) => v as String),
    keyframe: $checkedConvert('keyframe', (v) => v as String?),
    minDeviationMeters: $checkedConvert('minDeviationMeters', (v) => v as num?),
    thresholds: $checkedConvert('thresholds', (v) => v as bool? ?? false),
    edgeMinInterval: $checkedConvert('edgeMinInterval', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IngestPolicySampledToJson(
  IngestPolicySampled instance,
) => <String, dynamic>{
  'mode': _$IngestPolicySampledModeEnumEnumMap[instance.mode]!,
  'minInterval': instance.minInterval,
  'keyframe': ?instance.keyframe,
  'minDeviationMeters': ?instance.minDeviationMeters,
  'thresholds': ?instance.thresholds,
  'edgeMinInterval': ?instance.edgeMinInterval,
};

const _$IngestPolicySampledModeEnumEnumMap = {
  IngestPolicySampledModeEnum.SAMPLED: 'SAMPLED',
};
