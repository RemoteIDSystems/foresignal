// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_holds.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoHoldsCWProxy {
  SystemInfoHolds count(int? count);

  SystemInfoHolds rows(int? rows);

  SystemInfoHolds bytes(int? bytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoHolds(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoHolds(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoHolds call({int? count, int? rows, int? bytes});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoHolds.copyWith(...)` or call `instanceOfSystemInfoHolds.copyWith.fieldName(value)` for a single field.
class _$SystemInfoHoldsCWProxyImpl implements _$SystemInfoHoldsCWProxy {
  const _$SystemInfoHoldsCWProxyImpl(this._value);

  final SystemInfoHolds _value;

  @override
  SystemInfoHolds count(int? count) => call(count: count);

  @override
  SystemInfoHolds rows(int? rows) => call(rows: rows);

  @override
  SystemInfoHolds bytes(int? bytes) => call(bytes: bytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoHolds(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoHolds(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoHolds call({
    Object? count = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoHolds(
      count: count == const $CopyWithPlaceholder()
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int?,
      rows: rows == const $CopyWithPlaceholder()
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
    );
  }
}

extension $SystemInfoHoldsCopyWith on SystemInfoHolds {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoHolds.copyWith(...)` or `instanceOfSystemInfoHolds.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoHoldsCWProxy get copyWith => _$SystemInfoHoldsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoHolds _$SystemInfoHoldsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoHolds', json, ($checkedConvert) {
      final val = SystemInfoHolds(
        count: $checkedConvert('count', (v) => (v as num?)?.toInt()),
        rows: $checkedConvert('rows', (v) => (v as num?)?.toInt()),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoHoldsToJson(SystemInfoHolds instance) =>
    <String, dynamic>{
      'count': ?instance.count,
      'rows': ?instance.rows,
      'bytes': ?instance.bytes,
    };
