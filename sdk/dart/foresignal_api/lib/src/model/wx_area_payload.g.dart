// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'wx_area_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WxAreaPayloadCWProxy {
  WxAreaPayload field(WxAreaPayloadFieldEnum field);

  WxAreaPayload threshold(num threshold);

  WxAreaPayload aoi(String aoi);

  WxAreaPayload status(WeatherSectionStatus status);

  WxAreaPayload asOf(DateTime? asOf);

  WxAreaPayload areaKm2(num? areaKm2);

  WxAreaPayload maxValue(num? maxValue);

  WxAreaPayload coveragePct(int? coveragePct);

  WxAreaPayload automated(bool? automated);

  WxAreaPayload notAWarning(bool? notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WxAreaPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WxAreaPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  WxAreaPayload call({
    WxAreaPayloadFieldEnum field,
    num threshold,
    String aoi,
    WeatherSectionStatus status,
    DateTime? asOf,
    num? areaKm2,
    num? maxValue,
    int? coveragePct,
    bool? automated,
    bool? notAWarning,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWxAreaPayload.copyWith(...)` or call `instanceOfWxAreaPayload.copyWith.fieldName(value)` for a single field.
class _$WxAreaPayloadCWProxyImpl implements _$WxAreaPayloadCWProxy {
  const _$WxAreaPayloadCWProxyImpl(this._value);

  final WxAreaPayload _value;

  @override
  WxAreaPayload field(WxAreaPayloadFieldEnum field) => call(field: field);

  @override
  WxAreaPayload threshold(num threshold) => call(threshold: threshold);

  @override
  WxAreaPayload aoi(String aoi) => call(aoi: aoi);

  @override
  WxAreaPayload status(WeatherSectionStatus status) => call(status: status);

  @override
  WxAreaPayload asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WxAreaPayload areaKm2(num? areaKm2) => call(areaKm2: areaKm2);

  @override
  WxAreaPayload maxValue(num? maxValue) => call(maxValue: maxValue);

  @override
  WxAreaPayload coveragePct(int? coveragePct) => call(coveragePct: coveragePct);

  @override
  WxAreaPayload automated(bool? automated) => call(automated: automated);

  @override
  WxAreaPayload notAWarning(bool? notAWarning) =>
      call(notAWarning: notAWarning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WxAreaPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WxAreaPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WxAreaPayload call({
    Object? field = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
    Object? aoi = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? areaKm2 = const $CopyWithPlaceholder(),
    Object? maxValue = const $CopyWithPlaceholder(),
    Object? coveragePct = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
  }) {
    return WxAreaPayload(
      field: field == const $CopyWithPlaceholder() || field == null
          ? _value.field
          // ignore: cast_nullable_to_non_nullable
          : field as WxAreaPayloadFieldEnum,
      threshold: threshold == const $CopyWithPlaceholder() || threshold == null
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as num,
      aoi: aoi == const $CopyWithPlaceholder() || aoi == null
          ? _value.aoi
          // ignore: cast_nullable_to_non_nullable
          : aoi as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      areaKm2: areaKm2 == const $CopyWithPlaceholder()
          ? _value.areaKm2
          // ignore: cast_nullable_to_non_nullable
          : areaKm2 as num?,
      maxValue: maxValue == const $CopyWithPlaceholder()
          ? _value.maxValue
          // ignore: cast_nullable_to_non_nullable
          : maxValue as num?,
      coveragePct: coveragePct == const $CopyWithPlaceholder()
          ? _value.coveragePct
          // ignore: cast_nullable_to_non_nullable
          : coveragePct as int?,
      automated: automated == const $CopyWithPlaceholder()
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool?,
      notAWarning: notAWarning == const $CopyWithPlaceholder()
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool?,
    );
  }
}

extension $WxAreaPayloadCopyWith on WxAreaPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWxAreaPayload.copyWith(...)` or `instanceOfWxAreaPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WxAreaPayloadCWProxy get copyWith => _$WxAreaPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WxAreaPayload _$WxAreaPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WxAreaPayload', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['field', 'threshold', 'aoi', 'status']);
  final val = WxAreaPayload(
    field: $checkedConvert(
      'field',
      (v) => $enumDecode(_$WxAreaPayloadFieldEnumEnumMap, v),
    ),
    threshold: $checkedConvert('threshold', (v) => v as num),
    aoi: $checkedConvert('aoi', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    areaKm2: $checkedConvert('areaKm2', (v) => v as num?),
    maxValue: $checkedConvert('maxValue', (v) => v as num?),
    coveragePct: $checkedConvert('coveragePct', (v) => (v as num?)?.toInt()),
    automated: $checkedConvert('automated', (v) => v as bool?),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$WxAreaPayloadToJson(WxAreaPayload instance) =>
    <String, dynamic>{
      'field': _$WxAreaPayloadFieldEnumEnumMap[instance.field]!,
      'threshold': instance.threshold,
      'aoi': instance.aoi,
      'status': _$WeatherSectionStatusEnumMap[instance.status]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'areaKm2': ?instance.areaKm2,
      'maxValue': ?instance.maxValue,
      'coveragePct': ?instance.coveragePct,
      'automated': ?instance.automated,
      'notAWarning': ?instance.notAWarning,
    };

const _$WxAreaPayloadFieldEnumEnumMap = {
  WxAreaPayloadFieldEnum.refl: 'refl',
  WxAreaPayloadFieldEnum.mesh: 'mesh',
  WxAreaPayloadFieldEnum.precip: 'precip',
  WxAreaPayloadFieldEnum.rotation: 'rotation',
  WxAreaPayloadFieldEnum.qpe1h: 'qpe1h',
  WxAreaPayloadFieldEnum.ffg: 'ffg',
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
