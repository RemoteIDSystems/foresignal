// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_records.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoRecordsCWProxy {
  SystemInfoRecords current(int? current);

  SystemInfoRecords versions(int? versions);

  SystemInfoRecords lastSeq(int? lastSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoRecords(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoRecords(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoRecords call({int? current, int? versions, int? lastSeq});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoRecords.copyWith(...)` or call `instanceOfSystemInfoRecords.copyWith.fieldName(value)` for a single field.
class _$SystemInfoRecordsCWProxyImpl implements _$SystemInfoRecordsCWProxy {
  const _$SystemInfoRecordsCWProxyImpl(this._value);

  final SystemInfoRecords _value;

  @override
  SystemInfoRecords current(int? current) => call(current: current);

  @override
  SystemInfoRecords versions(int? versions) => call(versions: versions);

  @override
  SystemInfoRecords lastSeq(int? lastSeq) => call(lastSeq: lastSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoRecords(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoRecords(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoRecords call({
    Object? current = const $CopyWithPlaceholder(),
    Object? versions = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoRecords(
      current: current == const $CopyWithPlaceholder()
          ? _value.current
          // ignore: cast_nullable_to_non_nullable
          : current as int?,
      versions: versions == const $CopyWithPlaceholder()
          ? _value.versions
          // ignore: cast_nullable_to_non_nullable
          : versions as int?,
      lastSeq: lastSeq == const $CopyWithPlaceholder()
          ? _value.lastSeq
          // ignore: cast_nullable_to_non_nullable
          : lastSeq as int?,
    );
  }
}

extension $SystemInfoRecordsCopyWith on SystemInfoRecords {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoRecords.copyWith(...)` or `instanceOfSystemInfoRecords.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoRecordsCWProxy get copyWith =>
      _$SystemInfoRecordsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoRecords _$SystemInfoRecordsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoRecords', json, ($checkedConvert) {
      final val = SystemInfoRecords(
        current: $checkedConvert('current', (v) => (v as num?)?.toInt()),
        versions: $checkedConvert('versions', (v) => (v as num?)?.toInt()),
        lastSeq: $checkedConvert('lastSeq', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoRecordsToJson(SystemInfoRecords instance) =>
    <String, dynamic>{
      'current': ?instance.current,
      'versions': ?instance.versions,
      'lastSeq': ?instance.lastSeq,
    };
