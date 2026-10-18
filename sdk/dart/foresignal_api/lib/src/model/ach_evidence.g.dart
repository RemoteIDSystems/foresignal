// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ach_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AchEvidenceCWProxy {
  AchEvidence id(String id);

  AchEvidence setId(String setId);

  AchEvidence source_(AchEvidenceSource_Enum source_);

  AchEvidence ref(String? ref);

  AchEvidence evidenceGroupId(String? evidenceGroupId);

  AchEvidence description(String description);

  AchEvidence credibility(AchEvidenceCredibilityEnum credibility);

  AchEvidence relevance(AchEvidenceRelevanceEnum relevance);

  AchEvidence weight(num? weight);

  AchEvidence ratings(Map<String, AchEvidenceRatingsEnum> ratings);

  AchEvidence nonDiagnostic(bool? nonDiagnostic);

  AchEvidence addedBy(String addedBy);

  AchEvidence addedAt(DateTime addedAt);

  AchEvidence removedAt(DateTime? removedAt);

  AchEvidence evidenceItemId(String? evidenceItemId);

  AchEvidence provenanceChain(List<ProvenanceChainEntry>? provenanceChain);

  AchEvidence refs(List<String>? refs);

  AchEvidence caveats(List<AchEvidenceCaveatsEnum>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  AchEvidence call({
    String id,
    String setId,
    AchEvidenceSource_Enum source_,
    String? ref,
    String? evidenceGroupId,
    String description,
    AchEvidenceCredibilityEnum credibility,
    AchEvidenceRelevanceEnum relevance,
    num? weight,
    Map<String, AchEvidenceRatingsEnum> ratings,
    bool? nonDiagnostic,
    String addedBy,
    DateTime addedAt,
    DateTime? removedAt,
    String? evidenceItemId,
    List<ProvenanceChainEntry>? provenanceChain,
    List<String>? refs,
    List<AchEvidenceCaveatsEnum>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAchEvidence.copyWith(...)` or call `instanceOfAchEvidence.copyWith.fieldName(value)` for a single field.
class _$AchEvidenceCWProxyImpl implements _$AchEvidenceCWProxy {
  const _$AchEvidenceCWProxyImpl(this._value);

  final AchEvidence _value;

  @override
  AchEvidence id(String id) => call(id: id);

  @override
  AchEvidence setId(String setId) => call(setId: setId);

  @override
  AchEvidence source_(AchEvidenceSource_Enum source_) => call(source_: source_);

  @override
  AchEvidence ref(String? ref) => call(ref: ref);

  @override
  AchEvidence evidenceGroupId(String? evidenceGroupId) =>
      call(evidenceGroupId: evidenceGroupId);

  @override
  AchEvidence description(String description) => call(description: description);

  @override
  AchEvidence credibility(AchEvidenceCredibilityEnum credibility) =>
      call(credibility: credibility);

  @override
  AchEvidence relevance(AchEvidenceRelevanceEnum relevance) =>
      call(relevance: relevance);

  @override
  AchEvidence weight(num? weight) => call(weight: weight);

  @override
  AchEvidence ratings(Map<String, AchEvidenceRatingsEnum> ratings) =>
      call(ratings: ratings);

  @override
  AchEvidence nonDiagnostic(bool? nonDiagnostic) =>
      call(nonDiagnostic: nonDiagnostic);

  @override
  AchEvidence addedBy(String addedBy) => call(addedBy: addedBy);

  @override
  AchEvidence addedAt(DateTime addedAt) => call(addedAt: addedAt);

  @override
  AchEvidence removedAt(DateTime? removedAt) => call(removedAt: removedAt);

  @override
  AchEvidence evidenceItemId(String? evidenceItemId) =>
      call(evidenceItemId: evidenceItemId);

  @override
  AchEvidence provenanceChain(List<ProvenanceChainEntry>? provenanceChain) =>
      call(provenanceChain: provenanceChain);

  @override
  AchEvidence refs(List<String>? refs) => call(refs: refs);

  @override
  AchEvidence caveats(List<AchEvidenceCaveatsEnum>? caveats) =>
      call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AchEvidence call({
    Object? id = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? evidenceGroupId = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? relevance = const $CopyWithPlaceholder(),
    Object? weight = const $CopyWithPlaceholder(),
    Object? ratings = const $CopyWithPlaceholder(),
    Object? nonDiagnostic = const $CopyWithPlaceholder(),
    Object? addedBy = const $CopyWithPlaceholder(),
    Object? addedAt = const $CopyWithPlaceholder(),
    Object? removedAt = const $CopyWithPlaceholder(),
    Object? evidenceItemId = const $CopyWithPlaceholder(),
    Object? provenanceChain = const $CopyWithPlaceholder(),
    Object? refs = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return AchEvidence(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as AchEvidenceSource_Enum,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      evidenceGroupId: evidenceGroupId == const $CopyWithPlaceholder()
          ? _value.evidenceGroupId
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupId as String?,
      description:
          description == const $CopyWithPlaceholder() || description == null
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String,
      credibility:
          credibility == const $CopyWithPlaceholder() || credibility == null
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as AchEvidenceCredibilityEnum,
      relevance: relevance == const $CopyWithPlaceholder() || relevance == null
          ? _value.relevance
          // ignore: cast_nullable_to_non_nullable
          : relevance as AchEvidenceRelevanceEnum,
      weight: weight == const $CopyWithPlaceholder()
          ? _value.weight
          // ignore: cast_nullable_to_non_nullable
          : weight as num?,
      ratings: ratings == const $CopyWithPlaceholder() || ratings == null
          ? _value.ratings
          // ignore: cast_nullable_to_non_nullable
          : ratings as Map<String, AchEvidenceRatingsEnum>,
      nonDiagnostic: nonDiagnostic == const $CopyWithPlaceholder()
          ? _value.nonDiagnostic
          // ignore: cast_nullable_to_non_nullable
          : nonDiagnostic as bool?,
      addedBy: addedBy == const $CopyWithPlaceholder() || addedBy == null
          ? _value.addedBy
          // ignore: cast_nullable_to_non_nullable
          : addedBy as String,
      addedAt: addedAt == const $CopyWithPlaceholder() || addedAt == null
          ? _value.addedAt
          // ignore: cast_nullable_to_non_nullable
          : addedAt as DateTime,
      removedAt: removedAt == const $CopyWithPlaceholder()
          ? _value.removedAt
          // ignore: cast_nullable_to_non_nullable
          : removedAt as DateTime?,
      evidenceItemId: evidenceItemId == const $CopyWithPlaceholder()
          ? _value.evidenceItemId
          // ignore: cast_nullable_to_non_nullable
          : evidenceItemId as String?,
      provenanceChain: provenanceChain == const $CopyWithPlaceholder()
          ? _value.provenanceChain
          // ignore: cast_nullable_to_non_nullable
          : provenanceChain as List<ProvenanceChainEntry>?,
      refs: refs == const $CopyWithPlaceholder()
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<AchEvidenceCaveatsEnum>?,
    );
  }
}

extension $AchEvidenceCopyWith on AchEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAchEvidence.copyWith(...)` or `instanceOfAchEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AchEvidenceCWProxy get copyWith => _$AchEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AchEvidence _$AchEvidenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AchEvidence', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'setId',
      'source',
      'description',
      'credibility',
      'relevance',
      'ratings',
      'addedBy',
      'addedAt',
    ],
  );
  final val = AchEvidence(
    id: $checkedConvert('id', (v) => v as String),
    setId: $checkedConvert('setId', (v) => v as String),
    source_: $checkedConvert(
      'source',
      (v) => $enumDecode(_$AchEvidenceSource_EnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String?),
    evidenceGroupId: $checkedConvert('evidenceGroupId', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String),
    credibility: $checkedConvert(
      'credibility',
      (v) => $enumDecode(_$AchEvidenceCredibilityEnumEnumMap, v),
    ),
    relevance: $checkedConvert(
      'relevance',
      (v) => $enumDecode(_$AchEvidenceRelevanceEnumEnumMap, v),
    ),
    weight: $checkedConvert('weight', (v) => v as num?),
    ratings: $checkedConvert(
      'ratings',
      (v) => (v as Map<String, dynamic>).map(
        (k, e) => MapEntry(k, $enumDecode(_$AchEvidenceRatingsEnumEnumMap, e)),
      ),
    ),
    nonDiagnostic: $checkedConvert('nonDiagnostic', (v) => v as bool?),
    addedBy: $checkedConvert('addedBy', (v) => v as String),
    addedAt: $checkedConvert('addedAt', (v) => DateTime.parse(v as String)),
    removedAt: $checkedConvert(
      'removedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    evidenceItemId: $checkedConvert('evidenceItemId', (v) => v as String?),
    provenanceChain: $checkedConvert(
      'provenanceChain',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ProvenanceChainEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$AchEvidenceCaveatsEnumEnumMap, e))
          .toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$AchEvidenceToJson(
  AchEvidence instance,
) => <String, dynamic>{
  'id': instance.id,
  'setId': instance.setId,
  'source': _$AchEvidenceSource_EnumEnumMap[instance.source_]!,
  'ref': ?instance.ref,
  'evidenceGroupId': ?instance.evidenceGroupId,
  'description': instance.description,
  'credibility': _$AchEvidenceCredibilityEnumEnumMap[instance.credibility]!,
  'relevance': _$AchEvidenceRelevanceEnumEnumMap[instance.relevance]!,
  'weight': ?instance.weight,
  'ratings': instance.ratings.map(
    (k, e) => MapEntry(k, _$AchEvidenceRatingsEnumEnumMap[e]!),
  ),
  'nonDiagnostic': ?instance.nonDiagnostic,
  'addedBy': instance.addedBy,
  'addedAt': instance.addedAt.toIso8601String(),
  'removedAt': ?instance.removedAt?.toIso8601String(),
  'evidenceItemId': ?instance.evidenceItemId,
  'provenanceChain': ?instance.provenanceChain?.map((e) => e.toJson()).toList(),
  'refs': ?instance.refs,
  'caveats': ?instance.caveats
      ?.map((e) => _$AchEvidenceCaveatsEnumEnumMap[e]!)
      .toList(),
};

const _$AchEvidenceSource_EnumEnumMap = {
  AchEvidenceSource_Enum.indicator: 'indicator',
  AchEvidenceSource_Enum.record: 'record',
  AchEvidenceSource_Enum.finding: 'finding',
  AchEvidenceSource_Enum.intelEvent: 'intel_event',
  AchEvidenceSource_Enum.alert: 'alert',
  AchEvidenceSource_Enum.manual: 'manual',
  AchEvidenceSource_Enum.evidenceItem: 'evidence-item',
  AchEvidenceSource_Enum.socialClaim: 'social-claim',
};

const _$AchEvidenceCredibilityEnumEnumMap = {
  AchEvidenceCredibilityEnum.H: 'H',
  AchEvidenceCredibilityEnum.M: 'M',
  AchEvidenceCredibilityEnum.L: 'L',
};

const _$AchEvidenceRelevanceEnumEnumMap = {
  AchEvidenceRelevanceEnum.H: 'H',
  AchEvidenceRelevanceEnum.M: 'M',
  AchEvidenceRelevanceEnum.L: 'L',
};

const _$AchEvidenceRatingsEnumEnumMap = {
  AchEvidenceRatingsEnum.CC: 'CC',
  AchEvidenceRatingsEnum.C: 'C',
  AchEvidenceRatingsEnum.N: 'N',
  AchEvidenceRatingsEnum.NA: 'NA',
  AchEvidenceRatingsEnum.I: 'I',
  AchEvidenceRatingsEnum.II: 'II',
};

const _$AchEvidenceCaveatsEnumEnumMap = {
  AchEvidenceCaveatsEnum.CIRCULAR_REPORTING: 'CIRCULAR_REPORTING',
};
