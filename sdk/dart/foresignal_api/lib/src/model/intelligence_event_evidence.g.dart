// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intelligence_event_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelligenceEventEvidenceCWProxy {
  IntelligenceEventEvidence memberCount(int memberCount);

  IntelligenceEventEvidence signalFamilies(List<String>? signalFamilies);

  IntelligenceEventEvidence members(List<IntelEventMember> members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelligenceEventEvidence call({
    int memberCount,
    List<String>? signalFamilies,
    List<IntelEventMember> members,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelligenceEventEvidence.copyWith(...)` or call `instanceOfIntelligenceEventEvidence.copyWith.fieldName(value)` for a single field.
class _$IntelligenceEventEvidenceCWProxyImpl
    implements _$IntelligenceEventEvidenceCWProxy {
  const _$IntelligenceEventEvidenceCWProxyImpl(this._value);

  final IntelligenceEventEvidence _value;

  @override
  IntelligenceEventEvidence memberCount(int memberCount) =>
      call(memberCount: memberCount);

  @override
  IntelligenceEventEvidence signalFamilies(List<String>? signalFamilies) =>
      call(signalFamilies: signalFamilies);

  @override
  IntelligenceEventEvidence members(List<IntelEventMember> members) =>
      call(members: members);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelligenceEventEvidence call({
    Object? memberCount = const $CopyWithPlaceholder(),
    Object? signalFamilies = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
  }) {
    return IntelligenceEventEvidence(
      memberCount:
          memberCount == const $CopyWithPlaceholder() || memberCount == null
          ? _value.memberCount
          // ignore: cast_nullable_to_non_nullable
          : memberCount as int,
      signalFamilies: signalFamilies == const $CopyWithPlaceholder()
          ? _value.signalFamilies
          // ignore: cast_nullable_to_non_nullable
          : signalFamilies as List<String>?,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<IntelEventMember>,
    );
  }
}

extension $IntelligenceEventEvidenceCopyWith on IntelligenceEventEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelligenceEventEvidence.copyWith(...)` or `instanceOfIntelligenceEventEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelligenceEventEvidenceCWProxy get copyWith =>
      _$IntelligenceEventEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelligenceEventEvidence _$IntelligenceEventEvidenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelligenceEventEvidence', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['memberCount', 'members']);
  final val = IntelligenceEventEvidence(
    memberCount: $checkedConvert('memberCount', (v) => (v as num).toInt()),
    signalFamilies: $checkedConvert(
      'signalFamilies',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>)
          .map((e) => IntelEventMember.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelligenceEventEvidenceToJson(
  IntelligenceEventEvidence instance,
) => <String, dynamic>{
  'memberCount': instance.memberCount,
  'signalFamilies': ?instance.signalFamilies,
  'members': instance.members.map((e) => e.toJson()).toList(),
};
