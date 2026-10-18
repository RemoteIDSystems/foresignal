// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_device_setup_overland.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestDeviceSetupOverlandCWProxy {
  IngestDeviceSetupOverland endpoint(String? endpoint);

  IngestDeviceSetupOverland token(String? token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetupOverland(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetupOverland(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestDeviceSetupOverland call({String? endpoint, String? token});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestDeviceSetupOverland.copyWith(...)` or call `instanceOfIngestDeviceSetupOverland.copyWith.fieldName(value)` for a single field.
class _$IngestDeviceSetupOverlandCWProxyImpl
    implements _$IngestDeviceSetupOverlandCWProxy {
  const _$IngestDeviceSetupOverlandCWProxyImpl(this._value);

  final IngestDeviceSetupOverland _value;

  @override
  IngestDeviceSetupOverland endpoint(String? endpoint) =>
      call(endpoint: endpoint);

  @override
  IngestDeviceSetupOverland token(String? token) => call(token: token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetupOverland(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetupOverland(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestDeviceSetupOverland call({
    Object? endpoint = const $CopyWithPlaceholder(),
    Object? token = const $CopyWithPlaceholder(),
  }) {
    return IngestDeviceSetupOverland(
      endpoint: endpoint == const $CopyWithPlaceholder()
          ? _value.endpoint
          // ignore: cast_nullable_to_non_nullable
          : endpoint as String?,
      token: token == const $CopyWithPlaceholder()
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String?,
    );
  }
}

extension $IngestDeviceSetupOverlandCopyWith on IngestDeviceSetupOverland {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestDeviceSetupOverland.copyWith(...)` or `instanceOfIngestDeviceSetupOverland.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestDeviceSetupOverlandCWProxy get copyWith =>
      _$IngestDeviceSetupOverlandCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestDeviceSetupOverland _$IngestDeviceSetupOverlandFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IngestDeviceSetupOverland', json, ($checkedConvert) {
  final val = IngestDeviceSetupOverland(
    endpoint: $checkedConvert('endpoint', (v) => v as String?),
    token: $checkedConvert('token', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IngestDeviceSetupOverlandToJson(
  IngestDeviceSetupOverland instance,
) => <String, dynamic>{
  'endpoint': ?instance.endpoint,
  'token': ?instance.token,
};
