// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_member.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationMemberCWProxy {
  SituationMember id(String id);

  SituationMember situationId(String situationId);

  SituationMember memberType(SituationMemberMemberTypeEnum memberType);

  SituationMember ref(String ref);

  SituationMember objectRef(String objectRef);

  SituationMember seq(int? seq);

  SituationMember version(int? version);

  SituationMember kind(String? kind);

  SituationMember signalFamily(String? signalFamily);

  SituationMember title(String? title);

  SituationMember severity(String? severity);

  SituationMember observedAt(DateTime? observedAt);

  SituationMember role(SituationMemberRoleEnum role);

  SituationMember joinReason(SituationJoinReason? joinReason);

  SituationMember corroboration(SituationMemberCorroborationEnum corroboration);

  SituationMember addedAt(DateTime addedAt);

  SituationMember addedBy(String addedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMember(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMember(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationMember call({
    String id,
    String situationId,
    SituationMemberMemberTypeEnum memberType,
    String ref,
    String objectRef,
    int? seq,
    int? version,
    String? kind,
    String? signalFamily,
    String? title,
    String? severity,
    DateTime? observedAt,
    SituationMemberRoleEnum role,
    SituationJoinReason? joinReason,
    SituationMemberCorroborationEnum corroboration,
    DateTime addedAt,
    String addedBy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationMember.copyWith(...)` or call `instanceOfSituationMember.copyWith.fieldName(value)` for a single field.
class _$SituationMemberCWProxyImpl implements _$SituationMemberCWProxy {
  const _$SituationMemberCWProxyImpl(this._value);

  final SituationMember _value;

  @override
  SituationMember id(String id) => call(id: id);

  @override
  SituationMember situationId(String situationId) =>
      call(situationId: situationId);

  @override
  SituationMember memberType(SituationMemberMemberTypeEnum memberType) =>
      call(memberType: memberType);

  @override
  SituationMember ref(String ref) => call(ref: ref);

  @override
  SituationMember objectRef(String objectRef) => call(objectRef: objectRef);

  @override
  SituationMember seq(int? seq) => call(seq: seq);

  @override
  SituationMember version(int? version) => call(version: version);

  @override
  SituationMember kind(String? kind) => call(kind: kind);

  @override
  SituationMember signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  SituationMember title(String? title) => call(title: title);

  @override
  SituationMember severity(String? severity) => call(severity: severity);

  @override
  SituationMember observedAt(DateTime? observedAt) =>
      call(observedAt: observedAt);

  @override
  SituationMember role(SituationMemberRoleEnum role) => call(role: role);

  @override
  SituationMember joinReason(SituationJoinReason? joinReason) =>
      call(joinReason: joinReason);

  @override
  SituationMember corroboration(
    SituationMemberCorroborationEnum corroboration,
  ) => call(corroboration: corroboration);

  @override
  SituationMember addedAt(DateTime addedAt) => call(addedAt: addedAt);

  @override
  SituationMember addedBy(String addedBy) => call(addedBy: addedBy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMember(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMember(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationMember call({
    Object? id = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? memberType = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? objectRef = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? joinReason = const $CopyWithPlaceholder(),
    Object? corroboration = const $CopyWithPlaceholder(),
    Object? addedAt = const $CopyWithPlaceholder(),
    Object? addedBy = const $CopyWithPlaceholder(),
  }) {
    return SituationMember(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      situationId:
          situationId == const $CopyWithPlaceholder() || situationId == null
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String,
      memberType:
          memberType == const $CopyWithPlaceholder() || memberType == null
          ? _value.memberType
          // ignore: cast_nullable_to_non_nullable
          : memberType as SituationMemberMemberTypeEnum,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      objectRef: objectRef == const $CopyWithPlaceholder() || objectRef == null
          ? _value.objectRef
          // ignore: cast_nullable_to_non_nullable
          : objectRef as String,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as String?,
      observedAt: observedAt == const $CopyWithPlaceholder()
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime?,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as SituationMemberRoleEnum,
      joinReason: joinReason == const $CopyWithPlaceholder()
          ? _value.joinReason
          // ignore: cast_nullable_to_non_nullable
          : joinReason as SituationJoinReason?,
      corroboration:
          corroboration == const $CopyWithPlaceholder() || corroboration == null
          ? _value.corroboration
          // ignore: cast_nullable_to_non_nullable
          : corroboration as SituationMemberCorroborationEnum,
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

extension $SituationMemberCopyWith on SituationMember {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationMember.copyWith(...)` or `instanceOfSituationMember.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationMemberCWProxy get copyWith => _$SituationMemberCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationMember _$SituationMemberFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationMember', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'situationId',
          'memberType',
          'ref',
          'objectRef',
          'role',
          'corroboration',
          'addedAt',
          'addedBy',
        ],
      );
      final val = SituationMember(
        id: $checkedConvert('id', (v) => v as String),
        situationId: $checkedConvert('situationId', (v) => v as String),
        memberType: $checkedConvert(
          'memberType',
          (v) => $enumDecode(_$SituationMemberMemberTypeEnumEnumMap, v),
        ),
        ref: $checkedConvert('ref', (v) => v as String),
        objectRef: $checkedConvert('objectRef', (v) => v as String),
        seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
        version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
        kind: $checkedConvert('kind', (v) => v as String?),
        signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
        title: $checkedConvert('title', (v) => v as String?),
        severity: $checkedConvert('severity', (v) => v as String?),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$SituationMemberRoleEnumEnumMap, v),
        ),
        joinReason: $checkedConvert(
          'joinReason',
          (v) => v == null
              ? null
              : SituationJoinReason.fromJson(v as Map<String, dynamic>),
        ),
        corroboration: $checkedConvert(
          'corroboration',
          (v) => $enumDecode(_$SituationMemberCorroborationEnumEnumMap, v),
        ),
        addedAt: $checkedConvert('addedAt', (v) => DateTime.parse(v as String)),
        addedBy: $checkedConvert('addedBy', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SituationMemberToJson(
  SituationMember instance,
) => <String, dynamic>{
  'id': instance.id,
  'situationId': instance.situationId,
  'memberType': _$SituationMemberMemberTypeEnumEnumMap[instance.memberType]!,
  'ref': instance.ref,
  'objectRef': instance.objectRef,
  'seq': ?instance.seq,
  'version': ?instance.version,
  'kind': ?instance.kind,
  'signalFamily': ?instance.signalFamily,
  'title': ?instance.title,
  'severity': ?instance.severity,
  'observedAt': ?instance.observedAt?.toIso8601String(),
  'role': _$SituationMemberRoleEnumEnumMap[instance.role]!,
  'joinReason': ?instance.joinReason?.toJson(),
  'corroboration':
      _$SituationMemberCorroborationEnumEnumMap[instance.corroboration]!,
  'addedAt': instance.addedAt.toIso8601String(),
  'addedBy': instance.addedBy,
};

const _$SituationMemberMemberTypeEnumEnumMap = {
  SituationMemberMemberTypeEnum.intelEvent: 'intel_event',
  SituationMemberMemberTypeEnum.finding: 'finding',
  SituationMemberMemberTypeEnum.alert: 'alert',
  SituationMemberMemberTypeEnum.anomaly: 'anomaly',
  SituationMemberMemberTypeEnum.record: 'record',
  SituationMemberMemberTypeEnum.investigation: 'investigation',
};

const _$SituationMemberRoleEnumEnumMap = {
  SituationMemberRoleEnum.core: 'core',
  SituationMemberRoleEnum.context: 'context',
};

const _$SituationMemberCorroborationEnumEnumMap = {
  SituationMemberCorroborationEnum.corroborated: 'corroborated',
  SituationMemberCorroborationEnum.uncorroborated: 'uncorroborated',
  SituationMemberCorroborationEnum.unknown: 'unknown',
  SituationMemberCorroborationEnum.nSlashA: 'n/a',
};
