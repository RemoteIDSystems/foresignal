// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_record_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityRecordListCWProxy {
  EntityRecordList entityId(String entityId);

  EntityRecordList at(DateTime? at);

  EntityRecordList items(List<EntityRecord> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityRecordList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityRecordList(...).copyWith(id: 12, name: "My name")
  /// ```
  EntityRecordList call({
    String entityId,
    DateTime? at,
    List<EntityRecord> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntityRecordList.copyWith(...)` or call `instanceOfEntityRecordList.copyWith.fieldName(value)` for a single field.
class _$EntityRecordListCWProxyImpl implements _$EntityRecordListCWProxy {
  const _$EntityRecordListCWProxyImpl(this._value);

  final EntityRecordList _value;

  @override
  EntityRecordList entityId(String entityId) => call(entityId: entityId);

  @override
  EntityRecordList at(DateTime? at) => call(at: at);

  @override
  EntityRecordList items(List<EntityRecord> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityRecordList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityRecordList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EntityRecordList call({
    Object? entityId = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return EntityRecordList(
      entityId: entityId == const $CopyWithPlaceholder() || entityId == null
          ? _value.entityId
          // ignore: cast_nullable_to_non_nullable
          : entityId as String,
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<EntityRecord>,
    );
  }
}

extension $EntityRecordListCopyWith on EntityRecordList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntityRecordList.copyWith(...)` or `instanceOfEntityRecordList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityRecordListCWProxy get copyWith => _$EntityRecordListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EntityRecordList _$EntityRecordListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EntityRecordList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['entityId', 'items']);
      final val = EntityRecordList(
        entityId: $checkedConvert('entityId', (v) => v as String),
        at: $checkedConvert(
          'at',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => EntityRecord.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EntityRecordListToJson(EntityRecordList instance) =>
    <String, dynamic>{
      'entityId': instance.entityId,
      'at': ?instance.at?.toIso8601String(),
      'items': instance.items.map((e) => e.toJson()).toList(),
    };
