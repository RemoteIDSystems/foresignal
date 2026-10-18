// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_run_accepted.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorRunAcceptedCWProxy {
  DoctorRunAccepted reportId(String reportId);

  DoctorRunAccepted status(DoctorRunAcceptedStatusEnum status);

  DoctorRunAccepted location(String location);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorRunAccepted(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorRunAccepted(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorRunAccepted call({
    String reportId,
    DoctorRunAcceptedStatusEnum status,
    String location,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorRunAccepted.copyWith(...)` or call `instanceOfDoctorRunAccepted.copyWith.fieldName(value)` for a single field.
class _$DoctorRunAcceptedCWProxyImpl implements _$DoctorRunAcceptedCWProxy {
  const _$DoctorRunAcceptedCWProxyImpl(this._value);

  final DoctorRunAccepted _value;

  @override
  DoctorRunAccepted reportId(String reportId) => call(reportId: reportId);

  @override
  DoctorRunAccepted status(DoctorRunAcceptedStatusEnum status) =>
      call(status: status);

  @override
  DoctorRunAccepted location(String location) => call(location: location);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorRunAccepted(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorRunAccepted(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorRunAccepted call({
    Object? reportId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
  }) {
    return DoctorRunAccepted(
      reportId: reportId == const $CopyWithPlaceholder() || reportId == null
          ? _value.reportId
          // ignore: cast_nullable_to_non_nullable
          : reportId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DoctorRunAcceptedStatusEnum,
      location: location == const $CopyWithPlaceholder() || location == null
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as String,
    );
  }
}

extension $DoctorRunAcceptedCopyWith on DoctorRunAccepted {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorRunAccepted.copyWith(...)` or `instanceOfDoctorRunAccepted.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorRunAcceptedCWProxy get copyWith =>
      _$DoctorRunAcceptedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorRunAccepted _$DoctorRunAcceptedFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DoctorRunAccepted', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['reportId', 'status', 'location']);
      final val = DoctorRunAccepted(
        reportId: $checkedConvert('reportId', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$DoctorRunAcceptedStatusEnumEnumMap, v),
        ),
        location: $checkedConvert('location', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$DoctorRunAcceptedToJson(DoctorRunAccepted instance) =>
    <String, dynamic>{
      'reportId': instance.reportId,
      'status': _$DoctorRunAcceptedStatusEnumEnumMap[instance.status]!,
      'location': instance.location,
    };

const _$DoctorRunAcceptedStatusEnumEnumMap = {
  DoctorRunAcceptedStatusEnum.running: 'running',
};
