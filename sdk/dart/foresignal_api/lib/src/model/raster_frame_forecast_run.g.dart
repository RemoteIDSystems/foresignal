// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'raster_frame_forecast_run.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RasterFrameForecastRunCWProxy {
  RasterFrameForecastRun productId(String productId);

  RasterFrameForecastRun run(String run);

  RasterFrameForecastRun steps(List<int> steps);

  RasterFrameForecastRun validTimes(List<DateTime> validTimes);

  RasterFrameForecastRun tilesTemplate(String tilesTemplate);

  RasterFrameForecastRun partial(bool? partial);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrameForecastRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrameForecastRun(...).copyWith(id: 12, name: "My name")
  /// ```
  RasterFrameForecastRun call({
    String productId,
    String run,
    List<int> steps,
    List<DateTime> validTimes,
    String tilesTemplate,
    bool? partial,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRasterFrameForecastRun.copyWith(...)` or call `instanceOfRasterFrameForecastRun.copyWith.fieldName(value)` for a single field.
class _$RasterFrameForecastRunCWProxyImpl
    implements _$RasterFrameForecastRunCWProxy {
  const _$RasterFrameForecastRunCWProxyImpl(this._value);

  final RasterFrameForecastRun _value;

  @override
  RasterFrameForecastRun productId(String productId) =>
      call(productId: productId);

  @override
  RasterFrameForecastRun run(String run) => call(run: run);

  @override
  RasterFrameForecastRun steps(List<int> steps) => call(steps: steps);

  @override
  RasterFrameForecastRun validTimes(List<DateTime> validTimes) =>
      call(validTimes: validTimes);

  @override
  RasterFrameForecastRun tilesTemplate(String tilesTemplate) =>
      call(tilesTemplate: tilesTemplate);

  @override
  RasterFrameForecastRun partial(bool? partial) => call(partial: partial);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrameForecastRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrameForecastRun(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RasterFrameForecastRun call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? steps = const $CopyWithPlaceholder(),
    Object? validTimes = const $CopyWithPlaceholder(),
    Object? tilesTemplate = const $CopyWithPlaceholder(),
    Object? partial = const $CopyWithPlaceholder(),
  }) {
    return RasterFrameForecastRun(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      run: run == const $CopyWithPlaceholder() || run == null
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String,
      steps: steps == const $CopyWithPlaceholder() || steps == null
          ? _value.steps
          // ignore: cast_nullable_to_non_nullable
          : steps as List<int>,
      validTimes:
          validTimes == const $CopyWithPlaceholder() || validTimes == null
          ? _value.validTimes
          // ignore: cast_nullable_to_non_nullable
          : validTimes as List<DateTime>,
      tilesTemplate:
          tilesTemplate == const $CopyWithPlaceholder() || tilesTemplate == null
          ? _value.tilesTemplate
          // ignore: cast_nullable_to_non_nullable
          : tilesTemplate as String,
      partial: partial == const $CopyWithPlaceholder()
          ? _value.partial
          // ignore: cast_nullable_to_non_nullable
          : partial as bool?,
    );
  }
}

extension $RasterFrameForecastRunCopyWith on RasterFrameForecastRun {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRasterFrameForecastRun.copyWith(...)` or `instanceOfRasterFrameForecastRun.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RasterFrameForecastRunCWProxy get copyWith =>
      _$RasterFrameForecastRunCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RasterFrameForecastRun _$RasterFrameForecastRunFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RasterFrameForecastRun', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'productId',
      'run',
      'steps',
      'validTimes',
      'tilesTemplate',
    ],
  );
  final val = RasterFrameForecastRun(
    productId: $checkedConvert('productId', (v) => v as String),
    run: $checkedConvert('run', (v) => v as String),
    steps: $checkedConvert(
      'steps',
      (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
    ),
    validTimes: $checkedConvert(
      'validTimes',
      (v) =>
          (v as List<dynamic>).map((e) => DateTime.parse(e as String)).toList(),
    ),
    tilesTemplate: $checkedConvert('tilesTemplate', (v) => v as String),
    partial: $checkedConvert('partial', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$RasterFrameForecastRunToJson(
  RasterFrameForecastRun instance,
) => <String, dynamic>{
  'productId': instance.productId,
  'run': instance.run,
  'steps': instance.steps,
  'validTimes': instance.validTimes.map((e) => e.toIso8601String()).toList(),
  'tilesTemplate': instance.tilesTemplate,
  'partial': ?instance.partial,
};
