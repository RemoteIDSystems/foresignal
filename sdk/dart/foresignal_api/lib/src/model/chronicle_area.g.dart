// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleAreaCWProxy {
  ChronicleArea geometry(Geometry? geometry);

  ChronicleArea placeIds(List<String>? placeIds);

  ChronicleArea regionCodes(List<String>? regionCodes);

  ChronicleArea bufferKm(num? bufferKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleArea(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleArea call({
    Geometry? geometry,
    List<String>? placeIds,
    List<String>? regionCodes,
    num? bufferKm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleArea.copyWith(...)` or call `instanceOfChronicleArea.copyWith.fieldName(value)` for a single field.
class _$ChronicleAreaCWProxyImpl implements _$ChronicleAreaCWProxy {
  const _$ChronicleAreaCWProxyImpl(this._value);

  final ChronicleArea _value;

  @override
  ChronicleArea geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  ChronicleArea placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  ChronicleArea regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  ChronicleArea bufferKm(num? bufferKm) => call(bufferKm: bufferKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleArea call({
    Object? geometry = const $CopyWithPlaceholder(),
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? bufferKm = const $CopyWithPlaceholder(),
  }) {
    return ChronicleArea(
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      bufferKm: bufferKm == const $CopyWithPlaceholder()
          ? _value.bufferKm
          // ignore: cast_nullable_to_non_nullable
          : bufferKm as num?,
    );
  }
}

extension $ChronicleAreaCopyWith on ChronicleArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleArea.copyWith(...)` or `instanceOfChronicleArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleAreaCWProxy get copyWith => _$ChronicleAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleArea _$ChronicleAreaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleArea', json, ($checkedConvert) {
      final val = ChronicleArea(
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        placeIds: $checkedConvert(
          'placeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bufferKm: $checkedConvert('bufferKm', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleAreaToJson(ChronicleArea instance) =>
    <String, dynamic>{
      'geometry': ?instance.geometry?.toJson(),
      'placeIds': ?instance.placeIds,
      'regionCodes': ?instance.regionCodes,
      'bufferKm': ?instance.bufferKm,
    };
