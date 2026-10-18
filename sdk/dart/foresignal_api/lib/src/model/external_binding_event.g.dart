// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_binding_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalBindingEventCWProxy {
  ExternalBindingEvent bindingId(String bindingId);

  ExternalBindingEvent sourceId(String sourceId);

  ExternalBindingEvent reason(String reason);

  ExternalBindingEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalBindingEvent call({
    String bindingId,
    String sourceId,
    String reason,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalBindingEvent.copyWith(...)` or call `instanceOfExternalBindingEvent.copyWith.fieldName(value)` for a single field.
class _$ExternalBindingEventCWProxyImpl
    implements _$ExternalBindingEventCWProxy {
  const _$ExternalBindingEventCWProxyImpl(this._value);

  final ExternalBindingEvent _value;

  @override
  ExternalBindingEvent bindingId(String bindingId) =>
      call(bindingId: bindingId);

  @override
  ExternalBindingEvent sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ExternalBindingEvent reason(String reason) => call(reason: reason);

  @override
  ExternalBindingEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalBindingEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalBindingEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalBindingEvent call({
    Object? bindingId = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ExternalBindingEvent(
      bindingId: bindingId == const $CopyWithPlaceholder() || bindingId == null
          ? _value.bindingId
          // ignore: cast_nullable_to_non_nullable
          : bindingId as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ExternalBindingEventCopyWith on ExternalBindingEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalBindingEvent.copyWith(...)` or `instanceOfExternalBindingEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalBindingEventCWProxy get copyWith =>
      _$ExternalBindingEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalBindingEvent _$ExternalBindingEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalBindingEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['bindingId', 'sourceId', 'reason', 'at'],
  );
  final val = ExternalBindingEvent(
    bindingId: $checkedConvert('bindingId', (v) => v as String),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    reason: $checkedConvert('reason', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ExternalBindingEventToJson(
  ExternalBindingEvent instance,
) => <String, dynamic>{
  'bindingId': instance.bindingId,
  'sourceId': instance.sourceId,
  'reason': instance.reason,
  'at': instance.at.toIso8601String(),
};
