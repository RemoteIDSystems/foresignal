// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'probe_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProbeResultCWProxy {
  ProbeResult sourceId(String sourceId);

  ProbeResult requests(List<ProbeResultRequestsInner> requests);

  ProbeResult records(List<NormalizedRecordPreview> records);

  ProbeResult parseError(String? parseError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProbeResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProbeResult(...).copyWith(id: 12, name: "My name")
  /// ```
  ProbeResult call({
    String sourceId,
    List<ProbeResultRequestsInner> requests,
    List<NormalizedRecordPreview> records,
    String? parseError,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProbeResult.copyWith(...)` or call `instanceOfProbeResult.copyWith.fieldName(value)` for a single field.
class _$ProbeResultCWProxyImpl implements _$ProbeResultCWProxy {
  const _$ProbeResultCWProxyImpl(this._value);

  final ProbeResult _value;

  @override
  ProbeResult sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ProbeResult requests(List<ProbeResultRequestsInner> requests) =>
      call(requests: requests);

  @override
  ProbeResult records(List<NormalizedRecordPreview> records) =>
      call(records: records);

  @override
  ProbeResult parseError(String? parseError) => call(parseError: parseError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProbeResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProbeResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProbeResult call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? requests = const $CopyWithPlaceholder(),
    Object? records = const $CopyWithPlaceholder(),
    Object? parseError = const $CopyWithPlaceholder(),
  }) {
    return ProbeResult(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      requests: requests == const $CopyWithPlaceholder() || requests == null
          ? _value.requests
          // ignore: cast_nullable_to_non_nullable
          : requests as List<ProbeResultRequestsInner>,
      records: records == const $CopyWithPlaceholder() || records == null
          ? _value.records
          // ignore: cast_nullable_to_non_nullable
          : records as List<NormalizedRecordPreview>,
      parseError: parseError == const $CopyWithPlaceholder()
          ? _value.parseError
          // ignore: cast_nullable_to_non_nullable
          : parseError as String?,
    );
  }
}

extension $ProbeResultCopyWith on ProbeResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProbeResult.copyWith(...)` or `instanceOfProbeResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProbeResultCWProxy get copyWith => _$ProbeResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProbeResult _$ProbeResultFromJson(Map<String, dynamic> json) => $checkedCreate(
  'ProbeResult',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['sourceId', 'requests', 'records']);
    final val = ProbeResult(
      sourceId: $checkedConvert('sourceId', (v) => v as String),
      requests: $checkedConvert(
        'requests',
        (v) => (v as List<dynamic>)
            .map(
              (e) =>
                  ProbeResultRequestsInner.fromJson(e as Map<String, dynamic>),
            )
            .toList(),
      ),
      records: $checkedConvert(
        'records',
        (v) => (v as List<dynamic>)
            .map(
              (e) =>
                  NormalizedRecordPreview.fromJson(e as Map<String, dynamic>),
            )
            .toList(),
      ),
      parseError: $checkedConvert('parseError', (v) => v as String?),
    );
    return val;
  },
);

Map<String, dynamic> _$ProbeResultToJson(ProbeResult instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'requests': instance.requests.map((e) => e.toJson()).toList(),
      'records': instance.records.map((e) => e.toJson()).toList(),
      'parseError': ?instance.parseError,
    };
