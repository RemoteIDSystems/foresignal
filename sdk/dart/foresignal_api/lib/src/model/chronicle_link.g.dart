// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleLinkCWProxy {
  ChronicleLink linkId(String linkId);

  ChronicleLink disclosure(ChronicleLinkDisclosureEnum disclosure);

  ChronicleLink a(String a);

  ChronicleLink b(String b);

  ChronicleLink other(ObjectEnvelope? other);

  ChronicleLink strength(ChronicleLinkStrengthEnum strength);

  ChronicleLink dimensions(List<ChronicleLinkDimensionsEnum> dimensions);

  ChronicleLink scores(ChronicleLinkScores? scores);

  ChronicleLink temporal(ChronicleLinkTemporalEnum temporal);

  ChronicleLink timeOverlap(ChronicleLinkTimeOverlapEnum? timeOverlap);

  ChronicleLink sharedSituations(List<String>? sharedSituations);

  ChronicleLink sharedCanonicalEvents(List<String>? sharedCanonicalEvents);

  ChronicleLink sharedEntities(List<String>? sharedEntities);

  ChronicleLink sharedKeyCount(int? sharedKeyCount);

  ChronicleLink status(ChronicleLinkStatusEnum status);

  ChronicleLink firstFound(DateTime firstFound);

  ChronicleLink lastUpdated(DateTime? lastUpdated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLink(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleLink call({
    String linkId,
    ChronicleLinkDisclosureEnum disclosure,
    String a,
    String b,
    ObjectEnvelope? other,
    ChronicleLinkStrengthEnum strength,
    List<ChronicleLinkDimensionsEnum> dimensions,
    ChronicleLinkScores? scores,
    ChronicleLinkTemporalEnum temporal,
    ChronicleLinkTimeOverlapEnum? timeOverlap,
    List<String>? sharedSituations,
    List<String>? sharedCanonicalEvents,
    List<String>? sharedEntities,
    int? sharedKeyCount,
    ChronicleLinkStatusEnum status,
    DateTime firstFound,
    DateTime? lastUpdated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleLink.copyWith(...)` or call `instanceOfChronicleLink.copyWith.fieldName(value)` for a single field.
class _$ChronicleLinkCWProxyImpl implements _$ChronicleLinkCWProxy {
  const _$ChronicleLinkCWProxyImpl(this._value);

  final ChronicleLink _value;

  @override
  ChronicleLink linkId(String linkId) => call(linkId: linkId);

  @override
  ChronicleLink disclosure(ChronicleLinkDisclosureEnum disclosure) =>
      call(disclosure: disclosure);

  @override
  ChronicleLink a(String a) => call(a: a);

  @override
  ChronicleLink b(String b) => call(b: b);

  @override
  ChronicleLink other(ObjectEnvelope? other) => call(other: other);

  @override
  ChronicleLink strength(ChronicleLinkStrengthEnum strength) =>
      call(strength: strength);

  @override
  ChronicleLink dimensions(List<ChronicleLinkDimensionsEnum> dimensions) =>
      call(dimensions: dimensions);

  @override
  ChronicleLink scores(ChronicleLinkScores? scores) => call(scores: scores);

  @override
  ChronicleLink temporal(ChronicleLinkTemporalEnum temporal) =>
      call(temporal: temporal);

  @override
  ChronicleLink timeOverlap(ChronicleLinkTimeOverlapEnum? timeOverlap) =>
      call(timeOverlap: timeOverlap);

  @override
  ChronicleLink sharedSituations(List<String>? sharedSituations) =>
      call(sharedSituations: sharedSituations);

  @override
  ChronicleLink sharedCanonicalEvents(List<String>? sharedCanonicalEvents) =>
      call(sharedCanonicalEvents: sharedCanonicalEvents);

  @override
  ChronicleLink sharedEntities(List<String>? sharedEntities) =>
      call(sharedEntities: sharedEntities);

  @override
  ChronicleLink sharedKeyCount(int? sharedKeyCount) =>
      call(sharedKeyCount: sharedKeyCount);

  @override
  ChronicleLink status(ChronicleLinkStatusEnum status) => call(status: status);

  @override
  ChronicleLink firstFound(DateTime firstFound) => call(firstFound: firstFound);

  @override
  ChronicleLink lastUpdated(DateTime? lastUpdated) =>
      call(lastUpdated: lastUpdated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleLink call({
    Object? linkId = const $CopyWithPlaceholder(),
    Object? disclosure = const $CopyWithPlaceholder(),
    Object? a = const $CopyWithPlaceholder(),
    Object? b = const $CopyWithPlaceholder(),
    Object? other = const $CopyWithPlaceholder(),
    Object? strength = const $CopyWithPlaceholder(),
    Object? dimensions = const $CopyWithPlaceholder(),
    Object? scores = const $CopyWithPlaceholder(),
    Object? temporal = const $CopyWithPlaceholder(),
    Object? timeOverlap = const $CopyWithPlaceholder(),
    Object? sharedSituations = const $CopyWithPlaceholder(),
    Object? sharedCanonicalEvents = const $CopyWithPlaceholder(),
    Object? sharedEntities = const $CopyWithPlaceholder(),
    Object? sharedKeyCount = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? firstFound = const $CopyWithPlaceholder(),
    Object? lastUpdated = const $CopyWithPlaceholder(),
  }) {
    return ChronicleLink(
      linkId: linkId == const $CopyWithPlaceholder() || linkId == null
          ? _value.linkId
          // ignore: cast_nullable_to_non_nullable
          : linkId as String,
      disclosure:
          disclosure == const $CopyWithPlaceholder() || disclosure == null
          ? _value.disclosure
          // ignore: cast_nullable_to_non_nullable
          : disclosure as ChronicleLinkDisclosureEnum,
      a: a == const $CopyWithPlaceholder() || a == null
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as String,
      b: b == const $CopyWithPlaceholder() || b == null
          ? _value.b
          // ignore: cast_nullable_to_non_nullable
          : b as String,
      other: other == const $CopyWithPlaceholder()
          ? _value.other
          // ignore: cast_nullable_to_non_nullable
          : other as ObjectEnvelope?,
      strength: strength == const $CopyWithPlaceholder() || strength == null
          ? _value.strength
          // ignore: cast_nullable_to_non_nullable
          : strength as ChronicleLinkStrengthEnum,
      dimensions:
          dimensions == const $CopyWithPlaceholder() || dimensions == null
          ? _value.dimensions
          // ignore: cast_nullable_to_non_nullable
          : dimensions as List<ChronicleLinkDimensionsEnum>,
      scores: scores == const $CopyWithPlaceholder()
          ? _value.scores
          // ignore: cast_nullable_to_non_nullable
          : scores as ChronicleLinkScores?,
      temporal: temporal == const $CopyWithPlaceholder() || temporal == null
          ? _value.temporal
          // ignore: cast_nullable_to_non_nullable
          : temporal as ChronicleLinkTemporalEnum,
      timeOverlap: timeOverlap == const $CopyWithPlaceholder()
          ? _value.timeOverlap
          // ignore: cast_nullable_to_non_nullable
          : timeOverlap as ChronicleLinkTimeOverlapEnum?,
      sharedSituations: sharedSituations == const $CopyWithPlaceholder()
          ? _value.sharedSituations
          // ignore: cast_nullable_to_non_nullable
          : sharedSituations as List<String>?,
      sharedCanonicalEvents:
          sharedCanonicalEvents == const $CopyWithPlaceholder()
          ? _value.sharedCanonicalEvents
          // ignore: cast_nullable_to_non_nullable
          : sharedCanonicalEvents as List<String>?,
      sharedEntities: sharedEntities == const $CopyWithPlaceholder()
          ? _value.sharedEntities
          // ignore: cast_nullable_to_non_nullable
          : sharedEntities as List<String>?,
      sharedKeyCount: sharedKeyCount == const $CopyWithPlaceholder()
          ? _value.sharedKeyCount
          // ignore: cast_nullable_to_non_nullable
          : sharedKeyCount as int?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ChronicleLinkStatusEnum,
      firstFound:
          firstFound == const $CopyWithPlaceholder() || firstFound == null
          ? _value.firstFound
          // ignore: cast_nullable_to_non_nullable
          : firstFound as DateTime,
      lastUpdated: lastUpdated == const $CopyWithPlaceholder()
          ? _value.lastUpdated
          // ignore: cast_nullable_to_non_nullable
          : lastUpdated as DateTime?,
    );
  }
}

extension $ChronicleLinkCopyWith on ChronicleLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleLink.copyWith(...)` or `instanceOfChronicleLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleLinkCWProxy get copyWith => _$ChronicleLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleLink _$ChronicleLinkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleLink', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'linkId',
          'disclosure',
          'a',
          'b',
          'strength',
          'dimensions',
          'temporal',
          'status',
          'firstFound',
        ],
      );
      final val = ChronicleLink(
        linkId: $checkedConvert('linkId', (v) => v as String),
        disclosure: $checkedConvert(
          'disclosure',
          (v) => $enumDecode(_$ChronicleLinkDisclosureEnumEnumMap, v),
        ),
        a: $checkedConvert('a', (v) => v as String),
        b: $checkedConvert('b', (v) => v as String),
        other: $checkedConvert(
          'other',
          (v) => v == null
              ? null
              : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        strength: $checkedConvert(
          'strength',
          (v) => $enumDecode(_$ChronicleLinkStrengthEnumEnumMap, v),
        ),
        dimensions: $checkedConvert(
          'dimensions',
          (v) => (v as List<dynamic>)
              .map((e) => $enumDecode(_$ChronicleLinkDimensionsEnumEnumMap, e))
              .toList(),
        ),
        scores: $checkedConvert(
          'scores',
          (v) => v == null
              ? null
              : ChronicleLinkScores.fromJson(v as Map<String, dynamic>),
        ),
        temporal: $checkedConvert(
          'temporal',
          (v) => $enumDecode(_$ChronicleLinkTemporalEnumEnumMap, v),
        ),
        timeOverlap: $checkedConvert(
          'timeOverlap',
          (v) => $enumDecodeNullable(_$ChronicleLinkTimeOverlapEnumEnumMap, v),
        ),
        sharedSituations: $checkedConvert(
          'sharedSituations',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        sharedCanonicalEvents: $checkedConvert(
          'sharedCanonicalEvents',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        sharedEntities: $checkedConvert(
          'sharedEntities',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        sharedKeyCount: $checkedConvert(
          'sharedKeyCount',
          (v) => (v as num?)?.toInt(),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ChronicleLinkStatusEnumEnumMap, v),
        ),
        firstFound: $checkedConvert(
          'firstFound',
          (v) => DateTime.parse(v as String),
        ),
        lastUpdated: $checkedConvert(
          'lastUpdated',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleLinkToJson(
  ChronicleLink instance,
) => <String, dynamic>{
  'linkId': instance.linkId,
  'disclosure': _$ChronicleLinkDisclosureEnumEnumMap[instance.disclosure]!,
  'a': instance.a,
  'b': instance.b,
  'other': ?instance.other?.toJson(),
  'strength': _$ChronicleLinkStrengthEnumEnumMap[instance.strength]!,
  'dimensions': instance.dimensions
      .map((e) => _$ChronicleLinkDimensionsEnumEnumMap[e]!)
      .toList(),
  'scores': ?instance.scores?.toJson(),
  'temporal': _$ChronicleLinkTemporalEnumEnumMap[instance.temporal]!,
  'timeOverlap': ?_$ChronicleLinkTimeOverlapEnumEnumMap[instance.timeOverlap],
  'sharedSituations': ?instance.sharedSituations,
  'sharedCanonicalEvents': ?instance.sharedCanonicalEvents,
  'sharedEntities': ?instance.sharedEntities,
  'sharedKeyCount': ?instance.sharedKeyCount,
  'status': _$ChronicleLinkStatusEnumEnumMap[instance.status]!,
  'firstFound': instance.firstFound.toIso8601String(),
  'lastUpdated': ?instance.lastUpdated?.toIso8601String(),
};

const _$ChronicleLinkDisclosureEnumEnumMap = {
  ChronicleLinkDisclosureEnum.full: 'full',
};

const _$ChronicleLinkStrengthEnumEnumMap = {
  ChronicleLinkStrengthEnum.low: 'low',
  ChronicleLinkStrengthEnum.medium: 'medium',
  ChronicleLinkStrengthEnum.high: 'high',
};

const _$ChronicleLinkDimensionsEnumEnumMap = {
  ChronicleLinkDimensionsEnum.evidence: 'evidence',
  ChronicleLinkDimensionsEnum.entities: 'entities',
  ChronicleLinkDimensionsEnum.areaTime: 'area-time',
  ChronicleLinkDimensionsEnum.topics: 'topics',
};

const _$ChronicleLinkTemporalEnumEnumMap = {
  ChronicleLinkTemporalEnum.aPrecedesB: 'a-precedes-b',
  ChronicleLinkTemporalEnum.bPrecedesA: 'b-precedes-a',
  ChronicleLinkTemporalEnum.concurrent: 'concurrent',
};

const _$ChronicleLinkTimeOverlapEnumEnumMap = {
  ChronicleLinkTimeOverlapEnum.none: 'none',
  ChronicleLinkTimeOverlapEnum.partial: 'partial',
  ChronicleLinkTimeOverlapEnum.full: 'full',
};

const _$ChronicleLinkStatusEnumEnumMap = {
  ChronicleLinkStatusEnum.active: 'active',
  ChronicleLinkStatusEnum.dismissed: 'dismissed',
};
