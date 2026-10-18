// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_report_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormReportPayloadCWProxy {
  StormReportPayload type(StormReportPayloadTypeEnum type);

  StormReportPayload magnitude(Object? magnitude);

  StormReportPayload magnitudeUnits(
    StormReportPayloadMagnitudeUnitsEnum? magnitudeUnits,
  );

  StormReportPayload location(String location);

  StormReportPayload county(String? county);

  StormReportPayload state(String? state);

  StormReportPayload wfo(String? wfo);

  StormReportPayload comments(String? comments);

  StormReportPayload title(String title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormReportPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormReportPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  StormReportPayload call({
    StormReportPayloadTypeEnum type,
    Object? magnitude,
    StormReportPayloadMagnitudeUnitsEnum? magnitudeUnits,
    String location,
    String? county,
    String? state,
    String? wfo,
    String? comments,
    String title,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormReportPayload.copyWith(...)` or call `instanceOfStormReportPayload.copyWith.fieldName(value)` for a single field.
class _$StormReportPayloadCWProxyImpl implements _$StormReportPayloadCWProxy {
  const _$StormReportPayloadCWProxyImpl(this._value);

  final StormReportPayload _value;

  @override
  StormReportPayload type(StormReportPayloadTypeEnum type) => call(type: type);

  @override
  StormReportPayload magnitude(Object? magnitude) => call(magnitude: magnitude);

  @override
  StormReportPayload magnitudeUnits(
    StormReportPayloadMagnitudeUnitsEnum? magnitudeUnits,
  ) => call(magnitudeUnits: magnitudeUnits);

  @override
  StormReportPayload location(String location) => call(location: location);

  @override
  StormReportPayload county(String? county) => call(county: county);

  @override
  StormReportPayload state(String? state) => call(state: state);

  @override
  StormReportPayload wfo(String? wfo) => call(wfo: wfo);

  @override
  StormReportPayload comments(String? comments) => call(comments: comments);

  @override
  StormReportPayload title(String title) => call(title: title);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormReportPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormReportPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormReportPayload call({
    Object? type = const $CopyWithPlaceholder(),
    Object? magnitude = const $CopyWithPlaceholder(),
    Object? magnitudeUnits = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
    Object? county = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? wfo = const $CopyWithPlaceholder(),
    Object? comments = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
  }) {
    return StormReportPayload(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as StormReportPayloadTypeEnum,
      magnitude: magnitude == const $CopyWithPlaceholder()
          ? _value.magnitude
          // ignore: cast_nullable_to_non_nullable
          : magnitude as Object?,
      magnitudeUnits: magnitudeUnits == const $CopyWithPlaceholder()
          ? _value.magnitudeUnits
          // ignore: cast_nullable_to_non_nullable
          : magnitudeUnits as StormReportPayloadMagnitudeUnitsEnum?,
      location: location == const $CopyWithPlaceholder() || location == null
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as String,
      county: county == const $CopyWithPlaceholder()
          ? _value.county
          // ignore: cast_nullable_to_non_nullable
          : county as String?,
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as String?,
      wfo: wfo == const $CopyWithPlaceholder()
          ? _value.wfo
          // ignore: cast_nullable_to_non_nullable
          : wfo as String?,
      comments: comments == const $CopyWithPlaceholder()
          ? _value.comments
          // ignore: cast_nullable_to_non_nullable
          : comments as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
    );
  }
}

extension $StormReportPayloadCopyWith on StormReportPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormReportPayload.copyWith(...)` or `instanceOfStormReportPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormReportPayloadCWProxy get copyWith =>
      _$StormReportPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormReportPayload _$StormReportPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StormReportPayload', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'location', 'title']);
      final val = StormReportPayload(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$StormReportPayloadTypeEnumEnumMap, v),
        ),
        magnitude: $checkedConvert('magnitude', (v) => v),
        magnitudeUnits: $checkedConvert(
          'magnitudeUnits',
          (v) => $enumDecodeNullable(
            _$StormReportPayloadMagnitudeUnitsEnumEnumMap,
            v,
          ),
        ),
        location: $checkedConvert('location', (v) => v as String),
        county: $checkedConvert('county', (v) => v as String?),
        state: $checkedConvert('state', (v) => v as String?),
        wfo: $checkedConvert('wfo', (v) => v as String?),
        comments: $checkedConvert('comments', (v) => v as String?),
        title: $checkedConvert('title', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$StormReportPayloadToJson(
  StormReportPayload instance,
) => <String, dynamic>{
  'type': _$StormReportPayloadTypeEnumEnumMap[instance.type]!,
  'magnitude': ?instance.magnitude,
  'magnitudeUnits':
      ?_$StormReportPayloadMagnitudeUnitsEnumEnumMap[instance.magnitudeUnits],
  'location': instance.location,
  'county': ?instance.county,
  'state': ?instance.state,
  'wfo': ?instance.wfo,
  'comments': ?instance.comments,
  'title': instance.title,
};

const _$StormReportPayloadTypeEnumEnumMap = {
  StormReportPayloadTypeEnum.tornado: 'tornado',
  StormReportPayloadTypeEnum.wind: 'wind',
  StormReportPayloadTypeEnum.hail: 'hail',
};

const _$StormReportPayloadMagnitudeUnitsEnumEnumMap = {
  StormReportPayloadMagnitudeUnitsEnum.EF: 'EF',
  StormReportPayloadMagnitudeUnitsEnum.mph: 'mph',
  StormReportPayloadMagnitudeUnitsEnum.in_: 'in',
};
