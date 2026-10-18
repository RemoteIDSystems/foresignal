// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_policy_all.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestPolicyAllCWProxy {
  IngestPolicyAll mode(IngestPolicyAllModeEnum mode);

  IngestPolicyAll keyframe(String? keyframe);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicyAll(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicyAll(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestPolicyAll call({IngestPolicyAllModeEnum mode, String? keyframe});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestPolicyAll.copyWith(...)` or call `instanceOfIngestPolicyAll.copyWith.fieldName(value)` for a single field.
class _$IngestPolicyAllCWProxyImpl implements _$IngestPolicyAllCWProxy {
  const _$IngestPolicyAllCWProxyImpl(this._value);

  final IngestPolicyAll _value;

  @override
  IngestPolicyAll mode(IngestPolicyAllModeEnum mode) => call(mode: mode);

  @override
  IngestPolicyAll keyframe(String? keyframe) => call(keyframe: keyframe);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestPolicyAll(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestPolicyAll(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestPolicyAll call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? keyframe = const $CopyWithPlaceholder(),
  }) {
    return IngestPolicyAll(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as IngestPolicyAllModeEnum,
      keyframe: keyframe == const $CopyWithPlaceholder()
          ? _value.keyframe
          // ignore: cast_nullable_to_non_nullable
          : keyframe as String?,
    );
  }
}

extension $IngestPolicyAllCopyWith on IngestPolicyAll {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestPolicyAll.copyWith(...)` or `instanceOfIngestPolicyAll.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestPolicyAllCWProxy get copyWith => _$IngestPolicyAllCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestPolicyAll _$IngestPolicyAllFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestPolicyAll', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['mode']);
      final val = IngestPolicyAll(
        mode: $checkedConvert(
          'mode',
          (v) => $enumDecode(_$IngestPolicyAllModeEnumEnumMap, v),
        ),
        keyframe: $checkedConvert('keyframe', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$IngestPolicyAllToJson(IngestPolicyAll instance) =>
    <String, dynamic>{
      'mode': _$IngestPolicyAllModeEnumEnumMap[instance.mode]!,
      'keyframe': ?instance.keyframe,
    };

const _$IngestPolicyAllModeEnumEnumMap = {IngestPolicyAllModeEnum.ALL: 'ALL'};
