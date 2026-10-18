// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_member_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventMemberInputCWProxy {
  IntelEventMemberInput memberType(
    IntelEventMemberInputMemberTypeEnum memberType,
  );

  IntelEventMemberInput role(IntelEventMemberInputRoleEnum? role);

  IntelEventMemberInput sourceId(String? sourceId);

  IntelEventMemberInput externalId(String? externalId);

  IntelEventMemberInput seq(int? seq);

  IntelEventMemberInput alertId(int? alertId);

  IntelEventMemberInput anomalyId(int? anomalyId);

  IntelEventMemberInput eventId(String? eventId);

  IntelEventMemberInput signalFamily(String? signalFamily);

  IntelEventMemberInput weight(num? weight);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMemberInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMemberInput(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventMemberInput call({
    IntelEventMemberInputMemberTypeEnum memberType,
    IntelEventMemberInputRoleEnum? role,
    String? sourceId,
    String? externalId,
    int? seq,
    int? alertId,
    int? anomalyId,
    String? eventId,
    String? signalFamily,
    num? weight,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventMemberInput.copyWith(...)` or call `instanceOfIntelEventMemberInput.copyWith.fieldName(value)` for a single field.
class _$IntelEventMemberInputCWProxyImpl
    implements _$IntelEventMemberInputCWProxy {
  const _$IntelEventMemberInputCWProxyImpl(this._value);

  final IntelEventMemberInput _value;

  @override
  IntelEventMemberInput memberType(
    IntelEventMemberInputMemberTypeEnum memberType,
  ) => call(memberType: memberType);

  @override
  IntelEventMemberInput role(IntelEventMemberInputRoleEnum? role) =>
      call(role: role);

  @override
  IntelEventMemberInput sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  IntelEventMemberInput externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  IntelEventMemberInput seq(int? seq) => call(seq: seq);

  @override
  IntelEventMemberInput alertId(int? alertId) => call(alertId: alertId);

  @override
  IntelEventMemberInput anomalyId(int? anomalyId) => call(anomalyId: anomalyId);

  @override
  IntelEventMemberInput eventId(String? eventId) => call(eventId: eventId);

  @override
  IntelEventMemberInput signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  IntelEventMemberInput weight(num? weight) => call(weight: weight);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMemberInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMemberInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventMemberInput call({
    Object? memberType = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? anomalyId = const $CopyWithPlaceholder(),
    Object? eventId = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? weight = const $CopyWithPlaceholder(),
  }) {
    return IntelEventMemberInput(
      memberType:
          memberType == const $CopyWithPlaceholder() || memberType == null
          ? _value.memberType
          // ignore: cast_nullable_to_non_nullable
          : memberType as IntelEventMemberInputMemberTypeEnum,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as IntelEventMemberInputRoleEnum?,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      alertId: alertId == const $CopyWithPlaceholder()
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int?,
      anomalyId: anomalyId == const $CopyWithPlaceholder()
          ? _value.anomalyId
          // ignore: cast_nullable_to_non_nullable
          : anomalyId as int?,
      eventId: eventId == const $CopyWithPlaceholder()
          ? _value.eventId
          // ignore: cast_nullable_to_non_nullable
          : eventId as String?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      weight: weight == const $CopyWithPlaceholder()
          ? _value.weight
          // ignore: cast_nullable_to_non_nullable
          : weight as num?,
    );
  }
}

extension $IntelEventMemberInputCopyWith on IntelEventMemberInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventMemberInput.copyWith(...)` or `instanceOfIntelEventMemberInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventMemberInputCWProxy get copyWith =>
      _$IntelEventMemberInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventMemberInput _$IntelEventMemberInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventMemberInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['memberType']);
  final val = IntelEventMemberInput(
    memberType: $checkedConvert(
      'memberType',
      (v) => $enumDecode(_$IntelEventMemberInputMemberTypeEnumEnumMap, v),
    ),
    role: $checkedConvert(
      'role',
      (v) => $enumDecodeNullable(_$IntelEventMemberInputRoleEnumEnumMap, v),
    ),
    sourceId: $checkedConvert('sourceId', (v) => v as String?),
    externalId: $checkedConvert('externalId', (v) => v as String?),
    seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
    alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
    anomalyId: $checkedConvert('anomalyId', (v) => (v as num?)?.toInt()),
    eventId: $checkedConvert('eventId', (v) => v as String?),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
    weight: $checkedConvert('weight', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$IntelEventMemberInputToJson(
  IntelEventMemberInput instance,
) => <String, dynamic>{
  'memberType':
      _$IntelEventMemberInputMemberTypeEnumEnumMap[instance.memberType]!,
  'role': ?_$IntelEventMemberInputRoleEnumEnumMap[instance.role],
  'sourceId': ?instance.sourceId,
  'externalId': ?instance.externalId,
  'seq': ?instance.seq,
  'alertId': ?instance.alertId,
  'anomalyId': ?instance.anomalyId,
  'eventId': ?instance.eventId,
  'signalFamily': ?instance.signalFamily,
  'weight': ?instance.weight,
};

const _$IntelEventMemberInputMemberTypeEnumEnumMap = {
  IntelEventMemberInputMemberTypeEnum.record: 'record',
  IntelEventMemberInputMemberTypeEnum.alert: 'alert',
  IntelEventMemberInputMemberTypeEnum.anomaly: 'anomaly',
  IntelEventMemberInputMemberTypeEnum.event: 'event',
};

const _$IntelEventMemberInputRoleEnumEnumMap = {
  IntelEventMemberInputRoleEnum.supporting: 'supporting',
  IntelEventMemberInputRoleEnum.contradicting: 'contradicting',
  IntelEventMemberInputRoleEnum.context: 'context',
};
