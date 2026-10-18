// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelTestResultCWProxy {
  ChannelTestResult success(bool success);

  ChannelTestResult status(int? status);

  ChannelTestResult durationMs(int? durationMs);

  ChannelTestResult error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelTestResult call({
    bool success,
    int? status,
    int? durationMs,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelTestResult.copyWith(...)` or call `instanceOfChannelTestResult.copyWith.fieldName(value)` for a single field.
class _$ChannelTestResultCWProxyImpl implements _$ChannelTestResultCWProxy {
  const _$ChannelTestResultCWProxyImpl(this._value);

  final ChannelTestResult _value;

  @override
  ChannelTestResult success(bool success) => call(success: success);

  @override
  ChannelTestResult status(int? status) => call(status: status);

  @override
  ChannelTestResult durationMs(int? durationMs) => call(durationMs: durationMs);

  @override
  ChannelTestResult error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelTestResult call({
    Object? success = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? durationMs = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return ChannelTestResult(
      success: success == const $CopyWithPlaceholder() || success == null
          ? _value.success
          // ignore: cast_nullable_to_non_nullable
          : success as bool,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as int?,
      durationMs: durationMs == const $CopyWithPlaceholder()
          ? _value.durationMs
          // ignore: cast_nullable_to_non_nullable
          : durationMs as int?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $ChannelTestResultCopyWith on ChannelTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelTestResult.copyWith(...)` or `instanceOfChannelTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelTestResultCWProxy get copyWith =>
      _$ChannelTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelTestResult _$ChannelTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChannelTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['success']);
      final val = ChannelTestResult(
        success: $checkedConvert('success', (v) => v as bool),
        status: $checkedConvert('status', (v) => (v as num?)?.toInt()),
        durationMs: $checkedConvert('durationMs', (v) => (v as num?)?.toInt()),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ChannelTestResultToJson(ChannelTestResult instance) =>
    <String, dynamic>{
      'success': instance.success,
      'status': ?instance.status,
      'durationMs': ?instance.durationMs,
      'error': ?instance.error,
    };
