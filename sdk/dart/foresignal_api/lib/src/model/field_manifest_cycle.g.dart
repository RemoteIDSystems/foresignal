// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'field_manifest_cycle.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FieldManifestCycleCWProxy {
  FieldManifestCycle date(String date);

  FieldManifestCycle hour(int hour);

  FieldManifestCycle forecastHour(int forecastHour);

  FieldManifestCycle runIso(DateTime runIso);

  FieldManifestCycle validIso(DateTime validIso);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifestCycle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifestCycle(...).copyWith(id: 12, name: "My name")
  /// ```
  FieldManifestCycle call({
    String date,
    int hour,
    int forecastHour,
    DateTime runIso,
    DateTime validIso,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFieldManifestCycle.copyWith(...)` or call `instanceOfFieldManifestCycle.copyWith.fieldName(value)` for a single field.
class _$FieldManifestCycleCWProxyImpl implements _$FieldManifestCycleCWProxy {
  const _$FieldManifestCycleCWProxyImpl(this._value);

  final FieldManifestCycle _value;

  @override
  FieldManifestCycle date(String date) => call(date: date);

  @override
  FieldManifestCycle hour(int hour) => call(hour: hour);

  @override
  FieldManifestCycle forecastHour(int forecastHour) =>
      call(forecastHour: forecastHour);

  @override
  FieldManifestCycle runIso(DateTime runIso) => call(runIso: runIso);

  @override
  FieldManifestCycle validIso(DateTime validIso) => call(validIso: validIso);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FieldManifestCycle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FieldManifestCycle(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FieldManifestCycle call({
    Object? date = const $CopyWithPlaceholder(),
    Object? hour = const $CopyWithPlaceholder(),
    Object? forecastHour = const $CopyWithPlaceholder(),
    Object? runIso = const $CopyWithPlaceholder(),
    Object? validIso = const $CopyWithPlaceholder(),
  }) {
    return FieldManifestCycle(
      date: date == const $CopyWithPlaceholder() || date == null
          ? _value.date
          // ignore: cast_nullable_to_non_nullable
          : date as String,
      hour: hour == const $CopyWithPlaceholder() || hour == null
          ? _value.hour
          // ignore: cast_nullable_to_non_nullable
          : hour as int,
      forecastHour:
          forecastHour == const $CopyWithPlaceholder() || forecastHour == null
          ? _value.forecastHour
          // ignore: cast_nullable_to_non_nullable
          : forecastHour as int,
      runIso: runIso == const $CopyWithPlaceholder() || runIso == null
          ? _value.runIso
          // ignore: cast_nullable_to_non_nullable
          : runIso as DateTime,
      validIso: validIso == const $CopyWithPlaceholder() || validIso == null
          ? _value.validIso
          // ignore: cast_nullable_to_non_nullable
          : validIso as DateTime,
    );
  }
}

extension $FieldManifestCycleCopyWith on FieldManifestCycle {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFieldManifestCycle.copyWith(...)` or `instanceOfFieldManifestCycle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FieldManifestCycleCWProxy get copyWith =>
      _$FieldManifestCycleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FieldManifestCycle _$FieldManifestCycleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FieldManifestCycle', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['date', 'hour', 'forecastHour', 'runIso', 'validIso'],
  );
  final val = FieldManifestCycle(
    date: $checkedConvert('date', (v) => v as String),
    hour: $checkedConvert('hour', (v) => (v as num).toInt()),
    forecastHour: $checkedConvert('forecastHour', (v) => (v as num).toInt()),
    runIso: $checkedConvert('runIso', (v) => DateTime.parse(v as String)),
    validIso: $checkedConvert('validIso', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$FieldManifestCycleToJson(FieldManifestCycle instance) =>
    <String, dynamic>{
      'date': instance.date,
      'hour': instance.hour,
      'forecastHour': instance.forecastHour,
      'runIso': instance.runIso.toIso8601String(),
      'validIso': instance.validIso.toIso8601String(),
    };
