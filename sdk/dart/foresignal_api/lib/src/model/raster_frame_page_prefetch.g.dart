// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'raster_frame_page_prefetch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RasterFramePagePrefetchCWProxy {
  RasterFramePagePrefetch recommendedFrames(int recommendedFrames);

  RasterFramePagePrefetch maxZoomForAnimation(int? maxZoomForAnimation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFramePagePrefetch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFramePagePrefetch(...).copyWith(id: 12, name: "My name")
  /// ```
  RasterFramePagePrefetch call({
    int recommendedFrames,
    int? maxZoomForAnimation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRasterFramePagePrefetch.copyWith(...)` or call `instanceOfRasterFramePagePrefetch.copyWith.fieldName(value)` for a single field.
class _$RasterFramePagePrefetchCWProxyImpl
    implements _$RasterFramePagePrefetchCWProxy {
  const _$RasterFramePagePrefetchCWProxyImpl(this._value);

  final RasterFramePagePrefetch _value;

  @override
  RasterFramePagePrefetch recommendedFrames(int recommendedFrames) =>
      call(recommendedFrames: recommendedFrames);

  @override
  RasterFramePagePrefetch maxZoomForAnimation(int? maxZoomForAnimation) =>
      call(maxZoomForAnimation: maxZoomForAnimation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFramePagePrefetch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFramePagePrefetch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RasterFramePagePrefetch call({
    Object? recommendedFrames = const $CopyWithPlaceholder(),
    Object? maxZoomForAnimation = const $CopyWithPlaceholder(),
  }) {
    return RasterFramePagePrefetch(
      recommendedFrames:
          recommendedFrames == const $CopyWithPlaceholder() ||
              recommendedFrames == null
          ? _value.recommendedFrames
          // ignore: cast_nullable_to_non_nullable
          : recommendedFrames as int,
      maxZoomForAnimation: maxZoomForAnimation == const $CopyWithPlaceholder()
          ? _value.maxZoomForAnimation
          // ignore: cast_nullable_to_non_nullable
          : maxZoomForAnimation as int?,
    );
  }
}

extension $RasterFramePagePrefetchCopyWith on RasterFramePagePrefetch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRasterFramePagePrefetch.copyWith(...)` or `instanceOfRasterFramePagePrefetch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RasterFramePagePrefetchCWProxy get copyWith =>
      _$RasterFramePagePrefetchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RasterFramePagePrefetch _$RasterFramePagePrefetchFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RasterFramePagePrefetch', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['recommendedFrames']);
  final val = RasterFramePagePrefetch(
    recommendedFrames: $checkedConvert(
      'recommendedFrames',
      (v) => (v as num).toInt(),
    ),
    maxZoomForAnimation: $checkedConvert(
      'maxZoomForAnimation',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$RasterFramePagePrefetchToJson(
  RasterFramePagePrefetch instance,
) => <String, dynamic>{
  'recommendedFrames': instance.recommendedFrames,
  'maxZoomForAnimation': ?instance.maxZoomForAnimation,
};
