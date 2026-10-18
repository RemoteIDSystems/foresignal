// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'escalation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EscalationCWProxy {
  Escalation alertId(int alertId);

  Escalation policyId(String policyId);

  Escalation nextStep(int? nextStep);

  Escalation nextAt(DateTime? nextAt);

  Escalation repeatsDone(int? repeatsDone);

  Escalation state(EscalationStateEnum state);

  Escalation stoppedReason(String? stoppedReason);

  Escalation updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Escalation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Escalation(...).copyWith(id: 12, name: "My name")
  /// ```
  Escalation call({
    int alertId,
    String policyId,
    int? nextStep,
    DateTime? nextAt,
    int? repeatsDone,
    EscalationStateEnum state,
    String? stoppedReason,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEscalation.copyWith(...)` or call `instanceOfEscalation.copyWith.fieldName(value)` for a single field.
class _$EscalationCWProxyImpl implements _$EscalationCWProxy {
  const _$EscalationCWProxyImpl(this._value);

  final Escalation _value;

  @override
  Escalation alertId(int alertId) => call(alertId: alertId);

  @override
  Escalation policyId(String policyId) => call(policyId: policyId);

  @override
  Escalation nextStep(int? nextStep) => call(nextStep: nextStep);

  @override
  Escalation nextAt(DateTime? nextAt) => call(nextAt: nextAt);

  @override
  Escalation repeatsDone(int? repeatsDone) => call(repeatsDone: repeatsDone);

  @override
  Escalation state(EscalationStateEnum state) => call(state: state);

  @override
  Escalation stoppedReason(String? stoppedReason) =>
      call(stoppedReason: stoppedReason);

  @override
  Escalation updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Escalation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Escalation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Escalation call({
    Object? alertId = const $CopyWithPlaceholder(),
    Object? policyId = const $CopyWithPlaceholder(),
    Object? nextStep = const $CopyWithPlaceholder(),
    Object? nextAt = const $CopyWithPlaceholder(),
    Object? repeatsDone = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? stoppedReason = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Escalation(
      alertId: alertId == const $CopyWithPlaceholder() || alertId == null
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int,
      policyId: policyId == const $CopyWithPlaceholder() || policyId == null
          ? _value.policyId
          // ignore: cast_nullable_to_non_nullable
          : policyId as String,
      nextStep: nextStep == const $CopyWithPlaceholder()
          ? _value.nextStep
          // ignore: cast_nullable_to_non_nullable
          : nextStep as int?,
      nextAt: nextAt == const $CopyWithPlaceholder()
          ? _value.nextAt
          // ignore: cast_nullable_to_non_nullable
          : nextAt as DateTime?,
      repeatsDone: repeatsDone == const $CopyWithPlaceholder()
          ? _value.repeatsDone
          // ignore: cast_nullable_to_non_nullable
          : repeatsDone as int?,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as EscalationStateEnum,
      stoppedReason: stoppedReason == const $CopyWithPlaceholder()
          ? _value.stoppedReason
          // ignore: cast_nullable_to_non_nullable
          : stoppedReason as String?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $EscalationCopyWith on Escalation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEscalation.copyWith(...)` or `instanceOfEscalation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EscalationCWProxy get copyWith => _$EscalationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Escalation _$EscalationFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Escalation',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['alertId', 'policyId', 'state']);
    final val = Escalation(
      alertId: $checkedConvert('alertId', (v) => (v as num).toInt()),
      policyId: $checkedConvert('policyId', (v) => v as String),
      nextStep: $checkedConvert('nextStep', (v) => (v as num?)?.toInt()),
      nextAt: $checkedConvert(
        'nextAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      repeatsDone: $checkedConvert('repeatsDone', (v) => (v as num?)?.toInt()),
      state: $checkedConvert(
        'state',
        (v) => $enumDecode(_$EscalationStateEnumEnumMap, v),
      ),
      stoppedReason: $checkedConvert('stoppedReason', (v) => v as String?),
      updatedAt: $checkedConvert(
        'updatedAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$EscalationToJson(Escalation instance) =>
    <String, dynamic>{
      'alertId': instance.alertId,
      'policyId': instance.policyId,
      'nextStep': ?instance.nextStep,
      'nextAt': ?instance.nextAt?.toIso8601String(),
      'repeatsDone': ?instance.repeatsDone,
      'state': _$EscalationStateEnumEnumMap[instance.state]!,
      'stoppedReason': ?instance.stoppedReason,
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };

const _$EscalationStateEnumEnumMap = {
  EscalationStateEnum.active: 'active',
  EscalationStateEnum.stopped: 'stopped',
  EscalationStateEnum.exhausted: 'exhausted',
};
