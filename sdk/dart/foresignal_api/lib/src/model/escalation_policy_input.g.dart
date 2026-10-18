// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'escalation_policy_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EscalationPolicyInputCWProxy {
  EscalationPolicyInput name(String name);

  EscalationPolicyInput steps(List<EscalationStep> steps);

  EscalationPolicyInput repeat(EscalationPolicyInputRepeat? repeat);

  EscalationPolicyInput stopOn(EscalationPolicyInputStopOn? stopOn);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  EscalationPolicyInput call({
    String name,
    List<EscalationStep> steps,
    EscalationPolicyInputRepeat? repeat,
    EscalationPolicyInputStopOn? stopOn,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEscalationPolicyInput.copyWith(...)` or call `instanceOfEscalationPolicyInput.copyWith.fieldName(value)` for a single field.
class _$EscalationPolicyInputCWProxyImpl
    implements _$EscalationPolicyInputCWProxy {
  const _$EscalationPolicyInputCWProxyImpl(this._value);

  final EscalationPolicyInput _value;

  @override
  EscalationPolicyInput name(String name) => call(name: name);

  @override
  EscalationPolicyInput steps(List<EscalationStep> steps) => call(steps: steps);

  @override
  EscalationPolicyInput repeat(EscalationPolicyInputRepeat? repeat) =>
      call(repeat: repeat);

  @override
  EscalationPolicyInput stopOn(EscalationPolicyInputStopOn? stopOn) =>
      call(stopOn: stopOn);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EscalationPolicyInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? steps = const $CopyWithPlaceholder(),
    Object? repeat = const $CopyWithPlaceholder(),
    Object? stopOn = const $CopyWithPlaceholder(),
  }) {
    return EscalationPolicyInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      steps: steps == const $CopyWithPlaceholder() || steps == null
          ? _value.steps
          // ignore: cast_nullable_to_non_nullable
          : steps as List<EscalationStep>,
      repeat: repeat == const $CopyWithPlaceholder()
          ? _value.repeat
          // ignore: cast_nullable_to_non_nullable
          : repeat as EscalationPolicyInputRepeat?,
      stopOn: stopOn == const $CopyWithPlaceholder()
          ? _value.stopOn
          // ignore: cast_nullable_to_non_nullable
          : stopOn as EscalationPolicyInputStopOn?,
    );
  }
}

extension $EscalationPolicyInputCopyWith on EscalationPolicyInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEscalationPolicyInput.copyWith(...)` or `instanceOfEscalationPolicyInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EscalationPolicyInputCWProxy get copyWith =>
      _$EscalationPolicyInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EscalationPolicyInput _$EscalationPolicyInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EscalationPolicyInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'steps']);
  final val = EscalationPolicyInput(
    name: $checkedConvert('name', (v) => v as String),
    steps: $checkedConvert(
      'steps',
      (v) => (v as List<dynamic>)
          .map((e) => EscalationStep.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    repeat: $checkedConvert(
      'repeat',
      (v) => v == null
          ? null
          : EscalationPolicyInputRepeat.fromJson(v as Map<String, dynamic>),
    ),
    stopOn: $checkedConvert(
      'stopOn',
      (v) => v == null
          ? null
          : EscalationPolicyInputStopOn.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$EscalationPolicyInputToJson(
  EscalationPolicyInput instance,
) => <String, dynamic>{
  'name': instance.name,
  'steps': instance.steps.map((e) => e.toJson()).toList(),
  'repeat': ?instance.repeat?.toJson(),
  'stopOn': ?instance.stopOn?.toJson(),
};
