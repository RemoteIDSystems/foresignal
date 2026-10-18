// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_member.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventMemberCWProxy {
  IntelEventMember id(String id);

  IntelEventMember role(IntelEventMemberRoleEnum role);

  IntelEventMember memberType(IntelEventMemberMemberTypeEnum memberType);

  IntelEventMember sourceId(String? sourceId);

  IntelEventMember externalId(String? externalId);

  IntelEventMember seq(int? seq);

  IntelEventMember version(int? version);

  IntelEventMember alertId(int? alertId);

  IntelEventMember anomalyId(int? anomalyId);

  IntelEventMember eventId(String? eventId);

  IntelEventMember kind(String? kind);

  IntelEventMember observedAt(DateTime? observedAt);

  IntelEventMember joinReason(IntelEventMemberJoinReason? joinReason);

  IntelEventMember signalFamily(String? signalFamily);

  IntelEventMember weight(num? weight);

  IntelEventMember snapshot(Map<String, Object>? snapshot);

  IntelEventMember addedAt(DateTime addedAt);

  IntelEventMember addedBy(String addedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMember(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMember(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventMember call({
    String id,
    IntelEventMemberRoleEnum role,
    IntelEventMemberMemberTypeEnum memberType,
    String? sourceId,
    String? externalId,
    int? seq,
    int? version,
    int? alertId,
    int? anomalyId,
    String? eventId,
    String? kind,
    DateTime? observedAt,
    IntelEventMemberJoinReason? joinReason,
    String? signalFamily,
    num? weight,
    Map<String, Object>? snapshot,
    DateTime addedAt,
    String addedBy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventMember.copyWith(...)` or call `instanceOfIntelEventMember.copyWith.fieldName(value)` for a single field.
class _$IntelEventMemberCWProxyImpl implements _$IntelEventMemberCWProxy {
  const _$IntelEventMemberCWProxyImpl(this._value);

  final IntelEventMember _value;

  @override
  IntelEventMember id(String id) => call(id: id);

  @override
  IntelEventMember role(IntelEventMemberRoleEnum role) => call(role: role);

  @override
  IntelEventMember memberType(IntelEventMemberMemberTypeEnum memberType) =>
      call(memberType: memberType);

  @override
  IntelEventMember sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  IntelEventMember externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  IntelEventMember seq(int? seq) => call(seq: seq);

  @override
  IntelEventMember version(int? version) => call(version: version);

  @override
  IntelEventMember alertId(int? alertId) => call(alertId: alertId);

  @override
  IntelEventMember anomalyId(int? anomalyId) => call(anomalyId: anomalyId);

  @override
  IntelEventMember eventId(String? eventId) => call(eventId: eventId);

  @override
  IntelEventMember kind(String? kind) => call(kind: kind);

  @override
  IntelEventMember observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  IntelEventMember joinReason(IntelEventMemberJoinReason? joinReason) =>
      call(joinReason: joinReason);

  @override
  IntelEventMember signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  IntelEventMember weight(num? weight) => call(weight: weight);

  @override
  IntelEventMember snapshot(Map<String, Object>? snapshot) =>
      call(snapshot: snapshot);

  @override
  IntelEventMember addedAt(DateTime addedAt) => call(addedAt: addedAt);

  @override
  IntelEventMember addedBy(String addedBy) => call(addedBy: addedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventMember(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventMember(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventMember call({
    Object? id = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? memberType = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? anomalyId = const $CopyWithPlaceholder(),
    Object? eventId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? joinReason = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? weight = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
    Object? addedAt = const $CopyWithPlaceholder(),
    Object? addedBy = const $CopyWithPlaceholder(),
  }) {
    return IntelEventMember(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as IntelEventMemberRoleEnum,
      memberType:
          memberType == const $CopyWithPlaceholder() || memberType == null
          ? _value.memberType
          // ignore: cast_nullable_to_non_nullable
          : memberType as IntelEventMemberMemberTypeEnum,
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
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
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
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      joinReason: joinReason == const $CopyWithPlaceholder()
          ? _value.joinReason
          // ignore: cast_nullable_to_non_nullable
          : joinReason as IntelEventMemberJoinReason?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      weight: weight == const $CopyWithPlaceholder()
          ? _value.weight
          // ignore: cast_nullable_to_non_nullable
          : weight as num?,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
      addedAt: addedAt == const $CopyWithPlaceholder() || addedAt == null
          ? _value.addedAt
          // ignore: cast_nullable_to_non_nullable
          : addedAt as DateTime,
      addedBy: addedBy == const $CopyWithPlaceholder() || addedBy == null
          ? _value.addedBy
          // ignore: cast_nullable_to_non_nullable
          : addedBy as String,
    );
  }
}

extension $IntelEventMemberCopyWith on IntelEventMember {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventMember.copyWith(...)` or `instanceOfIntelEventMember.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventMemberCWProxy get copyWith => _$IntelEventMemberCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventMember _$IntelEventMemberFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IntelEventMember', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'role', 'memberType', 'addedAt', 'addedBy'],
      );
      final val = IntelEventMember(
        id: $checkedConvert('id', (v) => v as String),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$IntelEventMemberRoleEnumEnumMap, v),
        ),
        memberType: $checkedConvert(
          'memberType',
          (v) => $enumDecode(_$IntelEventMemberMemberTypeEnumEnumMap, v),
        ),
        sourceId: $checkedConvert('sourceId', (v) => v as String?),
        externalId: $checkedConvert('externalId', (v) => v as String?),
        seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
        alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
        anomalyId: $checkedConvert('anomalyId', (v) => (v as num?)?.toInt()),
        eventId: $checkedConvert('eventId', (v) => v as String?),
        kind: $checkedConvert('kind', (v) => v as String?),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        joinReason: $checkedConvert(
          'joinReason',
          (v) => v == null
              ? null
              : IntelEventMemberJoinReason.fromJson(v as Map<String, dynamic>),
        ),
        signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
        weight: $checkedConvert('weight', (v) => v as num?),
        snapshot: $checkedConvert(
          'snapshot',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        addedAt: $checkedConvert('addedAt', (v) => DateTime.parse(v as String)),
        addedBy: $checkedConvert('addedBy', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$IntelEventMemberToJson(
  IntelEventMember instance,
) => <String, dynamic>{
  'id': instance.id,
  'role': _$IntelEventMemberRoleEnumEnumMap[instance.role]!,
  'memberType': _$IntelEventMemberMemberTypeEnumEnumMap[instance.memberType]!,
  'sourceId': ?instance.sourceId,
  'externalId': ?instance.externalId,
  'seq': ?instance.seq,
  'version': ?instance.version,
  'alertId': ?instance.alertId,
  'anomalyId': ?instance.anomalyId,
  'eventId': ?instance.eventId,
  'kind': ?instance.kind,
  'observedAt': ?instance.observedAt?.toIso8601String(),
  'joinReason': ?instance.joinReason?.toJson(),
  'signalFamily': ?instance.signalFamily,
  'weight': ?instance.weight,
  'snapshot': ?instance.snapshot,
  'addedAt': instance.addedAt.toIso8601String(),
  'addedBy': instance.addedBy,
};

const _$IntelEventMemberRoleEnumEnumMap = {
  IntelEventMemberRoleEnum.supporting: 'supporting',
  IntelEventMemberRoleEnum.contradicting: 'contradicting',
  IntelEventMemberRoleEnum.context: 'context',
};

const _$IntelEventMemberMemberTypeEnumEnumMap = {
  IntelEventMemberMemberTypeEnum.record: 'record',
  IntelEventMemberMemberTypeEnum.alert: 'alert',
  IntelEventMemberMemberTypeEnum.anomaly: 'anomaly',
  IntelEventMemberMemberTypeEnum.event: 'event',
};
