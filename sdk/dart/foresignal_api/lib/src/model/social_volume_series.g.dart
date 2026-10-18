// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_volume_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVolumeSeriesCWProxy {
  SocialVolumeSeries platform(String platform);

  SocialVolumeSeries language(String? language);

  SocialVolumeSeries seriesRef(String seriesRef);

  SocialVolumeSeries collectionType(SocialCollectionType collectionType);

  SocialVolumeSeries baselineEligible(bool baselineEligible);

  SocialVolumeSeries points(List<SocialVolumePoint> points);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolumeSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolumeSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVolumeSeries call({
    String platform,
    String? language,
    String seriesRef,
    SocialCollectionType collectionType,
    bool baselineEligible,
    List<SocialVolumePoint> points,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVolumeSeries.copyWith(...)` or call `instanceOfSocialVolumeSeries.copyWith.fieldName(value)` for a single field.
class _$SocialVolumeSeriesCWProxyImpl implements _$SocialVolumeSeriesCWProxy {
  const _$SocialVolumeSeriesCWProxyImpl(this._value);

  final SocialVolumeSeries _value;

  @override
  SocialVolumeSeries platform(String platform) => call(platform: platform);

  @override
  SocialVolumeSeries language(String? language) => call(language: language);

  @override
  SocialVolumeSeries seriesRef(String seriesRef) => call(seriesRef: seriesRef);

  @override
  SocialVolumeSeries collectionType(SocialCollectionType collectionType) =>
      call(collectionType: collectionType);

  @override
  SocialVolumeSeries baselineEligible(bool baselineEligible) =>
      call(baselineEligible: baselineEligible);

  @override
  SocialVolumeSeries points(List<SocialVolumePoint> points) =>
      call(points: points);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolumeSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolumeSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVolumeSeries call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? seriesRef = const $CopyWithPlaceholder(),
    Object? collectionType = const $CopyWithPlaceholder(),
    Object? baselineEligible = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
  }) {
    return SocialVolumeSeries(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      seriesRef: seriesRef == const $CopyWithPlaceholder() || seriesRef == null
          ? _value.seriesRef
          // ignore: cast_nullable_to_non_nullable
          : seriesRef as String,
      collectionType:
          collectionType == const $CopyWithPlaceholder() ||
              collectionType == null
          ? _value.collectionType
          // ignore: cast_nullable_to_non_nullable
          : collectionType as SocialCollectionType,
      baselineEligible:
          baselineEligible == const $CopyWithPlaceholder() ||
              baselineEligible == null
          ? _value.baselineEligible
          // ignore: cast_nullable_to_non_nullable
          : baselineEligible as bool,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<SocialVolumePoint>,
    );
  }
}

extension $SocialVolumeSeriesCopyWith on SocialVolumeSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVolumeSeries.copyWith(...)` or `instanceOfSocialVolumeSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVolumeSeriesCWProxy get copyWith =>
      _$SocialVolumeSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVolumeSeries _$SocialVolumeSeriesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialVolumeSeries', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'platform',
          'seriesRef',
          'collectionType',
          'baselineEligible',
          'points',
        ],
      );
      final val = SocialVolumeSeries(
        platform: $checkedConvert('platform', (v) => v as String),
        language: $checkedConvert('language', (v) => v as String?),
        seriesRef: $checkedConvert('seriesRef', (v) => v as String),
        collectionType: $checkedConvert(
          'collectionType',
          (v) => $enumDecode(_$SocialCollectionTypeEnumMap, v),
        ),
        baselineEligible: $checkedConvert('baselineEligible', (v) => v as bool),
        points: $checkedConvert(
          'points',
          (v) => (v as List<dynamic>)
              .map((e) => SocialVolumePoint.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SocialVolumeSeriesToJson(SocialVolumeSeries instance) =>
    <String, dynamic>{
      'platform': instance.platform,
      'language': ?instance.language,
      'seriesRef': instance.seriesRef,
      'collectionType': _$SocialCollectionTypeEnumMap[instance.collectionType]!,
      'baselineEligible': instance.baselineEligible,
      'points': instance.points.map((e) => e.toJson()).toList(),
    };

const _$SocialCollectionTypeEnumMap = {
  SocialCollectionType.complete: 'complete',
  SocialCollectionType.paged: 'paged',
  SocialCollectionType.sampled: 'sampled',
};
