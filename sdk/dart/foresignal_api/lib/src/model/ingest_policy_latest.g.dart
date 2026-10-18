// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_policy_latest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestPolicyLatestCWProxy {
  IngestPolicyLatest mode(IngestPolicyLatestModeEnum mode);

  IngestPolicyLatest minInterval(String? minInterval);

  IngestPolicyLatest keyframe(String? keyframe);

  IngestPolicyLatest edgeMinInterval(String? edgeMinInterval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicyLatest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicyLatest(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestPolicyLatest call({
    IngestPolicyLatestModeEnum mode,
    String? minInterval,
    String? keyframe,
    String? edgeMinInterval,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestPolicyLatest.copyWith(...)` or call `instanceOfIngestPolicyLatest.copyWith.fieldName(value)` for a single field.
class _$IngestPolicyLatestCWProxyImpl implements _$IngestPolicyLatestCWProxy {
  const _$IngestPolicyLatestCWProxyImpl(this._value);

  final IngestPolicyLatest _value;

  @override
  IngestPolicyLatest mode(IngestPolicyLatestModeEnum mode) => call(mode: mode);

  @override
  IngestPolicyLatest minInterval(String? minInterval) =>
      call(minInterval: minInterval);

  @override
  IngestPolicyLatest keyframe(String? keyframe) => call(keyframe: keyframe);

  @override
  IngestPolicyLatest edgeMinInterval(String? edgeMinInterval) =>
      call(edgeMinInterval: edgeMinInterval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicyLatest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicyLatest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestPolicyLatest call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? minInterval = const $CopyWithPlaceholder(),
    Object? keyframe = const $CopyWithPlaceholder(),
    Object? edgeMinInterval = const $CopyWithPlaceholder(),
  }) {
    return IngestPolicyLatest(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as IngestPolicyLatestModeEnum,
      minInterval: minInterval == const $CopyWithPlaceholder()
          ? _value.minInterval
          // ignore: cast_nullable_to_non_nullable
          : minInterval as String?,
      keyframe: keyframe == const $CopyWithPlaceholder()
          ? _value.keyframe
          // ignore: cast_nullable_to_non_nullable
          : keyframe as String?,
      edgeMinInterval: edgeMinInterval == const $CopyWithPlaceholder()
          ? _value.edgeMinInterval
          // ignore: cast_nullable_to_non_nullable
          : edgeMinInterval as String?,
    );
  }
}

extension $IngestPolicyLatestCopyWith on IngestPolicyLatest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestPolicyLatest.copyWith(...)` or `instanceOfIngestPolicyLatest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestPolicyLatestCWProxy get copyWith =>
      _$IngestPolicyLatestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestPolicyLatest _$IngestPolicyLatestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestPolicyLatest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['mode']);
      final val = IngestPolicyLatest(
        mode: $checkedConvert(
          'mode',
          (v) => $enumDecode(_$IngestPolicyLatestModeEnumEnumMap, v),
        ),
        minInterval: $checkedConvert('minInterval', (v) => v as String?),
        keyframe: $checkedConvert('keyframe', (v) => v as String?),
        edgeMinInterval: $checkedConvert(
          'edgeMinInterval',
          (v) => v as String?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$IngestPolicyLatestToJson(IngestPolicyLatest instance) =>
    <String, dynamic>{
      'mode': _$IngestPolicyLatestModeEnumEnumMap[instance.mode]!,
      'minInterval': ?instance.minInterval,
      'keyframe': ?instance.keyframe,
      'edgeMinInterval': ?instance.edgeMinInterval,
    };

const _$IngestPolicyLatestModeEnumEnumMap = {
  IngestPolicyLatestModeEnum.LATEST: 'LATEST',
};
