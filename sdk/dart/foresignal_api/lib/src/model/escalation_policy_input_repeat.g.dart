// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'escalation_policy_input_repeat.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EscalationPolicyInputRepeatCWProxy {
  EscalationPolicyInputRepeat everyMinutes(int everyMinutes);

  EscalationPolicyInputRepeat maxTimes(int maxTimes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicyInputRepeat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicyInputRepeat(...).copyWith(id: 12, name: "My name")
  /// ```
  EscalationPolicyInputRepeat call({int everyMinutes, int maxTimes});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEscalationPolicyInputRepeat.copyWith(...)` or call `instanceOfEscalationPolicyInputRepeat.copyWith.fieldName(value)` for a single field.
class _$EscalationPolicyInputRepeatCWProxyImpl
    implements _$EscalationPolicyInputRepeatCWProxy {
  const _$EscalationPolicyInputRepeatCWProxyImpl(this._value);

  final EscalationPolicyInputRepeat _value;

  @override
  EscalationPolicyInputRepeat everyMinutes(int everyMinutes) =>
      call(everyMinutes: everyMinutes);

  @override
  EscalationPolicyInputRepeat maxTimes(int maxTimes) =>
      call(maxTimes: maxTimes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicyInputRepeat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicyInputRepeat(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EscalationPolicyInputRepeat call({
    Object? everyMinutes = const $CopyWithPlaceholder(),
    Object? maxTimes = const $CopyWithPlaceholder(),
  }) {
    return EscalationPolicyInputRepeat(
      everyMinutes:
          everyMinutes == const $CopyWithPlaceholder() || everyMinutes == null
          ? _value.everyMinutes
          // ignore: cast_nullable_to_non_nullable
          : everyMinutes as int,
      maxTimes: maxTimes == const $CopyWithPlaceholder() || maxTimes == null
          ? _value.maxTimes
          // ignore: cast_nullable_to_non_nullable
          : maxTimes as int,
    );
  }
}

extension $EscalationPolicyInputRepeatCopyWith on EscalationPolicyInputRepeat {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEscalationPolicyInputRepeat.copyWith(...)` or `instanceOfEscalationPolicyInputRepeat.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EscalationPolicyInputRepeatCWProxy get copyWith =>
      _$EscalationPolicyInputRepeatCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EscalationPolicyInputRepeat _$EscalationPolicyInputRepeatFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EscalationPolicyInputRepeat', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['everyMinutes', 'maxTimes']);
  final val = EscalationPolicyInputRepeat(
    everyMinutes: $checkedConvert('everyMinutes', (v) => (v as num).toInt()),
    maxTimes: $checkedConvert('maxTimes', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$EscalationPolicyInputRepeatToJson(
  EscalationPolicyInputRepeat instance,
) => <String, dynamic>{
  'everyMinutes': instance.everyMinutes,
  'maxTimes': instance.maxTimes,
};
