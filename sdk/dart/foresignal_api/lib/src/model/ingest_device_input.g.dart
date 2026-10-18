// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_device_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestDeviceInputCWProxy {
  IngestDeviceInput externalId(String externalId);

  IngestDeviceInput name(String name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestDeviceInput call({String externalId, String name});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestDeviceInput.copyWith(...)` or call `instanceOfIngestDeviceInput.copyWith.fieldName(value)` for a single field.
class _$IngestDeviceInputCWProxyImpl implements _$IngestDeviceInputCWProxy {
  const _$IngestDeviceInputCWProxyImpl(this._value);

  final IngestDeviceInput _value;

  @override
  IngestDeviceInput externalId(String externalId) =>
      call(externalId: externalId);

  @override
  IngestDeviceInput name(String name) => call(name: name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestDeviceInput call({
    Object? externalId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
  }) {
    return IngestDeviceInput(
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
    );
  }
}

extension $IngestDeviceInputCopyWith on IngestDeviceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestDeviceInput.copyWith(...)` or `instanceOfIngestDeviceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestDeviceInputCWProxy get copyWith =>
      _$IngestDeviceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestDeviceInput _$IngestDeviceInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestDeviceInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['externalId', 'name']);
      final val = IngestDeviceInput(
        externalId: $checkedConvert('externalId', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$IngestDeviceInputToJson(IngestDeviceInput instance) =>
    <String, dynamic>{'externalId': instance.externalId, 'name': instance.name};
