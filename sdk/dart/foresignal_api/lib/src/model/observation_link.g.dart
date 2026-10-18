// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'observation_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObservationLinkCWProxy {
  ObservationLink canonicalEventId(String canonicalEventId);

  ObservationLink recordRef(String? recordRef);

  ObservationLink sourceId(String sourceId);

  ObservationLink externalId(String externalId);

  ObservationLink seq(int seq);

  ObservationLink version(int? version);

  ObservationLink originatorId(String originatorId);

  ObservationLink originator(Originator? originator);

  ObservationLink upstreamEventId(String? upstreamEventId);

  ObservationLink evidenceGroupId(String evidenceGroupId);

  ObservationLink linkConfidence(num linkConfidence);

  ObservationLink linkReason(ObservationLinkLinkReason linkReason);

  ObservationLink linkedAt(DateTime linkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservationLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservationLink(...).copyWith(id: 12, name: "My name")
  /// ```
  ObservationLink call({
    String canonicalEventId,
    String? recordRef,
    String sourceId,
    String externalId,
    int seq,
    int? version,
    String originatorId,
    Originator? originator,
    String? upstreamEventId,
    String evidenceGroupId,
    num linkConfidence,
    ObservationLinkLinkReason linkReason,
    DateTime linkedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObservationLink.copyWith(...)` or call `instanceOfObservationLink.copyWith.fieldName(value)` for a single field.
class _$ObservationLinkCWProxyImpl implements _$ObservationLinkCWProxy {
  const _$ObservationLinkCWProxyImpl(this._value);

  final ObservationLink _value;

  @override
  ObservationLink canonicalEventId(String canonicalEventId) =>
      call(canonicalEventId: canonicalEventId);

  @override
  ObservationLink recordRef(String? recordRef) => call(recordRef: recordRef);

  @override
  ObservationLink sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ObservationLink externalId(String externalId) => call(externalId: externalId);

  @override
  ObservationLink seq(int seq) => call(seq: seq);

  @override
  ObservationLink version(int? version) => call(version: version);

  @override
  ObservationLink originatorId(String originatorId) =>
      call(originatorId: originatorId);

  @override
  ObservationLink originator(Originator? originator) =>
      call(originator: originator);

  @override
  ObservationLink upstreamEventId(String? upstreamEventId) =>
      call(upstreamEventId: upstreamEventId);

  @override
  ObservationLink evidenceGroupId(String evidenceGroupId) =>
      call(evidenceGroupId: evidenceGroupId);

  @override
  ObservationLink linkConfidence(num linkConfidence) =>
      call(linkConfidence: linkConfidence);

  @override
  ObservationLink linkReason(ObservationLinkLinkReason linkReason) =>
      call(linkReason: linkReason);

  @override
  ObservationLink linkedAt(DateTime linkedAt) => call(linkedAt: linkedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObservationLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObservationLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObservationLink call({
    Object? canonicalEventId = const $CopyWithPlaceholder(),
    Object? recordRef = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? originatorId = const $CopyWithPlaceholder(),
    Object? originator = const $CopyWithPlaceholder(),
    Object? upstreamEventId = const $CopyWithPlaceholder(),
    Object? evidenceGroupId = const $CopyWithPlaceholder(),
    Object? linkConfidence = const $CopyWithPlaceholder(),
    Object? linkReason = const $CopyWithPlaceholder(),
    Object? linkedAt = const $CopyWithPlaceholder(),
  }) {
    return ObservationLink(
      canonicalEventId:
          canonicalEventId == const $CopyWithPlaceholder() ||
              canonicalEventId == null
          ? _value.canonicalEventId
          // ignore: cast_nullable_to_non_nullable
          : canonicalEventId as String,
      recordRef: recordRef == const $CopyWithPlaceholder()
          ? _value.recordRef
          // ignore: cast_nullable_to_non_nullable
          : recordRef as String?,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
      originatorId:
          originatorId == const $CopyWithPlaceholder() || originatorId == null
          ? _value.originatorId
          // ignore: cast_nullable_to_non_nullable
          : originatorId as String,
      originator: originator == const $CopyWithPlaceholder()
          ? _value.originator
          // ignore: cast_nullable_to_non_nullable
          : originator as Originator?,
      upstreamEventId: upstreamEventId == const $CopyWithPlaceholder()
          ? _value.upstreamEventId
          // ignore: cast_nullable_to_non_nullable
          : upstreamEventId as String?,
      evidenceGroupId:
          evidenceGroupId == const $CopyWithPlaceholder() ||
              evidenceGroupId == null
          ? _value.evidenceGroupId
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupId as String,
      linkConfidence:
          linkConfidence == const $CopyWithPlaceholder() ||
              linkConfidence == null
          ? _value.linkConfidence
          // ignore: cast_nullable_to_non_nullable
          : linkConfidence as num,
      linkReason:
          linkReason == const $CopyWithPlaceholder() || linkReason == null
          ? _value.linkReason
          // ignore: cast_nullable_to_non_nullable
          : linkReason as ObservationLinkLinkReason,
      linkedAt: linkedAt == const $CopyWithPlaceholder() || linkedAt == null
          ? _value.linkedAt
          // ignore: cast_nullable_to_non_nullable
          : linkedAt as DateTime,
    );
  }
}

extension $ObservationLinkCopyWith on ObservationLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObservationLink.copyWith(...)` or `instanceOfObservationLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObservationLinkCWProxy get copyWith => _$ObservationLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObservationLink _$ObservationLinkFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ObservationLink', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'canonicalEventId',
      'sourceId',
      'externalId',
      'seq',
      'originatorId',
      'evidenceGroupId',
      'linkConfidence',
      'linkReason',
      'linkedAt',
    ],
  );
  final val = ObservationLink(
    canonicalEventId: $checkedConvert('canonicalEventId', (v) => v as String),
    recordRef: $checkedConvert('recordRef', (v) => v as String?),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    seq: $checkedConvert('seq', (v) => (v as num).toInt()),
    version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
    originatorId: $checkedConvert('originatorId', (v) => v as String),
    originator: $checkedConvert(
      'originator',
      (v) => v == null ? null : Originator.fromJson(v as Map<String, dynamic>),
    ),
    upstreamEventId: $checkedConvert('upstreamEventId', (v) => v as String?),
    evidenceGroupId: $checkedConvert('evidenceGroupId', (v) => v as String),
    linkConfidence: $checkedConvert('linkConfidence', (v) => v as num),
    linkReason: $checkedConvert(
      'linkReason',
      (v) => ObservationLinkLinkReason.fromJson(v as Map<String, dynamic>),
    ),
    linkedAt: $checkedConvert('linkedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ObservationLinkToJson(ObservationLink instance) =>
    <String, dynamic>{
      'canonicalEventId': instance.canonicalEventId,
      'recordRef': ?instance.recordRef,
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'seq': instance.seq,
      'version': ?instance.version,
      'originatorId': instance.originatorId,
      'originator': ?instance.originator?.toJson(),
      'upstreamEventId': ?instance.upstreamEventId,
      'evidenceGroupId': instance.evidenceGroupId,
      'linkConfidence': instance.linkConfidence,
      'linkReason': instance.linkReason.toJson(),
      'linkedAt': instance.linkedAt.toIso8601String(),
    };
