// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report_probe.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportProbeCWProxy {
  DoctorReportProbe skipped(bool skipped);

  DoctorReportProbe status(String? status);

  DoctorReportProbe httpStatus(int? httpStatus);

  DoctorReportProbe contentType(String? contentType);

  DoctorReportProbe bytes(int? bytes);

  DoctorReportProbe url(String? url);

  DoctorReportProbe durationMs(int? durationMs);

  DoctorReportProbe skipReason(DoctorReportProbeSkipReasonEnum? skipReason);

  DoctorReportProbe records(int? records);

  DoctorReportProbe requests(int? requests);

  DoctorReportProbe error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportProbe(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportProbe(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReportProbe call({
    bool skipped,
    String? status,
    int? httpStatus,
    String? contentType,
    int? bytes,
    String? url,
    int? durationMs,
    DoctorReportProbeSkipReasonEnum? skipReason,
    int? records,
    int? requests,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReportProbe.copyWith(...)` or call `instanceOfDoctorReportProbe.copyWith.fieldName(value)` for a single field.
class _$DoctorReportProbeCWProxyImpl implements _$DoctorReportProbeCWProxy {
  const _$DoctorReportProbeCWProxyImpl(this._value);

  final DoctorReportProbe _value;

  @override
  DoctorReportProbe skipped(bool skipped) => call(skipped: skipped);

  @override
  DoctorReportProbe status(String? status) => call(status: status);

  @override
  DoctorReportProbe httpStatus(int? httpStatus) => call(httpStatus: httpStatus);

  @override
  DoctorReportProbe contentType(String? contentType) =>
      call(contentType: contentType);

  @override
  DoctorReportProbe bytes(int? bytes) => call(bytes: bytes);

  @override
  DoctorReportProbe url(String? url) => call(url: url);

  @override
  DoctorReportProbe durationMs(int? durationMs) => call(durationMs: durationMs);

  @override
  DoctorReportProbe skipReason(DoctorReportProbeSkipReasonEnum? skipReason) =>
      call(skipReason: skipReason);

  @override
  DoctorReportProbe records(int? records) => call(records: records);

  @override
  DoctorReportProbe requests(int? requests) => call(requests: requests);

  @override
  DoctorReportProbe error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportProbe(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportProbe(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReportProbe call({
    Object? skipped = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? httpStatus = const $CopyWithPlaceholder(),
    Object? contentType = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? durationMs = const $CopyWithPlaceholder(),
    Object? skipReason = const $CopyWithPlaceholder(),
    Object? records = const $CopyWithPlaceholder(),
    Object? requests = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return DoctorReportProbe(
      skipped: skipped == const $CopyWithPlaceholder() || skipped == null
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as bool,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String?,
      httpStatus: httpStatus == const $CopyWithPlaceholder()
          ? _value.httpStatus
          // ignore: cast_nullable_to_non_nullable
          : httpStatus as int?,
      contentType: contentType == const $CopyWithPlaceholder()
          ? _value.contentType
          // ignore: cast_nullable_to_non_nullable
          : contentType as String?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      durationMs: durationMs == const $CopyWithPlaceholder()
          ? _value.durationMs
          // ignore: cast_nullable_to_non_nullable
          : durationMs as int?,
      skipReason: skipReason == const $CopyWithPlaceholder()
          ? _value.skipReason
          // ignore: cast_nullable_to_non_nullable
          : skipReason as DoctorReportProbeSkipReasonEnum?,
      records: records == const $CopyWithPlaceholder()
          ? _value.records
          // ignore: cast_nullable_to_non_nullable
          : records as int?,
      requests: requests == const $CopyWithPlaceholder()
          ? _value.requests
          // ignore: cast_nullable_to_non_nullable
          : requests as int?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $DoctorReportProbeCopyWith on DoctorReportProbe {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReportProbe.copyWith(...)` or `instanceOfDoctorReportProbe.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportProbeCWProxy get copyWith =>
      _$DoctorReportProbeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReportProbe _$DoctorReportProbeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DoctorReportProbe', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['skipped']);
      final val = DoctorReportProbe(
        skipped: $checkedConvert('skipped', (v) => v as bool),
        status: $checkedConvert('status', (v) => v as String?),
        httpStatus: $checkedConvert('httpStatus', (v) => (v as num?)?.toInt()),
        contentType: $checkedConvert('contentType', (v) => v as String?),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
        url: $checkedConvert('url', (v) => v as String?),
        durationMs: $checkedConvert('durationMs', (v) => (v as num?)?.toInt()),
        skipReason: $checkedConvert(
          'skipReason',
          (v) =>
              $enumDecodeNullable(_$DoctorReportProbeSkipReasonEnumEnumMap, v),
        ),
        records: $checkedConvert('records', (v) => (v as num?)?.toInt()),
        requests: $checkedConvert('requests', (v) => (v as num?)?.toInt()),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$DoctorReportProbeToJson(
  DoctorReportProbe instance,
) => <String, dynamic>{
  'skipped': instance.skipped,
  'status': ?instance.status,
  'httpStatus': ?instance.httpStatus,
  'contentType': ?instance.contentType,
  'bytes': ?instance.bytes,
  'url': ?instance.url,
  'durationMs': ?instance.durationMs,
  'skipReason': ?_$DoctorReportProbeSkipReasonEnumEnumMap[instance.skipReason],
  'records': ?instance.records,
  'requests': ?instance.requests,
  'error': ?instance.error,
};

const _$DoctorReportProbeSkipReasonEnumEnumMap = {
  DoctorReportProbeSkipReasonEnum.budget: 'budget',
  DoctorReportProbeSkipReasonEnum.backoff: 'backoff',
  DoctorReportProbeSkipReasonEnum.noUpstream: 'no-upstream',
  DoctorReportProbeSkipReasonEnum.notRequested: 'not-requested',
};
