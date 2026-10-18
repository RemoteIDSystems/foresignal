// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'geofence_ref.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GeofenceRefCWProxy {
  GeofenceRef country(List<String>? country);

  GeofenceRef admin1(List<String>? admin1);

  GeofenceRef county(List<String>? county);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GeofenceRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GeofenceRef(...).copyWith(id: 12, name: "My name")
  /// ```
  GeofenceRef call({
    List<String>? country,
    List<String>? admin1,
    List<String>? county,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGeofenceRef.copyWith(...)` or call `instanceOfGeofenceRef.copyWith.fieldName(value)` for a single field.
class _$GeofenceRefCWProxyImpl implements _$GeofenceRefCWProxy {
  const _$GeofenceRefCWProxyImpl(this._value);

  final GeofenceRef _value;

  @override
  GeofenceRef country(List<String>? country) => call(country: country);

  @override
  GeofenceRef admin1(List<String>? admin1) => call(admin1: admin1);

  @override
  GeofenceRef county(List<String>? county) => call(county: county);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GeofenceRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GeofenceRef(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GeofenceRef call({
    Object? country = const $CopyWithPlaceholder(),
    Object? admin1 = const $CopyWithPlaceholder(),
    Object? county = const $CopyWithPlaceholder(),
  }) {
    return GeofenceRef(
      country: country == const $CopyWithPlaceholder()
          ? _value.country
          // ignore: cast_nullable_to_non_nullable
          : country as List<String>?,
      admin1: admin1 == const $CopyWithPlaceholder()
          ? _value.admin1
          // ignore: cast_nullable_to_non_nullable
          : admin1 as List<String>?,
      county: county == const $CopyWithPlaceholder()
          ? _value.county
          // ignore: cast_nullable_to_non_nullable
          : county as List<String>?,
    );
  }
}

extension $GeofenceRefCopyWith on GeofenceRef {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGeofenceRef.copyWith(...)` or `instanceOfGeofenceRef.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GeofenceRefCWProxy get copyWith => _$GeofenceRefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GeofenceRef _$GeofenceRefFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GeofenceRef', json, ($checkedConvert) {
      final val = GeofenceRef(
        country: $checkedConvert(
          'country',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        admin1: $checkedConvert(
          'admin1',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        county: $checkedConvert(
          'county',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GeofenceRefToJson(GeofenceRef instance) =>
    <String, dynamic>{
      'country': ?instance.country,
      'admin1': ?instance.admin1,
      'county': ?instance.county,
    };
