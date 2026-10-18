// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_traffic.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureTrafficCWProxy {
  ExposureTraffic status(ExposureTrafficStatusEnum status);

  ExposureTraffic attribution(List<Attribution>? attribution);

  ExposureTraffic aircraft(int? aircraft);

  ExposureTraffic vessels(int? vessels);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureTraffic(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureTraffic(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureTraffic call({
    ExposureTrafficStatusEnum status,
    List<Attribution>? attribution,
    int? aircraft,
    int? vessels,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureTraffic.copyWith(...)` or call `instanceOfExposureTraffic.copyWith.fieldName(value)` for a single field.
class _$ExposureTrafficCWProxyImpl implements _$ExposureTrafficCWProxy {
  const _$ExposureTrafficCWProxyImpl(this._value);

  final ExposureTraffic _value;

  @override
  ExposureTraffic status(ExposureTrafficStatusEnum status) =>
      call(status: status);

  @override
  ExposureTraffic attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  ExposureTraffic aircraft(int? aircraft) => call(aircraft: aircraft);

  @override
  ExposureTraffic vessels(int? vessels) => call(vessels: vessels);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureTraffic(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureTraffic(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureTraffic call({
    Object? status = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? aircraft = const $CopyWithPlaceholder(),
    Object? vessels = const $CopyWithPlaceholder(),
  }) {
    return ExposureTraffic(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExposureTrafficStatusEnum,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      aircraft: aircraft == const $CopyWithPlaceholder()
          ? _value.aircraft
          // ignore: cast_nullable_to_non_nullable
          : aircraft as int?,
      vessels: vessels == const $CopyWithPlaceholder()
          ? _value.vessels
          // ignore: cast_nullable_to_non_nullable
          : vessels as int?,
    );
  }
}

extension $ExposureTrafficCopyWith on ExposureTraffic {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureTraffic.copyWith(...)` or `instanceOfExposureTraffic.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureTrafficCWProxy get copyWith => _$ExposureTrafficCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureTraffic _$ExposureTrafficFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposureTraffic', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = ExposureTraffic(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ExposureTrafficStatusEnumEnumMap, v),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        aircraft: $checkedConvert('aircraft', (v) => (v as num?)?.toInt()),
        vessels: $checkedConvert('vessels', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ExposureTrafficToJson(ExposureTraffic instance) =>
    <String, dynamic>{
      'status': _$ExposureTrafficStatusEnumEnumMap[instance.status]!,
      'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
      'aircraft': ?instance.aircraft,
      'vessels': ?instance.vessels,
    };

const _$ExposureTrafficStatusEnumEnumMap = {
  ExposureTrafficStatusEnum.ok: 'ok',
  ExposureTrafficStatusEnum.unavailable: 'unavailable',
  ExposureTrafficStatusEnum.notInstalled: 'not-installed',
};
