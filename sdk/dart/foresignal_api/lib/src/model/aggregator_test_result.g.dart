// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'aggregator_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AggregatorTestResultCWProxy {
  AggregatorTestResult ok(bool ok);

  AggregatorTestResult reachable(bool reachable);

  AggregatorTestResult sampleCount(int? sampleCount);

  AggregatorTestResult message(String? message);

  AggregatorTestResult checkedAt(DateTime? checkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AggregatorTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AggregatorTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  AggregatorTestResult call({
    bool ok,
    bool reachable,
    int? sampleCount,
    String? message,
    DateTime? checkedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAggregatorTestResult.copyWith(...)` or call `instanceOfAggregatorTestResult.copyWith.fieldName(value)` for a single field.
class _$AggregatorTestResultCWProxyImpl
    implements _$AggregatorTestResultCWProxy {
  const _$AggregatorTestResultCWProxyImpl(this._value);

  final AggregatorTestResult _value;

  @override
  AggregatorTestResult ok(bool ok) => call(ok: ok);

  @override
  AggregatorTestResult reachable(bool reachable) => call(reachable: reachable);

  @override
  AggregatorTestResult sampleCount(int? sampleCount) =>
      call(sampleCount: sampleCount);

  @override
  AggregatorTestResult message(String? message) => call(message: message);

  @override
  AggregatorTestResult checkedAt(DateTime? checkedAt) =>
      call(checkedAt: checkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AggregatorTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AggregatorTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AggregatorTestResult call({
    Object? ok = const $CopyWithPlaceholder(),
    Object? reachable = const $CopyWithPlaceholder(),
    Object? sampleCount = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? checkedAt = const $CopyWithPlaceholder(),
  }) {
    return AggregatorTestResult(
      ok: ok == const $CopyWithPlaceholder() || ok == null
          ? _value.ok
          // ignore: cast_nullable_to_non_nullable
          : ok as bool,
      reachable: reachable == const $CopyWithPlaceholder() || reachable == null
          ? _value.reachable
          // ignore: cast_nullable_to_non_nullable
          : reachable as bool,
      sampleCount: sampleCount == const $CopyWithPlaceholder()
          ? _value.sampleCount
          // ignore: cast_nullable_to_non_nullable
          : sampleCount as int?,
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

extension $AggregatorTestResultCopyWith on AggregatorTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAggregatorTestResult.copyWith(...)` or `instanceOfAggregatorTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AggregatorTestResultCWProxy get copyWith =>
      _$AggregatorTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AggregatorTestResult _$AggregatorTestResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AggregatorTestResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ok', 'reachable']);
  final val = AggregatorTestResult(
    ok: $checkedConvert('ok', (v) => v as bool),
    reachable: $checkedConvert('reachable', (v) => v as bool),
    sampleCount: $checkedConvert('sampleCount', (v) => (v as num?)?.toInt()),
    message: $checkedConvert('message', (v) => v as String?),
    checkedAt: $checkedConvert(
      'checkedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$AggregatorTestResultToJson(
  AggregatorTestResult instance,
) => <String, dynamic>{
  'ok': instance.ok,
  'reachable': instance.reachable,
  'sampleCount': ?instance.sampleCount,
  'message': ?instance.message,
  'checkedAt': ?instance.checkedAt?.toIso8601String(),
};
