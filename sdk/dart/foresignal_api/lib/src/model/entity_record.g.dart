// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_record.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityRecordCWProxy {
  EntityRecord record(Record record);

  EntityRecord link(EntityLink link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  EntityRecord call({Record record, EntityLink link});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntityRecord.copyWith(...)` or call `instanceOfEntityRecord.copyWith.fieldName(value)` for a single field.
class _$EntityRecordCWProxyImpl implements _$EntityRecordCWProxy {
  const _$EntityRecordCWProxyImpl(this._value);

  final EntityRecord _value;

  @override
  EntityRecord record(Record record) => call(record: record);

  @override
  EntityRecord link(EntityLink link) => call(link: link);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EntityRecord call({
    Object? record = const $CopyWithPlaceholder(),
    Object? link = const $CopyWithPlaceholder(),
  }) {
    return EntityRecord(
      record: record == const $CopyWithPlaceholder() || record == null
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as Record,
      link: link == const $CopyWithPlaceholder() || link == null
          ? _value.link
          // ignore: cast_nullable_to_non_nullable
          : link as EntityLink,
    );
  }
}

extension $EntityRecordCopyWith on EntityRecord {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntityRecord.copyWith(...)` or `instanceOfEntityRecord.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityRecordCWProxy get copyWith => _$EntityRecordCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EntityRecord _$EntityRecordFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EntityRecord', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['record', 'link']);
      final val = EntityRecord(
        record: $checkedConvert(
          'record',
          (v) => Record.fromJson(v as Map<String, dynamic>),
        ),
        link: $checkedConvert(
          'link',
          (v) => EntityLink.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EntityRecordToJson(EntityRecord instance) =>
    <String, dynamic>{
      'record': instance.record.toJson(),
      'link': instance.link.toJson(),
    };
