// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_delta_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskDeltaEventCWProxy {
  AskDeltaEvent text(String text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskDeltaEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskDeltaEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AskDeltaEvent call({String text});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskDeltaEvent.copyWith(...)` or call `instanceOfAskDeltaEvent.copyWith.fieldName(value)` for a single field.
class _$AskDeltaEventCWProxyImpl implements _$AskDeltaEventCWProxy {
  const _$AskDeltaEventCWProxyImpl(this._value);

  final AskDeltaEvent _value;

  @override
  AskDeltaEvent text(String text) => call(text: text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskDeltaEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskDeltaEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskDeltaEvent call({Object? text = const $CopyWithPlaceholder()}) {
    return AskDeltaEvent(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
    );
  }
}

extension $AskDeltaEventCopyWith on AskDeltaEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskDeltaEvent.copyWith(...)` or `instanceOfAskDeltaEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskDeltaEventCWProxy get copyWith => _$AskDeltaEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskDeltaEvent _$AskDeltaEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskDeltaEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['text']);
      final val = AskDeltaEvent(
        text: $checkedConvert('text', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AskDeltaEventToJson(AskDeltaEvent instance) =>
    <String, dynamic>{'text': instance.text};
