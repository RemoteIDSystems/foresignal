// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_test_result_warnings_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerTestResultWarningsInnerCWProxy {
  TriggerTestResultWarningsInner code(String code);

  TriggerTestResultWarningsInner message(String message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerTestResultWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerTestResultWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerTestResultWarningsInner call({String code, String message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerTestResultWarningsInner.copyWith(...)` or call `instanceOfTriggerTestResultWarningsInner.copyWith.fieldName(value)` for a single field.
class _$TriggerTestResultWarningsInnerCWProxyImpl
    implements _$TriggerTestResultWarningsInnerCWProxy {
  const _$TriggerTestResultWarningsInnerCWProxyImpl(this._value);

  final TriggerTestResultWarningsInner _value;

  @override
  TriggerTestResultWarningsInner code(String code) => call(code: code);

  @override
  TriggerTestResultWarningsInner message(String message) =>
      call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerTestResultWarningsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerTestResultWarningsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerTestResultWarningsInner call({
    Object? code = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
  }) {
    return TriggerTestResultWarningsInner(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
    );
  }
}

extension $TriggerTestResultWarningsInnerCopyWith
    on TriggerTestResultWarningsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerTestResultWarningsInner.copyWith(...)` or `instanceOfTriggerTestResultWarningsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerTestResultWarningsInnerCWProxy get copyWith =>
      _$TriggerTestResultWarningsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerTestResultWarningsInner _$TriggerTestResultWarningsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerTestResultWarningsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['code', 'message']);
  final val = TriggerTestResultWarningsInner(
    code: $checkedConvert('code', (v) => v as String),
    message: $checkedConvert('message', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$TriggerTestResultWarningsInnerToJson(
  TriggerTestResultWarningsInner instance,
) => <String, dynamic>{'code': instance.code, 'message': instance.message};
