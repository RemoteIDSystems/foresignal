// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_period_hazards_entered_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestPeriodHazardsEnteredInnerCWProxy {
  PlaceWeatherDigestPeriodHazardsEnteredInner kind(String kind);

  PlaceWeatherDigestPeriodHazardsEnteredInner title(String title);

  PlaceWeatherDigestPeriodHazardsEnteredInner at(DateTime at);

  PlaceWeatherDigestPeriodHazardsEnteredInner ref(String? ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestPeriodHazardsEnteredInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestPeriodHazardsEnteredInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestPeriodHazardsEnteredInner call({
    String kind,
    String title,
    DateTime at,
    String? ref,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestPeriodHazardsEnteredInner.copyWith(...)` or call `instanceOfPlaceWeatherDigestPeriodHazardsEnteredInner.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestPeriodHazardsEnteredInnerCWProxyImpl
    implements _$PlaceWeatherDigestPeriodHazardsEnteredInnerCWProxy {
  const _$PlaceWeatherDigestPeriodHazardsEnteredInnerCWProxyImpl(this._value);

  final PlaceWeatherDigestPeriodHazardsEnteredInner _value;

  @override
  PlaceWeatherDigestPeriodHazardsEnteredInner kind(String kind) =>
      call(kind: kind);

  @override
  PlaceWeatherDigestPeriodHazardsEnteredInner title(String title) =>
      call(title: title);

  @override
  PlaceWeatherDigestPeriodHazardsEnteredInner at(DateTime at) => call(at: at);

  @override
  PlaceWeatherDigestPeriodHazardsEnteredInner ref(String? ref) =>
      call(ref: ref);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestPeriodHazardsEnteredInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestPeriodHazardsEnteredInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestPeriodHazardsEnteredInner call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestPeriodHazardsEnteredInner(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
    );
  }
}

extension $PlaceWeatherDigestPeriodHazardsEnteredInnerCopyWith
    on PlaceWeatherDigestPeriodHazardsEnteredInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestPeriodHazardsEnteredInner.copyWith(...)` or `instanceOfPlaceWeatherDigestPeriodHazardsEnteredInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestPeriodHazardsEnteredInnerCWProxy get copyWith =>
      _$PlaceWeatherDigestPeriodHazardsEnteredInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestPeriodHazardsEnteredInner
_$PlaceWeatherDigestPeriodHazardsEnteredInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestPeriodHazardsEnteredInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['kind', 'title', 'at']);
  final val = PlaceWeatherDigestPeriodHazardsEnteredInner(
    kind: $checkedConvert('kind', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    ref: $checkedConvert('ref', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestPeriodHazardsEnteredInnerToJson(
  PlaceWeatherDigestPeriodHazardsEnteredInner instance,
) => <String, dynamic>{
  'kind': instance.kind,
  'title': instance.title,
  'at': instance.at.toIso8601String(),
  'ref': ?instance.ref,
};
