// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_evidence.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordEvidenceCWProxy {
  RecordEvidence sourceId(String sourceId);

  RecordEvidence externalId(String externalId);

  RecordEvidence originator(Originator originator);

  RecordEvidence links(List<ObservationLink> links);

  RecordEvidence canonicalEvents(List<CanonicalEvent> canonicalEvents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordEvidence call({
    String sourceId,
    String externalId,
    Originator originator,
    List<ObservationLink> links,
    List<CanonicalEvent> canonicalEvents,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordEvidence.copyWith(...)` or call `instanceOfRecordEvidence.copyWith.fieldName(value)` for a single field.
class _$RecordEvidenceCWProxyImpl implements _$RecordEvidenceCWProxy {
  const _$RecordEvidenceCWProxyImpl(this._value);

  final RecordEvidence _value;

  @override
  RecordEvidence sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  RecordEvidence externalId(String externalId) => call(externalId: externalId);

  @override
  RecordEvidence originator(Originator originator) =>
      call(originator: originator);

  @override
  RecordEvidence links(List<ObservationLink> links) => call(links: links);

  @override
  RecordEvidence canonicalEvents(List<CanonicalEvent> canonicalEvents) =>
      call(canonicalEvents: canonicalEvents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordEvidence(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordEvidence(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordEvidence call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? originator = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? canonicalEvents = const $CopyWithPlaceholder(),
  }) {
    return RecordEvidence(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      originator:
          originator == const $CopyWithPlaceholder() || originator == null
          ? _value.originator
          // ignore: cast_nullable_to_non_nullable
          : originator as Originator,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<ObservationLink>,
      canonicalEvents:
          canonicalEvents == const $CopyWithPlaceholder() ||
              canonicalEvents == null
          ? _value.canonicalEvents
          // ignore: cast_nullable_to_non_nullable
          : canonicalEvents as List<CanonicalEvent>,
    );
  }
}

extension $RecordEvidenceCopyWith on RecordEvidence {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordEvidence.copyWith(...)` or `instanceOfRecordEvidence.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordEvidenceCWProxy get copyWith => _$RecordEvidenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordEvidence _$RecordEvidenceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RecordEvidence', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'sourceId',
          'externalId',
          'originator',
          'links',
          'canonicalEvents',
        ],
      );
      final val = RecordEvidence(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        originator: $checkedConvert(
          'originator',
          (v) => Originator.fromJson(v as Map<String, dynamic>),
        ),
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>)
              .map((e) => ObservationLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        canonicalEvents: $checkedConvert(
          'canonicalEvents',
          (v) => (v as List<dynamic>)
              .map((e) => CanonicalEvent.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RecordEvidenceToJson(
  RecordEvidence instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'externalId': instance.externalId,
  'originator': instance.originator.toJson(),
  'links': instance.links.map((e) => e.toJson()).toList(),
  'canonicalEvents': instance.canonicalEvents.map((e) => e.toJson()).toList(),
};
