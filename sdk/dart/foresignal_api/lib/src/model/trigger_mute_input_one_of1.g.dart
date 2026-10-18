// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_mute_input_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerMuteInputOneOf1CWProxy {
  TriggerMuteInputOneOf1 for_(String for_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerMuteInputOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerMuteInputOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerMuteInputOneOf1 call({String for_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerMuteInputOneOf1.copyWith(...)` or call `instanceOfTriggerMuteInputOneOf1.copyWith.fieldName(value)` for a single field.
class _$TriggerMuteInputOneOf1CWProxyImpl
    implements _$TriggerMuteInputOneOf1CWProxy {
  const _$TriggerMuteInputOneOf1CWProxyImpl(this._value);

  final TriggerMuteInputOneOf1 _value;

  @override
  TriggerMuteInputOneOf1 for_(String for_) => call(for_: for_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerMuteInputOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerMuteInputOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerMuteInputOneOf1 call({Object? for_ = const $CopyWithPlaceholder()}) {
    return TriggerMuteInputOneOf1(
      for_: for_ == const $CopyWithPlaceholder() || for_ == null
          ? _value.for_
          // ignore: cast_nullable_to_non_nullable
          : for_ as String,
    );
  }
}

extension $TriggerMuteInputOneOf1CopyWith on TriggerMuteInputOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerMuteInputOneOf1.copyWith(...)` or `instanceOfTriggerMuteInputOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerMuteInputOneOf1CWProxy get copyWith =>
      _$TriggerMuteInputOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerMuteInputOneOf1 _$TriggerMuteInputOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerMuteInputOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['for']);
  final val = TriggerMuteInputOneOf1(
    for_: $checkedConvert('for', (v) => v as String),
  );
  return val;
}, fieldKeyMap: const {'for_': 'for'});

Map<String, dynamic> _$TriggerMuteInputOneOf1ToJson(
  TriggerMuteInputOneOf1 instance,
) => <String, dynamic>{'for': instance.for_};
