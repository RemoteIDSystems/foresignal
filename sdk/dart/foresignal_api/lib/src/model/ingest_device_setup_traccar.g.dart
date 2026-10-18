// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_device_setup_traccar.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestDeviceSetupTraccarCWProxy {
  IngestDeviceSetupTraccar serverUrl(String? serverUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetupTraccar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetupTraccar(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestDeviceSetupTraccar call({String? serverUrl});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestDeviceSetupTraccar.copyWith(...)` or call `instanceOfIngestDeviceSetupTraccar.copyWith.fieldName(value)` for a single field.
class _$IngestDeviceSetupTraccarCWProxyImpl
    implements _$IngestDeviceSetupTraccarCWProxy {
  const _$IngestDeviceSetupTraccarCWProxyImpl(this._value);

  final IngestDeviceSetupTraccar _value;

  @override
  IngestDeviceSetupTraccar serverUrl(String? serverUrl) =>
      call(serverUrl: serverUrl);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetupTraccar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetupTraccar(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestDeviceSetupTraccar call({
    Object? serverUrl = const $CopyWithPlaceholder(),
  }) {
    return IngestDeviceSetupTraccar(
      serverUrl: serverUrl == const $CopyWithPlaceholder()
          ? _value.serverUrl
          // ignore: cast_nullable_to_non_nullable
          : serverUrl as String?,
    );
  }
}

extension $IngestDeviceSetupTraccarCopyWith on IngestDeviceSetupTraccar {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestDeviceSetupTraccar.copyWith(...)` or `instanceOfIngestDeviceSetupTraccar.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestDeviceSetupTraccarCWProxy get copyWith =>
      _$IngestDeviceSetupTraccarCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestDeviceSetupTraccar _$IngestDeviceSetupTraccarFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestDeviceSetupTraccar', json, ($checkedConvert) {
  final val = IngestDeviceSetupTraccar(
    serverUrl: $checkedConvert('serverUrl', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IngestDeviceSetupTraccarToJson(
  IngestDeviceSetupTraccar instance,
) => <String, dynamic>{'serverUrl': ?instance.serverUrl};
