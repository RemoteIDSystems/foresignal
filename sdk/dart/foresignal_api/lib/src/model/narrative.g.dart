// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativeCWProxy {
  Narrative id(String id);

  Narrative ref(String ref);

  Narrative title(String title);

  Narrative canonicalEventId(String canonicalEventId);

  Narrative topics(List<String>? topics);

  Narrative placeRefs(List<String>? placeRefs);

  Narrative entityRefs(List<String>? entityRefs);

  Narrative regionCodes(List<String>? regionCodes);

  Narrative languages(List<String>? languages);

  Narrative platforms(List<NarrativePlatformStats> platforms);

  Narrative firstObservedAt(DateTime firstObservedAt);

  Narrative lastActivityAt(DateTime lastActivityAt);

  Narrative postCount(int postCount);

  Narrative uniqueAuthors(int uniqueAuthors);

  Narrative evidenceGroupCount(int evidenceGroupCount);

  Narrative originConcentration(num originConcentration);

  Narrative velocity(num? velocity);

  Narrative mediaFingerprints(List<String>? mediaFingerprints);

  Narrative newsStoryIds(List<String>? newsStoryIds);

  Narrative situationIds(List<String>? situationIds);

  Narrative status(NarrativeStatusEnum status);

  Narrative state(NarrativeStateEnum state);

  Narrative coordinationIndicatorsPresent(bool? coordinationIndicatorsPresent);

  Narrative mergedInto(String? mergedInto);

  Narrative tlp(Tlp tlp);

  Narrative envelope(ObjectEnvelope envelope);

  Narrative claims(List<Claim> claims);

  Narrative media(List<MediaFingerprint> media);

  Narrative coordination(CoordinationIndicators? coordination);

  Narrative newsStories(List<ObjectEnvelope>? newsStories);

  Narrative situations(List<ObjectEnvelope>? situations);

  Narrative comparisonIds(List<String>? comparisonIds);

  Narrative history(List<NarrativeLogEntry> history);

  Narrative clusterer(DetectorRef clusterer);

  Narrative manifestId(String manifestId);

  Narrative caveats(List<Caveat>? caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Narrative(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Narrative(...).copyWith(id: 12, name: "My name")
  /// ```
  Narrative call({
    String id,
    String ref,
    String title,
    String canonicalEventId,
    List<String>? topics,
    List<String>? placeRefs,
    List<String>? entityRefs,
    List<String>? regionCodes,
    List<String>? languages,
    List<NarrativePlatformStats> platforms,
    DateTime firstObservedAt,
    DateTime lastActivityAt,
    int postCount,
    int uniqueAuthors,
    int evidenceGroupCount,
    num originConcentration,
    num? velocity,
    List<String>? mediaFingerprints,
    List<String>? newsStoryIds,
    List<String>? situationIds,
    NarrativeStatusEnum status,
    NarrativeStateEnum state,
    bool? coordinationIndicatorsPresent,
    String? mergedInto,
    Tlp tlp,
    ObjectEnvelope envelope,
    List<Claim> claims,
    List<MediaFingerprint> media,
    CoordinationIndicators? coordination,
    List<ObjectEnvelope>? newsStories,
    List<ObjectEnvelope>? situations,
    List<String>? comparisonIds,
    List<NarrativeLogEntry> history,
    DetectorRef clusterer,
    String manifestId,
    List<Caveat>? caveats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrative.copyWith(...)` or call `instanceOfNarrative.copyWith.fieldName(value)` for a single field.
class _$NarrativeCWProxyImpl implements _$NarrativeCWProxy {
  const _$NarrativeCWProxyImpl(this._value);

  final Narrative _value;

  @override
  Narrative id(String id) => call(id: id);

  @override
  Narrative ref(String ref) => call(ref: ref);

  @override
  Narrative title(String title) => call(title: title);

  @override
  Narrative canonicalEventId(String canonicalEventId) =>
      call(canonicalEventId: canonicalEventId);

  @override
  Narrative topics(List<String>? topics) => call(topics: topics);

  @override
  Narrative placeRefs(List<String>? placeRefs) => call(placeRefs: placeRefs);

  @override
  Narrative entityRefs(List<String>? entityRefs) =>
      call(entityRefs: entityRefs);

  @override
  Narrative regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  Narrative languages(List<String>? languages) => call(languages: languages);

  @override
  Narrative platforms(List<NarrativePlatformStats> platforms) =>
      call(platforms: platforms);

  @override
  Narrative firstObservedAt(DateTime firstObservedAt) =>
      call(firstObservedAt: firstObservedAt);

  @override
  Narrative lastActivityAt(DateTime lastActivityAt) =>
      call(lastActivityAt: lastActivityAt);

  @override
  Narrative postCount(int postCount) => call(postCount: postCount);

  @override
  Narrative uniqueAuthors(int uniqueAuthors) =>
      call(uniqueAuthors: uniqueAuthors);

  @override
  Narrative evidenceGroupCount(int evidenceGroupCount) =>
      call(evidenceGroupCount: evidenceGroupCount);

  @override
  Narrative originConcentration(num originConcentration) =>
      call(originConcentration: originConcentration);

  @override
  Narrative velocity(num? velocity) => call(velocity: velocity);

  @override
  Narrative mediaFingerprints(List<String>? mediaFingerprints) =>
      call(mediaFingerprints: mediaFingerprints);

  @override
  Narrative newsStoryIds(List<String>? newsStoryIds) =>
      call(newsStoryIds: newsStoryIds);

  @override
  Narrative situationIds(List<String>? situationIds) =>
      call(situationIds: situationIds);

  @override
  Narrative status(NarrativeStatusEnum status) => call(status: status);

  @override
  Narrative state(NarrativeStateEnum state) => call(state: state);

  @override
  Narrative coordinationIndicatorsPresent(
    bool? coordinationIndicatorsPresent,
  ) => call(coordinationIndicatorsPresent: coordinationIndicatorsPresent);

  @override
  Narrative mergedInto(String? mergedInto) => call(mergedInto: mergedInto);

  @override
  Narrative tlp(Tlp tlp) => call(tlp: tlp);

  @override
  Narrative envelope(ObjectEnvelope envelope) => call(envelope: envelope);

  @override
  Narrative claims(List<Claim> claims) => call(claims: claims);

  @override
  Narrative media(List<MediaFingerprint> media) => call(media: media);

  @override
  Narrative coordination(CoordinationIndicators? coordination) =>
      call(coordination: coordination);

  @override
  Narrative newsStories(List<ObjectEnvelope>? newsStories) =>
      call(newsStories: newsStories);

  @override
  Narrative situations(List<ObjectEnvelope>? situations) =>
      call(situations: situations);

  @override
  Narrative comparisonIds(List<String>? comparisonIds) =>
      call(comparisonIds: comparisonIds);

  @override
  Narrative history(List<NarrativeLogEntry> history) => call(history: history);

  @override
  Narrative clusterer(DetectorRef clusterer) => call(clusterer: clusterer);

  @override
  Narrative manifestId(String manifestId) => call(manifestId: manifestId);

  @override
  Narrative caveats(List<Caveat>? caveats) => call(caveats: caveats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Narrative(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Narrative(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Narrative call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? canonicalEventId = const $CopyWithPlaceholder(),
    Object? topics = const $CopyWithPlaceholder(),
    Object? placeRefs = const $CopyWithPlaceholder(),
    Object? entityRefs = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? languages = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? firstObservedAt = const $CopyWithPlaceholder(),
    Object? lastActivityAt = const $CopyWithPlaceholder(),
    Object? postCount = const $CopyWithPlaceholder(),
    Object? uniqueAuthors = const $CopyWithPlaceholder(),
    Object? evidenceGroupCount = const $CopyWithPlaceholder(),
    Object? originConcentration = const $CopyWithPlaceholder(),
    Object? velocity = const $CopyWithPlaceholder(),
    Object? mediaFingerprints = const $CopyWithPlaceholder(),
    Object? newsStoryIds = const $CopyWithPlaceholder(),
    Object? situationIds = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? coordinationIndicatorsPresent = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? claims = const $CopyWithPlaceholder(),
    Object? media = const $CopyWithPlaceholder(),
    Object? coordination = const $CopyWithPlaceholder(),
    Object? newsStories = const $CopyWithPlaceholder(),
    Object? situations = const $CopyWithPlaceholder(),
    Object? comparisonIds = const $CopyWithPlaceholder(),
    Object? history = const $CopyWithPlaceholder(),
    Object? clusterer = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
  }) {
    return Narrative(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      canonicalEventId:
          canonicalEventId == const $CopyWithPlaceholder() ||
              canonicalEventId == null
          ? _value.canonicalEventId
          // ignore: cast_nullable_to_non_nullable
          : canonicalEventId as String,
      topics: topics == const $CopyWithPlaceholder()
          ? _value.topics
          // ignore: cast_nullable_to_non_nullable
          : topics as List<String>?,
      placeRefs: placeRefs == const $CopyWithPlaceholder()
          ? _value.placeRefs
          // ignore: cast_nullable_to_non_nullable
          : placeRefs as List<String>?,
      entityRefs: entityRefs == const $CopyWithPlaceholder()
          ? _value.entityRefs
          // ignore: cast_nullable_to_non_nullable
          : entityRefs as List<String>?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      languages: languages == const $CopyWithPlaceholder()
          ? _value.languages
          // ignore: cast_nullable_to_non_nullable
          : languages as List<String>?,
      platforms: platforms == const $CopyWithPlaceholder() || platforms == null
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<NarrativePlatformStats>,
      firstObservedAt:
          firstObservedAt == const $CopyWithPlaceholder() ||
              firstObservedAt == null
          ? _value.firstObservedAt
          // ignore: cast_nullable_to_non_nullable
          : firstObservedAt as DateTime,
      lastActivityAt:
          lastActivityAt == const $CopyWithPlaceholder() ||
              lastActivityAt == null
          ? _value.lastActivityAt
          // ignore: cast_nullable_to_non_nullable
          : lastActivityAt as DateTime,
      postCount: postCount == const $CopyWithPlaceholder() || postCount == null
          ? _value.postCount
          // ignore: cast_nullable_to_non_nullable
          : postCount as int,
      uniqueAuthors:
          uniqueAuthors == const $CopyWithPlaceholder() || uniqueAuthors == null
          ? _value.uniqueAuthors
          // ignore: cast_nullable_to_non_nullable
          : uniqueAuthors as int,
      evidenceGroupCount:
          evidenceGroupCount == const $CopyWithPlaceholder() ||
              evidenceGroupCount == null
          ? _value.evidenceGroupCount
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupCount as int,
      originConcentration:
          originConcentration == const $CopyWithPlaceholder() ||
              originConcentration == null
          ? _value.originConcentration
          // ignore: cast_nullable_to_non_nullable
          : originConcentration as num,
      velocity: velocity == const $CopyWithPlaceholder()
          ? _value.velocity
          // ignore: cast_nullable_to_non_nullable
          : velocity as num?,
      mediaFingerprints: mediaFingerprints == const $CopyWithPlaceholder()
          ? _value.mediaFingerprints
          // ignore: cast_nullable_to_non_nullable
          : mediaFingerprints as List<String>?,
      newsStoryIds: newsStoryIds == const $CopyWithPlaceholder()
          ? _value.newsStoryIds
          // ignore: cast_nullable_to_non_nullable
          : newsStoryIds as List<String>?,
      situationIds: situationIds == const $CopyWithPlaceholder()
          ? _value.situationIds
          // ignore: cast_nullable_to_non_nullable
          : situationIds as List<String>?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as NarrativeStatusEnum,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as NarrativeStateEnum,
      coordinationIndicatorsPresent:
          coordinationIndicatorsPresent == const $CopyWithPlaceholder()
          ? _value.coordinationIndicatorsPresent
          // ignore: cast_nullable_to_non_nullable
          : coordinationIndicatorsPresent as bool?,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      tlp: tlp == const $CopyWithPlaceholder() || tlp == null
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp,
      envelope: envelope == const $CopyWithPlaceholder() || envelope == null
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope,
      claims: claims == const $CopyWithPlaceholder() || claims == null
          ? _value.claims
          // ignore: cast_nullable_to_non_nullable
          : claims as List<Claim>,
      media: media == const $CopyWithPlaceholder() || media == null
          ? _value.media
          // ignore: cast_nullable_to_non_nullable
          : media as List<MediaFingerprint>,
      coordination: coordination == const $CopyWithPlaceholder()
          ? _value.coordination
          // ignore: cast_nullable_to_non_nullable
          : coordination as CoordinationIndicators?,
      newsStories: newsStories == const $CopyWithPlaceholder()
          ? _value.newsStories
          // ignore: cast_nullable_to_non_nullable
          : newsStories as List<ObjectEnvelope>?,
      situations: situations == const $CopyWithPlaceholder()
          ? _value.situations
          // ignore: cast_nullable_to_non_nullable
          : situations as List<ObjectEnvelope>?,
      comparisonIds: comparisonIds == const $CopyWithPlaceholder()
          ? _value.comparisonIds
          // ignore: cast_nullable_to_non_nullable
          : comparisonIds as List<String>?,
      history: history == const $CopyWithPlaceholder() || history == null
          ? _value.history
          // ignore: cast_nullable_to_non_nullable
          : history as List<NarrativeLogEntry>,
      clusterer: clusterer == const $CopyWithPlaceholder() || clusterer == null
          ? _value.clusterer
          // ignore: cast_nullable_to_non_nullable
          : clusterer as DetectorRef,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
    );
  }
}

extension $NarrativeCopyWith on Narrative {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrative.copyWith(...)` or `instanceOfNarrative.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativeCWProxy get copyWith => _$NarrativeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Narrative _$NarrativeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Narrative', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'ref',
      'title',
      'canonicalEventId',
      'platforms',
      'firstObservedAt',
      'lastActivityAt',
      'postCount',
      'uniqueAuthors',
      'evidenceGroupCount',
      'originConcentration',
      'status',
      'state',
      'tlp',
      'envelope',
      'claims',
      'media',
      'history',
      'clusterer',
      'manifestId',
    ],
  );
  final val = Narrative(
    id: $checkedConvert('id', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    canonicalEventId: $checkedConvert('canonicalEventId', (v) => v as String),
    topics: $checkedConvert(
      'topics',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    placeRefs: $checkedConvert(
      'placeRefs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    entityRefs: $checkedConvert(
      'entityRefs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    languages: $checkedConvert(
      'languages',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    platforms: $checkedConvert(
      'platforms',
      (v) => (v as List<dynamic>)
          .map(
            (e) => NarrativePlatformStats.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    firstObservedAt: $checkedConvert(
      'firstObservedAt',
      (v) => DateTime.parse(v as String),
    ),
    lastActivityAt: $checkedConvert(
      'lastActivityAt',
      (v) => DateTime.parse(v as String),
    ),
    postCount: $checkedConvert('postCount', (v) => (v as num).toInt()),
    uniqueAuthors: $checkedConvert('uniqueAuthors', (v) => (v as num).toInt()),
    evidenceGroupCount: $checkedConvert(
      'evidenceGroupCount',
      (v) => (v as num).toInt(),
    ),
    originConcentration: $checkedConvert(
      'originConcentration',
      (v) => v as num,
    ),
    velocity: $checkedConvert('velocity', (v) => v as num?),
    mediaFingerprints: $checkedConvert(
      'mediaFingerprints',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    newsStoryIds: $checkedConvert(
      'newsStoryIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    situationIds: $checkedConvert(
      'situationIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$NarrativeStatusEnumEnumMap, v),
    ),
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$NarrativeStateEnumEnumMap, v),
    ),
    coordinationIndicatorsPresent: $checkedConvert(
      'coordinationIndicatorsPresent',
      (v) => v as bool?,
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    tlp: $checkedConvert('tlp', (v) => $enumDecode(_$TlpEnumMap, v)),
    envelope: $checkedConvert(
      'envelope',
      (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
    ),
    claims: $checkedConvert(
      'claims',
      (v) => (v as List<dynamic>)
          .map((e) => Claim.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    media: $checkedConvert(
      'media',
      (v) => (v as List<dynamic>)
          .map((e) => MediaFingerprint.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    coordination: $checkedConvert(
      'coordination',
      (v) => v == null
          ? null
          : CoordinationIndicators.fromJson(v as Map<String, dynamic>),
    ),
    newsStories: $checkedConvert(
      'newsStories',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    situations: $checkedConvert(
      'situations',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    comparisonIds: $checkedConvert(
      'comparisonIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    history: $checkedConvert(
      'history',
      (v) => (v as List<dynamic>)
          .map((e) => NarrativeLogEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    clusterer: $checkedConvert(
      'clusterer',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NarrativeToJson(Narrative instance) => <String, dynamic>{
  'id': instance.id,
  'ref': instance.ref,
  'title': instance.title,
  'canonicalEventId': instance.canonicalEventId,
  'topics': ?instance.topics,
  'placeRefs': ?instance.placeRefs,
  'entityRefs': ?instance.entityRefs,
  'regionCodes': ?instance.regionCodes,
  'languages': ?instance.languages,
  'platforms': instance.platforms.map((e) => e.toJson()).toList(),
  'firstObservedAt': instance.firstObservedAt.toIso8601String(),
  'lastActivityAt': instance.lastActivityAt.toIso8601String(),
  'postCount': instance.postCount,
  'uniqueAuthors': instance.uniqueAuthors,
  'evidenceGroupCount': instance.evidenceGroupCount,
  'originConcentration': instance.originConcentration,
  'velocity': ?instance.velocity,
  'mediaFingerprints': ?instance.mediaFingerprints,
  'newsStoryIds': ?instance.newsStoryIds,
  'situationIds': ?instance.situationIds,
  'status': _$NarrativeStatusEnumEnumMap[instance.status]!,
  'state': _$NarrativeStateEnumEnumMap[instance.state]!,
  'coordinationIndicatorsPresent': ?instance.coordinationIndicatorsPresent,
  'mergedInto': ?instance.mergedInto,
  'tlp': _$TlpEnumMap[instance.tlp]!,
  'envelope': instance.envelope.toJson(),
  'claims': instance.claims.map((e) => e.toJson()).toList(),
  'media': instance.media.map((e) => e.toJson()).toList(),
  'coordination': ?instance.coordination?.toJson(),
  'newsStories': ?instance.newsStories?.map((e) => e.toJson()).toList(),
  'situations': ?instance.situations?.map((e) => e.toJson()).toList(),
  'comparisonIds': ?instance.comparisonIds,
  'history': instance.history.map((e) => e.toJson()).toList(),
  'clusterer': instance.clusterer.toJson(),
  'manifestId': instance.manifestId,
  'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
};

const _$NarrativeStatusEnumEnumMap = {
  NarrativeStatusEnum.unassessed: 'unassessed',
  NarrativeStatusEnum.pending: 'pending',
  NarrativeStatusEnum.corroborated: 'corroborated',
  NarrativeStatusEnum.unobserved: 'unobserved',
  NarrativeStatusEnum.contradicted: 'contradicted',
  NarrativeStatusEnum.mixed: 'mixed',
  NarrativeStatusEnum.insufficientCoverage: 'insufficient-coverage',
};

const _$NarrativeStateEnumEnumMap = {
  NarrativeStateEnum.emerging: 'emerging',
  NarrativeStateEnum.active: 'active',
  NarrativeStateEnum.fading: 'fading',
  NarrativeStateEnum.dormant: 'dormant',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
