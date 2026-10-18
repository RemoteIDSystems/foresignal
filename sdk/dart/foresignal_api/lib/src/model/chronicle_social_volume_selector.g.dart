// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_social_volume_selector.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSocialVolumeSelectorCWProxy {
  ChronicleSocialVolumeSelector type(
    ChronicleSocialVolumeSelectorTypeEnum type,
  );

  ChronicleSocialVolumeSelector scopes(
    List<ChronicleSocialVolumeSelectorScopesInner> scopes,
  );

  ChronicleSocialVolumeSelector platforms(List<String>? platforms);

  ChronicleSocialVolumeSelector metrics(
    List<ChronicleSocialVolumeSelectorMetricsEnum>? metrics,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSocialVolumeSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSocialVolumeSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSocialVolumeSelector call({
    ChronicleSocialVolumeSelectorTypeEnum type,
    List<ChronicleSocialVolumeSelectorScopesInner> scopes,
    List<String>? platforms,
    List<ChronicleSocialVolumeSelectorMetricsEnum>? metrics,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSocialVolumeSelector.copyWith(...)` or call `instanceOfChronicleSocialVolumeSelector.copyWith.fieldName(value)` for a single field.
class _$ChronicleSocialVolumeSelectorCWProxyImpl
    implements _$ChronicleSocialVolumeSelectorCWProxy {
  const _$ChronicleSocialVolumeSelectorCWProxyImpl(this._value);

  final ChronicleSocialVolumeSelector _value;

  @override
  ChronicleSocialVolumeSelector type(
    ChronicleSocialVolumeSelectorTypeEnum type,
  ) => call(type: type);

  @override
  ChronicleSocialVolumeSelector scopes(
    List<ChronicleSocialVolumeSelectorScopesInner> scopes,
  ) => call(scopes: scopes);

  @override
  ChronicleSocialVolumeSelector platforms(List<String>? platforms) =>
      call(platforms: platforms);

  @override
  ChronicleSocialVolumeSelector metrics(
    List<ChronicleSocialVolumeSelectorMetricsEnum>? metrics,
  ) => call(metrics: metrics);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSocialVolumeSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSocialVolumeSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSocialVolumeSelector call({
    Object? type = const $CopyWithPlaceholder(),
    Object? scopes = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? metrics = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSocialVolumeSelector(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChronicleSocialVolumeSelectorTypeEnum,
      scopes: scopes == const $CopyWithPlaceholder() || scopes == null
          ? _value.scopes
          // ignore: cast_nullable_to_non_nullable
          : scopes as List<ChronicleSocialVolumeSelectorScopesInner>,
      platforms: platforms == const $CopyWithPlaceholder()
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<String>?,
      metrics: metrics == const $CopyWithPlaceholder()
          ? _value.metrics
          // ignore: cast_nullable_to_non_nullable
          : metrics as List<ChronicleSocialVolumeSelectorMetricsEnum>?,
    );
  }
}

extension $ChronicleSocialVolumeSelectorCopyWith
    on ChronicleSocialVolumeSelector {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSocialVolumeSelector.copyWith(...)` or `instanceOfChronicleSocialVolumeSelector.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSocialVolumeSelectorCWProxy get copyWith =>
      _$ChronicleSocialVolumeSelectorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSocialVolumeSelector _$ChronicleSocialVolumeSelectorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSocialVolumeSelector', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'scopes']);
  final val = ChronicleSocialVolumeSelector(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChronicleSocialVolumeSelectorTypeEnumEnumMap, v),
    ),
    scopes: $checkedConvert(
      'scopes',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ChronicleSocialVolumeSelectorScopesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    platforms: $checkedConvert(
      'platforms',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    metrics: $checkedConvert(
      'metrics',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => $enumDecode(
              _$ChronicleSocialVolumeSelectorMetricsEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSocialVolumeSelectorToJson(
  ChronicleSocialVolumeSelector instance,
) => <String, dynamic>{
  'type': _$ChronicleSocialVolumeSelectorTypeEnumEnumMap[instance.type]!,
  'scopes': instance.scopes.map((e) => e.toJson()).toList(),
  'platforms': ?instance.platforms,
  'metrics': ?instance.metrics
      ?.map((e) => _$ChronicleSocialVolumeSelectorMetricsEnumEnumMap[e]!)
      .toList(),
};

const _$ChronicleSocialVolumeSelectorTypeEnumEnumMap = {
  ChronicleSocialVolumeSelectorTypeEnum.socialVolume: 'social-volume',
};

const _$ChronicleSocialVolumeSelectorMetricsEnumEnumMap = {
  ChronicleSocialVolumeSelectorMetricsEnum.posts: 'posts',
  ChronicleSocialVolumeSelectorMetricsEnum.uniqueAuthors: 'uniqueAuthors',
  ChronicleSocialVolumeSelectorMetricsEnum.reposts: 'reposts',
  ChronicleSocialVolumeSelectorMetricsEnum.engagement: 'engagement',
};
