// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_run_result_consumer_clamp.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionRunResultConsumerClampCWProxy {
  RetentionRunResultConsumerClamp active(bool? active);

  RetentionRunResultConsumerClamp consumer(String? consumer);

  RetentionRunResultConsumerClamp cursorSeq(int? cursorSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResultConsumerClamp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResultConsumerClamp(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionRunResultConsumerClamp call({
    bool? active,
    String? consumer,
    int? cursorSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionRunResultConsumerClamp.copyWith(...)` or call `instanceOfRetentionRunResultConsumerClamp.copyWith.fieldName(value)` for a single field.
class _$RetentionRunResultConsumerClampCWProxyImpl
    implements _$RetentionRunResultConsumerClampCWProxy {
  const _$RetentionRunResultConsumerClampCWProxyImpl(this._value);

  final RetentionRunResultConsumerClamp _value;

  @override
  RetentionRunResultConsumerClamp active(bool? active) => call(active: active);

  @override
  RetentionRunResultConsumerClamp consumer(String? consumer) =>
      call(consumer: consumer);

  @override
  RetentionRunResultConsumerClamp cursorSeq(int? cursorSeq) =>
      call(cursorSeq: cursorSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResultConsumerClamp(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResultConsumerClamp(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionRunResultConsumerClamp call({
    Object? active = const $CopyWithPlaceholder(),
    Object? consumer = const $CopyWithPlaceholder(),
    Object? cursorSeq = const $CopyWithPlaceholder(),
  }) {
    return RetentionRunResultConsumerClamp(
      active: active == const $CopyWithPlaceholder()
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as bool?,
      consumer: consumer == const $CopyWithPlaceholder()
          ? _value.consumer
          // ignore: cast_nullable_to_non_nullable
          : consumer as String?,
      cursorSeq: cursorSeq == const $CopyWithPlaceholder()
          ? _value.cursorSeq
          // ignore: cast_nullable_to_non_nullable
          : cursorSeq as int?,
    );
  }
}

extension $RetentionRunResultConsumerClampCopyWith
    on RetentionRunResultConsumerClamp {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionRunResultConsumerClamp.copyWith(...)` or `instanceOfRetentionRunResultConsumerClamp.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionRunResultConsumerClampCWProxy get copyWith =>
      _$RetentionRunResultConsumerClampCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionRunResultConsumerClamp _$RetentionRunResultConsumerClampFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionRunResultConsumerClamp', json, ($checkedConvert) {
  final val = RetentionRunResultConsumerClamp(
    active: $checkedConvert('active', (v) => v as bool?),
    consumer: $checkedConvert('consumer', (v) => v as String?),
    cursorSeq: $checkedConvert('cursorSeq', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$RetentionRunResultConsumerClampToJson(
  RetentionRunResultConsumerClamp instance,
) => <String, dynamic>{
  'active': ?instance.active,
  'consumer': ?instance.consumer,
  'cursorSeq': ?instance.cursorSeq,
};
