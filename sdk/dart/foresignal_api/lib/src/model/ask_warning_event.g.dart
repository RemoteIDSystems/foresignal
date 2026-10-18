// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_warning_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskWarningEventCWProxy {
  AskWarningEvent message(String message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskWarningEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskWarningEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AskWarningEvent call({String message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskWarningEvent.copyWith(...)` or call `instanceOfAskWarningEvent.copyWith.fieldName(value)` for a single field.
class _$AskWarningEventCWProxyImpl implements _$AskWarningEventCWProxy {
  const _$AskWarningEventCWProxyImpl(this._value);

  final AskWarningEvent _value;

  @override
  AskWarningEvent message(String message) => call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskWarningEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskWarningEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskWarningEvent call({Object? message = const $CopyWithPlaceholder()}) {
    return AskWarningEvent(
      message: message == const $CopyWithPlaceholder() || message == null
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String,
    );
  }
}

extension $AskWarningEventCopyWith on AskWarningEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskWarningEvent.copyWith(...)` or `instanceOfAskWarningEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskWarningEventCWProxy get copyWith => _$AskWarningEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskWarningEvent _$AskWarningEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskWarningEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['message']);
      final val = AskWarningEvent(
        message: $checkedConvert('message', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AskWarningEventToJson(AskWarningEvent instance) =>
    <String, dynamic>{'message': instance.message};
