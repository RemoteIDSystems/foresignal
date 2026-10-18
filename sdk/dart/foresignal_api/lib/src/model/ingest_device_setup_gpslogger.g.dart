// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_device_setup_gpslogger.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestDeviceSetupGpsloggerCWProxy {
  IngestDeviceSetupGpslogger urlTemplate(String? urlTemplate);

  IngestDeviceSetupGpslogger method(String? method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetupGpslogger(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetupGpslogger(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestDeviceSetupGpslogger call({String? urlTemplate, String? method});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestDeviceSetupGpslogger.copyWith(...)` or call `instanceOfIngestDeviceSetupGpslogger.copyWith.fieldName(value)` for a single field.
class _$IngestDeviceSetupGpsloggerCWProxyImpl
    implements _$IngestDeviceSetupGpsloggerCWProxy {
  const _$IngestDeviceSetupGpsloggerCWProxyImpl(this._value);

  final IngestDeviceSetupGpslogger _value;

  @override
  IngestDeviceSetupGpslogger urlTemplate(String? urlTemplate) =>
      call(urlTemplate: urlTemplate);

  @override
  IngestDeviceSetupGpslogger method(String? method) => call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetupGpslogger(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetupGpslogger(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestDeviceSetupGpslogger call({
    Object? urlTemplate = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return IngestDeviceSetupGpslogger(
      urlTemplate: urlTemplate == const $CopyWithPlaceholder()
          ? _value.urlTemplate
          // ignore: cast_nullable_to_non_nullable
          : urlTemplate as String?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String?,
    );
  }
}

extension $IngestDeviceSetupGpsloggerCopyWith on IngestDeviceSetupGpslogger {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestDeviceSetupGpslogger.copyWith(...)` or `instanceOfIngestDeviceSetupGpslogger.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestDeviceSetupGpsloggerCWProxy get copyWith =>
      _$IngestDeviceSetupGpsloggerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestDeviceSetupGpslogger _$IngestDeviceSetupGpsloggerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestDeviceSetupGpslogger', json, ($checkedConvert) {
  final val = IngestDeviceSetupGpslogger(
    urlTemplate: $checkedConvert('urlTemplate', (v) => v as String?),
    method: $checkedConvert('method', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IngestDeviceSetupGpsloggerToJson(
  IngestDeviceSetupGpslogger instance,
) => <String, dynamic>{
  'urlTemplate': ?instance.urlTemplate,
  'method': ?instance.method,
};
