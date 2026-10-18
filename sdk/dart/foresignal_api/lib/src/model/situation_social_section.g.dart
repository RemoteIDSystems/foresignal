// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_social_section.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationSocialSectionCWProxy {
  SituationSocialSection narratives(
    List<SituationSocialSectionNarrativesInner> narratives,
  );

  SituationSocialSection socialEvidenceGroups(int socialEvidenceGroups);

  SituationSocialSection attentionGap(
    SituationSocialSectionAttentionGap? attentionGap,
  );

  SituationSocialSection socialCoverageOk(bool socialCoverageOk);

  SituationSocialSection updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSocialSection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSocialSection(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationSocialSection call({
    List<SituationSocialSectionNarrativesInner> narratives,
    int socialEvidenceGroups,
    SituationSocialSectionAttentionGap? attentionGap,
    bool socialCoverageOk,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationSocialSection.copyWith(...)` or call `instanceOfSituationSocialSection.copyWith.fieldName(value)` for a single field.
class _$SituationSocialSectionCWProxyImpl
    implements _$SituationSocialSectionCWProxy {
  const _$SituationSocialSectionCWProxyImpl(this._value);

  final SituationSocialSection _value;

  @override
  SituationSocialSection narratives(
    List<SituationSocialSectionNarrativesInner> narratives,
  ) => call(narratives: narratives);

  @override
  SituationSocialSection socialEvidenceGroups(int socialEvidenceGroups) =>
      call(socialEvidenceGroups: socialEvidenceGroups);

  @override
  SituationSocialSection attentionGap(
    SituationSocialSectionAttentionGap? attentionGap,
  ) => call(attentionGap: attentionGap);

  @override
  SituationSocialSection socialCoverageOk(bool socialCoverageOk) =>
      call(socialCoverageOk: socialCoverageOk);

  @override
  SituationSocialSection updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationSocialSection(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationSocialSection(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationSocialSection call({
    Object? narratives = const $CopyWithPlaceholder(),
    Object? socialEvidenceGroups = const $CopyWithPlaceholder(),
    Object? attentionGap = const $CopyWithPlaceholder(),
    Object? socialCoverageOk = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return SituationSocialSection(
      narratives:
          narratives == const $CopyWithPlaceholder() || narratives == null
          ? _value.narratives
          // ignore: cast_nullable_to_non_nullable
          : narratives as List<SituationSocialSectionNarrativesInner>,
      socialEvidenceGroups:
          socialEvidenceGroups == const $CopyWithPlaceholder() ||
              socialEvidenceGroups == null
          ? _value.socialEvidenceGroups
          // ignore: cast_nullable_to_non_nullable
          : socialEvidenceGroups as int,
      attentionGap: attentionGap == const $CopyWithPlaceholder()
          ? _value.attentionGap
          // ignore: cast_nullable_to_non_nullable
          : attentionGap as SituationSocialSectionAttentionGap?,
      socialCoverageOk:
          socialCoverageOk == const $CopyWithPlaceholder() ||
              socialCoverageOk == null
          ? _value.socialCoverageOk
          // ignore: cast_nullable_to_non_nullable
          : socialCoverageOk as bool,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $SituationSocialSectionCopyWith on SituationSocialSection {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationSocialSection.copyWith(...)` or `instanceOfSituationSocialSection.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationSocialSectionCWProxy get copyWith =>
      _$SituationSocialSectionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationSocialSection _$SituationSocialSectionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SituationSocialSection', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'narratives',
      'socialEvidenceGroups',
      'socialCoverageOk',
    ],
  );
  final val = SituationSocialSection(
    narratives: $checkedConvert(
      'narratives',
      (v) => (v as List<dynamic>)
          .map(
            (e) => SituationSocialSectionNarrativesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    socialEvidenceGroups: $checkedConvert(
      'socialEvidenceGroups',
      (v) => (v as num).toInt(),
    ),
    attentionGap: $checkedConvert(
      'attentionGap',
      (v) => v == null
          ? null
          : SituationSocialSectionAttentionGap.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    socialCoverageOk: $checkedConvert('socialCoverageOk', (v) => v as bool),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$SituationSocialSectionToJson(
  SituationSocialSection instance,
) => <String, dynamic>{
  'narratives': instance.narratives.map((e) => e.toJson()).toList(),
  'socialEvidenceGroups': instance.socialEvidenceGroups,
  'attentionGap': ?instance.attentionGap?.toJson(),
  'socialCoverageOk': instance.socialCoverageOk,
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};
