// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collector_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectorStatusCWProxy {
  CollectorStatus cap(int cap);

  CollectorStatus inUse(int inUse);

  CollectorStatus waiting(int waiting);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectorStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectorStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  CollectorStatus call({int cap, int inUse, int waiting});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCollectorStatus.copyWith(...)` or call `instanceOfCollectorStatus.copyWith.fieldName(value)` for a single field.
class _$CollectorStatusCWProxyImpl implements _$CollectorStatusCWProxy {
  const _$CollectorStatusCWProxyImpl(this._value);

  final CollectorStatus _value;

  @override
  CollectorStatus cap(int cap) => call(cap: cap);

  @override
  CollectorStatus inUse(int inUse) => call(inUse: inUse);

  @override
  CollectorStatus waiting(int waiting) => call(waiting: waiting);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CollectorStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CollectorStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CollectorStatus call({
    Object? cap = const $CopyWithPlaceholder(),
    Object? inUse = const $CopyWithPlaceholder(),
    Object? waiting = const $CopyWithPlaceholder(),
  }) {
    return CollectorStatus(
      cap: cap == const $CopyWithPlaceholder() || cap == null
          ? _value.cap
          // ignore: cast_nullable_to_non_nullable
          : cap as int,
      inUse: inUse == const $CopyWithPlaceholder() || inUse == null
          ? _value.inUse
          // ignore: cast_nullable_to_non_nullable
          : inUse as int,
      waiting: waiting == const $CopyWithPlaceholder() || waiting == null
          ? _value.waiting
          // ignore: cast_nullable_to_non_nullable
          : waiting as int,
    );
  }
}

extension $CollectorStatusCopyWith on CollectorStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCollectorStatus.copyWith(...)` or `instanceOfCollectorStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectorStatusCWProxy get copyWith => _$CollectorStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectorStatus _$CollectorStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CollectorStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['cap', 'inUse', 'waiting']);
      final val = CollectorStatus(
        cap: $checkedConvert('cap', (v) => (v as num).toInt()),
        inUse: $checkedConvert('inUse', (v) => (v as num).toInt()),
        waiting: $checkedConvert('waiting', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$CollectorStatusToJson(CollectorStatus instance) =>
    <String, dynamic>{
      'cap': instance.cap,
      'inUse': instance.inUse,
      'waiting': instance.waiting,
    };
