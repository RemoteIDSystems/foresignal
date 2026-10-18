// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taf_payload_forecast_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TafPayloadForecastInnerCWProxy {
  TafPayloadForecastInner changeIndicator(String? changeIndicator);

  TafPayloadForecastInner from(DateTime? from);

  TafPayloadForecastInner to(DateTime? to);

  TafPayloadForecastInner wind(Map<String, Object>? wind);

  TafPayloadForecastInner visibility(String? visibility);

  TafPayloadForecastInner wx(List<String>? wx);

  TafPayloadForecastInner clouds(List<Map<String, Object>>? clouds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TafPayloadForecastInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TafPayloadForecastInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TafPayloadForecastInner call({
    String? changeIndicator,
    DateTime? from,
    DateTime? to,
    Map<String, Object>? wind,
    String? visibility,
    List<String>? wx,
    List<Map<String, Object>>? clouds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTafPayloadForecastInner.copyWith(...)` or call `instanceOfTafPayloadForecastInner.copyWith.fieldName(value)` for a single field.
class _$TafPayloadForecastInnerCWProxyImpl
    implements _$TafPayloadForecastInnerCWProxy {
  const _$TafPayloadForecastInnerCWProxyImpl(this._value);

  final TafPayloadForecastInner _value;

  @override
  TafPayloadForecastInner changeIndicator(String? changeIndicator) =>
      call(changeIndicator: changeIndicator);

  @override
  TafPayloadForecastInner from(DateTime? from) => call(from: from);

  @override
  TafPayloadForecastInner to(DateTime? to) => call(to: to);

  @override
  TafPayloadForecastInner wind(Map<String, Object>? wind) => call(wind: wind);

  @override
  TafPayloadForecastInner visibility(String? visibility) =>
      call(visibility: visibility);

  @override
  TafPayloadForecastInner wx(List<String>? wx) => call(wx: wx);

  @override
  TafPayloadForecastInner clouds(List<Map<String, Object>>? clouds) =>
      call(clouds: clouds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TafPayloadForecastInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TafPayloadForecastInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TafPayloadForecastInner call({
    Object? changeIndicator = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? wind = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
    Object? wx = const $CopyWithPlaceholder(),
    Object? clouds = const $CopyWithPlaceholder(),
  }) {
    return TafPayloadForecastInner(
      changeIndicator: changeIndicator == const $CopyWithPlaceholder()
          ? _value.changeIndicator
          // ignore: cast_nullable_to_non_nullable
          : changeIndicator as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      wind: wind == const $CopyWithPlaceholder()
          ? _value.wind
          // ignore: cast_nullable_to_non_nullable
          : wind as Map<String, Object>?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as String?,
      wx: wx == const $CopyWithPlaceholder()
          ? _value.wx
          // ignore: cast_nullable_to_non_nullable
          : wx as List<String>?,
      clouds: clouds == const $CopyWithPlaceholder()
          ? _value.clouds
          // ignore: cast_nullable_to_non_nullable
          : clouds as List<Map<String, Object>>?,
    );
  }
}

extension $TafPayloadForecastInnerCopyWith on TafPayloadForecastInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTafPayloadForecastInner.copyWith(...)` or `instanceOfTafPayloadForecastInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TafPayloadForecastInnerCWProxy get copyWith =>
      _$TafPayloadForecastInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TafPayloadForecastInner _$TafPayloadForecastInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TafPayloadForecastInner', json, ($checkedConvert) {
  final val = TafPayloadForecastInner(
    changeIndicator: $checkedConvert('changeIndicator', (v) => v as String?),
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    wind: $checkedConvert(
      'wind',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    visibility: $checkedConvert('visibility', (v) => v as String?),
    wx: $checkedConvert(
      'wx',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    clouds: $checkedConvert(
      'clouds',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$TafPayloadForecastInnerToJson(
  TafPayloadForecastInner instance,
) => <String, dynamic>{
  'changeIndicator': ?instance.changeIndicator,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'wind': ?instance.wind,
  'visibility': ?instance.visibility,
  'wx': ?instance.wx,
  'clouds': ?instance.clouds,
};
