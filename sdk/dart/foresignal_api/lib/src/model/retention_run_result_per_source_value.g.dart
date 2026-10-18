// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_run_result_per_source_value.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionRunResultPerSourceValueCWProxy {
  RetentionRunResultPerSourceValue versionsDeleted(int? versionsDeleted);

  RetentionRunResultPerSourceValue floorSeq(int? floorSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResultPerSourceValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResultPerSourceValue(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionRunResultPerSourceValue call({int? versionsDeleted, int? floorSeq});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionRunResultPerSourceValue.copyWith(...)` or call `instanceOfRetentionRunResultPerSourceValue.copyWith.fieldName(value)` for a single field.
class _$RetentionRunResultPerSourceValueCWProxyImpl
    implements _$RetentionRunResultPerSourceValueCWProxy {
  const _$RetentionRunResultPerSourceValueCWProxyImpl(this._value);

  final RetentionRunResultPerSourceValue _value;

  @override
  RetentionRunResultPerSourceValue versionsDeleted(int? versionsDeleted) =>
      call(versionsDeleted: versionsDeleted);

  @override
  RetentionRunResultPerSourceValue floorSeq(int? floorSeq) =>
      call(floorSeq: floorSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionRunResultPerSourceValue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionRunResultPerSourceValue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionRunResultPerSourceValue call({
    Object? versionsDeleted = const $CopyWithPlaceholder(),
    Object? floorSeq = const $CopyWithPlaceholder(),
  }) {
    return RetentionRunResultPerSourceValue(
      versionsDeleted: versionsDeleted == const $CopyWithPlaceholder()
          ? _value.versionsDeleted
          // ignore: cast_nullable_to_non_nullable
          : versionsDeleted as int?,
      floorSeq: floorSeq == const $CopyWithPlaceholder()
          ? _value.floorSeq
          // ignore: cast_nullable_to_non_nullable
          : floorSeq as int?,
    );
  }
}

extension $RetentionRunResultPerSourceValueCopyWith
    on RetentionRunResultPerSourceValue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionRunResultPerSourceValue.copyWith(...)` or `instanceOfRetentionRunResultPerSourceValue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionRunResultPerSourceValueCWProxy get copyWith =>
      _$RetentionRunResultPerSourceValueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionRunResultPerSourceValue _$RetentionRunResultPerSourceValueFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('RetentionRunResultPerSourceValue', json, ($checkedConvert) {
      final val = RetentionRunResultPerSourceValue(
        versionsDeleted: $checkedConvert(
          'versionsDeleted',
          (v) => (v as num?)?.toInt(),
        ),
        floorSeq: $checkedConvert('floorSeq', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$RetentionRunResultPerSourceValueToJson(
  RetentionRunResultPerSourceValue instance,
) => <String, dynamic>{
  'versionsDeleted': ?instance.versionsDeleted,
  'floorSeq': ?instance.floorSeq,
};
