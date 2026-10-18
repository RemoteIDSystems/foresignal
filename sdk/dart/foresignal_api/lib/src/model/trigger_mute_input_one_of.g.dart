// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_mute_input_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerMuteInputOneOfCWProxy {
  TriggerMuteInputOneOf until(DateTime until);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerMuteInputOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerMuteInputOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerMuteInputOneOf call({DateTime until});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerMuteInputOneOf.copyWith(...)` or call `instanceOfTriggerMuteInputOneOf.copyWith.fieldName(value)` for a single field.
class _$TriggerMuteInputOneOfCWProxyImpl
    implements _$TriggerMuteInputOneOfCWProxy {
  const _$TriggerMuteInputOneOfCWProxyImpl(this._value);

  final TriggerMuteInputOneOf _value;

  @override
  TriggerMuteInputOneOf until(DateTime until) => call(until: until);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerMuteInputOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerMuteInputOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerMuteInputOneOf call({Object? until = const $CopyWithPlaceholder()}) {
    return TriggerMuteInputOneOf(
      until: until == const $CopyWithPlaceholder() || until == null
          ? _value.until
          // ignore: cast_nullable_to_non_nullable
          : until as DateTime,
    );
  }
}

extension $TriggerMuteInputOneOfCopyWith on TriggerMuteInputOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerMuteInputOneOf.copyWith(...)` or `instanceOfTriggerMuteInputOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerMuteInputOneOfCWProxy get copyWith =>
      _$TriggerMuteInputOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerMuteInputOneOf _$TriggerMuteInputOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerMuteInputOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['until']);
  final val = TriggerMuteInputOneOf(
    until: $checkedConvert('until', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$TriggerMuteInputOneOfToJson(
  TriggerMuteInputOneOf instance,
) => <String, dynamic>{'until': instance.until.toIso8601String()};
