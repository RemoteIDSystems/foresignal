// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationEvidenceCWProxy {
  SituationEvidence memberCounts(SituationEvidenceMemberCounts memberCounts);

  SituationEvidence families(List<SituationEvidenceFamiliesInner> families);

  SituationEvidence corroboration(SituationEvidenceCorroboration corroboration);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationEvidence call({
    SituationEvidenceMemberCounts memberCounts,
    List<SituationEvidenceFamiliesInner> families,
    SituationEvidenceCorroboration corroboration,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationEvidence.copyWith(...)` or call `instanceOfSituationEvidence.copyWith.fieldName(value)` for a single field.
class _$SituationEvidenceCWProxyImpl implements _$SituationEvidenceCWProxy {
  const _$SituationEvidenceCWProxyImpl(this._value);

  final SituationEvidence _value;

  @override
  SituationEvidence memberCounts(SituationEvidenceMemberCounts memberCounts) =>
      call(memberCounts: memberCounts);

  @override
  SituationEvidence families(List<SituationEvidenceFamiliesInner> families) =>
      call(families: families);

  @override
  SituationEvidence corroboration(
    SituationEvidenceCorroboration corroboration,
  ) => call(corroboration: corroboration);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationEvidence call({
    Object? memberCounts = const $CopyWithPlaceholder(),
    Object? families = const $CopyWithPlaceholder(),
    Object? corroboration = const $CopyWithPlaceholder(),
  }) {
    return SituationEvidence(
      memberCounts:
          memberCounts == const $CopyWithPlaceholder() || memberCounts == null
          ? _value.memberCounts
          // ignore: cast_nullable_to_non_nullable
          : memberCounts as SituationEvidenceMemberCounts,
      families: families == const $CopyWithPlaceholder() || families == null
          ? _value.families
          // ignore: cast_nullable_to_non_nullable
          : families as List<SituationEvidenceFamiliesInner>,
      corroboration:
          corroboration == const $CopyWithPlaceholder() || corroboration == null
          ? _value.corroboration
          // ignore: cast_nullable_to_non_nullable
          : corroboration as SituationEvidenceCorroboration,
    );
  }
}

extension $SituationEvidenceCopyWith on SituationEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationEvidence.copyWith(...)` or `instanceOfSituationEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationEvidenceCWProxy get copyWith =>
      _$SituationEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationEvidence _$SituationEvidenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationEvidence', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['memberCounts', 'families', 'corroboration'],
  );
  final val = SituationEvidence(
    memberCounts: $checkedConvert(
      'memberCounts',
      (v) => SituationEvidenceMemberCounts.fromJson(v as Map<String, dynamic>),
    ),
    families: $checkedConvert(
      'families',
      (v) => (v as List<dynamic>)
          .map(
            (e) => SituationEvidenceFamiliesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    corroboration: $checkedConvert(
      'corroboration',
      (v) => SituationEvidenceCorroboration.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationEvidenceToJson(SituationEvidence instance) =>
    <String, dynamic>{
      'memberCounts': instance.memberCounts.toJson(),
      'families': instance.families.map((e) => e.toJson()).toList(),
      'corroboration': instance.corroboration.toJson(),
    };
