// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'probe_result_requests_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProbeResultRequestsInnerCWProxy {
  ProbeResultRequestsInner method(String? method);

  ProbeResultRequestsInner url(String? url);

  ProbeResultRequestsInner status(int? status);

  ProbeResultRequestsInner durationMs(int? durationMs);

  ProbeResultRequestsInner contentType(String? contentType);

  ProbeResultRequestsInner bytes(int? bytes);

  ProbeResultRequestsInner truncated(bool? truncated);

  ProbeResultRequestsInner body(String? body);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProbeResultRequestsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProbeResultRequestsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProbeResultRequestsInner call({
    String? method,
    String? url,
    int? status,
    int? durationMs,
    String? contentType,
    int? bytes,
    bool? truncated,
    String? body,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProbeResultRequestsInner.copyWith(...)` or call `instanceOfProbeResultRequestsInner.copyWith.fieldName(value)` for a single field.
class _$ProbeResultRequestsInnerCWProxyImpl
    implements _$ProbeResultRequestsInnerCWProxy {
  const _$ProbeResultRequestsInnerCWProxyImpl(this._value);

  final ProbeResultRequestsInner _value;

  @override
  ProbeResultRequestsInner method(String? method) => call(method: method);

  @override
  ProbeResultRequestsInner url(String? url) => call(url: url);

  @override
  ProbeResultRequestsInner status(int? status) => call(status: status);

  @override
  ProbeResultRequestsInner durationMs(int? durationMs) =>
      call(durationMs: durationMs);

  @override
  ProbeResultRequestsInner contentType(String? contentType) =>
      call(contentType: contentType);

  @override
  ProbeResultRequestsInner bytes(int? bytes) => call(bytes: bytes);

  @override
  ProbeResultRequestsInner truncated(bool? truncated) =>
      call(truncated: truncated);

  @override
  ProbeResultRequestsInner body(String? body) => call(body: body);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProbeResultRequestsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProbeResultRequestsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProbeResultRequestsInner call({
    Object? method = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? durationMs = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
  }) {
    return ProbeResultRequestsInner(
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as int?,
      durationMs: durationMs == const $CopyWithPlaceholder()
          ? _value.durationMs
          // ignore: cast_nullable_to_non_nullable
          : durationMs as int?,
      contentType: contentType == const $CopyWithPlaceholder()
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as String?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
    );
  }
}

extension $ProbeResultRequestsInnerCopyWith on ProbeResultRequestsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProbeResultRequestsInner.copyWith(...)` or `instanceOfProbeResultRequestsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProbeResultRequestsInnerCWProxy get copyWith =>
      _$ProbeResultRequestsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProbeResultRequestsInner _$ProbeResultRequestsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProbeResultRequestsInner', json, ($checkedConvert) {
  final val = ProbeResultRequestsInner(
    method: $checkedConvert('method', (v) => v as String?),
    url: $checkedConvert('url', (v) => v as String?),
    status: $checkedConvert('status', (v) => (v as num?)?.toInt()),
    durationMs: $checkedConvert('durationMs', (v) => (v as num?)?.toInt()),
    contentType: $checkedConvert('contentType', (v) => v as String?),
    bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    body: $checkedConvert('body', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ProbeResultRequestsInnerToJson(
  ProbeResultRequestsInner instance,
) => <String, dynamic>{
  'method': ?instance.method,
  'url': ?instance.url,
  'status': ?instance.status,
  'durationMs': ?instance.durationMs,
  'contentType': ?instance.contentType,
  'bytes': ?instance.bytes,
  'truncated': ?instance.truncated,
  'body': ?instance.body,
};
