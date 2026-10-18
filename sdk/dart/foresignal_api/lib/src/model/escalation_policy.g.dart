// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'escalation_policy.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EscalationPolicyCWProxy {
  EscalationPolicy name(String name);

  EscalationPolicy steps(List<EscalationStep> steps);

  EscalationPolicy repeat(EscalationPolicyInputRepeat? repeat);

  EscalationPolicy stopOn(EscalationPolicyInputStopOn? stopOn);

  EscalationPolicy id(String id);

  EscalationPolicy createdAt(DateTime createdAt);

  EscalationPolicy updatedAt(DateTime updatedAt);

  EscalationPolicy ruleCount(int? ruleCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicy(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicy(...).copyWith(id: 12, name: "My name")
  /// ```
  EscalationPolicy call({
    String name,
    List<EscalationStep> steps,
    EscalationPolicyInputRepeat? repeat,
    EscalationPolicyInputStopOn? stopOn,
    String id,
    DateTime createdAt,
    DateTime updatedAt,
    int? ruleCount,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEscalationPolicy.copyWith(...)` or call `instanceOfEscalationPolicy.copyWith.fieldName(value)` for a single field.
class _$EscalationPolicyCWProxyImpl implements _$EscalationPolicyCWProxy {
  const _$EscalationPolicyCWProxyImpl(this._value);

  final EscalationPolicy _value;

  @override
  EscalationPolicy name(String name) => call(name: name);

  @override
  EscalationPolicy steps(List<EscalationStep> steps) => call(steps: steps);

  @override
  EscalationPolicy repeat(EscalationPolicyInputRepeat? repeat) =>
      call(repeat: repeat);

  @override
  EscalationPolicy stopOn(EscalationPolicyInputStopOn? stopOn) =>
      call(stopOn: stopOn);

  @override
  EscalationPolicy id(String id) => call(id: id);

  @override
  EscalationPolicy createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  EscalationPolicy updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  EscalationPolicy ruleCount(int? ruleCount) => call(ruleCount: ruleCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EscalationPolicy(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EscalationPolicy(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EscalationPolicy call({
    Object? name = const $CopyWithPlaceholder(),
    Object? steps = const $CopyWithPlaceholder(),
    Object? repeat = const $CopyWithPlaceholder(),
    Object? stopOn = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? ruleCount = const $CopyWithPlaceholder(),
  }) {
    return EscalationPolicy(
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
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      ruleCount: ruleCount == const $CopyWithPlaceholder()
          ? _value.ruleCount
          // ignore: cast_nullable_to_non_nullable
          : ruleCount as int?,
    );
  }
}

extension $EscalationPolicyCopyWith on EscalationPolicy {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEscalationPolicy.copyWith(...)` or `instanceOfEscalationPolicy.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EscalationPolicyCWProxy get copyWith => _$EscalationPolicyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EscalationPolicy _$EscalationPolicyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EscalationPolicy', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['name', 'steps', 'id', 'createdAt', 'updatedAt'],
  );
  final val = EscalationPolicy(
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
    id: $checkedConvert('id', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    ruleCount: $checkedConvert('ruleCount', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$EscalationPolicyToJson(EscalationPolicy instance) =>
    <String, dynamic>{
      'name': instance.name,
      'steps': instance.steps.map((e) => e.toJson()).toList(),
      'repeat': ?instance.repeat?.toJson(),
      'stopOn': ?instance.stopOn?.toJson(),
      'id': instance.id,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': instance.updatedAt.toIso8601String(),
      'ruleCount': ?instance.ruleCount,
    };
