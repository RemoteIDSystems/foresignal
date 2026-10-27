// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'smtp_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SmtpTestResultCWProxy {
  SmtpTestResult ok(bool ok);

  SmtpTestResult message(String? message);

  SmtpTestResult sentAt(DateTime? sentAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  SmtpTestResult call({bool ok, String? message, DateTime? sentAt});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSmtpTestResult.copyWith(...)` or call `instanceOfSmtpTestResult.copyWith.fieldName(value)` for a single field.
class _$SmtpTestResultCWProxyImpl implements _$SmtpTestResultCWProxy {
  const _$SmtpTestResultCWProxyImpl(this._value);

  final SmtpTestResult _value;

  @override
  SmtpTestResult ok(bool ok) => call(ok: ok);

  @override
  SmtpTestResult message(String? message) => call(message: message);

  @override
  SmtpTestResult sentAt(DateTime? sentAt) => call(sentAt: sentAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SmtpTestResult call({
    Object? ok = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? sentAt = const $CopyWithPlaceholder(),
  }) {
    return SmtpTestResult(
      ok: ok == const $CopyWithPlaceholder() || ok == null
          ? _value.ok
          // ignore: cast_nullable_to_non_nullable
          : ok as bool,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      sentAt: sentAt == const $CopyWithPlaceholder()
          ? _value.sentAt
          // ignore: cast_nullable_to_non_nullable
          : sentAt as DateTime?,
    );
  }
}

extension $SmtpTestResultCopyWith on SmtpTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSmtpTestResult.copyWith(...)` or `instanceOfSmtpTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SmtpTestResultCWProxy get copyWith => _$SmtpTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SmtpTestResult _$SmtpTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SmtpTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ok']);
      final val = SmtpTestResult(
        ok: $checkedConvert('ok', (v) => v as bool),
        message: $checkedConvert('message', (v) => v as String?),
        sentAt: $checkedConvert(
          'sentAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SmtpTestResultToJson(SmtpTestResult instance) =>
    <String, dynamic>{
      'ok': instance.ok,
      'message': ?instance.message,
      'sentAt': ?instance.sentAt?.toIso8601String(),
    };
