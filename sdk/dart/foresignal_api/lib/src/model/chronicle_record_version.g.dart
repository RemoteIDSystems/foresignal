// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_record_version.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleRecordVersionCWProxy {
  ChronicleRecordVersion ref(String ref);

  ChronicleRecordVersion sourceId(String sourceId);

  ChronicleRecordVersion externalId(String externalId);

  ChronicleRecordVersion kind(String kind);

  ChronicleRecordVersion seq(int seq);

  ChronicleRecordVersion observedAt(DateTime observedAt);

  ChronicleRecordVersion eventTime(DateTime? eventTime);

  ChronicleRecordVersion ingestedAt(DateTime? ingestedAt);

  ChronicleRecordVersion geometry(Geometry? geometry);

  ChronicleRecordVersion payload(Map<String, Object>? payload);

  ChronicleRecordVersion persistReason(String? persistReason);

  ChronicleRecordVersion layerId(String layerId);

  ChronicleRecordVersion backfilled(bool? backfilled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleRecordVersion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleRecordVersion(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleRecordVersion call({
    String ref,
    String sourceId,
    String externalId,
    String kind,
    int seq,
    DateTime observedAt,
    DateTime? eventTime,
    DateTime? ingestedAt,
    Geometry? geometry,
    Map<String, Object>? payload,
    String? persistReason,
    String layerId,
    bool? backfilled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleRecordVersion.copyWith(...)` or call `instanceOfChronicleRecordVersion.copyWith.fieldName(value)` for a single field.
class _$ChronicleRecordVersionCWProxyImpl
    implements _$ChronicleRecordVersionCWProxy {
  const _$ChronicleRecordVersionCWProxyImpl(this._value);

  final ChronicleRecordVersion _value;

  @override
  ChronicleRecordVersion ref(String ref) => call(ref: ref);

  @override
  ChronicleRecordVersion sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ChronicleRecordVersion externalId(String externalId) =>
      call(externalId: externalId);

  @override
  ChronicleRecordVersion kind(String kind) => call(kind: kind);

  @override
  ChronicleRecordVersion seq(int seq) => call(seq: seq);

  @override
  ChronicleRecordVersion observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  ChronicleRecordVersion eventTime(DateTime? eventTime) =>
      call(eventTime: eventTime);

  @override
  ChronicleRecordVersion ingestedAt(DateTime? ingestedAt) =>
      call(ingestedAt: ingestedAt);

  @override
  ChronicleRecordVersion geometry(Geometry? geometry) =>
      call(geometry: geometry);

  @override
  ChronicleRecordVersion payload(Map<String, Object>? payload) =>
      call(payload: payload);

  @override
  ChronicleRecordVersion persistReason(String? persistReason) =>
      call(persistReason: persistReason);

  @override
  ChronicleRecordVersion layerId(String layerId) => call(layerId: layerId);

  @override
  ChronicleRecordVersion backfilled(bool? backfilled) =>
      call(backfilled: backfilled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleRecordVersion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleRecordVersion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleRecordVersion call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? ingestedAt = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? persistReason = const $CopyWithPlaceholder(),
    Object? layerId = const $CopyWithPlaceholder(),
    Object? backfilled = const $CopyWithPlaceholder(),
  }) {
    return ChronicleRecordVersion(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      ingestedAt: ingestedAt == const $CopyWithPlaceholder()
          ? _value.ingestedAt
          // ignore: cast_nullable_to_non_nullable
          : ingestedAt as DateTime?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      payload: payload == const $CopyWithPlaceholder()
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>?,
      persistReason: persistReason == const $CopyWithPlaceholder()
          ? _value.persistReason
          // ignore: cast_nullable_to_non_nullable
          : persistReason as String?,
      layerId: layerId == const $CopyWithPlaceholder() || layerId == null
          ? _value.layerId
          // ignore: cast_nullable_to_non_nullable
          : layerId as String,
      backfilled: backfilled == const $CopyWithPlaceholder()
          ? _value.backfilled
          // ignore: cast_nullable_to_non_nullable
          : backfilled as bool?,
    );
  }
}

extension $ChronicleRecordVersionCopyWith on ChronicleRecordVersion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleRecordVersion.copyWith(...)` or `instanceOfChronicleRecordVersion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleRecordVersionCWProxy get copyWith =>
      _$ChronicleRecordVersionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleRecordVersion _$ChronicleRecordVersionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleRecordVersion', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'ref',
      'sourceId',
      'externalId',
      'kind',
      'seq',
      'observedAt',
      'layerId',
    ],
  );
  final val = ChronicleRecordVersion(
    ref: $checkedConvert('ref', (v) => v as String),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String),
    seq: $checkedConvert('seq', (v) => (v as num).toInt()),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    eventTime: $checkedConvert(
      'eventTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    ingestedAt: $checkedConvert(
      'ingestedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    payload: $checkedConvert(
      'payload',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    persistReason: $checkedConvert('persistReason', (v) => v as String?),
    layerId: $checkedConvert('layerId', (v) => v as String),
    backfilled: $checkedConvert('backfilled', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ChronicleRecordVersionToJson(
  ChronicleRecordVersion instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'sourceId': instance.sourceId,
  'externalId': instance.externalId,
  'kind': instance.kind,
  'seq': instance.seq,
  'observedAt': instance.observedAt.toIso8601String(),
  'eventTime': ?instance.eventTime?.toIso8601String(),
  'ingestedAt': ?instance.ingestedAt?.toIso8601String(),
  'geometry': ?instance.geometry?.toJson(),
  'payload': ?instance.payload,
  'persistReason': ?instance.persistReason,
  'layerId': instance.layerId,
  'backfilled': ?instance.backfilled,
};
