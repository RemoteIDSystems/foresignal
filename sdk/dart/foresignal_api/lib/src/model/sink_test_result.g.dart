// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkTestResultCWProxy {
  SinkTestResult ok(bool ok);

  SinkTestResult phase(SinkTestResultPhaseEnum phase);

  SinkTestResult message(String message);

  SinkTestResult latencyMs(int? latencyMs);

  SinkTestResult details(Map<String, Object>? details);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkTestResult call({
    bool ok,
    SinkTestResultPhaseEnum phase,
    String message,
    int? latencyMs,
    Map<String, Object>? details,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkTestResult.copyWith(...)` or call `instanceOfSinkTestResult.copyWith.fieldName(value)` for a single field.
class _$SinkTestResultCWProxyImpl implements _$SinkTestResultCWProxy {
  const _$SinkTestResultCWProxyImpl(this._value);

  final SinkTestResult _value;

  @override
  SinkTestResult ok(bool ok) => call(ok: ok);

  @override
  SinkTestResult phase(SinkTestResultPhaseEnum phase) => call(phase: phase);

  @override
  SinkTestResult message(String message) => call(message: message);

  @override
  SinkTestResult latencyMs(int? latencyMs) => call(latencyMs: latencyMs);

  @override
  SinkTestResult details(Map<String, Object>? details) =>
      call(details: details);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkTestResult call({
    Object? ok = const $CopyWithPlaceholder(),
    Object? phase = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? latencyMs = const $CopyWithPlaceholder(),
    Object? details = const $CopyWithPlaceholder(),
  }) {
    return SinkTestResult(
      ok: ok == const $CopyWithPlaceholder() || ok == null
          ? _value.ok
          // ignore: cast_nullable_to_non_nullable
          : ok as bool,
      phase: phase == const $CopyWithPlaceholder() || phase == null
          ? _value.phase
          // ignore: cast_nullable_to_non_nullable
          : phase as SinkTestResultPhaseEnum,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
      latencyMs: latencyMs == const $CopyWithPlaceholder()
          ? _value.latencyMs
          // ignore: cast_nullable_to_non_nullable
          : latencyMs as int?,
      details: details == const $CopyWithPlaceholder()
          ? _value.details
          // ignore: cast_nullable_to_non_nullable
          : details as Map<String, Object>?,
    );
  }
}

extension $SinkTestResultCopyWith on SinkTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkTestResult.copyWith(...)` or `instanceOfSinkTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkTestResultCWProxy get copyWith => _$SinkTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkTestResult _$SinkTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SinkTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ok', 'phase', 'message']);
      final val = SinkTestResult(
        ok: $checkedConvert('ok', (v) => v as bool),
        phase: $checkedConvert(
          'phase',
          (v) => $enumDecode(_$SinkTestResultPhaseEnumEnumMap, v),
        ),
        message: $checkedConvert('message', (v) => v as String),
        latencyMs: $checkedConvert('latencyMs', (v) => (v as num?)?.toInt()),
        details: $checkedConvert(
          'details',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SinkTestResultToJson(SinkTestResult instance) =>
    <String, dynamic>{
      'ok': instance.ok,
      'phase': _$SinkTestResultPhaseEnumEnumMap[instance.phase]!,
      'message': instance.message,
      'latencyMs': ?instance.latencyMs,
      'details': ?instance.details,
    };

const _$SinkTestResultPhaseEnumEnumMap = {
  SinkTestResultPhaseEnum.validate: 'validate',
  SinkTestResultPhaseEnum.connect: 'connect',
  SinkTestResultPhaseEnum.bind: 'bind',
  SinkTestResultPhaseEnum.send: 'send',
};
