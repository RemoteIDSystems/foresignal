// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_member_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationMemberInputCWProxy {
  SituationMemberInput memberType(
    SituationMemberInputMemberTypeEnum memberType,
  );

  SituationMemberInput ref(String ref);

  SituationMemberInput seq(int? seq);

  SituationMemberInput role(SituationMemberInputRoleEnum? role);

  SituationMemberInput moveFromSituationId(String? moveFromSituationId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMemberInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMemberInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationMemberInput call({
    SituationMemberInputMemberTypeEnum memberType,
    String ref,
    int? seq,
    SituationMemberInputRoleEnum? role,
    String? moveFromSituationId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationMemberInput.copyWith(...)` or call `instanceOfSituationMemberInput.copyWith.fieldName(value)` for a single field.
class _$SituationMemberInputCWProxyImpl
    implements _$SituationMemberInputCWProxy {
  const _$SituationMemberInputCWProxyImpl(this._value);

  final SituationMemberInput _value;

  @override
  SituationMemberInput memberType(
    SituationMemberInputMemberTypeEnum memberType,
  ) => call(memberType: memberType);

  @override
  SituationMemberInput ref(String ref) => call(ref: ref);

  @override
  SituationMemberInput seq(int? seq) => call(seq: seq);

  @override
  SituationMemberInput role(SituationMemberInputRoleEnum? role) =>
      call(role: role);

  @override
  SituationMemberInput moveFromSituationId(String? moveFromSituationId) =>
      call(moveFromSituationId: moveFromSituationId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMemberInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMemberInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationMemberInput call({
    Object? memberType = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? moveFromSituationId = const $CopyWithPlaceholder(),
  }) {
    return SituationMemberInput(
      memberType:
          memberType == const $CopyWithPlaceholder() || memberType == null
          ? _value.memberType
          // ignore: cast_nullable_to_non_nullable
          : memberType as SituationMemberInputMemberTypeEnum,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as SituationMemberInputRoleEnum?,
      moveFromSituationId: moveFromSituationId == const $CopyWithPlaceholder()
          ? _value.moveFromSituationId
          // ignore: cast_nullable_to_non_nullable
          : moveFromSituationId as String?,
    );
  }
}

extension $SituationMemberInputCopyWith on SituationMemberInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationMemberInput.copyWith(...)` or `instanceOfSituationMemberInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationMemberInputCWProxy get copyWith =>
      _$SituationMemberInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationMemberInput _$SituationMemberInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationMemberInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['memberType', 'ref']);
  final val = SituationMemberInput(
    memberType: $checkedConvert(
      'memberType',
      (v) => $enumDecode(_$SituationMemberInputMemberTypeEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String),
    seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
    role: $checkedConvert(
      'role',
      (v) => $enumDecodeNullable(_$SituationMemberInputRoleEnumEnumMap, v),
    ),
    moveFromSituationId: $checkedConvert(
      'moveFromSituationId',
      (v) => v as String?,
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationMemberInputToJson(
  SituationMemberInput instance,
) => <String, dynamic>{
  'memberType':
      _$SituationMemberInputMemberTypeEnumEnumMap[instance.memberType]!,
  'ref': instance.ref,
  'seq': ?instance.seq,
  'role': ?_$SituationMemberInputRoleEnumEnumMap[instance.role],
  'moveFromSituationId': ?instance.moveFromSituationId,
};

const _$SituationMemberInputMemberTypeEnumEnumMap = {
  SituationMemberInputMemberTypeEnum.intelEvent: 'intel_event',
  SituationMemberInputMemberTypeEnum.finding: 'finding',
  SituationMemberInputMemberTypeEnum.alert: 'alert',
  SituationMemberInputMemberTypeEnum.anomaly: 'anomaly',
  SituationMemberInputMemberTypeEnum.record: 'record',
  SituationMemberInputMemberTypeEnum.investigation: 'investigation',
};

const _$SituationMemberInputRoleEnumEnumMap = {
  SituationMemberInputRoleEnum.core: 'core',
  SituationMemberInputRoleEnum.context: 'context',
};
