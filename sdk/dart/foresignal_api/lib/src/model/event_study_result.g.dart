// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyResultCWProxy {
  EventStudyResult detector(DetectorRef detector);

  EventStudyResult definitionHash(String? definitionHash);

  EventStudyResult target(String target);

  EventStudyResult frequency(EventStudyResultFrequencyEnum frequency);

  EventStudyResult transform(EventStudyResultTransformEnum? transform);

  EventStudyResult windows(EventStudyResultWindows windows);

  EventStudyResult range(TimeRange? range);

  EventStudyResult asOfSeq(int? asOfSeq);

  EventStudyResult placeboSeed(String? placeboSeed);

  EventStudyResult nEvents(int nEvents);

  EventStudyResult nClusters(int? nClusters);

  EventStudyResult nExcluded(int? nExcluded);

  EventStudyResult events(List<EventStudyResultEventsInner> events);

  EventStudyResult caar(EventStudyResultCaar caar);

  EventStudyResult caveats(List<Caveat> caveats);

  EventStudyResult valuesWithheld(bool? valuesWithheld);

  EventStudyResult truncated(bool? truncated);

  EventStudyResult manifestId(String manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResult(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyResult call({
    DetectorRef detector,
    String? definitionHash,
    String target,
    EventStudyResultFrequencyEnum frequency,
    EventStudyResultTransformEnum? transform,
    EventStudyResultWindows windows,
    TimeRange? range,
    int? asOfSeq,
    String? placeboSeed,
    int nEvents,
    int? nClusters,
    int? nExcluded,
    List<EventStudyResultEventsInner> events,
    EventStudyResultCaar caar,
    List<Caveat> caveats,
    bool? valuesWithheld,
    bool? truncated,
    String manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyResult.copyWith(...)` or call `instanceOfEventStudyResult.copyWith.fieldName(value)` for a single field.
class _$EventStudyResultCWProxyImpl implements _$EventStudyResultCWProxy {
  const _$EventStudyResultCWProxyImpl(this._value);

  final EventStudyResult _value;

  @override
  EventStudyResult detector(DetectorRef detector) => call(detector: detector);

  @override
  EventStudyResult definitionHash(String? definitionHash) =>
      call(definitionHash: definitionHash);

  @override
  EventStudyResult target(String target) => call(target: target);

  @override
  EventStudyResult frequency(EventStudyResultFrequencyEnum frequency) =>
      call(frequency: frequency);

  @override
  EventStudyResult transform(EventStudyResultTransformEnum? transform) =>
      call(transform: transform);

  @override
  EventStudyResult windows(EventStudyResultWindows windows) =>
      call(windows: windows);

  @override
  EventStudyResult range(TimeRange? range) => call(range: range);

  @override
  EventStudyResult asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  EventStudyResult placeboSeed(String? placeboSeed) =>
      call(placeboSeed: placeboSeed);

  @override
  EventStudyResult nEvents(int nEvents) => call(nEvents: nEvents);

  @override
  EventStudyResult nClusters(int? nClusters) => call(nClusters: nClusters);

  @override
  EventStudyResult nExcluded(int? nExcluded) => call(nExcluded: nExcluded);

  @override
  EventStudyResult events(List<EventStudyResultEventsInner> events) =>
      call(events: events);

  @override
  EventStudyResult caar(EventStudyResultCaar caar) => call(caar: caar);

  @override
  EventStudyResult caveats(List<Caveat> caveats) => call(caveats: caveats);

  @override
  EventStudyResult valuesWithheld(bool? valuesWithheld) =>
      call(valuesWithheld: valuesWithheld);

  @override
  EventStudyResult truncated(bool? truncated) => call(truncated: truncated);

  @override
  EventStudyResult manifestId(String manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyResult call({
    Object? detector = const $CopyWithPlaceholder(),
    Object? definitionHash = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? transform = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
    Object? range = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? placeboSeed = const $CopyWithPlaceholder(),
    Object? nEvents = const $CopyWithPlaceholder(),
    Object? nClusters = const $CopyWithPlaceholder(),
    Object? nExcluded = const $CopyWithPlaceholder(),
    Object? events = const $CopyWithPlaceholder(),
    Object? caar = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? valuesWithheld = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return EventStudyResult(
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      definitionHash: definitionHash == const $CopyWithPlaceholder()
          ? _value.definitionHash
          // ignore: cast_nullable_to_non_nullable
          : definitionHash as String?,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as String,
      frequency: frequency == const $CopyWithPlaceholder() || frequency == null
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as EventStudyResultFrequencyEnum,
      transform: transform == const $CopyWithPlaceholder()
          ? _value.transform
          // ignore: cast_nullable_to_non_nullable
          : transform as EventStudyResultTransformEnum?,
      windows: windows == const $CopyWithPlaceholder() || windows == null
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as EventStudyResultWindows,
      range: range == const $CopyWithPlaceholder()
          ? _value.range
          // ignore: cast_nullable_to_non_nullable
          : range as TimeRange?,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      placeboSeed: placeboSeed == const $CopyWithPlaceholder()
          ? _value.placeboSeed
          // ignore: cast_nullable_to_non_nullable
          : placeboSeed as String?,
      nEvents: nEvents == const $CopyWithPlaceholder() || nEvents == null
          ? _value.nEvents
          // ignore: cast_nullable_to_non_nullable
          : nEvents as int,
      nClusters: nClusters == const $CopyWithPlaceholder()
          ? _value.nClusters
          // ignore: cast_nullable_to_non_nullable
          : nClusters as int?,
      nExcluded: nExcluded == const $CopyWithPlaceholder()
          ? _value.nExcluded
          // ignore: cast_nullable_to_non_nullable
          : nExcluded as int?,
      events: events == const $CopyWithPlaceholder() || events == null
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<EventStudyResultEventsInner>,
      caar: caar == const $CopyWithPlaceholder() || caar == null
          ? _value.caar
          // ignore: cast_nullable_to_non_nullable
          : caar as EventStudyResultCaar,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
      valuesWithheld: valuesWithheld == const $CopyWithPlaceholder()
          ? _value.valuesWithheld
          // ignore: cast_nullable_to_non_nullable
          : valuesWithheld as bool?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
    );
  }
}

extension $EventStudyResultCopyWith on EventStudyResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyResult.copyWith(...)` or `instanceOfEventStudyResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyResultCWProxy get copyWith => _$EventStudyResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyResult _$EventStudyResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EventStudyResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'detector',
          'target',
          'frequency',
          'windows',
          'nEvents',
          'events',
          'caar',
          'caveats',
          'manifestId',
        ],
      );
      final val = EventStudyResult(
        detector: $checkedConvert(
          'detector',
          (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
        ),
        definitionHash: $checkedConvert('definitionHash', (v) => v as String?),
        target: $checkedConvert('target', (v) => v as String),
        frequency: $checkedConvert(
          'frequency',
          (v) => $enumDecode(_$EventStudyResultFrequencyEnumEnumMap, v),
        ),
        transform: $checkedConvert(
          'transform',
          (v) => $enumDecodeNullable(_$EventStudyResultTransformEnumEnumMap, v),
        ),
        windows: $checkedConvert(
          'windows',
          (v) => EventStudyResultWindows.fromJson(v as Map<String, dynamic>),
        ),
        range: $checkedConvert(
          'range',
          (v) =>
              v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
        ),
        asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
        placeboSeed: $checkedConvert('placeboSeed', (v) => v as String?),
        nEvents: $checkedConvert('nEvents', (v) => (v as num).toInt()),
        nClusters: $checkedConvert('nClusters', (v) => (v as num?)?.toInt()),
        nExcluded: $checkedConvert('nExcluded', (v) => (v as num?)?.toInt()),
        events: $checkedConvert(
          'events',
          (v) => (v as List<dynamic>)
              .map(
                (e) => EventStudyResultEventsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        caar: $checkedConvert(
          'caar',
          (v) => EventStudyResultCaar.fromJson(v as Map<String, dynamic>),
        ),
        caveats: $checkedConvert(
          'caveats',
          (v) => (v as List<dynamic>)
              .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        valuesWithheld: $checkedConvert('valuesWithheld', (v) => v as bool?),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
        manifestId: $checkedConvert('manifestId', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$EventStudyResultToJson(EventStudyResult instance) =>
    <String, dynamic>{
      'detector': instance.detector.toJson(),
      'definitionHash': ?instance.definitionHash,
      'target': instance.target,
      'frequency': _$EventStudyResultFrequencyEnumEnumMap[instance.frequency]!,
      'transform': ?_$EventStudyResultTransformEnumEnumMap[instance.transform],
      'windows': instance.windows.toJson(),
      'range': ?instance.range?.toJson(),
      'asOfSeq': ?instance.asOfSeq,
      'placeboSeed': ?instance.placeboSeed,
      'nEvents': instance.nEvents,
      'nClusters': ?instance.nClusters,
      'nExcluded': ?instance.nExcluded,
      'events': instance.events.map((e) => e.toJson()).toList(),
      'caar': instance.caar.toJson(),
      'caveats': instance.caveats.map((e) => e.toJson()).toList(),
      'valuesWithheld': ?instance.valuesWithheld,
      'truncated': ?instance.truncated,
      'manifestId': instance.manifestId,
    };

const _$EventStudyResultFrequencyEnumEnumMap = {
  EventStudyResultFrequencyEnum.day: 'day',
  EventStudyResultFrequencyEnum.week: 'week',
};

const _$EventStudyResultTransformEnumEnumMap = {
  EventStudyResultTransformEnum.logReturn: 'log-return',
  EventStudyResultTransformEnum.logLevelStandardised: 'log-level-standardised',
  EventStudyResultTransformEnum.weeklyDifferenceStandardised:
      'weekly-difference-standardised',
  EventStudyResultTransformEnum.anscombeResidual: 'anscombe-residual',
};
