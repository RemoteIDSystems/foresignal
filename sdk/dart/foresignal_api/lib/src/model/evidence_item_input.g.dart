// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'evidence_item_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EvidenceItemInputCWProxy {
  EvidenceItemInput title(String title);

  EvidenceItemInput description(String? description);

  EvidenceItemInput attachmentIds(List<String>? attachmentIds);

  EvidenceItemInput url(String? url);

  EvidenceItemInput sourceDescription(String? sourceDescription);

  EvidenceItemInput sourceReliability(
    EvidenceItemInputSourceReliabilityEnum? sourceReliability,
  );

  EvidenceItemInput informationCredibility(int? informationCredibility);

  EvidenceItemInput citedSourceId(String? citedSourceId);

  EvidenceItemInput citedRefs(List<String>? citedRefs);

  EvidenceItemInput geometry(Geometry? geometry);

  EvidenceItemInput eventTime(DateTime? eventTime);

  EvidenceItemInput tagIds(List<String>? tagIds);

  EvidenceItemInput visibility(Visibility? visibility);

  EvidenceItemInput tlp(Tlp? tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceItemInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceItemInput(...).copyWith(id: 12, name: "My name")
  /// ```
  EvidenceItemInput call({
    String title,
    String? description,
    List<String>? attachmentIds,
    String? url,
    String? sourceDescription,
    EvidenceItemInputSourceReliabilityEnum? sourceReliability,
    int? informationCredibility,
    String? citedSourceId,
    List<String>? citedRefs,
    Geometry? geometry,
    DateTime? eventTime,
    List<String>? tagIds,
    Visibility? visibility,
    Tlp? tlp,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEvidenceItemInput.copyWith(...)` or call `instanceOfEvidenceItemInput.copyWith.fieldName(value)` for a single field.
class _$EvidenceItemInputCWProxyImpl implements _$EvidenceItemInputCWProxy {
  const _$EvidenceItemInputCWProxyImpl(this._value);

  final EvidenceItemInput _value;

  @override
  EvidenceItemInput title(String title) => call(title: title);

  @override
  EvidenceItemInput description(String? description) =>
      call(description: description);

  @override
  EvidenceItemInput attachmentIds(List<String>? attachmentIds) =>
      call(attachmentIds: attachmentIds);

  @override
  EvidenceItemInput url(String? url) => call(url: url);

  @override
  EvidenceItemInput sourceDescription(String? sourceDescription) =>
      call(sourceDescription: sourceDescription);

  @override
  EvidenceItemInput sourceReliability(
    EvidenceItemInputSourceReliabilityEnum? sourceReliability,
  ) => call(sourceReliability: sourceReliability);

  @override
  EvidenceItemInput informationCredibility(int? informationCredibility) =>
      call(informationCredibility: informationCredibility);

  @override
  EvidenceItemInput citedSourceId(String? citedSourceId) =>
      call(citedSourceId: citedSourceId);

  @override
  EvidenceItemInput citedRefs(List<String>? citedRefs) =>
      call(citedRefs: citedRefs);

  @override
  EvidenceItemInput geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  EvidenceItemInput eventTime(DateTime? eventTime) =>
      call(eventTime: eventTime);

  @override
  EvidenceItemInput tagIds(List<String>? tagIds) => call(tagIds: tagIds);

  @override
  EvidenceItemInput visibility(Visibility? visibility) =>
      call(visibility: visibility);

  @override
  EvidenceItemInput tlp(Tlp? tlp) => call(tlp: tlp);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EvidenceItemInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EvidenceItemInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EvidenceItemInput call({
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
    Object? tagIds = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
  }) {
    return EvidenceItemInput(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
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
          : sourceReliability as EvidenceItemInputSourceReliabilityEnum?,
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
      tagIds: tagIds == const $CopyWithPlaceholder()
          ? _value.tagIds
          // ignore: cast_nullable_to_non_nullable
          : tagIds as List<String>?,
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

extension $EvidenceItemInputCopyWith on EvidenceItemInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEvidenceItemInput.copyWith(...)` or `instanceOfEvidenceItemInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EvidenceItemInputCWProxy get copyWith =>
      _$EvidenceItemInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EvidenceItemInput _$EvidenceItemInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EvidenceItemInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['title']);
      final val = EvidenceItemInput(
        title: $checkedConvert('title', (v) => v as String),
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
            _$EvidenceItemInputSourceReliabilityEnumEnumMap,
            v,
          ),
        ),
        informationCredibility: $checkedConvert(
          'informationCredibility',
          (v) => (v as num?)?.toInt() ?? 6,
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
        tagIds: $checkedConvert(
          'tagIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
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

Map<String, dynamic> _$EvidenceItemInputToJson(EvidenceItemInput instance) =>
    <String, dynamic>{
      'title': instance.title,
      'description': ?instance.description,
      'attachmentIds': ?instance.attachmentIds,
      'url': ?instance.url,
      'sourceDescription': ?instance.sourceDescription,
      'sourceReliability':
          ?_$EvidenceItemInputSourceReliabilityEnumEnumMap[instance
              .sourceReliability],
      'informationCredibility': ?instance.informationCredibility,
      'citedSourceId': ?instance.citedSourceId,
      'citedRefs': ?instance.citedRefs,
      'geometry': ?instance.geometry?.toJson(),
      'eventTime': ?instance.eventTime?.toIso8601String(),
      'tagIds': ?instance.tagIds,
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
      'tlp': ?_$TlpEnumMap[instance.tlp],
    };

const _$EvidenceItemInputSourceReliabilityEnumEnumMap = {
  EvidenceItemInputSourceReliabilityEnum.A: 'A',
  EvidenceItemInputSourceReliabilityEnum.B: 'B',
  EvidenceItemInputSourceReliabilityEnum.C: 'C',
  EvidenceItemInputSourceReliabilityEnum.D: 'D',
  EvidenceItemInputSourceReliabilityEnum.E: 'E',
  EvidenceItemInputSourceReliabilityEnum.F: 'F',
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
