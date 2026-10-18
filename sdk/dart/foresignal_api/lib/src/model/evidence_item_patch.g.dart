// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evidence_item_patch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EvidenceItemPatchCWProxy {
  EvidenceItemPatch title(String? title);

  EvidenceItemPatch description(String? description);

  EvidenceItemPatch attachmentIds(List<String>? attachmentIds);

  EvidenceItemPatch url(String? url);

  EvidenceItemPatch sourceDescription(String? sourceDescription);

  EvidenceItemPatch sourceReliability(
    EvidenceItemPatchSourceReliabilityEnum? sourceReliability,
  );

  EvidenceItemPatch informationCredibility(int? informationCredibility);

  EvidenceItemPatch citedSourceId(String? citedSourceId);

  EvidenceItemPatch citedRefs(List<String>? citedRefs);

  EvidenceItemPatch geometry(Geometry? geometry);

  EvidenceItemPatch eventTime(DateTime? eventTime);

  EvidenceItemPatch visibility(Visibility? visibility);

  EvidenceItemPatch tlp(Tlp? tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceItemPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceItemPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  EvidenceItemPatch call({
    String? title,
    String? description,
    List<String>? attachmentIds,
    String? url,
    String? sourceDescription,
    EvidenceItemPatchSourceReliabilityEnum? sourceReliability,
    int? informationCredibility,
    String? citedSourceId,
    List<String>? citedRefs,
    Geometry? geometry,
    DateTime? eventTime,
    Visibility? visibility,
    Tlp? tlp,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEvidenceItemPatch.copyWith(...)` or call `instanceOfEvidenceItemPatch.copyWith.fieldName(value)` for a single field.
class _$EvidenceItemPatchCWProxyImpl implements _$EvidenceItemPatchCWProxy {
  const _$EvidenceItemPatchCWProxyImpl(this._value);

  final EvidenceItemPatch _value;

  @override
  EvidenceItemPatch title(String? title) => call(title: title);

  @override
  EvidenceItemPatch description(String? description) =>
      call(description: description);

  @override
  EvidenceItemPatch attachmentIds(List<String>? attachmentIds) =>
      call(attachmentIds: attachmentIds);

  @override
  EvidenceItemPatch url(String? url) => call(url: url);

  @override
  EvidenceItemPatch sourceDescription(String? sourceDescription) =>
      call(sourceDescription: sourceDescription);

  @override
  EvidenceItemPatch sourceReliability(
    EvidenceItemPatchSourceReliabilityEnum? sourceReliability,
  ) => call(sourceReliability: sourceReliability);

  @override
  EvidenceItemPatch informationCredibility(int? informationCredibility) =>
      call(informationCredibility: informationCredibility);

  @override
  EvidenceItemPatch citedSourceId(String? citedSourceId) =>
      call(citedSourceId: citedSourceId);

  @override
  EvidenceItemPatch citedRefs(List<String>? citedRefs) =>
      call(citedRefs: citedRefs);

  @override
  EvidenceItemPatch geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  EvidenceItemPatch eventTime(DateTime? eventTime) =>
      call(eventTime: eventTime);

  @override
  EvidenceItemPatch visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  EvidenceItemPatch tlp(Tlp? tlp) => call(tlp: tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceItemPatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceItemPatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EvidenceItemPatch call({
    Object? title = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? attachmentIds = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? sourceDescription = const $CopyWithPlaceholder(),
    Object? sourceReliability = const $CopyWithPlaceholder(),
    Object? informationCredibility = const $CopyWithPlaceholder(),
    Object? citedSourceId = const $CopyWithPlaceholder(),
    Object? citedRefs = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
  }) {
    return EvidenceItemPatch(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      attachmentIds: attachmentIds == const $CopyWithPlaceholder()
          ? _value.attachmentIds
          // ignore: cast_nullable_to_non_nullable
          : attachmentIds as List<String>?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      sourceDescription: sourceDescription == const $CopyWithPlaceholder()
          ? _value.sourceDescription
          // ignore: cast_nullable_to_non_nullable
          : sourceDescription as String?,
      sourceReliability: sourceReliability == const $CopyWithPlaceholder()
          ? _value.sourceReliability
          // ignore: cast_nullable_to_non_nullable
          : sourceReliability as EvidenceItemPatchSourceReliabilityEnum?,
      informationCredibility:
          informationCredibility == const $CopyWithPlaceholder()
          ? _value.informationCredibility
          // ignore: cast_nullable_to_non_nullable
          : informationCredibility as int?,
      citedSourceId: citedSourceId == const $CopyWithPlaceholder()
          ? _value.citedSourceId
          // ignore: cast_nullable_to_non_nullable
          : citedSourceId as String?,
      citedRefs: citedRefs == const $CopyWithPlaceholder()
          ? _value.citedRefs
          // ignore: cast_nullable_to_non_nullable
          : citedRefs as List<String>?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
    );
  }
}

extension $EvidenceItemPatchCopyWith on EvidenceItemPatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEvidenceItemPatch.copyWith(...)` or `instanceOfEvidenceItemPatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EvidenceItemPatchCWProxy get copyWith =>
      _$EvidenceItemPatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EvidenceItemPatch _$EvidenceItemPatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EvidenceItemPatch', json, ($checkedConvert) {
      final val = EvidenceItemPatch(
        title: $checkedConvert('title', (v) => v as String?),
        description: $checkedConvert('description', (v) => v as String?),
        attachmentIds: $checkedConvert(
          'attachmentIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        url: $checkedConvert('url', (v) => v as String?),
        sourceDescription: $checkedConvert(
          'sourceDescription',
          (v) => v as String?,
        ),
        sourceReliability: $checkedConvert(
          'sourceReliability',
          (v) => $enumDecodeNullable(
            _$EvidenceItemPatchSourceReliabilityEnumEnumMap,
            v,
          ),
        ),
        informationCredibility: $checkedConvert(
          'informationCredibility',
          (v) => (v as num?)?.toInt(),
        ),
        citedSourceId: $checkedConvert('citedSourceId', (v) => v as String?),
        citedRefs: $checkedConvert(
          'citedRefs',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        eventTime: $checkedConvert(
          'eventTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        visibility: $checkedConvert(
          'visibility',
          (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
        ),
        tlp: $checkedConvert(
          'tlp',
          (v) => $enumDecodeNullable(_$TlpEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EvidenceItemPatchToJson(EvidenceItemPatch instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'description': ?instance.description,
      'attachmentIds': ?instance.attachmentIds,
      'url': ?instance.url,
      'sourceDescription': ?instance.sourceDescription,
      'sourceReliability':
          ?_$EvidenceItemPatchSourceReliabilityEnumEnumMap[instance
              .sourceReliability],
      'informationCredibility': ?instance.informationCredibility,
      'citedSourceId': ?instance.citedSourceId,
      'citedRefs': ?instance.citedRefs,
      'geometry': ?instance.geometry?.toJson(),
      'eventTime': ?instance.eventTime?.toIso8601String(),
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'tlp': ?_$TlpEnumMap[instance.tlp],
    };

const _$EvidenceItemPatchSourceReliabilityEnumEnumMap = {
  EvidenceItemPatchSourceReliabilityEnum.A: 'A',
  EvidenceItemPatchSourceReliabilityEnum.B: 'B',
  EvidenceItemPatchSourceReliabilityEnum.C: 'C',
  EvidenceItemPatchSourceReliabilityEnum.D: 'D',
  EvidenceItemPatchSourceReliabilityEnum.E: 'E',
  EvidenceItemPatchSourceReliabilityEnum.F: 'F',
};

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
