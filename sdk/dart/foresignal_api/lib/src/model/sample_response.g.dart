// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SampleResponseCWProxy {
  SampleResponse lon(num lon);

  SampleResponse lat(num lat);

  SampleResponse placeId(String? placeId);

  SampleResponse timezone(String? timezone);

  SampleResponse time(String? time);

  SampleResponse serverTime(DateTime serverTime);

  SampleResponse values(List<SampleValue> values);

  SampleResponse disagreement(ModelDisagreement? disagreement);

  SampleResponse position(SamplePosition? position);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  SampleResponse call({
    num lon,
    num lat,
    String? placeId,
    String? timezone,
    String? time,
    DateTime serverTime,
    List<SampleValue> values,
    ModelDisagreement? disagreement,
    SamplePosition? position,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSampleResponse.copyWith(...)` or call `instanceOfSampleResponse.copyWith.fieldName(value)` for a single field.
class _$SampleResponseCWProxyImpl implements _$SampleResponseCWProxy {
  const _$SampleResponseCWProxyImpl(this._value);

  final SampleResponse _value;

  @override
  SampleResponse lon(num lon) => call(lon: lon);

  @override
  SampleResponse lat(num lat) => call(lat: lat);

  @override
  SampleResponse placeId(String? placeId) => call(placeId: placeId);

  @override
  SampleResponse timezone(String? timezone) => call(timezone: timezone);

  @override
  SampleResponse time(String? time) => call(time: time);

  @override
  SampleResponse serverTime(DateTime serverTime) =>
      call(serverTime: serverTime);

  @override
  SampleResponse values(List<SampleValue> values) => call(values: values);

  @override
  SampleResponse disagreement(ModelDisagreement? disagreement) =>
      call(disagreement: disagreement);

  @override
  SampleResponse position(SamplePosition? position) => call(position: position);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SampleResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SampleResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SampleResponse call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
    Object? values = const $CopyWithPlaceholder(),
    Object? disagreement = const $CopyWithPlaceholder(),
    Object? position = const $CopyWithPlaceholder(),
  }) {
    return SampleResponse(
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as String?,
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
      values: values == const $CopyWithPlaceholder() || values == null
          ? _value.values
          // ignore: cast_nullable_to_non_nullable
          : values as List<SampleValue>,
      disagreement: disagreement == const $CopyWithPlaceholder()
          ? _value.disagreement
          // ignore: cast_nullable_to_non_nullable
          : disagreement as ModelDisagreement?,
      position: position == const $CopyWithPlaceholder()
          ? _value.position
          // ignore: cast_nullable_to_non_nullable
          : position as SamplePosition?,
    );
  }
}

extension $SampleResponseCopyWith on SampleResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSampleResponse.copyWith(...)` or `instanceOfSampleResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SampleResponseCWProxy get copyWith => _$SampleResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SampleResponse _$SampleResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SampleResponse', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lon', 'lat', 'serverTime', 'values']);
  final val = SampleResponse(
    lon: $checkedConvert('lon', (v) => v as num),
    lat: $checkedConvert('lat', (v) => v as num),
    placeId: $checkedConvert('placeId', (v) => v as String?),
    timezone: $checkedConvert('timezone', (v) => v as String?),
    time: $checkedConvert('time', (v) => v as String?),
    serverTime: $checkedConvert(
      'serverTime',
      (v) => DateTime.parse(v as String),
    ),
    values: $checkedConvert(
      'values',
      (v) => (v as List<dynamic>)
          .map((e) => SampleValue.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    disagreement: $checkedConvert(
      'disagreement',
      (v) => v == null
          ? null
          : ModelDisagreement.fromJson(v as Map<String, dynamic>),
    ),
    position: $checkedConvert(
      'position',
      (v) =>
          v == null ? null : SamplePosition.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SampleResponseToJson(SampleResponse instance) =>
    <String, dynamic>{
      'lon': instance.lon,
      'lat': instance.lat,
      'placeId': ?instance.placeId,
      'timezone': ?instance.timezone,
      'time': ?instance.time,
      'serverTime': instance.serverTime.toIso8601String(),
      'values': instance.values.map((e) => e.toJson()).toList(),
      'disagreement': ?instance.disagreement?.toJson(),
      'position': ?instance.position?.toJson(),
    };
