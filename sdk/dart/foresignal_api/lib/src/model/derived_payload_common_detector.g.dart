// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'derived_payload_common_detector.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DerivedPayloadCommonDetectorCWProxy {
  DerivedPayloadCommonDetector id(String id);

  DerivedPayloadCommonDetector version(String version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DerivedPayloadCommonDetector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DerivedPayloadCommonDetector(...).copyWith(id: 12, name: "My name")
  /// ```
  DerivedPayloadCommonDetector call({String id, String version});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDerivedPayloadCommonDetector.copyWith(...)` or call `instanceOfDerivedPayloadCommonDetector.copyWith.fieldName(value)` for a single field.
class _$DerivedPayloadCommonDetectorCWProxyImpl
    implements _$DerivedPayloadCommonDetectorCWProxy {
  const _$DerivedPayloadCommonDetectorCWProxyImpl(this._value);

  final DerivedPayloadCommonDetector _value;

  @override
  DerivedPayloadCommonDetector id(String id) => call(id: id);

  @override
  DerivedPayloadCommonDetector version(String version) =>
      call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DerivedPayloadCommonDetector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DerivedPayloadCommonDetector(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DerivedPayloadCommonDetector call({
    Object? id = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return DerivedPayloadCommonDetector(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
    );
  }
}

extension $DerivedPayloadCommonDetectorCopyWith
    on DerivedPayloadCommonDetector {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDerivedPayloadCommonDetector.copyWith(...)` or `instanceOfDerivedPayloadCommonDetector.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DerivedPayloadCommonDetectorCWProxy get copyWith =>
      _$DerivedPayloadCommonDetectorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DerivedPayloadCommonDetector _$DerivedPayloadCommonDetectorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DerivedPayloadCommonDetector', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'version']);
  final val = DerivedPayloadCommonDetector(
    id: $checkedConvert('id', (v) => v as String),
    version: $checkedConvert('version', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$DerivedPayloadCommonDetectorToJson(
  DerivedPayloadCommonDetector instance,
) => <String, dynamic>{'id': instance.id, 'version': instance.version};
