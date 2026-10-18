// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_device_setup.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestDeviceSetupCWProxy {
  IngestDeviceSetup owntracks(Map<String, Object>? owntracks);

  IngestDeviceSetup overland(IngestDeviceSetupOverland? overland);

  IngestDeviceSetup traccar(IngestDeviceSetupTraccar? traccar);

  IngestDeviceSetup gpslogger(IngestDeviceSetupGpslogger? gpslogger);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetup(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestDeviceSetup call({
    Map<String, Object>? owntracks,
    IngestDeviceSetupOverland? overland,
    IngestDeviceSetupTraccar? traccar,
    IngestDeviceSetupGpslogger? gpslogger,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestDeviceSetup.copyWith(...)` or call `instanceOfIngestDeviceSetup.copyWith.fieldName(value)` for a single field.
class _$IngestDeviceSetupCWProxyImpl implements _$IngestDeviceSetupCWProxy {
  const _$IngestDeviceSetupCWProxyImpl(this._value);

  final IngestDeviceSetup _value;

  @override
  IngestDeviceSetup owntracks(Map<String, Object>? owntracks) =>
      call(owntracks: owntracks);

  @override
  IngestDeviceSetup overland(IngestDeviceSetupOverland? overland) =>
      call(overland: overland);

  @override
  IngestDeviceSetup traccar(IngestDeviceSetupTraccar? traccar) =>
      call(traccar: traccar);

  @override
  IngestDeviceSetup gpslogger(IngestDeviceSetupGpslogger? gpslogger) =>
      call(gpslogger: gpslogger);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestDeviceSetup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestDeviceSetup(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestDeviceSetup call({
    Object? owntracks = const $CopyWithPlaceholder(),
    Object? overland = const $CopyWithPlaceholder(),
    Object? traccar = const $CopyWithPlaceholder(),
    Object? gpslogger = const $CopyWithPlaceholder(),
  }) {
    return IngestDeviceSetup(
      owntracks: owntracks == const $CopyWithPlaceholder()
          ? _value.owntracks
          // ignore: cast_nullable_to_non_nullable
          : owntracks as Map<String, Object>?,
      overland: overland == const $CopyWithPlaceholder()
          ? _value.overland
          // ignore: cast_nullable_to_non_nullable
          : overland as IngestDeviceSetupOverland?,
      traccar: traccar == const $CopyWithPlaceholder()
          ? _value.traccar
          // ignore: cast_nullable_to_non_nullable
          : traccar as IngestDeviceSetupTraccar?,
      gpslogger: gpslogger == const $CopyWithPlaceholder()
          ? _value.gpslogger
          // ignore: cast_nullable_to_non_nullable
          : gpslogger as IngestDeviceSetupGpslogger?,
    );
  }
}

extension $IngestDeviceSetupCopyWith on IngestDeviceSetup {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestDeviceSetup.copyWith(...)` or `instanceOfIngestDeviceSetup.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestDeviceSetupCWProxy get copyWith =>
      _$IngestDeviceSetupCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestDeviceSetup _$IngestDeviceSetupFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestDeviceSetup', json, ($checkedConvert) {
      final val = IngestDeviceSetup(
        owntracks: $checkedConvert(
          'owntracks',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        overland: $checkedConvert(
          'overland',
          (v) => v == null
              ? null
              : IngestDeviceSetupOverland.fromJson(v as Map<String, dynamic>),
        ),
        traccar: $checkedConvert(
          'traccar',
          (v) => v == null
              ? null
              : IngestDeviceSetupTraccar.fromJson(v as Map<String, dynamic>),
        ),
        gpslogger: $checkedConvert(
          'gpslogger',
          (v) => v == null
              ? null
              : IngestDeviceSetupGpslogger.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$IngestDeviceSetupToJson(IngestDeviceSetup instance) =>
    <String, dynamic>{
      'owntracks': ?instance.owntracks,
      'overland': ?instance.overland?.toJson(),
      'traccar': ?instance.traccar?.toJson(),
      'gpslogger': ?instance.gpslogger?.toJson(),
    };
