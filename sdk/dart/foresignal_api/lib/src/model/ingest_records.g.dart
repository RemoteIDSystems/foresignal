// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ingest_records.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IngestRecordsCWProxy {
  IngestRecords records(List<NormalizedRecordPreview> records);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecords(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecords(...).copyWith(id: 12, name: "My name")
  /// ```
  IngestRecords call({List<NormalizedRecordPreview> records});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIngestRecords.copyWith(...)` or call `instanceOfIngestRecords.copyWith.fieldName(value)` for a single field.
class _$IngestRecordsCWProxyImpl implements _$IngestRecordsCWProxy {
  const _$IngestRecordsCWProxyImpl(this._value);

  final IngestRecords _value;

  @override
  IngestRecords records(List<NormalizedRecordPreview> records) =>
      call(records: records);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IngestRecords(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IngestRecords(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IngestRecords call({Object? records = const $CopyWithPlaceholder()}) {
    return IngestRecords(
      records: records == const $CopyWithPlaceholder() || records == null
          ? _value.records
          // ignore: cast_nullable_to_non_nullable
          : records as List<NormalizedRecordPreview>,
    );
  }
}

extension $IngestRecordsCopyWith on IngestRecords {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIngestRecords.copyWith(...)` or `instanceOfIngestRecords.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IngestRecordsCWProxy get copyWith => _$IngestRecordsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IngestRecords _$IngestRecordsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IngestRecords', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['records']);
      final val = IngestRecords(
        records: $checkedConvert(
          'records',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    NormalizedRecordPreview.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$IngestRecordsToJson(IngestRecords instance) =>
    <String, dynamic>{
      'records': instance.records.map((e) => e.toJson()).toList(),
    };
