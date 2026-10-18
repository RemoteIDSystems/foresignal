// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'overland_batch.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OverlandBatchCWProxy {
  OverlandBatch locations(List<OverlandBatchLocationsInner> locations);

  OverlandBatch trip(Object? trip);

  OverlandBatch current(Object? current);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  OverlandBatch call({
    List<OverlandBatchLocationsInner> locations,
    Object? trip,
    Object? current,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOverlandBatch.copyWith(...)` or call `instanceOfOverlandBatch.copyWith.fieldName(value)` for a single field.
class _$OverlandBatchCWProxyImpl implements _$OverlandBatchCWProxy {
  const _$OverlandBatchCWProxyImpl(this._value);

  final OverlandBatch _value;

  @override
  OverlandBatch locations(List<OverlandBatchLocationsInner> locations) =>
      call(locations: locations);

  @override
  OverlandBatch trip(Object? trip) => call(trip: trip);

  @override
  OverlandBatch current(Object? current) => call(current: current);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandBatch(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandBatch(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OverlandBatch call({
    Object? locations = const $CopyWithPlaceholder(),
    Object? trip = const $CopyWithPlaceholder(),
    Object? current = const $CopyWithPlaceholder(),
  }) {
    return OverlandBatch(
      locations: locations == const $CopyWithPlaceholder() || locations == null
          ? _value.locations
          // ignore: cast_nullable_to_non_nullable
          : locations as List<OverlandBatchLocationsInner>,
      trip: trip == const $CopyWithPlaceholder()
          ? _value.trip
          // ignore: cast_nullable_to_non_nullable
          : trip as Object?,
      current: current == const $CopyWithPlaceholder()
          ? _value.current
          // ignore: cast_nullable_to_non_nullable
          : current as Object?,
    );
  }
}

extension $OverlandBatchCopyWith on OverlandBatch {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOverlandBatch.copyWith(...)` or `instanceOfOverlandBatch.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OverlandBatchCWProxy get copyWith => _$OverlandBatchCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OverlandBatch _$OverlandBatchFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OverlandBatch', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['locations']);
      final val = OverlandBatch(
        locations: $checkedConvert(
          'locations',
          (v) => (v as List<dynamic>)
              .map(
                (e) => OverlandBatchLocationsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        trip: $checkedConvert('trip', (v) => v),
        current: $checkedConvert('current', (v) => v),
      );
      return val;
    });

Map<String, dynamic> _$OverlandBatchToJson(OverlandBatch instance) =>
    <String, dynamic>{
      'locations': instance.locations.map((e) => e.toJson()).toList(),
      'trip': ?instance.trip,
      'current': ?instance.current,
    };
