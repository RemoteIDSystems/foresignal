// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_exports.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoExportsCWProxy {
  SystemInfoExports queued(int? queued);

  SystemInfoExports running(int? running);

  SystemInfoExports bytes(int? bytes);

  SystemInfoExports quotaBytes(int? quotaBytes);

  SystemInfoExports maxConcurrent(int? maxConcurrent);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoExports(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoExports(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoExports call({
    int? queued,
    int? running,
    int? bytes,
    int? quotaBytes,
    int? maxConcurrent,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoExports.copyWith(...)` or call `instanceOfSystemInfoExports.copyWith.fieldName(value)` for a single field.
class _$SystemInfoExportsCWProxyImpl implements _$SystemInfoExportsCWProxy {
  const _$SystemInfoExportsCWProxyImpl(this._value);

  final SystemInfoExports _value;

  @override
  SystemInfoExports queued(int? queued) => call(queued: queued);

  @override
  SystemInfoExports running(int? running) => call(running: running);

  @override
  SystemInfoExports bytes(int? bytes) => call(bytes: bytes);

  @override
  SystemInfoExports quotaBytes(int? quotaBytes) => call(quotaBytes: quotaBytes);

  @override
  SystemInfoExports maxConcurrent(int? maxConcurrent) =>
      call(maxConcurrent: maxConcurrent);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoExports(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoExports(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoExports call({
    Object? queued = const $CopyWithPlaceholder(),
    Object? running = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? quotaBytes = const $CopyWithPlaceholder(),
    Object? maxConcurrent = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoExports(
      queued: queued == const $CopyWithPlaceholder()
          ? _value.queued
          // ignore: cast_nullable_to_non_nullable
          : queued as int?,
      running: running == const $CopyWithPlaceholder()
          ? _value.running
          // ignore: cast_nullable_to_non_nullable
          : running as int?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      quotaBytes: quotaBytes == const $CopyWithPlaceholder()
          ? _value.quotaBytes
          // ignore: cast_nullable_to_non_nullable
          : quotaBytes as int?,
      maxConcurrent: maxConcurrent == const $CopyWithPlaceholder()
          ? _value.maxConcurrent
          // ignore: cast_nullable_to_non_nullable
          : maxConcurrent as int?,
    );
  }
}

extension $SystemInfoExportsCopyWith on SystemInfoExports {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoExports.copyWith(...)` or `instanceOfSystemInfoExports.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoExportsCWProxy get copyWith =>
      _$SystemInfoExportsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoExports _$SystemInfoExportsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoExports', json, ($checkedConvert) {
      final val = SystemInfoExports(
        queued: $checkedConvert('queued', (v) => (v as num?)?.toInt()),
        running: $checkedConvert('running', (v) => (v as num?)?.toInt()),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
        quotaBytes: $checkedConvert('quotaBytes', (v) => (v as num?)?.toInt()),
        maxConcurrent: $checkedConvert(
          'maxConcurrent',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoExportsToJson(SystemInfoExports instance) =>
    <String, dynamic>{
      'queued': ?instance.queued,
      'running': ?instance.running,
      'bytes': ?instance.bytes,
      'quotaBytes': ?instance.quotaBytes,
      'maxConcurrent': ?instance.maxConcurrent,
    };
