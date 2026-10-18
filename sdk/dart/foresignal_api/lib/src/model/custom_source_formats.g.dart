// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'custom_source_formats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CustomSourceFormatsCWProxy {
  CustomSourceFormats native_(Object? native_);

  CustomSourceFormats alertmanager(
    CustomSourceFormatsAlertmanager? alertmanager,
  );

  CustomSourceFormats cloudevents(CustomSourceFormatsCloudevents? cloudevents);

  CustomSourceFormats cap(CustomSourceFormatsCap? cap);

  CustomSourceFormats mapping(Object? mapping);

  CustomSourceFormats owntracks(CustomSourceFormatsOwntracks? owntracks);

  CustomSourceFormats osmand(CustomSourceFormatsOsmand? osmand);

  CustomSourceFormats overland(CustomSourceFormatsOverland? overland);

  CustomSourceFormats gpslogger(CustomSourceFormatsGpslogger? gpslogger);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormats(...).copyWith(id: 12, name: "My name")
  /// ```
  CustomSourceFormats call({
    Object? native_,
    CustomSourceFormatsAlertmanager? alertmanager,
    CustomSourceFormatsCloudevents? cloudevents,
    CustomSourceFormatsCap? cap,
    Object? mapping,
    CustomSourceFormatsOwntracks? owntracks,
    CustomSourceFormatsOsmand? osmand,
    CustomSourceFormatsOverland? overland,
    CustomSourceFormatsGpslogger? gpslogger,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCustomSourceFormats.copyWith(...)` or call `instanceOfCustomSourceFormats.copyWith.fieldName(value)` for a single field.
class _$CustomSourceFormatsCWProxyImpl implements _$CustomSourceFormatsCWProxy {
  const _$CustomSourceFormatsCWProxyImpl(this._value);

  final CustomSourceFormats _value;

  @override
  CustomSourceFormats native_(Object? native_) => call(native_: native_);

  @override
  CustomSourceFormats alertmanager(
    CustomSourceFormatsAlertmanager? alertmanager,
  ) => call(alertmanager: alertmanager);

  @override
  CustomSourceFormats cloudevents(
    CustomSourceFormatsCloudevents? cloudevents,
  ) => call(cloudevents: cloudevents);

  @override
  CustomSourceFormats cap(CustomSourceFormatsCap? cap) => call(cap: cap);

  @override
  CustomSourceFormats mapping(Object? mapping) => call(mapping: mapping);

  @override
  CustomSourceFormats owntracks(CustomSourceFormatsOwntracks? owntracks) =>
      call(owntracks: owntracks);

  @override
  CustomSourceFormats osmand(CustomSourceFormatsOsmand? osmand) =>
      call(osmand: osmand);

  @override
  CustomSourceFormats overland(CustomSourceFormatsOverland? overland) =>
      call(overland: overland);

  @override
  CustomSourceFormats gpslogger(CustomSourceFormatsGpslogger? gpslogger) =>
      call(gpslogger: gpslogger);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CustomSourceFormats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CustomSourceFormats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CustomSourceFormats call({
    Object? native_ = const $CopyWithPlaceholder(),
    Object? alertmanager = const $CopyWithPlaceholder(),
    Object? cloudevents = const $CopyWithPlaceholder(),
    Object? cap = const $CopyWithPlaceholder(),
    Object? mapping = const $CopyWithPlaceholder(),
    Object? owntracks = const $CopyWithPlaceholder(),
    Object? osmand = const $CopyWithPlaceholder(),
    Object? overland = const $CopyWithPlaceholder(),
    Object? gpslogger = const $CopyWithPlaceholder(),
  }) {
    return CustomSourceFormats(
      native_: native_ == const $CopyWithPlaceholder()
          ? _value.native_
          // ignore: cast_nullable_to_non_nullable
          : native_ as Object?,
      alertmanager: alertmanager == const $CopyWithPlaceholder()
          ? _value.alertmanager
          // ignore: cast_nullable_to_non_nullable
          : alertmanager as CustomSourceFormatsAlertmanager?,
      cloudevents: cloudevents == const $CopyWithPlaceholder()
          ? _value.cloudevents
          // ignore: cast_nullable_to_non_nullable
          : cloudevents as CustomSourceFormatsCloudevents?,
      cap: cap == const $CopyWithPlaceholder()
          ? _value.cap
          // ignore: cast_nullable_to_non_nullable
          : cap as CustomSourceFormatsCap?,
      mapping: mapping == const $CopyWithPlaceholder()
          ? _value.mapping
          // ignore: cast_nullable_to_non_nullable
          : mapping as Object?,
      owntracks: owntracks == const $CopyWithPlaceholder()
          ? _value.owntracks
          // ignore: cast_nullable_to_non_nullable
          : owntracks as CustomSourceFormatsOwntracks?,
      osmand: osmand == const $CopyWithPlaceholder()
          ? _value.osmand
          // ignore: cast_nullable_to_non_nullable
          : osmand as CustomSourceFormatsOsmand?,
      overland: overland == const $CopyWithPlaceholder()
          ? _value.overland
          // ignore: cast_nullable_to_non_nullable
          : overland as CustomSourceFormatsOverland?,
      gpslogger: gpslogger == const $CopyWithPlaceholder()
          ? _value.gpslogger
          // ignore: cast_nullable_to_non_nullable
          : gpslogger as CustomSourceFormatsGpslogger?,
    );
  }
}

extension $CustomSourceFormatsCopyWith on CustomSourceFormats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCustomSourceFormats.copyWith(...)` or `instanceOfCustomSourceFormats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CustomSourceFormatsCWProxy get copyWith =>
      _$CustomSourceFormatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CustomSourceFormats _$CustomSourceFormatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CustomSourceFormats', json, ($checkedConvert) {
  final val = CustomSourceFormats(
    native_: $checkedConvert('native', (v) => v),
    alertmanager: $checkedConvert(
      'alertmanager',
      (v) => v == null
          ? null
          : CustomSourceFormatsAlertmanager.fromJson(v as Map<String, dynamic>),
    ),
    cloudevents: $checkedConvert(
      'cloudevents',
      (v) => v == null
          ? null
          : CustomSourceFormatsCloudevents.fromJson(v as Map<String, dynamic>),
    ),
    cap: $checkedConvert(
      'cap',
      (v) => v == null
          ? null
          : CustomSourceFormatsCap.fromJson(v as Map<String, dynamic>),
    ),
    mapping: $checkedConvert('mapping', (v) => v),
    owntracks: $checkedConvert(
      'owntracks',
      (v) => v == null
          ? null
          : CustomSourceFormatsOwntracks.fromJson(v as Map<String, dynamic>),
    ),
    osmand: $checkedConvert(
      'osmand',
      (v) => v == null
          ? null
          : CustomSourceFormatsOsmand.fromJson(v as Map<String, dynamic>),
    ),
    overland: $checkedConvert(
      'overland',
      (v) => v == null
          ? null
          : CustomSourceFormatsOverland.fromJson(v as Map<String, dynamic>),
    ),
    gpslogger: $checkedConvert(
      'gpslogger',
      (v) => v == null
          ? null
          : CustomSourceFormatsGpslogger.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
}, fieldKeyMap: const {'native_': 'native'});

Map<String, dynamic> _$CustomSourceFormatsToJson(
  CustomSourceFormats instance,
) => <String, dynamic>{
  'native': ?instance.native_,
  'alertmanager': ?instance.alertmanager?.toJson(),
  'cloudevents': ?instance.cloudevents?.toJson(),
  'cap': ?instance.cap?.toJson(),
  'mapping': ?instance.mapping,
  'owntracks': ?instance.owntracks?.toJson(),
  'osmand': ?instance.osmand?.toJson(),
  'overland': ?instance.overland?.toJson(),
  'gpslogger': ?instance.gpslogger?.toJson(),
};
