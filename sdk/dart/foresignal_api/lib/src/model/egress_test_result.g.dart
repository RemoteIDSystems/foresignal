// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressTestResultCWProxy {
  EgressTestResult ok(bool ok);

  EgressTestResult exitIp(String? exitIp);

  EgressTestResult latencyMs(num? latencyMs);

  EgressTestResult message(String? message);

  EgressTestResult checkedAt(DateTime? checkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressTestResult call({
    bool ok,
    String? exitIp,
    num? latencyMs,
    String? message,
    DateTime? checkedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressTestResult.copyWith(...)` or call `instanceOfEgressTestResult.copyWith.fieldName(value)` for a single field.
class _$EgressTestResultCWProxyImpl implements _$EgressTestResultCWProxy {
  const _$EgressTestResultCWProxyImpl(this._value);

  final EgressTestResult _value;

  @override
  EgressTestResult ok(bool ok) => call(ok: ok);

  @override
  EgressTestResult exitIp(String? exitIp) => call(exitIp: exitIp);

  @override
  EgressTestResult latencyMs(num? latencyMs) => call(latencyMs: latencyMs);

  @override
  EgressTestResult message(String? message) => call(message: message);

  @override
  EgressTestResult checkedAt(DateTime? checkedAt) => call(checkedAt: checkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressTestResult call({
    Object? ok = const $CopyWithPlaceholder(),
    Object? exitIp = const $CopyWithPlaceholder(),
    Object? latencyMs = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? checkedAt = const $CopyWithPlaceholder(),
  }) {
    return EgressTestResult(
      ok: ok == const $CopyWithPlaceholder() || ok == null
          ? _value.ok
          // ignore: cast_nullable_to_non_nullable
          : ok as bool,
      exitIp: exitIp == const $CopyWithPlaceholder()
          ? _value.exitIp
          // ignore: cast_nullable_to_non_nullable
          : exitIp as String?,
      latencyMs: latencyMs == const $CopyWithPlaceholder()
          ? _value.latencyMs
          // ignore: cast_nullable_to_non_nullable
          : latencyMs as num?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      checkedAt: checkedAt == const $CopyWithPlaceholder()
          ? _value.checkedAt
          // ignore: cast_nullable_to_non_nullable
          : checkedAt as DateTime?,
    );
  }
}

extension $EgressTestResultCopyWith on EgressTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressTestResult.copyWith(...)` or `instanceOfEgressTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressTestResultCWProxy get copyWith => _$EgressTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressTestResult _$EgressTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EgressTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ok']);
      final val = EgressTestResult(
        ok: $checkedConvert('ok', (v) => v as bool),
        exitIp: $checkedConvert('exitIp', (v) => v as String?),
        latencyMs: $checkedConvert('latencyMs', (v) => v as num?),
        message: $checkedConvert('message', (v) => v as String?),
        checkedAt: $checkedConvert(
          'checkedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EgressTestResultToJson(EgressTestResult instance) =>
    <String, dynamic>{
      'ok': instance.ok,
      'exitIp': ?instance.exitIp,
      'latencyMs': ?instance.latencyMs,
      'message': ?instance.message,
      'checkedAt': ?instance.checkedAt?.toIso8601String(),
    };
