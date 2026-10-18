// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_suggestion_estimate.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSuggestionEstimateCWProxy {
  ChronicleSuggestionEstimate totalRowsPerDay(num? totalRowsPerDay);

  ChronicleSuggestionEstimate totalBytesPerDay(num? totalBytesPerDay);

  ChronicleSuggestionEstimate layers(
    List<ChronicleSuggestionEstimateLayersInner>? layers,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestionEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestionEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSuggestionEstimate call({
    num? totalRowsPerDay,
    num? totalBytesPerDay,
    List<ChronicleSuggestionEstimateLayersInner>? layers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSuggestionEstimate.copyWith(...)` or call `instanceOfChronicleSuggestionEstimate.copyWith.fieldName(value)` for a single field.
class _$ChronicleSuggestionEstimateCWProxyImpl
    implements _$ChronicleSuggestionEstimateCWProxy {
  const _$ChronicleSuggestionEstimateCWProxyImpl(this._value);

  final ChronicleSuggestionEstimate _value;

  @override
  ChronicleSuggestionEstimate totalRowsPerDay(num? totalRowsPerDay) =>
      call(totalRowsPerDay: totalRowsPerDay);

  @override
  ChronicleSuggestionEstimate totalBytesPerDay(num? totalBytesPerDay) =>
      call(totalBytesPerDay: totalBytesPerDay);

  @override
  ChronicleSuggestionEstimate layers(
    List<ChronicleSuggestionEstimateLayersInner>? layers,
  ) => call(layers: layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSuggestionEstimate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSuggestionEstimate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSuggestionEstimate call({
    Object? totalRowsPerDay = const $CopyWithPlaceholder(),
    Object? totalBytesPerDay = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSuggestionEstimate(
      totalRowsPerDay: totalRowsPerDay == const $CopyWithPlaceholder()
          ? _value.totalRowsPerDay
          // ignore: cast_nullable_to_non_nullable
          : totalRowsPerDay as num?,
      totalBytesPerDay: totalBytesPerDay == const $CopyWithPlaceholder()
          ? _value.totalBytesPerDay
          // ignore: cast_nullable_to_non_nullable
          : totalBytesPerDay as num?,
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<ChronicleSuggestionEstimateLayersInner>?,
    );
  }
}

extension $ChronicleSuggestionEstimateCopyWith on ChronicleSuggestionEstimate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSuggestionEstimate.copyWith(...)` or `instanceOfChronicleSuggestionEstimate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSuggestionEstimateCWProxy get copyWith =>
      _$ChronicleSuggestionEstimateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSuggestionEstimate _$ChronicleSuggestionEstimateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSuggestionEstimate', json, ($checkedConvert) {
  final val = ChronicleSuggestionEstimate(
    totalRowsPerDay: $checkedConvert('totalRowsPerDay', (v) => v as num?),
    totalBytesPerDay: $checkedConvert('totalBytesPerDay', (v) => v as num?),
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ChronicleSuggestionEstimateLayersInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSuggestionEstimateToJson(
  ChronicleSuggestionEstimate instance,
) => <String, dynamic>{
  'totalRowsPerDay': ?instance.totalRowsPerDay,
  'totalBytesPerDay': ?instance.totalBytesPerDay,
  'layers': ?instance.layers?.map((e) => e.toJson()).toList(),
};
