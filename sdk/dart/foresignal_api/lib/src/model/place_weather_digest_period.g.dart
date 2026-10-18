// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_period.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestPeriodCWProxy {
  PlaceWeatherDigestPeriod from(DateTime? from);

  PlaceWeatherDigestPeriod to(DateTime? to);

  PlaceWeatherDigestPeriod partial(bool? partial);

  PlaceWeatherDigestPeriod maxDbz(num? maxDbz);

  PlaceWeatherDigestPeriod maxMeshMm(num? maxMeshMm);

  PlaceWeatherDigestPeriod lightningTotal(int? lightningTotal);

  PlaceWeatherDigestPeriod hazardsEntered(
    List<PlaceWeatherDigestPeriodHazardsEnteredInner>? hazardsEntered,
  );

  PlaceWeatherDigestPeriod notes(List<String>? notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestPeriod(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestPeriod(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestPeriod call({
    DateTime? from,
    DateTime? to,
    bool? partial,
    num? maxDbz,
    num? maxMeshMm,
    int? lightningTotal,
    List<PlaceWeatherDigestPeriodHazardsEnteredInner>? hazardsEntered,
    List<String>? notes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestPeriod.copyWith(...)` or call `instanceOfPlaceWeatherDigestPeriod.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestPeriodCWProxyImpl
    implements _$PlaceWeatherDigestPeriodCWProxy {
  const _$PlaceWeatherDigestPeriodCWProxyImpl(this._value);

  final PlaceWeatherDigestPeriod _value;

  @override
  PlaceWeatherDigestPeriod from(DateTime? from) => call(from: from);

  @override
  PlaceWeatherDigestPeriod to(DateTime? to) => call(to: to);

  @override
  PlaceWeatherDigestPeriod partial(bool? partial) => call(partial: partial);

  @override
  PlaceWeatherDigestPeriod maxDbz(num? maxDbz) => call(maxDbz: maxDbz);

  @override
  PlaceWeatherDigestPeriod maxMeshMm(num? maxMeshMm) =>
      call(maxMeshMm: maxMeshMm);

  @override
  PlaceWeatherDigestPeriod lightningTotal(int? lightningTotal) =>
      call(lightningTotal: lightningTotal);

  @override
  PlaceWeatherDigestPeriod hazardsEntered(
    List<PlaceWeatherDigestPeriodHazardsEnteredInner>? hazardsEntered,
  ) => call(hazardsEntered: hazardsEntered);

  @override
  PlaceWeatherDigestPeriod notes(List<String>? notes) => call(notes: notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestPeriod(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestPeriod(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestPeriod call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? partial = const $CopyWithPlaceholder(),
    Object? maxDbz = const $CopyWithPlaceholder(),
    Object? maxMeshMm = const $CopyWithPlaceholder(),
    Object? lightningTotal = const $CopyWithPlaceholder(),
    Object? hazardsEntered = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestPeriod(
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      partial: partial == const $CopyWithPlaceholder()
          ? _value.partial
          // ignore: cast_nullable_to_non_nullable
          : partial as bool?,
      maxDbz: maxDbz == const $CopyWithPlaceholder()
          ? _value.maxDbz
          // ignore: cast_nullable_to_non_nullable
          : maxDbz as num?,
      maxMeshMm: maxMeshMm == const $CopyWithPlaceholder()
          ? _value.maxMeshMm
          // ignore: cast_nullable_to_non_nullable
          : maxMeshMm as num?,
      lightningTotal: lightningTotal == const $CopyWithPlaceholder()
          ? _value.lightningTotal
          // ignore: cast_nullable_to_non_nullable
          : lightningTotal as int?,
      hazardsEntered: hazardsEntered == const $CopyWithPlaceholder()
          ? _value.hazardsEntered
          // ignore: cast_nullable_to_non_nullable
          : hazardsEntered
                as List<PlaceWeatherDigestPeriodHazardsEnteredInner>?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as List<String>?,
    );
  }
}

extension $PlaceWeatherDigestPeriodCopyWith on PlaceWeatherDigestPeriod {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestPeriod.copyWith(...)` or `instanceOfPlaceWeatherDigestPeriod.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestPeriodCWProxy get copyWith =>
      _$PlaceWeatherDigestPeriodCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestPeriod _$PlaceWeatherDigestPeriodFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestPeriod', json, ($checkedConvert) {
  final val = PlaceWeatherDigestPeriod(
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    partial: $checkedConvert('partial', (v) => v as bool?),
    maxDbz: $checkedConvert('maxDbz', (v) => v as num?),
    maxMeshMm: $checkedConvert('maxMeshMm', (v) => v as num?),
    lightningTotal: $checkedConvert(
      'lightningTotal',
      (v) => (v as num?)?.toInt(),
    ),
    hazardsEntered: $checkedConvert(
      'hazardsEntered',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => PlaceWeatherDigestPeriodHazardsEnteredInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    notes: $checkedConvert(
      'notes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestPeriodToJson(
  PlaceWeatherDigestPeriod instance,
) => <String, dynamic>{
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'partial': ?instance.partial,
  'maxDbz': ?instance.maxDbz,
  'maxMeshMm': ?instance.maxMeshMm,
  'lightningTotal': ?instance.lightningTotal,
  'hazardsEntered': ?instance.hazardsEntered?.map((e) => e.toJson()).toList(),
  'notes': ?instance.notes,
};
