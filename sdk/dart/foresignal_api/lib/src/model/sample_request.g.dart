// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleRequestCWProxy {
  SampleRequest points(List<SampleRequestPointsInner> points);

  SampleRequest products(List<String> products);

  SampleRequest time(String? time);

  SampleRequest units(SampleRequestUnitsEnum? units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleRequest call({
    List<SampleRequestPointsInner> points,
    List<String> products,
    String? time,
    SampleRequestUnitsEnum? units,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleRequest.copyWith(...)` or call `instanceOfSampleRequest.copyWith.fieldName(value)` for a single field.
class _$SampleRequestCWProxyImpl implements _$SampleRequestCWProxy {
  const _$SampleRequestCWProxyImpl(this._value);

  final SampleRequest _value;

  @override
  SampleRequest points(List<SampleRequestPointsInner> points) =>
      call(points: points);

  @override
  SampleRequest products(List<String> products) => call(products: products);

  @override
  SampleRequest time(String? time) => call(time: time);

  @override
  SampleRequest units(SampleRequestUnitsEnum? units) => call(units: units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleRequest call({
    Object? points = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
  }) {
    return SampleRequest(
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<SampleRequestPointsInner>,
      products: products == const $CopyWithPlaceholder() || products == null
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as String?,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as SampleRequestUnitsEnum?,
    );
  }
}

extension $SampleRequestCopyWith on SampleRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleRequest.copyWith(...)` or `instanceOfSampleRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleRequestCWProxy get copyWith => _$SampleRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleRequest _$SampleRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SampleRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['points', 'products']);
      final val = SampleRequest(
        points: $checkedConvert(
          'points',
          (v) => (v as List<dynamic>)
              .map(
                (e) => SampleRequestPointsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        products: $checkedConvert(
          'products',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        time: $checkedConvert('time', (v) => v as String? ?? 'latest'),
        units: $checkedConvert(
          'units',
          (v) => $enumDecodeNullable(_$SampleRequestUnitsEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SampleRequestToJson(SampleRequest instance) =>
    <String, dynamic>{
      'points': instance.points.map((e) => e.toJson()).toList(),
      'products': instance.products,
      'time': ?instance.time,
      'units': ?_$SampleRequestUnitsEnumEnumMap[instance.units],
    };

const _$SampleRequestUnitsEnumEnumMap = {
  SampleRequestUnitsEnum.metric: 'metric',
  SampleRequestUnitsEnum.imperial: 'imperial',
};
