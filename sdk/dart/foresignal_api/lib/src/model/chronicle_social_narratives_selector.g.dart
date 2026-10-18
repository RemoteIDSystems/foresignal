// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_social_narratives_selector.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSocialNarrativesSelectorCWProxy {
  ChronicleSocialNarrativesSelector type(
    ChronicleSocialNarrativesSelectorTypeEnum type,
  );

  ChronicleSocialNarrativesSelector topicIds(List<String>? topicIds);

  ChronicleSocialNarrativesSelector placeIds(List<String>? placeIds);

  ChronicleSocialNarrativesSelector regionCodes(List<String>? regionCodes);

  ChronicleSocialNarrativesSelector situationIds(List<String>? situationIds);

  ChronicleSocialNarrativesSelector statuses(
    List<ChronicleSocialNarrativesSelectorStatusesEnum>? statuses,
  );

  ChronicleSocialNarrativesSelector keepPostText(bool? keepPostText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSocialNarrativesSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSocialNarrativesSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSocialNarrativesSelector call({
    ChronicleSocialNarrativesSelectorTypeEnum type,
    List<String>? topicIds,
    List<String>? placeIds,
    List<String>? regionCodes,
    List<String>? situationIds,
    List<ChronicleSocialNarrativesSelectorStatusesEnum>? statuses,
    bool? keepPostText,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSocialNarrativesSelector.copyWith(...)` or call `instanceOfChronicleSocialNarrativesSelector.copyWith.fieldName(value)` for a single field.
class _$ChronicleSocialNarrativesSelectorCWProxyImpl
    implements _$ChronicleSocialNarrativesSelectorCWProxy {
  const _$ChronicleSocialNarrativesSelectorCWProxyImpl(this._value);

  final ChronicleSocialNarrativesSelector _value;

  @override
  ChronicleSocialNarrativesSelector type(
    ChronicleSocialNarrativesSelectorTypeEnum type,
  ) => call(type: type);

  @override
  ChronicleSocialNarrativesSelector topicIds(List<String>? topicIds) =>
      call(topicIds: topicIds);

  @override
  ChronicleSocialNarrativesSelector placeIds(List<String>? placeIds) =>
      call(placeIds: placeIds);

  @override
  ChronicleSocialNarrativesSelector regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  ChronicleSocialNarrativesSelector situationIds(List<String>? situationIds) =>
      call(situationIds: situationIds);

  @override
  ChronicleSocialNarrativesSelector statuses(
    List<ChronicleSocialNarrativesSelectorStatusesEnum>? statuses,
  ) => call(statuses: statuses);

  @override
  ChronicleSocialNarrativesSelector keepPostText(bool? keepPostText) =>
      call(keepPostText: keepPostText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSocialNarrativesSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSocialNarrativesSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSocialNarrativesSelector call({
    Object? type = const $CopyWithPlaceholder(),
    Object? topicIds = const $CopyWithPlaceholder(),
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? situationIds = const $CopyWithPlaceholder(),
    Object? statuses = const $CopyWithPlaceholder(),
    Object? keepPostText = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSocialNarrativesSelector(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleSocialNarrativesSelectorTypeEnum,
      topicIds: topicIds == const $CopyWithPlaceholder()
          ? _value.topicIds
          // ignore: cast_nullable_to_non_nullable
          : topicIds as List<String>?,
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      situationIds: situationIds == const $CopyWithPlaceholder()
          ? _value.situationIds
          // ignore: cast_nullable_to_non_nullable
          : situationIds as List<String>?,
      statuses: statuses == const $CopyWithPlaceholder()
          ? _value.statuses
          // ignore: cast_nullable_to_non_nullable
          : statuses as List<ChronicleSocialNarrativesSelectorStatusesEnum>?,
      keepPostText: keepPostText == const $CopyWithPlaceholder()
          ? _value.keepPostText
          // ignore: cast_nullable_to_non_nullable
          : keepPostText as bool?,
    );
  }
}

extension $ChronicleSocialNarrativesSelectorCopyWith
    on ChronicleSocialNarrativesSelector {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSocialNarrativesSelector.copyWith(...)` or `instanceOfChronicleSocialNarrativesSelector.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSocialNarrativesSelectorCWProxy get copyWith =>
      _$ChronicleSocialNarrativesSelectorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSocialNarrativesSelector _$ChronicleSocialNarrativesSelectorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSocialNarrativesSelector', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = ChronicleSocialNarrativesSelector(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleSocialNarrativesSelectorTypeEnumEnumMap, v),
    ),
    topicIds: $checkedConvert(
      'topicIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    placeIds: $checkedConvert(
      'placeIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    situationIds: $checkedConvert(
      'situationIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    statuses: $checkedConvert(
      'statuses',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => $enumDecode(
              _$ChronicleSocialNarrativesSelectorStatusesEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
    keepPostText: $checkedConvert('keepPostText', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSocialNarrativesSelectorToJson(
  ChronicleSocialNarrativesSelector instance,
) => <String, dynamic>{
  'type': _$ChronicleSocialNarrativesSelectorTypeEnumEnumMap[instance.type]!,
  'topicIds': ?instance.topicIds,
  'placeIds': ?instance.placeIds,
  'regionCodes': ?instance.regionCodes,
  'situationIds': ?instance.situationIds,
  'statuses': ?instance.statuses
      ?.map((e) => _$ChronicleSocialNarrativesSelectorStatusesEnumEnumMap[e]!)
      .toList(),
  'keepPostText': ?instance.keepPostText,
};

const _$ChronicleSocialNarrativesSelectorTypeEnumEnumMap = {
  ChronicleSocialNarrativesSelectorTypeEnum.socialNarratives:
      'social-narratives',
};

const _$ChronicleSocialNarrativesSelectorStatusesEnumEnumMap = {
  ChronicleSocialNarrativesSelectorStatusesEnum.unassessed: 'unassessed',
  ChronicleSocialNarrativesSelectorStatusesEnum.pending: 'pending',
  ChronicleSocialNarrativesSelectorStatusesEnum.corroborated: 'corroborated',
  ChronicleSocialNarrativesSelectorStatusesEnum.unobserved: 'unobserved',
  ChronicleSocialNarrativesSelectorStatusesEnum.contradicted: 'contradicted',
  ChronicleSocialNarrativesSelectorStatusesEnum.mixed: 'mixed',
  ChronicleSocialNarrativesSelectorStatusesEnum.insufficientCoverage:
      'insufficient-coverage',
};
