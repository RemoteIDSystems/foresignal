// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushTestResultCWProxy {
  PushTestResult success(bool success);

  PushTestResult credentialMinted(bool? credentialMinted);

  PushTestResult sent(bool? sent);

  PushTestResult status(int? status);

  PushTestResult reason(String? reason);

  PushTestResult error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  PushTestResult call({
    bool success,
    bool? credentialMinted,
    bool? sent,
    int? status,
    String? reason,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushTestResult.copyWith(...)` or call `instanceOfPushTestResult.copyWith.fieldName(value)` for a single field.
class _$PushTestResultCWProxyImpl implements _$PushTestResultCWProxy {
  const _$PushTestResultCWProxyImpl(this._value);

  final PushTestResult _value;

  @override
  PushTestResult success(bool success) => call(success: success);

  @override
  PushTestResult credentialMinted(bool? credentialMinted) =>
      call(credentialMinted: credentialMinted);

  @override
  PushTestResult sent(bool? sent) => call(sent: sent);

  @override
  PushTestResult status(int? status) => call(status: status);

  @override
  PushTestResult reason(String? reason) => call(reason: reason);

  @override
  PushTestResult error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushTestResult call({
    Object? success = const $CopyWithPlaceholder(),
    Object? credentialMinted = const $CopyWithPlaceholder(),
    Object? sent = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return PushTestResult(
      success: success == const $CopyWithPlaceholder() || success == null
          ? _value.success
          // ignore: cast_nullable_to_non_nullable
          : success as bool,
      credentialMinted: credentialMinted == const $CopyWithPlaceholder()
          ? _value.credentialMinted
          // ignore: cast_nullable_to_non_nullable
          : credentialMinted as bool?,
      sent: sent == const $CopyWithPlaceholder()
          ? _value.sent
          // ignore: cast_nullable_to_non_nullable
          : sent as bool?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as int?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $PushTestResultCopyWith on PushTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushTestResult.copyWith(...)` or `instanceOfPushTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushTestResultCWProxy get copyWith => _$PushTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushTestResult _$PushTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['success']);
      final val = PushTestResult(
        success: $checkedConvert('success', (v) => v as bool),
        credentialMinted: $checkedConvert(
          'credentialMinted',
          (v) => v as bool?,
        ),
        sent: $checkedConvert('sent', (v) => v as bool?),
        status: $checkedConvert('status', (v) => (v as num?)?.toInt()),
        reason: $checkedConvert('reason', (v) => v as String?),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PushTestResultToJson(PushTestResult instance) =>
    <String, dynamic>{
      'success': instance.success,
      'credentialMinted': ?instance.credentialMinted,
      'sent': ?instance.sent,
      'status': ?instance.status,
      'reason': ?instance.reason,
      'error': ?instance.error,
    };
