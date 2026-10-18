// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'uv_forecast_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UvForecastPayloadCWProxy {
  UvForecastPayload zip(String zip);

  UvForecastPayload hours(List<UvForecastPayloadHoursInner> hours);

  UvForecastPayload maxUv(num? maxUv);

  UvForecastPayload maxAt(DateTime? maxAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UvForecastPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UvForecastPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  UvForecastPayload call({
    String zip,
    List<UvForecastPayloadHoursInner> hours,
    num? maxUv,
    DateTime? maxAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUvForecastPayload.copyWith(...)` or call `instanceOfUvForecastPayload.copyWith.fieldName(value)` for a single field.
class _$UvForecastPayloadCWProxyImpl implements _$UvForecastPayloadCWProxy {
  const _$UvForecastPayloadCWProxyImpl(this._value);

  final UvForecastPayload _value;

  @override
  UvForecastPayload zip(String zip) => call(zip: zip);

  @override
  UvForecastPayload hours(List<UvForecastPayloadHoursInner> hours) =>
      call(hours: hours);

  @override
  UvForecastPayload maxUv(num? maxUv) => call(maxUv: maxUv);

  @override
  UvForecastPayload maxAt(DateTime? maxAt) => call(maxAt: maxAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UvForecastPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UvForecastPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UvForecastPayload call({
    Object? zip = const $CopyWithPlaceholder(),
    Object? hours = const $CopyWithPlaceholder(),
    Object? maxUv = const $CopyWithPlaceholder(),
    Object? maxAt = const $CopyWithPlaceholder(),
  }) {
    return UvForecastPayload(
      zip: zip == const $CopyWithPlaceholder() || zip == null
          ? _value.zip
          // ignore: cast_nullable_to_non_nullable
          : zip as String,
      hours: hours == const $CopyWithPlaceholder() || hours == null
          ? _value.hours
          // ignore: cast_nullable_to_non_nullable
          : hours as List<UvForecastPayloadHoursInner>,
      maxUv: maxUv == const $CopyWithPlaceholder()
          ? _value.maxUv
          // ignore: cast_nullable_to_non_nullable
          : maxUv as num?,
      maxAt: maxAt == const $CopyWithPlaceholder()
          ? _value.maxAt
          // ignore: cast_nullable_to_non_nullable
          : maxAt as DateTime?,
    );
  }
}

extension $UvForecastPayloadCopyWith on UvForecastPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUvForecastPayload.copyWith(...)` or `instanceOfUvForecastPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UvForecastPayloadCWProxy get copyWith =>
      _$UvForecastPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UvForecastPayload _$UvForecastPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UvForecastPayload', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['zip', 'hours']);
      final val = UvForecastPayload(
        zip: $checkedConvert('zip', (v) => v as String),
        hours: $checkedConvert(
          'hours',
          (v) => (v as List<dynamic>)
              .map(
                (e) => UvForecastPayloadHoursInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        maxUv: $checkedConvert('maxUv', (v) => v as num?),
        maxAt: $checkedConvert(
          'maxAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$UvForecastPayloadToJson(UvForecastPayload instance) =>
    <String, dynamic>{
      'zip': instance.zip,
      'hours': instance.hours.map((e) => e.toJson()).toList(),
      'maxUv': ?instance.maxUv,
      'maxAt': ?instance.maxAt?.toIso8601String(),
    };
