// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_request_points_inner_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleRequestPointsInnerOneOfCWProxy {
  SampleRequestPointsInnerOneOf lon(num lon);

  SampleRequestPointsInnerOneOf lat(num lat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequestPointsInnerOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequestPointsInnerOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleRequestPointsInnerOneOf call({num lon, num lat});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleRequestPointsInnerOneOf.copyWith(...)` or call `instanceOfSampleRequestPointsInnerOneOf.copyWith.fieldName(value)` for a single field.
class _$SampleRequestPointsInnerOneOfCWProxyImpl
    implements _$SampleRequestPointsInnerOneOfCWProxy {
  const _$SampleRequestPointsInnerOneOfCWProxyImpl(this._value);

  final SampleRequestPointsInnerOneOf _value;

  @override
  SampleRequestPointsInnerOneOf lon(num lon) => call(lon: lon);

  @override
  SampleRequestPointsInnerOneOf lat(num lat) => call(lat: lat);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleRequestPointsInnerOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleRequestPointsInnerOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleRequestPointsInnerOneOf call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
  }) {
    return SampleRequestPointsInnerOneOf(
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
    );
  }
}

extension $SampleRequestPointsInnerOneOfCopyWith
    on SampleRequestPointsInnerOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleRequestPointsInnerOneOf.copyWith(...)` or `instanceOfSampleRequestPointsInnerOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleRequestPointsInnerOneOfCWProxy get copyWith =>
      _$SampleRequestPointsInnerOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleRequestPointsInnerOneOf _$SampleRequestPointsInnerOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SampleRequestPointsInnerOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lon', 'lat']);
  final val = SampleRequestPointsInnerOneOf(
    lon: $checkedConvert('lon', (v) => v as num),
    lat: $checkedConvert('lat', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$SampleRequestPointsInnerOneOfToJson(
  SampleRequestPointsInnerOneOf instance,
) => <String, dynamic>{'lon': instance.lon, 'lat': instance.lat};
