// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'holding_pattern_payload_all_of_nearest_airport.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HoldingPatternPayloadAllOfNearestAirportCWProxy {
  HoldingPatternPayloadAllOfNearestAirport ident(String ident);

  HoldingPatternPayloadAllOfNearestAirport name(String? name);

  HoldingPatternPayloadAllOfNearestAirport distanceKm(num? distanceKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldingPatternPayloadAllOfNearestAirport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldingPatternPayloadAllOfNearestAirport(...).copyWith(id: 12, name: "My name")
  /// ```
  HoldingPatternPayloadAllOfNearestAirport call({
    String ident,
    String? name,
    num? distanceKm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHoldingPatternPayloadAllOfNearestAirport.copyWith(...)` or call `instanceOfHoldingPatternPayloadAllOfNearestAirport.copyWith.fieldName(value)` for a single field.
class _$HoldingPatternPayloadAllOfNearestAirportCWProxyImpl
    implements _$HoldingPatternPayloadAllOfNearestAirportCWProxy {
  const _$HoldingPatternPayloadAllOfNearestAirportCWProxyImpl(this._value);

  final HoldingPatternPayloadAllOfNearestAirport _value;

  @override
  HoldingPatternPayloadAllOfNearestAirport ident(String ident) =>
      call(ident: ident);

  @override
  HoldingPatternPayloadAllOfNearestAirport name(String? name) =>
      call(name: name);

  @override
  HoldingPatternPayloadAllOfNearestAirport distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldingPatternPayloadAllOfNearestAirport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldingPatternPayloadAllOfNearestAirport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HoldingPatternPayloadAllOfNearestAirport call({
    Object? ident = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
  }) {
    return HoldingPatternPayloadAllOfNearestAirport(
      ident: ident == const $CopyWithPlaceholder() || ident == null
          ? _value.ident
          // ignore: cast_nullable_to_non_nullable
          : ident as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
    );
  }
}

extension $HoldingPatternPayloadAllOfNearestAirportCopyWith
    on HoldingPatternPayloadAllOfNearestAirport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHoldingPatternPayloadAllOfNearestAirport.copyWith(...)` or `instanceOfHoldingPatternPayloadAllOfNearestAirport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HoldingPatternPayloadAllOfNearestAirportCWProxy get copyWith =>
      _$HoldingPatternPayloadAllOfNearestAirportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HoldingPatternPayloadAllOfNearestAirport
_$HoldingPatternPayloadAllOfNearestAirportFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HoldingPatternPayloadAllOfNearestAirport', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['ident']);
      final val = HoldingPatternPayloadAllOfNearestAirport(
        ident: $checkedConvert('ident', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String?),
        distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$HoldingPatternPayloadAllOfNearestAirportToJson(
  HoldingPatternPayloadAllOfNearestAirport instance,
) => <String, dynamic>{
  'ident': instance.ident,
  'name': ?instance.name,
  'distanceKm': ?instance.distanceKm,
};
