// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_track_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleTrackResponseCWProxy {
  SampleTrackResponse record(String? record);

  SampleTrackResponse products(List<String> products);

  SampleTrackResponse points(List<SampleTrackResponsePointsInner> points);

  SampleTrackResponse attribution(List<Attribution>? attribution);

  SampleTrackResponse serverTime(DateTime? serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleTrackResponse call({
    String? record,
    List<String> products,
    List<SampleTrackResponsePointsInner> points,
    List<Attribution>? attribution,
    DateTime? serverTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleTrackResponse.copyWith(...)` or call `instanceOfSampleTrackResponse.copyWith.fieldName(value)` for a single field.
class _$SampleTrackResponseCWProxyImpl implements _$SampleTrackResponseCWProxy {
  const _$SampleTrackResponseCWProxyImpl(this._value);

  final SampleTrackResponse _value;

  @override
  SampleTrackResponse record(String? record) => call(record: record);

  @override
  SampleTrackResponse products(List<String> products) =>
      call(products: products);

  @override
  SampleTrackResponse points(List<SampleTrackResponsePointsInner> points) =>
      call(points: points);

  @override
  SampleTrackResponse attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  SampleTrackResponse serverTime(DateTime? serverTime) =>
      call(serverTime: serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleTrackResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleTrackResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleTrackResponse call({
    Object? record = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
  }) {
    return SampleTrackResponse(
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as String?,
      products: products == const $CopyWithPlaceholder() || products == null
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<SampleTrackResponsePointsInner>,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      serverTime: serverTime == const $CopyWithPlaceholder()
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime?,
    );
  }
}

extension $SampleTrackResponseCopyWith on SampleTrackResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleTrackResponse.copyWith(...)` or `instanceOfSampleTrackResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleTrackResponseCWProxy get copyWith =>
      _$SampleTrackResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleTrackResponse _$SampleTrackResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SampleTrackResponse', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['products', 'points']);
      final val = SampleTrackResponse(
        record: $checkedConvert('record', (v) => v as String?),
        products: $checkedConvert(
          'products',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        points: $checkedConvert(
          'points',
          (v) => (v as List<dynamic>)
              .map(
                (e) => SampleTrackResponsePointsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        serverTime: $checkedConvert(
          'serverTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SampleTrackResponseToJson(
  SampleTrackResponse instance,
) => <String, dynamic>{
  'record': ?instance.record,
  'products': instance.products,
  'points': instance.points.map((e) => e.toJson()).toList(),
  'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
  'serverTime': ?instance.serverTime?.toIso8601String(),
};
