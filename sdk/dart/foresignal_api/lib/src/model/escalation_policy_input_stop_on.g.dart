// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'escalation_policy_input_stop_on.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EscalationPolicyInputStopOnCWProxy {
  EscalationPolicyInputStopOn resolve(bool? resolve);

  EscalationPolicyInputStopOn snooze(bool? snooze);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicyInputStopOn(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicyInputStopOn(...).copyWith(id: 12, name: "My name")
  /// ```
  EscalationPolicyInputStopOn call({bool? resolve, bool? snooze});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEscalationPolicyInputStopOn.copyWith(...)` or call `instanceOfEscalationPolicyInputStopOn.copyWith.fieldName(value)` for a single field.
class _$EscalationPolicyInputStopOnCWProxyImpl
    implements _$EscalationPolicyInputStopOnCWProxy {
  const _$EscalationPolicyInputStopOnCWProxyImpl(this._value);

  final EscalationPolicyInputStopOn _value;

  @override
  EscalationPolicyInputStopOn resolve(bool? resolve) => call(resolve: resolve);

  @override
  EscalationPolicyInputStopOn snooze(bool? snooze) => call(snooze: snooze);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicyInputStopOn(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicyInputStopOn(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EscalationPolicyInputStopOn call({
    Object? resolve = const $CopyWithPlaceholder(),
    Object? snooze = const $CopyWithPlaceholder(),
  }) {
    return EscalationPolicyInputStopOn(
      resolve: resolve == const $CopyWithPlaceholder()
          ? _value.resolve
          // ignore: cast_nullable_to_non_nullable
          : resolve as bool?,
      snooze: snooze == const $CopyWithPlaceholder()
          ? _value.snooze
          // ignore: cast_nullable_to_non_nullable
          : snooze as bool?,
    );
  }
}

extension $EscalationPolicyInputStopOnCopyWith on EscalationPolicyInputStopOn {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEscalationPolicyInputStopOn.copyWith(...)` or `instanceOfEscalationPolicyInputStopOn.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EscalationPolicyInputStopOnCWProxy get copyWith =>
      _$EscalationPolicyInputStopOnCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EscalationPolicyInputStopOn _$EscalationPolicyInputStopOnFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EscalationPolicyInputStopOn', json, ($checkedConvert) {
  final val = EscalationPolicyInputStopOn(
    resolve: $checkedConvert('resolve', (v) => v as bool? ?? true),
    snooze: $checkedConvert('snooze', (v) => v as bool? ?? true),
  );
  return val;
});

Map<String, dynamic> _$EscalationPolicyInputStopOnToJson(
  EscalationPolicyInputStopOn instance,
) => <String, dynamic>{
  'resolve': ?instance.resolve,
  'snooze': ?instance.snooze,
};
