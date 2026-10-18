// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evidence_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EvidenceEventCWProxy {
  EvidenceEvent canonicalEventId(String canonicalEventId);

  EvidenceEvent ref(String ref);

  EvidenceEvent family(String family);

  EvidenceEvent status(EvidenceEventStatusEnum status);

  EvidenceEvent eventTime(DateTime eventTime);

  EvidenceEvent regionCodes(List<String>? regionCodes);

  EvidenceEvent observationCount(int observationCount);

  EvidenceEvent evidenceGroupCount(int evidenceGroupCount);

  EvidenceEvent qualityFlags(List<EvidenceEventQualityFlagsEnum> qualityFlags);

  EvidenceEvent flagsAdded(List<EvidenceEventFlagsAddedEnum>? flagsAdded);

  EvidenceEvent mergedInto(String? mergedInto);

  EvidenceEvent mergedFrom(List<String>? mergedFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  EvidenceEvent call({
    String canonicalEventId,
    String ref,
    String family,
    EvidenceEventStatusEnum status,
    DateTime eventTime,
    List<String>? regionCodes,
    int observationCount,
    int evidenceGroupCount,
    List<EvidenceEventQualityFlagsEnum> qualityFlags,
    List<EvidenceEventFlagsAddedEnum>? flagsAdded,
    String? mergedInto,
    List<String>? mergedFrom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEvidenceEvent.copyWith(...)` or call `instanceOfEvidenceEvent.copyWith.fieldName(value)` for a single field.
class _$EvidenceEventCWProxyImpl implements _$EvidenceEventCWProxy {
  const _$EvidenceEventCWProxyImpl(this._value);

  final EvidenceEvent _value;

  @override
  EvidenceEvent canonicalEventId(String canonicalEventId) =>
      call(canonicalEventId: canonicalEventId);

  @override
  EvidenceEvent ref(String ref) => call(ref: ref);

  @override
  EvidenceEvent family(String family) => call(family: family);

  @override
  EvidenceEvent status(EvidenceEventStatusEnum status) => call(status: status);

  @override
  EvidenceEvent eventTime(DateTime eventTime) => call(eventTime: eventTime);

  @override
  EvidenceEvent regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  EvidenceEvent observationCount(int observationCount) =>
      call(observationCount: observationCount);

  @override
  EvidenceEvent evidenceGroupCount(int evidenceGroupCount) =>
      call(evidenceGroupCount: evidenceGroupCount);

  @override
  EvidenceEvent qualityFlags(
    List<EvidenceEventQualityFlagsEnum> qualityFlags,
  ) => call(qualityFlags: qualityFlags);

  @override
  EvidenceEvent flagsAdded(List<EvidenceEventFlagsAddedEnum>? flagsAdded) =>
      call(flagsAdded: flagsAdded);

  @override
  EvidenceEvent mergedInto(String? mergedInto) => call(mergedInto: mergedInto);

  @override
  EvidenceEvent mergedFrom(List<String>? mergedFrom) =>
      call(mergedFrom: mergedFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EvidenceEvent call({
    Object? canonicalEventId = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? observationCount = const $CopyWithPlaceholder(),
    Object? evidenceGroupCount = const $CopyWithPlaceholder(),
    Object? qualityFlags = const $CopyWithPlaceholder(),
    Object? flagsAdded = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? mergedFrom = const $CopyWithPlaceholder(),
  }) {
    return EvidenceEvent(
      canonicalEventId:
          canonicalEventId == const $CopyWithPlaceholder() ||
              canonicalEventId == null
          ? _value.canonicalEventId
          // ignore: cast_nullable_to_non_nullable
          : canonicalEventId as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as EvidenceEventStatusEnum,
      eventTime: eventTime == const $CopyWithPlaceholder() || eventTime == null
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      observationCount:
          observationCount == const $CopyWithPlaceholder() ||
              observationCount == null
          ? _value.observationCount
          // ignore: cast_nullable_to_non_nullable
          : observationCount as int,
      evidenceGroupCount:
          evidenceGroupCount == const $CopyWithPlaceholder() ||
              evidenceGroupCount == null
          ? _value.evidenceGroupCount
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupCount as int,
      qualityFlags:
          qualityFlags == const $CopyWithPlaceholder() || qualityFlags == null
          ? _value.qualityFlags
          // ignore: cast_nullable_to_non_nullable
          : qualityFlags as List<EvidenceEventQualityFlagsEnum>,
      flagsAdded: flagsAdded == const $CopyWithPlaceholder()
          ? _value.flagsAdded
          // ignore: cast_nullable_to_non_nullable
          : flagsAdded as List<EvidenceEventFlagsAddedEnum>?,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      mergedFrom: mergedFrom == const $CopyWithPlaceholder()
          ? _value.mergedFrom
          // ignore: cast_nullable_to_non_nullable
          : mergedFrom as List<String>?,
    );
  }
}

extension $EvidenceEventCopyWith on EvidenceEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEvidenceEvent.copyWith(...)` or `instanceOfEvidenceEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EvidenceEventCWProxy get copyWith => _$EvidenceEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EvidenceEvent _$EvidenceEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EvidenceEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'canonicalEventId',
      'ref',
      'family',
      'status',
      'eventTime',
      'observationCount',
      'evidenceGroupCount',
      'qualityFlags',
    ],
  );
  final val = EvidenceEvent(
    canonicalEventId: $checkedConvert('canonicalEventId', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String),
    family: $checkedConvert('family', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$EvidenceEventStatusEnumEnumMap, v),
    ),
    eventTime: $checkedConvert('eventTime', (v) => DateTime.parse(v as String)),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    observationCount: $checkedConvert(
      'observationCount',
      (v) => (v as num).toInt(),
    ),
    evidenceGroupCount: $checkedConvert(
      'evidenceGroupCount',
      (v) => (v as num).toInt(),
    ),
    qualityFlags: $checkedConvert(
      'qualityFlags',
      (v) => (v as List<dynamic>)
          .map((e) => $enumDecode(_$EvidenceEventQualityFlagsEnumEnumMap, e))
          .toList(),
    ),
    flagsAdded: $checkedConvert(
      'flagsAdded',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$EvidenceEventFlagsAddedEnumEnumMap, e))
          .toList(),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    mergedFrom: $checkedConvert(
      'mergedFrom',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EvidenceEventToJson(EvidenceEvent instance) =>
    <String, dynamic>{
      'canonicalEventId': instance.canonicalEventId,
      'ref': instance.ref,
      'family': instance.family,
      'status': _$EvidenceEventStatusEnumEnumMap[instance.status]!,
      'eventTime': instance.eventTime.toIso8601String(),
      'regionCodes': ?instance.regionCodes,
      'observationCount': instance.observationCount,
      'evidenceGroupCount': instance.evidenceGroupCount,
      'qualityFlags': instance.qualityFlags
          .map((e) => _$EvidenceEventQualityFlagsEnumEnumMap[e]!)
          .toList(),
      'flagsAdded': ?instance.flagsAdded
          ?.map((e) => _$EvidenceEventFlagsAddedEnumEnumMap[e]!)
          .toList(),
      'mergedInto': ?instance.mergedInto,
      'mergedFrom': ?instance.mergedFrom,
    };

const _$EvidenceEventStatusEnumEnumMap = {
  EvidenceEventStatusEnum.open: 'open',
  EvidenceEventStatusEnum.merged: 'merged',
  EvidenceEventStatusEnum.closed: 'closed',
};

const _$EvidenceEventQualityFlagsEnumEnumMap = {
  EvidenceEventQualityFlagsEnum.ECHO: 'ECHO',
  EvidenceEventQualityFlagsEnum.TIMESTAMP_CONFLICT: 'TIMESTAMP_CONFLICT',
  EvidenceEventQualityFlagsEnum.SINGLE_ORIGINATOR: 'SINGLE_ORIGINATOR',
  EvidenceEventQualityFlagsEnum.LATE_REVISION: 'LATE_REVISION',
};

const _$EvidenceEventFlagsAddedEnumEnumMap = {
  EvidenceEventFlagsAddedEnum.ECHO: 'ECHO',
  EvidenceEventFlagsAddedEnum.TIMESTAMP_CONFLICT: 'TIMESTAMP_CONFLICT',
  EvidenceEventFlagsAddedEnum.SINGLE_ORIGINATOR: 'SINGLE_ORIGINATOR',
  EvidenceEventFlagsAddedEnum.LATE_REVISION: 'LATE_REVISION',
};
