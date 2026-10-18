// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'claim.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClaimCWProxy {
  Claim id(String id);

  Claim ref(String ref);

  Claim narrativeId(String narrativeId);

  Claim type(String type);

  Claim signalFamily(String signalFamily);

  Claim polarity(ClaimPolarityEnum polarity);

  Claim hedged(bool hedged);

  Claim place(ClaimPlace? place);

  Claim entityRefs(List<String>? entityRefs);

  Claim eventTime(ClaimEventTime? eventTime);

  Claim actor(String? actor);

  Claim supportCount(int supportCount);

  Claim evidenceGroupCount(int evidenceGroupCount);

  Claim firstObservedAt(DateTime firstObservedAt);

  Claim extraction(ClaimExtractionEnum extraction);

  Claim manifestId(String manifestId);

  Claim confidence(num confidence);

  Claim status(ClaimStatusEnum status);

  Claim comparisonId(String? comparisonId);

  Claim sourceReliability(ClaimSourceReliabilityEnum sourceReliability);

  Claim informationCredibility(int informationCredibility);

  Claim grade(String grade);

  Claim tlp(Tlp? tlp);

  Claim createdAt(DateTime? createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Claim(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Claim(...).copyWith(id: 12, name: "My name")
  /// ```
  Claim call({
    String id,
    String ref,
    String narrativeId,
    String type,
    String signalFamily,
    ClaimPolarityEnum polarity,
    bool hedged,
    ClaimPlace? place,
    List<String>? entityRefs,
    ClaimEventTime? eventTime,
    String? actor,
    int supportCount,
    int evidenceGroupCount,
    DateTime firstObservedAt,
    ClaimExtractionEnum extraction,
    String manifestId,
    num confidence,
    ClaimStatusEnum status,
    String? comparisonId,
    ClaimSourceReliabilityEnum sourceReliability,
    int informationCredibility,
    String grade,
    Tlp? tlp,
    DateTime? createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfClaim.copyWith(...)` or call `instanceOfClaim.copyWith.fieldName(value)` for a single field.
class _$ClaimCWProxyImpl implements _$ClaimCWProxy {
  const _$ClaimCWProxyImpl(this._value);

  final Claim _value;

  @override
  Claim id(String id) => call(id: id);

  @override
  Claim ref(String ref) => call(ref: ref);

  @override
  Claim narrativeId(String narrativeId) => call(narrativeId: narrativeId);

  @override
  Claim type(String type) => call(type: type);

  @override
  Claim signalFamily(String signalFamily) => call(signalFamily: signalFamily);

  @override
  Claim polarity(ClaimPolarityEnum polarity) => call(polarity: polarity);

  @override
  Claim hedged(bool hedged) => call(hedged: hedged);

  @override
  Claim place(ClaimPlace? place) => call(place: place);

  @override
  Claim entityRefs(List<String>? entityRefs) => call(entityRefs: entityRefs);

  @override
  Claim eventTime(ClaimEventTime? eventTime) => call(eventTime: eventTime);

  @override
  Claim actor(String? actor) => call(actor: actor);

  @override
  Claim supportCount(int supportCount) => call(supportCount: supportCount);

  @override
  Claim evidenceGroupCount(int evidenceGroupCount) =>
      call(evidenceGroupCount: evidenceGroupCount);

  @override
  Claim firstObservedAt(DateTime firstObservedAt) =>
      call(firstObservedAt: firstObservedAt);

  @override
  Claim extraction(ClaimExtractionEnum extraction) =>
      call(extraction: extraction);

  @override
  Claim manifestId(String manifestId) => call(manifestId: manifestId);

  @override
  Claim confidence(num confidence) => call(confidence: confidence);

  @override
  Claim status(ClaimStatusEnum status) => call(status: status);

  @override
  Claim comparisonId(String? comparisonId) => call(comparisonId: comparisonId);

  @override
  Claim sourceReliability(ClaimSourceReliabilityEnum sourceReliability) =>
      call(sourceReliability: sourceReliability);

  @override
  Claim informationCredibility(int informationCredibility) =>
      call(informationCredibility: informationCredibility);

  @override
  Claim grade(String grade) => call(grade: grade);

  @override
  Claim tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  Claim createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Claim(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Claim(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Claim call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? polarity = const $CopyWithPlaceholder(),
    Object? hedged = const $CopyWithPlaceholder(),
    Object? place = const $CopyWithPlaceholder(),
    Object? entityRefs = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? actor = const $CopyWithPlaceholder(),
    Object? supportCount = const $CopyWithPlaceholder(),
    Object? evidenceGroupCount = const $CopyWithPlaceholder(),
    Object? firstObservedAt = const $CopyWithPlaceholder(),
    Object? extraction = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? comparisonId = const $CopyWithPlaceholder(),
    Object? sourceReliability = const $CopyWithPlaceholder(),
    Object? informationCredibility = const $CopyWithPlaceholder(),
    Object? grade = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return Claim(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      signalFamily:
          signalFamily == const $CopyWithPlaceholder() || signalFamily == null
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String,
      polarity: polarity == const $CopyWithPlaceholder() || polarity == null
          ? _value.polarity
          // ignore: cast_nullable_to_non_nullable
          : polarity as ClaimPolarityEnum,
      hedged: hedged == const $CopyWithPlaceholder() || hedged == null
          ? _value.hedged
          // ignore: cast_nullable_to_non_nullable
          : hedged as bool,
      place: place == const $CopyWithPlaceholder()
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as ClaimPlace?,
      entityRefs: entityRefs == const $CopyWithPlaceholder()
          ? _value.entityRefs
          // ignore: cast_nullable_to_non_nullable
          : entityRefs as List<String>?,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as ClaimEventTime?,
      actor: actor == const $CopyWithPlaceholder()
          ? _value.actor
          // ignore: cast_nullable_to_non_nullable
          : actor as String?,
      supportCount:
          supportCount == const $CopyWithPlaceholder() || supportCount == null
          ? _value.supportCount
          // ignore: cast_nullable_to_non_nullable
          : supportCount as int,
      evidenceGroupCount:
          evidenceGroupCount == const $CopyWithPlaceholder() ||
              evidenceGroupCount == null
          ? _value.evidenceGroupCount
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupCount as int,
      firstObservedAt:
          firstObservedAt == const $CopyWithPlaceholder() ||
              firstObservedAt == null
          ? _value.firstObservedAt
          // ignore: cast_nullable_to_non_nullable
          : firstObservedAt as DateTime,
      extraction:
          extraction == const $CopyWithPlaceholder() || extraction == null
          ? _value.extraction
          // ignore: cast_nullable_to_non_nullable
          : extraction as ClaimExtractionEnum,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ClaimStatusEnum,
      comparisonId: comparisonId == const $CopyWithPlaceholder()
          ? _value.comparisonId
          // ignore: cast_nullable_to_non_nullable
          : comparisonId as String?,
      sourceReliability:
          sourceReliability == const $CopyWithPlaceholder() ||
              sourceReliability == null
          ? _value.sourceReliability
          // ignore: cast_nullable_to_non_nullable
          : sourceReliability as ClaimSourceReliabilityEnum,
      informationCredibility:
          informationCredibility == const $CopyWithPlaceholder() ||
              informationCredibility == null
          ? _value.informationCredibility
          // ignore: cast_nullable_to_non_nullable
          : informationCredibility as int,
      grade: grade == const $CopyWithPlaceholder() || grade == null
          ? _value.grade
          // ignore: cast_nullable_to_non_nullable
          : grade as String,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
    );
  }
}

extension $ClaimCopyWith on Claim {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfClaim.copyWith(...)` or `instanceOfClaim.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClaimCWProxy get copyWith => _$ClaimCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Claim _$ClaimFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Claim', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'ref',
      'narrativeId',
      'type',
      'signalFamily',
      'polarity',
      'hedged',
      'supportCount',
      'evidenceGroupCount',
      'firstObservedAt',
      'extraction',
      'manifestId',
      'confidence',
      'status',
      'sourceReliability',
      'informationCredibility',
      'grade',
    ],
  );
  final val = Claim(
    id: $checkedConvert('id', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String),
    narrativeId: $checkedConvert('narrativeId', (v) => v as String),
    type: $checkedConvert('type', (v) => v as String),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String),
    polarity: $checkedConvert(
      'polarity',
      (v) => $enumDecode(_$ClaimPolarityEnumEnumMap, v),
    ),
    hedged: $checkedConvert('hedged', (v) => v as bool),
    place: $checkedConvert(
      'place',
      (v) => v == null ? null : ClaimPlace.fromJson(v as Map<String, dynamic>),
    ),
    entityRefs: $checkedConvert(
      'entityRefs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    eventTime: $checkedConvert(
      'eventTime',
      (v) =>
          v == null ? null : ClaimEventTime.fromJson(v as Map<String, dynamic>),
    ),
    actor: $checkedConvert('actor', (v) => v as String?),
    supportCount: $checkedConvert('supportCount', (v) => (v as num).toInt()),
    evidenceGroupCount: $checkedConvert(
      'evidenceGroupCount',
      (v) => (v as num).toInt(),
    ),
    firstObservedAt: $checkedConvert(
      'firstObservedAt',
      (v) => DateTime.parse(v as String),
    ),
    extraction: $checkedConvert(
      'extraction',
      (v) => $enumDecode(_$ClaimExtractionEnumEnumMap, v),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String),
    confidence: $checkedConvert('confidence', (v) => v as num),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ClaimStatusEnumEnumMap, v),
    ),
    comparisonId: $checkedConvert('comparisonId', (v) => v as String?),
    sourceReliability: $checkedConvert(
      'sourceReliability',
      (v) => $enumDecode(_$ClaimSourceReliabilityEnumEnumMap, v),
    ),
    informationCredibility: $checkedConvert(
      'informationCredibility',
      (v) => (v as num).toInt(),
    ),
    grade: $checkedConvert('grade', (v) => v as String),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ClaimToJson(Claim instance) => <String, dynamic>{
  'id': instance.id,
  'ref': instance.ref,
  'narrativeId': instance.narrativeId,
  'type': instance.type,
  'signalFamily': instance.signalFamily,
  'polarity': _$ClaimPolarityEnumEnumMap[instance.polarity]!,
  'hedged': instance.hedged,
  'place': ?instance.place?.toJson(),
  'entityRefs': ?instance.entityRefs,
  'eventTime': ?instance.eventTime?.toJson(),
  'actor': ?instance.actor,
  'supportCount': instance.supportCount,
  'evidenceGroupCount': instance.evidenceGroupCount,
  'firstObservedAt': instance.firstObservedAt.toIso8601String(),
  'extraction': _$ClaimExtractionEnumEnumMap[instance.extraction]!,
  'manifestId': instance.manifestId,
  'confidence': instance.confidence,
  'status': _$ClaimStatusEnumEnumMap[instance.status]!,
  'comparisonId': ?instance.comparisonId,
  'sourceReliability':
      _$ClaimSourceReliabilityEnumEnumMap[instance.sourceReliability]!,
  'informationCredibility': instance.informationCredibility,
  'grade': instance.grade,
  'tlp': ?_$TlpEnumMap[instance.tlp],
  'createdAt': ?instance.createdAt?.toIso8601String(),
};

const _$ClaimPolarityEnumEnumMap = {
  ClaimPolarityEnum.asserted: 'asserted',
  ClaimPolarityEnum.denied: 'denied',
  ClaimPolarityEnum.questioned: 'questioned',
};

const _$ClaimExtractionEnumEnumMap = {
  ClaimExtractionEnum.deterministic: 'deterministic',
  ClaimExtractionEnum.llm: 'llm',
};

const _$ClaimStatusEnumEnumMap = {
  ClaimStatusEnum.unassessed: 'unassessed',
  ClaimStatusEnum.pending: 'pending',
  ClaimStatusEnum.corroborated: 'corroborated',
  ClaimStatusEnum.unobserved: 'unobserved',
  ClaimStatusEnum.contradicted: 'contradicted',
  ClaimStatusEnum.insufficientCoverage: 'insufficient-coverage',
};

const _$ClaimSourceReliabilityEnumEnumMap = {
  ClaimSourceReliabilityEnum.A: 'A',
  ClaimSourceReliabilityEnum.B: 'B',
  ClaimSourceReliabilityEnum.C: 'C',
  ClaimSourceReliabilityEnum.D: 'D',
  ClaimSourceReliabilityEnum.E: 'E',
  ClaimSourceReliabilityEnum.F: 'F',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
