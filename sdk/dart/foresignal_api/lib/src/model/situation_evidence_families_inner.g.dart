// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_evidence_families_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationEvidenceFamiliesInnerCWProxy {
  SituationEvidenceFamiliesInner family(String family);

  SituationEvidenceFamiliesInner count(int count);

  SituationEvidenceFamiliesInner coreCount(int? coreCount);

  SituationEvidenceFamiliesInner members(List<SituationMember> members);

  SituationEvidenceFamiliesInner more(bool? more);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidenceFamiliesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidenceFamiliesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationEvidenceFamiliesInner call({
    String family,
    int count,
    int? coreCount,
    List<SituationMember> members,
    bool? more,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationEvidenceFamiliesInner.copyWith(...)` or call `instanceOfSituationEvidenceFamiliesInner.copyWith.fieldName(value)` for a single field.
class _$SituationEvidenceFamiliesInnerCWProxyImpl
    implements _$SituationEvidenceFamiliesInnerCWProxy {
  const _$SituationEvidenceFamiliesInnerCWProxyImpl(this._value);

  final SituationEvidenceFamiliesInner _value;

  @override
  SituationEvidenceFamiliesInner family(String family) => call(family: family);

  @override
  SituationEvidenceFamiliesInner count(int count) => call(count: count);

  @override
  SituationEvidenceFamiliesInner coreCount(int? coreCount) =>
      call(coreCount: coreCount);

  @override
  SituationEvidenceFamiliesInner members(List<SituationMember> members) =>
      call(members: members);

  @override
  SituationEvidenceFamiliesInner more(bool? more) => call(more: more);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidenceFamiliesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidenceFamiliesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationEvidenceFamiliesInner call({
    Object? family = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? coreCount = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? more = const $CopyWithPlaceholder(),
  }) {
    return SituationEvidenceFamiliesInner(
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      coreCount: coreCount == const $CopyWithPlaceholder()
          ? _value.coreCount
          // ignore: cast_nullable_to_non_nullable
          : coreCount as int?,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<SituationMember>,
      more: more == const $CopyWithPlaceholder()
          ? _value.more
          // ignore: cast_nullable_to_non_nullable
          : more as bool?,
    );
  }
}

extension $SituationEvidenceFamiliesInnerCopyWith
    on SituationEvidenceFamiliesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationEvidenceFamiliesInner.copyWith(...)` or `instanceOfSituationEvidenceFamiliesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationEvidenceFamiliesInnerCWProxy get copyWith =>
      _$SituationEvidenceFamiliesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationEvidenceFamiliesInner _$SituationEvidenceFamiliesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationEvidenceFamiliesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['family', 'count', 'members']);
  final val = SituationEvidenceFamiliesInner(
    family: $checkedConvert('family', (v) => v as String),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    coreCount: $checkedConvert('coreCount', (v) => (v as num?)?.toInt()),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>)
          .map((e) => SituationMember.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    more: $checkedConvert('more', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$SituationEvidenceFamiliesInnerToJson(
  SituationEvidenceFamiliesInner instance,
) => <String, dynamic>{
  'family': instance.family,
  'count': instance.count,
  'coreCount': ?instance.coreCount,
  'members': instance.members.map((e) => e.toJson()).toList(),
  'more': ?instance.more,
};
