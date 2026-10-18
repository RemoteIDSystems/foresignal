// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportSummaryCWProxy {
  DoctorReportSummary id(String id);

  DoctorReportSummary status(DoctorReportSummaryStatusEnum status);

  DoctorReportSummary topFinding(DoctorReportSummaryTopFinding? topFinding);

  DoctorReportSummary createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReportSummary call({
    String id,
    DoctorReportSummaryStatusEnum status,
    DoctorReportSummaryTopFinding? topFinding,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReportSummary.copyWith(...)` or call `instanceOfDoctorReportSummary.copyWith.fieldName(value)` for a single field.
class _$DoctorReportSummaryCWProxyImpl implements _$DoctorReportSummaryCWProxy {
  const _$DoctorReportSummaryCWProxyImpl(this._value);

  final DoctorReportSummary _value;

  @override
  DoctorReportSummary id(String id) => call(id: id);

  @override
  DoctorReportSummary status(DoctorReportSummaryStatusEnum status) =>
      call(status: status);

  @override
  DoctorReportSummary topFinding(DoctorReportSummaryTopFinding? topFinding) =>
      call(topFinding: topFinding);

  @override
  DoctorReportSummary createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReportSummary call({
    Object? id = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? topFinding = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return DoctorReportSummary(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DoctorReportSummaryStatusEnum,
      topFinding: topFinding == const $CopyWithPlaceholder()
          ? _value.topFinding
          // ignore: cast_nullable_to_non_nullable
          : topFinding as DoctorReportSummaryTopFinding?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $DoctorReportSummaryCopyWith on DoctorReportSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReportSummary.copyWith(...)` or `instanceOfDoctorReportSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportSummaryCWProxy get copyWith =>
      _$DoctorReportSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReportSummary _$DoctorReportSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DoctorReportSummary', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'status', 'createdAt']);
  final val = DoctorReportSummary(
    id: $checkedConvert('id', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DoctorReportSummaryStatusEnumEnumMap, v),
    ),
    topFinding: $checkedConvert(
      'topFinding',
      (v) => v == null
          ? null
          : DoctorReportSummaryTopFinding.fromJson(v as Map<String, dynamic>),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$DoctorReportSummaryToJson(
  DoctorReportSummary instance,
) => <String, dynamic>{
  'id': instance.id,
  'status': _$DoctorReportSummaryStatusEnumEnumMap[instance.status]!,
  'topFinding': ?instance.topFinding?.toJson(),
  'createdAt': instance.createdAt.toIso8601String(),
};

const _$DoctorReportSummaryStatusEnumEnumMap = {
  DoctorReportSummaryStatusEnum.OK: 'OK',
  DoctorReportSummaryStatusEnum.DEGRADED_INFO: 'DEGRADED_INFO',
  DoctorReportSummaryStatusEnum.BROKEN: 'BROKEN',
};
