// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_tombstone.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingTombstoneCWProxy {
  RecordMappingTombstone path(String path);

  RecordMappingTombstone equals(Object? equals);

  RecordMappingTombstone in_(List<Object>? in_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingTombstone(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingTombstone(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingTombstone call({String path, Object? equals, List<Object>? in_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingTombstone.copyWith(...)` or call `instanceOfRecordMappingTombstone.copyWith.fieldName(value)` for a single field.
class _$RecordMappingTombstoneCWProxyImpl
    implements _$RecordMappingTombstoneCWProxy {
  const _$RecordMappingTombstoneCWProxyImpl(this._value);

  final RecordMappingTombstone _value;

  @override
  RecordMappingTombstone path(String path) => call(path: path);

  @override
  RecordMappingTombstone equals(Object? equals) => call(equals: equals);

  @override
  RecordMappingTombstone in_(List<Object>? in_) => call(in_: in_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingTombstone(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingTombstone(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingTombstone call({
    Object? path = const $CopyWithPlaceholder(),
    Object? equals = const $CopyWithPlaceholder(),
    Object? in_ = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingTombstone(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      equals: equals == const $CopyWithPlaceholder()
          ? _value.equals
          // ignore: cast_nullable_to_non_nullable
          : equals as Object?,
      in_: in_ == const $CopyWithPlaceholder()
          ? _value.in_
          // ignore: cast_nullable_to_non_nullable
          : in_ as List<Object>?,
    );
  }
}

extension $RecordMappingTombstoneCopyWith on RecordMappingTombstone {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingTombstone.copyWith(...)` or `instanceOfRecordMappingTombstone.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingTombstoneCWProxy get copyWith =>
      _$RecordMappingTombstoneCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingTombstone _$RecordMappingTombstoneFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMappingTombstone', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['path']);
  final val = RecordMappingTombstone(
    path: $checkedConvert('path', (v) => v as String),
    equals: $checkedConvert('equals', (v) => v),
    in_: $checkedConvert(
      'in',
      (v) => (v as List<dynamic>?)?.map((e) => e as Object).toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'in_': 'in'});

Map<String, dynamic> _$RecordMappingTombstoneToJson(
  RecordMappingTombstone instance,
) => <String, dynamic>{
  'path': instance.path,
  'equals': ?instance.equals,
  'in': ?instance.in_,
};
